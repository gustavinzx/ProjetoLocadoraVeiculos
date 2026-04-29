package model;

/**
 * Representa um veículo da frota da locadora.
 */
public class Veiculo {

    private int id;
    private String marca;
    private String modelo;
    private int ano;
    private boolean disponivel;

    // Construtor para criação (sem id - gerado pelo banco)
    public Veiculo(String marca, String modelo, int ano) {
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.disponivel = true;
    }

    // Construtor completo (usado ao carregar do banco)
    public Veiculo(int id, String marca, String modelo, int ano, boolean disponivel) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.disponivel = disponivel;
    }

    // ==================== Getters e Setters ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    // ==================== Métodos de negócio ====================

    public void alugar() {
        if (!this.disponivel) {
            throw new IllegalStateException("Veículo já está alugado.");
        }
        this.disponivel = false;
    }

    public void devolver() {
        if (this.disponivel) {
            throw new IllegalStateException("Veículo já está disponível.");
        }
        this.disponivel = true;
    }

    // ==================== Validações ====================

    public static void validar(String marca, String modelo, int ano) {
        if (marca == null || marca.isBlank()) {
            throw new IllegalArgumentException("Marca não pode ser vazia.");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("Modelo não pode ser vazio.");
        }
        if (ano < 1886 || ano > 2100) {
            throw new IllegalArgumentException("Ano inválido: " + ano);
        }
    }

    @Override
    public String toString() {
        return String.format("[ID:%d] %s %s (%d) - %s",
                id, marca, modelo, ano,
                disponivel ? "Disponível" : "Alugado");
    }
}
