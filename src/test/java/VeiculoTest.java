import model.Veiculo;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe Veiculo.
 *
 * CT-001 a CT-010
 */
@DisplayName("CT-001 a CT-010 | Testes Unitários - Veiculo")
class VeiculoTest {

    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        veiculo = new Veiculo("Toyota", "Corolla", 2022);
    }

    // ========== Construtor ==========

    @Test
    @DisplayName("CT-001 | Criação de veículo com dados válidos")
    void testCriacaoVeiculoValido() {
        assertEquals("Toyota", veiculo.getMarca());
        assertEquals("Corolla", veiculo.getModelo());
        assertEquals(2022, veiculo.getAno());
        assertTrue(veiculo.isDisponivel(), "Veículo recém-criado deve estar disponível");
    }

    @Test
    @DisplayName("CT-002 | Construtor completo (com ID) mantém todos os campos")
    void testConstrutorCompleto() {
        Veiculo v = new Veiculo(10, "Honda", "Civic", 2021, false);
        assertEquals(10, v.getId());
        assertEquals("Honda", v.getMarca());
        assertFalse(v.isDisponivel());
    }

    // ========== Alugar ==========

    @Test
    @DisplayName("CT-003 | Alugar veículo disponível muda status para indisponível")
    void testAlugarVeiculoDisponivel() {
        veiculo.alugar();
        assertFalse(veiculo.isDisponivel(), "Após alugar, veículo não deve estar disponível");
    }

    @Test
    @DisplayName("CT-004 | Alugar veículo já alugado lança IllegalStateException")
    void testAlugarVeiculoJaAlugado() {
        veiculo.alugar();
        assertThrows(IllegalStateException.class, () -> veiculo.alugar(),
                "Deve lançar exceção ao tentar alugar veículo já alugado");
    }

    // ========== Devolver ==========

    @Test
    @DisplayName("CT-005 | Devolver veículo alugado muda status para disponível")
    void testDevolverVeiculoAlugado() {
        veiculo.alugar();
        veiculo.devolver();
        assertTrue(veiculo.isDisponivel(), "Após devolver, veículo deve estar disponível");
    }

    @Test
    @DisplayName("CT-006 | Devolver veículo já disponível lança IllegalStateException")
    void testDevolverVeiculoJaDisponivel() {
        assertThrows(IllegalStateException.class, () -> veiculo.devolver(),
                "Deve lançar exceção ao tentar devolver veículo já disponível");
    }

    // ========== Validação ==========

    @Test
    @DisplayName("CT-007 | Validar com marca vazia lança IllegalArgumentException")
    void testValidarMarcaVazia() {
        assertThrows(IllegalArgumentException.class,
                () -> Veiculo.validar("", "Corolla", 2022));
    }

    @Test
    @DisplayName("CT-008 | Validar com modelo nulo lança IllegalArgumentException")
    void testValidarModeloNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> Veiculo.validar("Toyota", null, 2022));
    }

    @Test
    @DisplayName("CT-009 | Validar com ano inválido (< 1886) lança IllegalArgumentException")
    void testValidarAnoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> Veiculo.validar("Toyota", "Corolla", 1800));
    }

    @Test
    @DisplayName("CT-010 | toString retorna representação correta")
    void testToString() {
        Veiculo v = new Veiculo(1, "Ford", "Ka", 2020, true);
        String resultado = v.toString();
        assertTrue(resultado.contains("Ford"));
        assertTrue(resultado.contains("Ka"));
        assertTrue(resultado.contains("2020"));
        assertTrue(resultado.contains("Disponível"));
    }
}
