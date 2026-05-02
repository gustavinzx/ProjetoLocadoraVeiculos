package database;

import java.sql.*;

/**
 * Gerencia a conexão com o banco de dados SQLite.
 * Cria as tabelas necessárias na primeira execução.
 */
public class Database {

    private static String URL = "jdbc:sqlite:locadora.db";

    /** Permite sobrescrever a URL (útil para testes com banco em memória) */
    public static void setUrl(String url) {
        URL = url;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Inicializa o banco de dados: cria tabelas e insere usuário admin padrão.
     */
    public static void inicializar() {
        String sqlVeiculos = """
                CREATE TABLE IF NOT EXISTS veiculos (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    marca     TEXT    NOT NULL,
                    modelo    TEXT    NOT NULL,
                    ano       INTEGER NOT NULL,
                    disponivel INTEGER NOT NULL DEFAULT 1
                )
                """;

        String sqlUsuarios = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT    NOT NULL UNIQUE,
                    senha    TEXT    NOT NULL
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlVeiculos);
            stmt.execute(sqlUsuarios);

            // Cria usuário admin padrão se não existir
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM usuarios WHERE username = 'admin'");
            if (rs.getInt(1) == 0) {
                stmt.execute(
                        "INSERT INTO usuarios (username, senha) VALUES ('admin', 'admin123')");
                System.out.println("[DB] Usuário admin criado (senha: admin123)");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco de dados: " + e.getMessage(), e);
        }
    }
}
