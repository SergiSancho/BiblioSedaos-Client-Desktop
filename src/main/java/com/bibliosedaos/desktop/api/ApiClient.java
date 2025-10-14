package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.security.SessionStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client compartit per a comunicacions HTTP amb l'API REST.
 *
 * Proporciona infraestructura comuna per a totes les crides d'API:
 * client HTTP, serialització JSON i gestió de fils en segon pla.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class ApiClient {

    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

    /**
     * Retorna la URL base de la API (configurable mitjançant la propietat del sistema).
     *@return la URL base en forma de cadena
     */
    public static String getBaseUrl() {
        return System.getProperty("api.base.url", "http://localhost:8080");
    }

    /** Client HTTP reutilitzable amb configuració per defecte. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

    /** ObjectMapper compartit per serialització i deserialització JSON. */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /** Executor per a tasques en segon pla amb fils dimoni. */
    public static final ExecutorService BG_EXEC = createExecutor();

    /**
     * Constructor privat per a classe d'utilitats.
     * No s'ha d'instanciar.
     */
    private ApiClient() {

    }

    /**
     * Crea un ExecutorService configurat per a execució en segon pla.
     * Pool de 2-50 fils, cua de 200 tasques, política CallerRunsPolicy
     * i fils d'usuari persistents amb noms "biblio-bg-<id>".
     *
     * @return ExecutorService configurat
     */
    private static ExecutorService createExecutor() {
        int core = Math.max(2, Runtime.getRuntime().availableProcessors());
        int max = Math.max(core, 50); // límite razonable para picos
        long keepAlive = 60L;

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(200); // cola acotada
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("biblio-bg-" + t.getId());
            return t;
        };

        ThreadPoolExecutor exec = new ThreadPoolExecutor(
                core,
                max,
                keepAlive, TimeUnit.SECONDS,
                queue,
                tf,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        exec.allowCoreThreadTimeOut(false);
        return exec;
    }


    /**
     * Afegeix la capçalera d'autorització amb el token JWT si està disponible.
     *
     * @param builder constructor de la petició HTTP
     * @return el mateix builder amb la capçalera d'autorització afegida
     */
    public static HttpRequest.Builder withAuth(HttpRequest.Builder builder) {
        String token = SessionStore.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    /**
     * Classe interna per a la deserialització de respostes d'error del servidor.
     *
     * Utilitzada per extreure missatges d'error de les respostes JSON.
     */
    public static class ErrorResponse {

        private String message;

        /**
         * Constructor per defecte necessari per a la deserialització JSON.
         */
        public ErrorResponse() {
            // Constructor buit necessari per la deserialització JSON
        }

        /**
         * Retorna el missatge d'error.
         *
         * @return missatge d'error
         */
        public String getMessage() {
            return message;
        }

        /**
         * Estableix el missatge d'error.
         *
         * Necessari per la deserialització JSON amb Jackson.
         *
         * @param message missatge d'error
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Tanca l'executor de forma ordenada durant la finalització de l'aplicació.
     *
     * Permet als fils existents completar-se (fins a 10 segons) abans de forçar
     * el tancament. Registra advertiments si l'executor resisteix la finalització.
     */
    public static void shutdownExecutor() {
        BG_EXEC.shutdown();
        try {
            if (!BG_EXEC.awaitTermination(10, TimeUnit.SECONDS)) {
                BG_EXEC.shutdownNow();
                if (!BG_EXEC.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.warning("L'executor BG_EXEC no ha finalitzat després de la parada forçada.");
                }
            }
        } catch (InterruptedException e) {
            BG_EXEC.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "El fil que estava esperant la finalització de BG_EXEC ha estat interromput.", e);
        }
    }
    /**
     * Extrau un missatge legible des del cos de la resposta d'error del servidor.
     * Aquest mètode intenta primer analitzar la resposta com a JSON amb un camp "message".
     * Si falla l'anàlisi JSON o el camp message és buit, retorna el text pla netejat.
     *
     * @param body Cos de la resposta HTTP d'error
     * @param fallback Missatge per defecte a retornar si el cos és buit o null
     * @return Missatge d'error netejat i truncat si és necessari
     */
    public static String extractErrorMessage(String body, String fallback) {
        if (body == null || body.isBlank()) return fallback;
        try {
            ErrorResponse err = MAPPER.readValue(body, ErrorResponse.class);
            if (err != null && err.getMessage() != null && !err.getMessage().isBlank()) {
                return err.getMessage();
            }
        } catch (Exception ignored) {
            // no es JSON de la forma { "message": ... } -> usar texto plano
        }
        String txt = body.replaceAll("\\s+", " ").trim();
        return txt.length() <= 300 ? txt : txt.substring(0, 300) + "...";
    }
}