package Main;

import Locadora.Locadora;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        //cria o scanner pra ler a r3sposta do usuario
        Scanner scanner = new Scanner(System.in);

        //registra a classe onde vai gerenciar tudo
        Locadora minhaLocadora = new Locadora();

        System.out.println("========= BEM-VINDO A LOCADORA DE VEICULOS =========");

        //menu
        while (true) {
            System.out.println("\nSelecione uma opção:");
            System.out.println("1 - Cadastrar novo veiculo");
            System.out.println("2 - Listar todos os veiculos");
            System.out.println("3 - Alugar um veículo");
            System.out.println("0 - Sair do sistema");
            System.out.print("Opção: ");

            // lê a opção do usuário
            int opcao = scanner.nextInt();
            scanner.nextLine(); //

            // le qual a opcao o usuario vai escolher e chama a função necessaria
            switch (opcao) {
                case 1:
                    minhaLocadora.cadastrarVeiculo(scanner);
                    break;
                case 2:
                    minhaLocadora.listarTodosVeiculos();
                    break;
                case 3:
                    minhaLocadora.alugarVeiculo(scanner);
                    break;
                case 4:
                    minhaLocadora.devolverVeiculo(scanner);
                    break;
                case 0:
                    System.out.println("Obrigado por utilizar nosso sistema. Saindo...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opção invalida. Tente novamente.");
                    break;
            }
        }
    }
}
