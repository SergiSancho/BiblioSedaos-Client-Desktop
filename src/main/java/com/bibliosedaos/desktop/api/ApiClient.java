package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.security.SessionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client compartit per a comunicacions HTTP/HTTPS amb l'API REST.
 *
 * Proporciona infraestructura comuna per a totes les crides d'API:
 * client HTTP (configurable per truststore), serialització JSON i gestió
 * de fils en segon pla.
 *
 * Llegiu les propietats JVM suportades:
 * -Dapi.base.url=https://localhost:8443
 * -Dapi.ssl.trustStore=/ruta/a/truststore.jks
 * -Dapi.ssl.trustStorePassword=changeit
 *
 * Si no es passa trustStore, s'usarà l'SSLContext per defecte de la JVM
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
     * Per defecte HTTPS i port 8443.
     *
     * @return la URL base en forma de cadena
     */
    public static String getBaseUrl() {
        return System.getProperty("api.base.url", "https://localhost:8443");
    }

    /** HttpClient reutilitzable configurat amb SSLContext segons propietats. */
    public static final HttpClient HTTP_CLIENT = createHttpClient();

    /** ObjectMapper compartit per serialització i deserialització JSON. */
    public static final ObjectMapper MAPPER = createMapper();

    /** Executor per a tasques en segon pla amb fils dimoni. */
    public static final ExecutorService BG_EXEC = createExecutor();

    /**
     * Constructor privat per a classe d'utilitats.
     * No s'ha d'instanciar.
     */
    private ApiClient() {
        // utilitat: no instanciar
    }

    /**
     * Crea i configura l'ObjectMapper utilitzat a tota l'aplicació.
     *
     * @return ObjectMapper configurat
     */
    private static ObjectMapper createMapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return m;
    }

    /**
     * Crea l'ExecutorService per a tasques en segon pla.
     *
     * @return ExecutorService configurat
     */
    private static ExecutorService createExecutor() {
        int core = Math.max(2, Runtime.getRuntime().availableProcessors());
        int max = Math.max(core, 50);
        long keepAlive = 60L;

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(200);
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("biblio-bg-" + t.getId());
            return t;
        };

        ThreadPoolExecutor exec = new ThreadPoolExecutor(
                core,
                max,
                keepAlive, TimeUnit.SECONDS,
                queue,
                tf,
                new ThreadPoolExecutor.AbortPolicy()
        );
        exec.allowCoreThreadTimeOut(false);
        return exec;
    }

    /**
     * Crea un HttpClient configurat amb un SSLContext construït des d'un truststore opcional.
     * Propietats del sistema:
     * - api.ssl.trustStore
     * - api.ssl.trustStorePassword
     *
     * Si no es proporcionen, s'usa SSLContext.getDefault().
     *
     * @return HttpClient configurat
     */
    private static HttpClient createHttpClient() {
        try {
            String trustStorePath = System.getProperty("api.ssl.trustStore");
            String trustStorePass = System.getProperty("api.ssl.trustStorePassword");

            SSLContext sslContext;
            if (trustStorePath != null && !trustStorePath.isBlank()) {
                sslContext = buildSslContextFromTrustStore(trustStorePath, trustStorePass);
                LOGGER.info(() -> "SSLContext creat des de trustStore: " + trustStorePath);
            } else {
                sslContext = SSLContext.getDefault();
                LOGGER.info("Usant SSLContext per defecte de la JVM");
            }

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(10))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No s'ha pogut inicialitzar SSLContext personalitzat; s'usa HttpClient per defecte.", e);
            return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        }
    }

    /**
     * Construeix un SSLContext carregant el truststore JKS des de la ruta indicada.
     *
     * @param trustStorePath ruta al fitxer truststore.jks
     * @param trustStorePassword contrasenya del truststore (pot ser null)
     * @return SSLContext inicialitzat amb TrustManagers del truststore
     * @throws Exception si hi ha errors de lectura o inicialització
     */
    private static SSLContext buildSslContextFromTrustStore(String trustStorePath, String trustStorePassword) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream is = Files.newInputStream(Paths.get(trustStorePath))) {
            char[] pass = (trustStorePassword == null) ? null : trustStorePassword.toCharArray();
            trustStore.load(is, pass);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
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
     */
    public static class ErrorResponse {
        private String message;
        public ErrorResponse() { }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Tanca l'executor de forma ordenada durant la finalització de l'aplicació.
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
     * Extrau un missatge llegible des del cos de la resposta d'error del servidor.
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
        } catch (Exception ignored) { }
        String txt = body.replaceAll("\\s+", " ").trim();
        return txt.length() <= 300 ? txt : txt.substring(0, 300) + "...";
    }
}
