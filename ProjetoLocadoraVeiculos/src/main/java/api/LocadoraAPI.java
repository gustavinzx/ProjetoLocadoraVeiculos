package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.Veiculo;
import service.LocadoraService;
import service.UsuarioService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * API REST da Locadora de Veículos.
 *
 * Endpoints disponíveis:
 *   GET    /api/veiculos          - Lista todos os veículos
 *   GET    /api/veiculos/disponiveis - Lista veículos disponíveis
 *   POST   /api/veiculos          - Cadastra novo veículo
 *   PUT    /api/veiculos/{id}     - Atualiza veículo
 *   DELETE /api/veiculos/{id}     - Remove veículo
 *   POST   /api/veiculos/{id}/alugar   - Aluga veículo
 *   POST   /api/veiculos/{id}/devolver - Devolve veículo
 *   POST   /api/auth/login        - Autenticação
 */
public class LocadoraAPI {

    private final LocadoraService locadoraService;
    private final UsuarioService usuarioService;
    private final Gson gson = new Gson();
    private HttpServer server;

    public LocadoraAPI(LocadoraService locadoraService, UsuarioService usuarioService) {
        this.locadoraService = locadoraService;
        this.usuarioService = usuarioService;
    }

    public void iniciar(int porta) throws IOException {
        server = HttpServer.create(new InetSocketAddress(porta), 0);

        server.createContext("/api/veiculos", this::handleVeiculos);
        server.createContext("/api/auth/login", this::handleLogin);

        server.start();
        System.out.println("[API] Servidor iniciado na porta " + porta);
        System.out.println("[API] Acesse: http://localhost:" + porta + "/api/veiculos");
    }

    public void parar() {
        if (server != null) server.stop(0);
    }

    // ============================================================
    //  Handlers
    // ============================================================

    private void handleVeiculos(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String[] partes = path.split("/");

        try {
            // /api/veiculos/disponiveis
            if (path.endsWith("/disponiveis") && method.equals("GET")) {
                List<Veiculo> lista = locadoraService.listarDisponiveis();
                responder(exchange, 200, gson.toJson(lista));

            // /api/veiculos/{id}/alugar
            } else if (path.endsWith("/alugar") && method.equals("POST")) {
                int id = Integer.parseInt(partes[3]);
                locadoraService.alugarVeiculo(id);
                responder(exchange, 200, json("mensagem", "Veículo alugado com sucesso."));

            // /api/veiculos/{id}/devolver
            } else if (path.endsWith("/devolver") && method.equals("POST")) {
                int id = Integer.parseInt(partes[3]);
                locadoraService.devolverVeiculo(id);
                responder(exchange, 200, json("mensagem", "Veículo devolvido com sucesso."));

            // /api/veiculos/{id}  - PUT e DELETE
            } else if (partes.length == 4 && method.equals("PUT")) {
                int id = Integer.parseInt(partes[3]);
                JsonObject body = lerBodyJson(exchange);
                boolean ok = locadoraService.atualizarVeiculo(
                        id,
                        body.get("marca").getAsString(),
                        body.get("modelo").getAsString(),
                        body.get("ano").getAsInt()
                );
                if (ok) responder(exchange, 200, json("mensagem", "Veículo atualizado."));
                else    responder(exchange, 404, json("erro", "Veículo não encontrado."));

            } else if (partes.length == 4 && method.equals("DELETE")) {
                int id = Integer.parseInt(partes[3]);
                boolean ok = locadoraService.removerVeiculo(id);
                if (ok) responder(exchange, 200, json("mensagem", "Veículo removido."));
                else    responder(exchange, 404, json("erro", "Veículo não encontrado."));

            // /api/veiculos - GET e POST
            } else if (partes.length == 3 && method.equals("GET")) {
                List<Veiculo> lista = locadoraService.listarTodos();
                responder(exchange, 200, gson.toJson(lista));

            } else if (partes.length == 3 && method.equals("POST")) {
                JsonObject body = lerBodyJson(exchange);
                Veiculo v = locadoraService.cadastrarVeiculo(
                        body.get("marca").getAsString(),
                        body.get("modelo").getAsString(),
                        body.get("ano").getAsInt()
                );
                responder(exchange, 201, gson.toJson(v));

            } else {
                responder(exchange, 405, json("erro", "Método não permitido."));
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            responder(exchange, 400, json("erro", e.getMessage()));
        } catch (Exception e) {
            responder(exchange, 500, json("erro", "Erro interno: " + e.getMessage()));
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            responder(exchange, 405, json("erro", "Método não permitido."));
            return;
        }
        try {
            JsonObject body = lerBodyJson(exchange);
            var usuario = usuarioService.autenticar(
                    body.get("username").getAsString(),
                    body.get("senha").getAsString()
            );
            if (usuario != null) {
                responder(exchange, 200, json("mensagem", "Login realizado com sucesso."));
            } else {
                responder(exchange, 401, json("erro", "Credenciais inválidas."));
            }
        } catch (Exception e) {
            responder(exchange, 400, json("erro", "Requisição inválida."));
        }
    }

    // ============================================================
    //  Utilitários
    // ============================================================

    private void responder(HttpExchange exchange, int status, String corpo) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private JsonObject lerBodyJson(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(body, JsonObject.class);
        }
    }

    private String json(String chave, String valor) {
        return gson.toJson(Map.of(chave, valor));
    }
}
