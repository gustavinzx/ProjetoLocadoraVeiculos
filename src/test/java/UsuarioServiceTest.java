import database.Database;
import model.Usuario;
import org.junit.jupiter.api.*;
import service.UsuarioService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários/integração da classe UsuarioService.
 *
 * CT-026 a CT-033
 */
@DisplayName("CT-026 a CT-033 | Testes - UsuarioService (Autenticação)")
class UsuarioServiceTest {

    private static UsuarioService service;

    @BeforeAll
    static void inicializar() {
        Database.setUrl("jdbc:sqlite::memory:");
        Database.inicializar(); // Cria tabelas e usuário admin padrão
        service = new UsuarioService();
    }

    // ========== Autenticação ==========

    @Test
    @DisplayName("CT-026 | Login com credenciais válidas (admin) retorna Usuario")
    void testLoginValido() {
        Usuario u = service.autenticar("admin", "admin123");
        assertNotNull(u, "Login válido deve retornar um Usuario");
        assertEquals("admin", u.getUsername());
    }

    @Test
    @DisplayName("CT-027 | Login com senha errada retorna null")
    void testLoginSenhaErrada() {
        Usuario u = service.autenticar("admin", "senhaerrada");
        assertNull(u, "Senha incorreta deve retornar null");
    }

    @Test
    @DisplayName("CT-028 | Login com username inexistente retorna null")
    void testLoginUsernameInexistente() {
        Usuario u = service.autenticar("usuario_fantasma", "qualquersenha");
        assertNull(u);
    }

    @Test
    @DisplayName("CT-029 | Login com credenciais nulas retorna null")
    void testLoginCredenciaisNulas() {
        Usuario u = service.autenticar(null, null);
        assertNull(u, "Credenciais nulas devem retornar null sem exceção");
    }

    // ========== Cadastro de usuário ==========

    @Test
    @DisplayName("CT-030 | Cadastrar usuário com dados válidos retorna Usuario com ID")
    void testCadastrarUsuarioValido() {
        String username = "usuario_teste_" + System.currentTimeMillis();
        Usuario u = service.cadastrarUsuario(username, "senha123");

        assertNotNull(u);
        assertTrue(u.getId() > 0);
        assertEquals(username, u.getUsername());
    }

    @Test
    @DisplayName("CT-031 | Cadastrar usuário com senha curta lança IllegalArgumentException")
    void testCadastrarUsuarioSenhaCurta() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrarUsuario("novousuario", "123"));
    }

    @Test
    @DisplayName("CT-032 | Cadastrar usuário com username duplicado lança IllegalArgumentException")
    void testCadastrarUsuarioDuplicado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrarUsuario("admin", "outrasenha"));
    }

    @Test
    @DisplayName("CT-033 | usernameExiste retorna true para admin e false para desconhecido")
    void testUsernameExiste() {
        assertTrue(service.usernameExiste("admin"));
        assertFalse(service.usernameExiste("usuario_que_nao_existe_xyz"));
    }
}
