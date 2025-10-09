package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.security.SessionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per ApiClient.
 *
 * Verifica el comportament del client d'API, incloent autenticació
 * i execució de tasques en segon pla.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class ApiClientTest {

    private final Logger apiLogger = Logger.getLogger("com.bibliosedaos.desktop");

    /**
     * Configuració inicial abans de cada test.
     * Neteja l'estat de sessió i silencia logs per a sortida neta.
     */
    @BeforeEach
    void setUp() {
        SessionStore.getInstance().clear();
        apiLogger.setLevel(Level.OFF);
    }

    /**
     * Neteja després de cada test.
     * Assegura estat consistent entre proves.
     */
    @AfterEach
    void tearDown() {
        SessionStore.getInstance().clear();
    }

    /**
     * Prova que withAuth afegeix la capçalera d'autorització quan hi ha token.
     * Verifica el format correcte del header Bearer.
     */
    @Test
    void withAuth_QuanTokenPresent_AfegeixCapcaleraAutoritzacio() {
        SessionStore.getInstance().setToken("test-token-123");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://example.local/test"));

        HttpRequest req = ApiClient.withAuth(builder).build();

        assertTrue(req.headers().firstValue("Authorization").isPresent(),
                "La capçalera d'autorització ha d'estar present");
        assertEquals("Bearer test-token-123",
                req.headers().firstValue("Authorization").get(),
                "El format del token ha de ser 'Bearer {token}'");
    }

    /**
     * Prova que withAuth no afegeix capçalera quan no hi ha token.
     * Verifica el comportament amb sessió buida.
     */
    @Test
    void withAuth_QuanNoToken_NoAfegeixCapcalera() {
        SessionStore.getInstance().clear();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://example.local/test"));

        HttpRequest req = ApiClient.withAuth(builder).build();

        assertFalse(req.headers().firstValue("Authorization").isPresent(),
                "La capçalera d'autorització NO ha d'estar present sense token");
    }

    /**
     * Prova que l'executor BG_EXEC executa tasques correctament.
     * Verifica la funcionalitat bàsica del pool de fils.
     */
    @Test
    void bgExec_ExecutaTasquesCorrectament() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        ApiClient.BG_EXEC.submit(latch::countDown);

        boolean executat = latch.await(1, TimeUnit.SECONDS);
        assertTrue(executat, "BG_EXEC ha d'executar les tasques enviades");
    }
}