package Veiculo;

public class Veiculo {
    String marca;
    public String modelo;
    int ano;
    public boolean disponivel;

    public Veiculo(String marca, String modelo, int ano){
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.disponivel = true;
    }

    // serve pra representar em formato texto do seu objeto
    @Override
    public String toString(){
        return "Veiculo: " +
                "Marca: " + marca +
                ", Modelo: " + modelo +
                ", Ano : " + ano +
                ", Disponivel: " + disponivel;
    }
    public boolean isDisponivel(){
        return this.disponivel;
    }

    public void alugar(){
        this.disponivel = false;
    }

    public void devolver(){
        this.disponivel = true;
    }

}
