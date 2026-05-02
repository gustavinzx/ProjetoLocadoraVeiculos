import api.LocadoraAPI;
import database.Database;
import model.Veiculo;
import org.junit.jupiter.api.*;
import service.LocadoraService;
import service.UsuarioService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes E2E — Simulam o fluxo completo de um usuário real.
 * Sobe a API REST, executa o fluxo e verifica cada etapa.
 *
 * Fluxo testado:
 * Login → Cadastrar Veículo → Listar → Alugar → Devolver → Remover
 *
 * CT-E2E-01 a CT-E2E-07
 */
@DisplayName("CT-E2E-01 a CT-E2E-07 | Testes E2E - Fluxo Completo do Usuário")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class E2ETest {

    private static LocadoraAPI api;
    private static Connection sharedConn;
    private static int veiculoId; // ID do veículo criado, reutilizado nos testes seguintes
    private static final String BASE = "http://localhost:8081/api";

    @BeforeAll
    static void iniciar() throws Exception {
        // Banco em memória compartilhado exclusivo para E2E
        String url = "jdbc:sqlite:file:testE2E?mode=memory&cache=shared";
        Database.setUrl(url);
        sharedConn = DriverManager.getConnection(url);
        Database.inicializar();

        // Sobe a API na porta 8081 (diferente da 8080 para não conflitar)
        api = new LocadoraAPI(new LocadoraService(), new UsuarioService());
        api.iniciar(8081);

        // Pequena pausa para a API ficar pronta
        Thread.sleep(500);
        System.out.println("[E2E] API iniciada em " + BASE);
    }

    @AfterAll
    static void encerrar() throws Exception {
        if (api != null) api.parar();
        if (sharedConn != null) sharedConn.close();
        System.out.println("[E2E] API encerrada.");
    }

    // ================================================================
    //  CT-E2E-01 | Login com credenciais válidas
    // ================================================================
    @Test
    @Order(1)
    @DisplayName("CT-E2E-01 | Usuário faz login com credenciais válidas")
    void e2e_login_valido() throws Exception {
        String body = "{\"username\":\"admin\",\"senha\":\"admin123\"}";
        int status = post(BASE + "/auth/login", body);

        assertEquals(200, status,
                "E2E: Login com admin/admin123 deve retornar HTTP 200");
        System.out.println("[E2E] CT-E2E-01 PASSOU — Login OK");
    }

    // ================================================================
    //  CT-E2E-02 | Login com credenciais inválidas é bloqueado
    // ================================================================
    @Test
    @Order(2)
    @DisplayName("CT-E2E-02 | Login com senha errada é bloqueado")
    void e2e_login_invalido() throws Exception {
        String body = "{\"username\":\"admin\",\"senha\":\"senhaerrada\"}";
        int status = post(BASE + "/auth/login", body);

        assertEquals(401, status,
                "E2E: Login inválido deve retornar HTTP 401");
        System.out.println("[E2E] CT-E2E-02 PASSOU — Login inválido bloqueado");
    }

    // ================================================================
    //  CT-E2E-03 | Cadastrar novo veículo
    // ================================================================
    @Test
    @Order(3)
    @DisplayName("CT-E2E-03 | Usuário cadastra um novo veículo na frota")
    void e2e_cadastrar_veiculo() throws Exception {
        String body = "{\"marca\":\"Toyota\",\"modelo\":\"Corolla\",\"ano\":2022}";
        String[] result = postComResposta(BASE + "/veiculos", body);

        assertEquals("201", result[0],
                "E2E: Cadastro deve retornar HTTP 201");
        assertTrue(result[1].contains("Toyota"),
                "E2E: Resposta deve conter os dados do veículo cadastrado");

        // Extrai o ID para usar nos próximos testes
        veiculoId = extrairId(result[1]);
        assertTrue(veiculoId > 0, "E2E: Veículo deve ter ID gerado pelo banco");
        System.out.println("[E2E] CT-E2E-03 PASSOU — Veículo cadastrado com ID=" + veiculoId);
    }

    // ================================================================
    //  CT-E2E-04 | Listar veículos disponíveis
    // ================================================================
    @Test
    @Order(4)
    @DisplayName("CT-E2E-04 | Usuário lista veículos disponíveis e encontra o cadastrado")
    void e2e_listar_disponiveis() throws Exception {
        String[] result = getResposta(BASE + "/veiculos/disponiveis");

        assertEquals("200", result[0],
                "E2E: Listagem deve retornar HTTP 200");
        assertTrue(result[1].contains("Toyota"),
                "E2E: Lista deve conter o veículo Toyota recém cadastrado");
        System.out.println("[E2E] CT-E2E-04 PASSOU — Veículo aparece na lista de disponíveis");
    }

    // ================================================================
    //  CT-E2E-05 | Alugar veículo
    // ================================================================
    @Test
    @Order(5)
    @DisplayName("CT-E2E-05 | Usuário aluga o veículo cadastrado")
    void e2e_alugar_veiculo() throws Exception {
        int status = post(BASE + "/veiculos/" + veiculoId + "/alugar", "");

        assertEquals(200, status,
                "E2E: Aluguel deve retornar HTTP 200");

        // Confirma que saiu da lista de disponíveis
        String[] disponiveis = getResposta(BASE + "/veiculos/disponiveis");
        assertFalse(disponiveis[1].contains("\"id\":" + veiculoId),
                "E2E: Veículo alugado não deve aparecer nos disponíveis");
        System.out.println("[E2E] CT-E2E-05 PASSOU — Veículo alugado e removido dos disponíveis");
    }

    // ================================================================
    //  CT-E2E-06 | Devolver veículo
    // ================================================================
    @Test
    @Order(6)
    @DisplayName("CT-E2E-06 | Usuário devolve o veículo alugado")
    void e2e_devolver_veiculo() throws Exception {
        int status = post(BASE + "/veiculos/" + veiculoId + "/devolver", "");

        assertEquals(200, status,
                "E2E: Devolução deve retornar HTTP 200");

        // Confirma que voltou para disponíveis
        String[] disponiveis = getResposta(BASE + "/veiculos/disponiveis");
        assertTrue(disponiveis[1].contains("Toyota"),
                "E2E: Veículo devolvido deve aparecer novamente nos disponíveis");
        System.out.println("[E2E] CT-E2E-06 PASSOU — Veículo devolvido e disponível novamente");
    }

    // ================================================================
    //  CT-E2E-07 | Remover veículo da frota
    // ================================================================
    @Test
    @Order(7)
    @DisplayName("CT-E2E-07 | Usuário remove o veículo da frota")
    void e2e_remover_veiculo() throws Exception {
        int status = delete(BASE + "/veiculos/" + veiculoId);

        assertEquals(200, status,
                "E2E: Remoção deve retornar HTTP 200");

        // Confirma que não existe mais
        String[] todos = getResposta(BASE + "/veiculos");
        assertFalse(todos[1].contains("\"id\":" + veiculoId),
                "E2E: Veículo removido não deve mais existir na frota");
        System.out.println("[E2E] CT-E2E-07 PASSOU — Veículo removido da frota com sucesso");
    }

    // ================================================================
    //  Utilitários HTTP
    // ================================================================

    private int post(String urlStr, String body) throws Exception {
        HttpURLConnection conn = abrirConexao(urlStr, "POST");
        if (!body.isEmpty()) {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            conn.setDoOutput(true);
            conn.getOutputStream().write(bytes);
        }
        return conn.getResponseCode();
    }

    private String[] postComResposta(String urlStr, String body) throws Exception {
        HttpURLConnection conn = abrirConexao(urlStr, "POST");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setDoOutput(true);
        conn.getOutputStream().write(bytes);
        int status = conn.getResponseCode();
        String resp = lerResposta(conn);
        return new String[]{String.valueOf(status), resp};
    }

    private String[] getResposta(String urlStr) throws Exception {
        HttpURLConnection conn = abrirConexao(urlStr, "GET");
        int status = conn.getResponseCode();
        String resp = lerResposta(conn);
        return new String[]{String.valueOf(status), resp};
    }

    private int delete(String urlStr) throws Exception {
        HttpURLConnection conn = abrirConexao(urlStr, "DELETE");
        return conn.getResponseCode();
    }

    private HttpURLConnection abrirConexao(String urlStr, String method) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        return conn;
    }

    private String lerResposta(HttpURLConnection conn) throws Exception {
        InputStream is = conn.getResponseCode() < 400
                ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) return "";
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private int extrairId(String json) {
        try {
            String[] partes = json.split("\"id\":");
            if (partes.length > 1) {
                return Integer.parseInt(partes[1].split("[,}]")[0].trim());
            }
        } catch (Exception e) {
            fail("Não foi possível extrair o ID do JSON: " + json);
        }
        return -1;
    }
}
