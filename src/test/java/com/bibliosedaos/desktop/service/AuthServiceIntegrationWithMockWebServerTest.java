package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpAuthApi;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.security.SessionStore;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integració controlada de la capa d'autenticació:
 * MockWebServer + HttpAuthApi + AuthService.
 *
 * Aquesta prova simula respostes HTTP reals (200, 401, 500) sense necessitat d'un
 * servidor extern.
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class AuthServiceIntegrationWithMockWebServerTest {

    private MockWebServer mockWebServer;
    private AuthService authService;

    /**
     * Inicia el MockWebServer abans de cada test i configura la propietat
     * del sistema "api.base.url" perquè HttpAuthApi utilitzi la URL del mock.
     */
    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Forcem ApiClient a apuntar al MockWebServer durant la prova
        System.setProperty("api.base.url", mockWebServer.url("/").toString());

        HttpAuthApi httpAuthApi = new HttpAuthApi(); // Convertido a variable local
        authService = new AuthService(httpAuthApi);

        SessionStore.getInstance().clear();
    }

    /**
     * Atura el MockWebServer i neteja propietats/sessió després de cada test.
     */
    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty("api.base.url");
        SessionStore.getInstance().clear();

        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    /**
     * Prova que una resposta HTTP 200 amb JSON correcte es parseja bé i que
     * AuthService emmagatzema la sessió (token + userId).
     */
    @Test
    void login_RespostaExitosaDelServidor_SessioEmmagatzemada() throws Exception {
        String jsonRespostaExitosa = """
            {
                "token": "TOKEN-JWT-REAL-12345",
                "id": "99",
                "rol": 1,
                "nom": "Usuari",
                "cognom1": "Test",
                "cognom2": "MockWebServer"
            }
            """;

        MockResponse respostaMock = new MockResponse()
                .setResponseCode(200)
                .setBody(jsonRespostaExitosa)
                .addHeader("Content-Type", "application/json");

        mockWebServer.enqueue(respostaMock);

        LoginResponse resposta = authService.login("usuari", "contrasenya");

        assertNotNull(resposta, "La resposta no ha de ser null");
        assertEquals("99", resposta.getUserId(), "UserId ha de coincidir amb el JSON");
        assertEquals("TOKEN-JWT-REAL-12345", resposta.getAccessToken(), "Token ha de coincidir amb el JSON");

        assertEquals("TOKEN-JWT-REAL-12345", SessionStore.getInstance().getToken(),
                "El token ha d'haver estat emmagatzemat a la sessió");
        assertEquals("99", SessionStore.getInstance().getUserId(),
                "L'userId ha d'haver estat emmagatzemat a la sessió");
    }

    /**
     * Prova que una resposta HTTP 401 provoca que AuthService llanci ApiException
     * i que no s'emmagatzemi cap sessió.
     */
    @Test
    void login_CredencialsInvalides_PropagaApiException() {
        String jsonError = """
            {
                "message": "Credencials invàlides"
            }
            """;

        MockResponse respostaError = new MockResponse()
                .setResponseCode(401)
                .setBody(jsonError)
                .addHeader("Content-Type", "application/json");

        mockWebServer.enqueue(respostaError);

        ApiException excepcio = assertThrows(ApiException.class,
                () -> authService.login("invalid", "credentials"),
                "S'ha de llençar ApiException amb credencials invàlides");

        assertEquals(401, excepcio.getStatusCode(), "El codi d'estat ha de ser 401");
        assertNull(SessionStore.getInstance().getToken(), "No s'ha d'haver emmagatzemat cap token");
    }

    /**
     * Prova que una resposta HTTP 500 del servidor provoca ApiException amb codi 500.
     */
    @Test
    void login_ErrorInternServidor_PropagaApiException() {
        MockResponse respostaError = new MockResponse()
                .setResponseCode(500)
                .setBody("Error intern del servidor");

        mockWebServer.enqueue(respostaError);

        ApiException excepcio = assertThrows(ApiException.class,
                () -> authService.login("usuari", "contrasenya"),
                "S'ha de llençar ApiException amb error intern del servidor");

        assertEquals(500, excepcio.getStatusCode(), "El codi d'estat ha de ser 500");
    }

    /**
     * Aquest test ajuda a identificar problemes de configuració del MockWebServer.
     */
    @Test
    void debug_VerificarQueFunciona() {

        String jsonResposta = """
            {
                "token": "test-token-123",
                "id": "1",
                "rol": 2,
                "nom": "Debug",
                "cognom1": "Test",
                "cognom2": "User"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResposta)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        System.out.println("=== DEBUG INFO ===");
        System.out.println("Mock Server URL: " + mockWebServer.url("/"));
        System.out.println("ApiClient Base URL: " + System.getProperty("api.base.url"));

        assertDoesNotThrow(() -> {
            LoginResponse resp = authService.login("test", "test");
            assertNotNull(resp, "La resposta no ha de ser null");
            System.out.println("LoginResponse: " + resp);
            System.out.println("AccessToken: " + resp.getAccessToken());
            System.out.println("UserId: " + resp.getUserId());
        });
    }
}