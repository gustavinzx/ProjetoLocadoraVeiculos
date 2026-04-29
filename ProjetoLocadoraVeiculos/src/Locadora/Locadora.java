package Locadora;

import Veiculo.Veiculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Locadora {

    //cria a lista do do veiculo, como frota em ARRAY
    private List<Veiculo> frota;

    public Locadora() {
        this.frota = new ArrayList<>();
    }

    public void cadastrarVeiculo(Scanner scanner) {
        System.out.println("\n--- Cadastro de Novo Veiculo ---");
        System.out.print("Digite a marca: ");
        String marca = scanner.nextLine();

        System.out.print("Digite o modelo: ");
        String modelo = scanner.nextLine();

        System.out.print("Digite o ano: ");
        int ano = scanner.nextInt();
        scanner.nextLine();

        // cria um novo objeto chamado veiculo e adiciona a frota
        Veiculo novoVeiculo = new Veiculo(marca, modelo, ano);
        this.frota.add(novoVeiculo);

        System.out.println("Veiculo cadastrado com sucesso!");
    }

    //fazer a lista dos veiculos ja cadastrados
    public void listarTodosVeiculos() {
        System.out.println("\n--- Frota de Veiculos Cadastrados ---");
        if (this.frota.isEmpty()) {
            System.out.println("Nenhum veiculo cadastrado ainda.");
        } else {
            for (int i = 0; i < this.frota.size(); i++) {
                System.out.println((i + 1) + ". " + this.frota.get(i));
            }
        }
    }

    // metodo pra alugar o veiculo
    public void alugarVeiculo(Scanner scanner) {
        System.out.println("\n--- Alugar um Veiculo ---");

        List<Veiculo> carrosDisponiveis = new ArrayList<>();
        for (Veiculo carro : this.frota) {
            if (carro.isDisponivel()) {
                carrosDisponiveis.add(carro);
            }
        }

        if (carrosDisponiveis.isEmpty()) {
            System.out.println("Nenhum veículo disponível para aluguel no momento.");
        } else {
            System.out.println("Veiculos disponiveis:");
            for (int i = 0; i < carrosDisponiveis.size(); i++) {
                System.out.println((i + 1) + ". " + carrosDisponiveis.get(i));
            }

            System.out.print("\nDigite o numero do carro que deseja alugar (ou 0 para voltar): ");
            int numeroEscolhido = scanner.nextInt();
            scanner.nextLine();

            if (numeroEscolhido == 0) {
                System.out.println("Operação cancelada, voltando ao menu.");
            } else if (numeroEscolhido > 0 && numeroEscolhido <= carrosDisponiveis.size()) {
                Veiculo carroParaAlugar = carrosDisponiveis.get(numeroEscolhido - 1);
                carroParaAlugar.alugar();
                System.out.println("Veiculo '" + carroParaAlugar.modelo + "' alugado com sucesso!");
            } else {
                System.out.println("Opção invalida! Por favor, escolha um número da lista.");
            }
        }
    }

    public void devolverVeiculo(Scanner scanner){
        System.out.println("\n--- Devolver um Veiculo ---");
        List<Veiculo> carrosAlugados = new ArrayList<>();
        for(Veiculo carro : this.frota){
            if(!carro.isDisponivel()){
                carrosAlugados.add(carro);
            }
        }
        if(carrosAlugados.isEmpty()){
            System.out.println("Nenhum carro alugado no momento!");
        } else {
            System.out.println("Veiculos alugados:");
            for (int i = 0; i < carrosAlugados.size(); i++) {
                System.out.println((i + 1) + ". " + carrosAlugados.get(i));
            }
            System.out.println("Escolha o veiculo que deseja devolver: (ou 0 para voltar)");

            int numeroEscolhido = scanner.nextInt();
            scanner.nextLine();

            if (numeroEscolhido == 0){
                System.out.println("Operação cancelada, voltando para o menu.\n");
            } else if(numeroEscolhido > 0 && numeroEscolhido <= carrosAlugados.size()){
                Veiculo carroParaDevolver = carrosAlugados.get(numeroEscolhido - 1);
                carroParaDevolver.devolver();
                System.out.println("Veiculo "+ carroParaDevolver.modelo  + " devolvido com sucesso!");
            } else {
                System.out.println("Opção invalida! Por favor, escolha um número da lista.");
            }
        }

    }
}