package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.security.SessionStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.*;

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
     * Crea l'ExecutorService configurat per a tasques en segon pla.
     *
     * Utilitza fils dimoni que no impedeixen la sortida de l'aplicació
     * i un nombre de fils basat en els processadors disponibles.
     *
     * @return ExecutorService configurat
     */
    private static ExecutorService createExecutor() {
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("biblio-bg-" + t.getId());
            return t;
        };
        return Executors.newFixedThreadPool(threads, tf);
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
     * Tanca l'executor de forma ordenada quan es tanca l'aplicació.
     *
     * Permet als fils en curs completar-se abans de la finalització
     * i força la sortida si supera el temps límit.
     */
    public static void shutdownExecutor() {
        BG_EXEC.shutdown();
        try {
            if (!BG_EXEC.awaitTermination(3, TimeUnit.SECONDS)) {
                BG_EXEC.shutdownNow();
            }
        } catch (InterruptedException e) {
            BG_EXEC.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}