import api.LocadoraAPI;
import database.Database;
import model.Usuario;
import model.Veiculo;
import service.LocadoraService;
import service.UsuarioService;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Ponto de entrada do sistema de Locadora de Veículos.
 * Inicializa o banco de dados, a API REST e a interface CLI.
 */
public class Main {

    private static final LocadoraService locadoraService = new LocadoraService();
    private static final UsuarioService usuarioService = new UsuarioService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        // 1. Inicializa banco de dados
        Database.inicializar();

        // 2. Sobe a API REST em background (porta 8080)
        LocadoraAPI api = new LocadoraAPI(locadoraService, usuarioService);
        api.iniciar(8080);

        System.out.println("\n========= BEM-VINDO À LOCADORA DE VEÍCULOS =========");

        // 3. Tela de login
        Usuario usuarioLogado = telaLogin();
        if (usuarioLogado == null) {
            System.out.println("Número máximo de tentativas atingido. Encerrando.");
            api.parar();
            return;
        }

        System.out.println("Bem-vindo, " + usuarioLogado.getUsername() + "!\n");

        // 4. Menu principal
        menuPrincipal(api);
    }

    // ============================================================
    //  Tela de Login
    // ============================================================

    private static Usuario telaLogin() {
        int tentativas = 3;
        while (tentativas > 0) {
            System.out.println("\n--- Login ---");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Senha: ");
            String senha = scanner.nextLine().trim();

            Usuario u = usuarioService.autenticar(username, senha);
            if (u != null) return u;

            tentativas--;
            System.out.println("Credenciais inválidas. Tentativas restantes: " + tentativas);
        }
        return null;
    }

    // ============================================================
    //  Menu Principal
    // ============================================================

    private static void menuPrincipal(LocadoraAPI api) {
        while (true) {
            System.out.println("\n========= MENU PRINCIPAL =========");
            System.out.println("1 - Cadastrar novo veículo");
            System.out.println("2 - Listar todos os veículos");
            System.out.println("3 - Atualizar veículo");
            System.out.println("4 - Remover veículo");
            System.out.println("5 - Alugar veículo");
            System.out.println("6 - Devolver veículo");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            String entrada = scanner.nextLine().trim();
            switch (entrada) {
                case "1" -> cadastrarVeiculo();
                case "2" -> listarVeiculos();
                case "3" -> atualizarVeiculo();
                case "4" -> removerVeiculo();
                case "5" -> alugarVeiculo();
                case "6" -> devolverVeiculo();
                case "0" -> {
                    System.out.println("Obrigado por utilizar nosso sistema. Saindo...");
                    api.parar();
                    scanner.close();
                    return;
                }
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    // ============================================================
    //  Operações de Veículo
    // ============================================================

    private static void cadastrarVeiculo() {
        System.out.println("\n--- Cadastro de Novo Veículo ---");
        System.out.print("Marca: ");
        String marca = scanner.nextLine().trim();
        System.out.print("Modelo: ");
        String modelo = scanner.nextLine().trim();
        System.out.print("Ano: ");
        int ano = lerInteiro();

        try {
            Veiculo v = locadoraService.cadastrarVeiculo(marca, modelo, ano);
            System.out.println("Veículo cadastrado: " + v);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void listarVeiculos() {
        System.out.println("\n--- Frota Completa ---");
        List<Veiculo> lista = locadoraService.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum veículo cadastrado.");
        } else {
            lista.forEach(System.out::println);
        }
    }

    private static void atualizarVeiculo() {
        System.out.println("\n--- Atualizar Veículo ---");
        listarVeiculos();
        System.out.print("ID do veículo a atualizar (0 para voltar): ");
        int id = lerInteiro();
        if (id == 0) return;

        System.out.print("Nova marca: ");
        String marca = scanner.nextLine().trim();
        System.out.print("Novo modelo: ");
        String modelo = scanner.nextLine().trim();
        System.out.print("Novo ano: ");
        int ano = lerInteiro();

        try {
            boolean ok = locadoraService.atualizarVeiculo(id, marca, modelo, ano);
            System.out.println(ok ? "Veículo atualizado com sucesso!" : "Veículo não encontrado.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void removerVeiculo() {
        System.out.println("\n--- Remover Veículo ---");
        listarVeiculos();
        System.out.print("ID do veículo a remover (0 para voltar): ");
        int id = lerInteiro();
        if (id == 0) return;

        try {
            boolean ok = locadoraService.removerVeiculo(id);
            System.out.println(ok ? "Veículo removido com sucesso!" : "Veículo não encontrado.");
        } catch (IllegalStateException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void alugarVeiculo() {
        System.out.println("\n--- Alugar Veículo ---");
        List<Veiculo> disponiveis = locadoraService.listarDisponiveis();
        if (disponiveis.isEmpty()) {
            System.out.println("Nenhum veículo disponível no momento.");
            return;
        }
        disponiveis.forEach(System.out::println);
        System.out.print("ID do veículo a alugar (0 para voltar): ");
        int id = lerInteiro();
        if (id == 0) return;

        try {
            locadoraService.alugarVeiculo(id);
            System.out.println("Veículo alugado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void devolverVeiculo() {
        System.out.println("\n--- Devolver Veículo ---");
        List<Veiculo> alugados = locadoraService.listarAlugados();
        if (alugados.isEmpty()) {
            System.out.println("Nenhum veículo alugado no momento.");
            return;
        }
        alugados.forEach(System.out::println);
        System.out.print("ID do veículo a devolver (0 para voltar): ");
        int id = lerInteiro();
        if (id == 0) return;

        try {
            locadoraService.devolverVeiculo(id);
            System.out.println("Veículo devolvido com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ============================================================
    //  Auxiliar
    // ============================================================

    private static int lerInteiro() {
        while (true) {
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                return valor;
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Digite um número: ");
            }
        }
    }
}
