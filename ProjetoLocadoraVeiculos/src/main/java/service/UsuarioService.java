package service;

import database.Database;
import model.Usuario;

import java.sql.*;

/**
 * Serviço responsável pela autenticação e gerenciamento de usuários.
 */
public class UsuarioService {

    /**
     * Autentica um usuário pelo username e senha.
     *
     * @return Usuario autenticado, ou null se credenciais inválidas
     */
    public Usuario autenticar(String username, String senha) {
        if (username == null || senha == null) return null;

        String sql = "SELECT id, username, senha FROM usuarios WHERE username = ? AND senha = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, senha);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("senha")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Cadastra um novo usuário no sistema.
     *
     * @throws IllegalArgumentException se username já existir ou dados inválidos
     */
    public Usuario cadastrarUsuario(String username, String senha) {
        Usuario.validar(username, senha);

        String sql = "INSERT INTO usuarios (username, senha) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, senha);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return new Usuario(rs.getInt(1), username, senha);

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("Username '" + username + "' já está em uso.");
            }
            throw new RuntimeException("Erro ao cadastrar usuário: " + e.getMessage(), e);
        }
    }

    /** Verifica se um username já existe. */
    public boolean usernameExiste(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar username: " + e.getMessage(), e);
        }
    }
}
