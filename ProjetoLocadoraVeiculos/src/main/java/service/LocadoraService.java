package service;

import database.Database;
import model.Veiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável pelas operações de CRUD de veículos na locadora.
 * Persiste dados no SQLite via JDBC.
 */
public class LocadoraService {

    // ==================== CREATE ====================

    /**
     * Cadastra um novo veículo no banco de dados.
     *
     * @param marca  Marca do veículo
     * @param modelo Modelo do veículo
     * @param ano    Ano de fabricação
     * @return Veículo cadastrado com ID gerado
     */
    public Veiculo cadastrarVeiculo(String marca, String modelo, int ano) {
        Veiculo.validar(marca, modelo, ano);

        String sql = "INSERT INTO veiculos (marca, modelo, ano, disponivel) VALUES (?, ?, ?, 1)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, marca);
            ps.setString(2, modelo);
            ps.setInt(3, ano);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = rs.getInt(1);

            return new Veiculo(id, marca, modelo, ano, true);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar veículo: " + e.getMessage(), e);
        }
    }

    // ==================== READ ====================

    /** Retorna todos os veículos da frota. */
    public List<Veiculo> listarTodos() {
        String sql = "SELECT id, marca, modelo, ano, disponivel FROM veiculos ORDER BY id";
        List<Veiculo> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar veículos: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Retorna apenas os veículos disponíveis para aluguel. */
    public List<Veiculo> listarDisponiveis() {
        return listarPorDisponibilidade(true);
    }

    /** Retorna apenas os veículos atualmente alugados. */
    public List<Veiculo> listarAlugados() {
        return listarPorDisponibilidade(false);
    }

    /** Busca um veículo pelo ID. Retorna null se não encontrado. */
    public Veiculo buscarPorId(int id) {
        String sql = "SELECT id, marca, modelo, ano, disponivel FROM veiculos WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar veículo: " + e.getMessage(), e);
        }
        return null;
    }

    // ==================== UPDATE ====================

    /**
     * Atualiza os dados cadastrais de um veículo.
     *
     * @return true se atualizado com sucesso, false se o ID não foi encontrado
     */
    public boolean atualizarVeiculo(int id, String novaMarca, String novoModelo, int novoAno) {
        Veiculo.validar(novaMarca, novoModelo, novoAno);

        String sql = "UPDATE veiculos SET marca = ?, modelo = ?, ano = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, novaMarca);
            ps.setString(2, novoModelo);
            ps.setInt(3, novoAno);
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar veículo: " + e.getMessage(), e);
        }
    }

    /**
     * Registra o aluguel de um veículo disponível.
     *
     * @throws IllegalStateException se o veículo não estiver disponível
     */
    public void alugarVeiculo(int id) {
        Veiculo veiculo = buscarPorId(id);
        if (veiculo == null) {
            throw new IllegalArgumentException("Veículo com ID " + id + " não encontrado.");
        }
        veiculo.alugar(); // lança exception se já alugado

        String sql = "UPDATE veiculos SET disponivel = 0 WHERE id = ?";
        executarUpdate(sql, id);
    }

    /**
     * Registra a devolução de um veículo alugado.
     *
     * @throws IllegalStateException se o veículo já estiver disponível
     */
    public void devolverVeiculo(int id) {
        Veiculo veiculo = buscarPorId(id);
        if (veiculo == null) {
            throw new IllegalArgumentException("Veículo com ID " + id + " não encontrado.");
        }
        veiculo.devolver(); // lança exception se já disponível

        String sql = "UPDATE veiculos SET disponivel = 1 WHERE id = ?";
        executarUpdate(sql, id);
    }

    // ==================== DELETE ====================

    /**
     * Remove um veículo da frota permanentemente.
     *
     * @return true se removido, false se não encontrado
     * @throws IllegalStateException se o veículo estiver alugado
     */
    public boolean removerVeiculo(int id) {
        Veiculo veiculo = buscarPorId(id);
        if (veiculo == null) return false;
        if (!veiculo.isDisponivel()) {
            throw new IllegalStateException("Não é possível remover um veículo alugado.");
        }

        String sql = "DELETE FROM veiculos WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover veículo: " + e.getMessage(), e);
        }
    }

    // ==================== Auxiliares ====================

    private List<Veiculo> listarPorDisponibilidade(boolean disponivel) {
        String sql = "SELECT id, marca, modelo, ano, disponivel FROM veiculos WHERE disponivel = ?";
        List<Veiculo> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, disponivel ? 1 : 0);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar veículos: " + e.getMessage(), e);
        }
        return lista;
    }

    private void executarUpdate(String sql, int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro na operação: " + e.getMessage(), e);
        }
    }

    private Veiculo mapRow(ResultSet rs) throws SQLException {
        return new Veiculo(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("ano"),
                rs.getInt("disponivel") == 1
        );
    }
}
