import api.LocadoraAPI;
import database.Database;
import service.LocadoraService;
import service.UsuarioService;

/**
 * Sobe apenas a API REST — sem menu interativo.
 * Usado no pipeline do GitLab para os testes de API com Newman.
 */
public class ApiRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Iniciando API para testes ===");

        Database.inicializar();

        LocadoraService locadoraService = new LocadoraService();
        UsuarioService usuarioService   = new UsuarioService();

        LocadoraAPI api = new LocadoraAPI(locadoraService, usuarioService);
        api.iniciar(8080);

        System.out.println("API pronta em http://localhost:8080");
        System.out.println("Aguardando requisições... (Ctrl+C para parar)");

        // Mantém o processo vivo até ser encerrado pelo CI
        Thread.currentThread().join();
    }
}
