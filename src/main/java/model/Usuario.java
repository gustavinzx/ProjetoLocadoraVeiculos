package model;

/**
 * Representa um usuário do sistema de locadora.
 */
public class Usuario {

    private int id;
    private String username;
    private String senha;

    public Usuario(String username, String senha) {
        this.username = username;
        this.senha = senha;
    }

    public Usuario(int id, String username, String senha) {
        this.id = id;
        this.username = username;
        this.senha = senha;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getSenha() { return senha; }

    public void setUsername(String username) { this.username = username; }
    public void setSenha(String senha) { this.senha = senha; }

    public static void validar(String username, String senha) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        if (senha == null || senha.length() < 4) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 4 caracteres.");
        }
    }

    @Override
    public String toString() {
        return String.format("[ID:%d] %s", id, username);
    }
}
