import database.Database;
import model.Veiculo;
import org.junit.jupiter.api.*;
import service.LocadoraService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração da classe LocadoraService.
 * Usa banco de dados SQLite em memória para isolamento.
 *
 * CT-011 a CT-025
 */
@DisplayName("CT-011 a CT-025 | Testes de Integração - LocadoraService")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class LocadoraServiceTest {

    private static LocadoraService service;

    @BeforeAll
    static void inicializarBanco() {
        // Usa banco em memória para testes (não persiste entre execuções)
        Database.setUrl("jdbc:sqlite::memory:");
        Database.inicializar();
        service = new LocadoraService();
    }

    @BeforeEach
    void limparDados() {
        // Garante banco limpo antes de cada teste
        try (var conn = Database.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM veiculos");
        } catch (Exception e) {
            fail("Falha ao limpar banco: " + e.getMessage());
        }
    }

    // ========== CREATE ==========

    @Test
    @DisplayName("CT-011 | Cadastrar veículo com dados válidos retorna veículo com ID")
    void testCadastrarVeiculoValido() {
        Veiculo v = service.cadastrarVeiculo("Toyota", "Corolla", 2022);

        assertNotNull(v);
        assertTrue(v.getId() > 0, "ID deve ser gerado pelo banco");
        assertEquals("Toyota", v.getMarca());
        assertEquals("Corolla", v.getModelo());
        assertEquals(2022, v.getAno());
        assertTrue(v.isDisponivel());
    }

    @Test
    @DisplayName("CT-012 | Cadastrar veículo com marca vazia lança IllegalArgumentException")
    void testCadastrarVeiculoMarcaVazia() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrarVeiculo("", "Corolla", 2022));
    }

    @Test
    @DisplayName("CT-013 | Cadastrar veículo com ano inválido lança IllegalArgumentException")
    void testCadastrarVeiculoAnoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrarVeiculo("Toyota", "Corolla", 1800));
    }

    // ========== READ ==========

    @Test
    @DisplayName("CT-014 | Listar todos retorna lista vazia quando não há veículos")
    void testListarTodosVazio() {
        List<Veiculo> lista = service.listarTodos();
        assertTrue(lista.isEmpty());
    }

    @Test
    @DisplayName("CT-015 | Listar todos retorna veículos cadastrados")
    void testListarTodosComVeiculos() {
        service.cadastrarVeiculo("Honda", "Civic", 2021);
        service.cadastrarVeiculo("Fiat", "Uno", 2018);

        List<Veiculo> lista = service.listarTodos();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("CT-016 | Listar disponíveis retorna apenas veículos não alugados")
    void testListarDisponiveis() {
        Veiculo v1 = service.cadastrarVeiculo("Honda", "Civic", 2021);
        Veiculo v2 = service.cadastrarVeiculo("Fiat", "Uno", 2018);
        service.alugarVeiculo(v1.getId());

        List<Veiculo> disponiveis = service.listarDisponiveis();
        assertEquals(1, disponiveis.size());
        assertEquals(v2.getId(), disponiveis.get(0).getId());
    }

    @Test
    @DisplayName("CT-017 | Buscar por ID existente retorna veículo correto")
    void testBuscarPorIdExistente() {
        Veiculo cadastrado = service.cadastrarVeiculo("Ford", "Ka", 2020);
        Veiculo encontrado = service.buscarPorId(cadastrado.getId());

        assertNotNull(encontrado);
        assertEquals(cadastrado.getId(), encontrado.getId());
        assertEquals("Ford", encontrado.getMarca());
    }

    @Test
    @DisplayName("CT-018 | Buscar por ID inexistente retorna null")
    void testBuscarPorIdInexistente() {
        Veiculo encontrado = service.buscarPorId(9999);
        assertNull(encontrado);
    }

    // ========== UPDATE ==========

    @Test
    @DisplayName("CT-019 | Atualizar veículo existente retorna true e persiste dados")
    void testAtualizarVeiculoExistente() {
        Veiculo v = service.cadastrarVeiculo("Toyota", "Corolla", 2020);

        boolean ok = service.atualizarVeiculo(v.getId(), "Toyota", "Camry", 2023);

        assertTrue(ok);
        Veiculo atualizado = service.buscarPorId(v.getId());
        assertEquals("Camry", atualizado.getModelo());
        assertEquals(2023, atualizado.getAno());
    }

    @Test
    @DisplayName("CT-020 | Atualizar veículo inexistente retorna false")
    void testAtualizarVeiculoInexistente() {
        boolean ok = service.atualizarVeiculo(9999, "Marca", "Modelo", 2020);
        assertFalse(ok);
    }

    // ========== ALUGAR / DEVOLVER ==========

    @Test
    @DisplayName("CT-021 | Alugar veículo disponível muda status no banco")
    void testAlugarVeiculoDisponivel() {
        Veiculo v = service.cadastrarVeiculo("Chevrolet", "Onix", 2022);
        service.alugarVeiculo(v.getId());

        Veiculo atualizado = service.buscarPorId(v.getId());
        assertFalse(atualizado.isDisponivel());
    }

    @Test
    @DisplayName("CT-022 | Alugar veículo já alugado lança IllegalStateException")
    void testAlugarVeiculoJaAlugado() {
        Veiculo v = service.cadastrarVeiculo("Chevrolet", "Onix", 2022);
        service.alugarVeiculo(v.getId());

        assertThrows(IllegalStateException.class,
                () -> service.alugarVeiculo(v.getId()));
    }

    @Test
    @DisplayName("CT-023 | Devolver veículo alugado muda status no banco")
    void testDevolverVeiculoAlugado() {
        Veiculo v = service.cadastrarVeiculo("VW", "Gol", 2019);
        service.alugarVeiculo(v.getId());
        service.devolverVeiculo(v.getId());

        Veiculo atualizado = service.buscarPorId(v.getId());
        assertTrue(atualizado.isDisponivel());
    }

    // ========== DELETE ==========

    @Test
    @DisplayName("CT-024 | Remover veículo disponível retorna true e remove do banco")
    void testRemoverVeiculoDisponivel() {
        Veiculo v = service.cadastrarVeiculo("Renault", "Sandero", 2021);
        boolean ok = service.removerVeiculo(v.getId());

        assertTrue(ok);
        assertNull(service.buscarPorId(v.getId()));
    }

    @Test
    @DisplayName("CT-025 | Remover veículo alugado lança IllegalStateException")
    void testRemoverVeiculoAlugado() {
        Veiculo v = service.cadastrarVeiculo("Renault", "Kwid", 2021);
        service.alugarVeiculo(v.getId());

        assertThrows(IllegalStateException.class,
                () -> service.removerVeiculo(v.getId()));
    }
}
