package com.bibliosedaos.desktop.api.mock;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.dto.LoginRequest;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per MockAuthApi.
 *
 * Verifica el comportament del mock d'autenticació amb diferents escenaris:
 * - Login amb credencials vàlides d'admin i usuari
 * - Login amb credencials invàlides
 * - Comportament del logout
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class MockAuthApiTest {

    private MockAuthApi authApi;

    /**
     * Configuració inicial abans de cada test.
     * Crea una nova instància de MockAuthApi per aïllar cada prova.
     */
    @BeforeEach
    void setUp() {
        authApi = new MockAuthApi();
    }

    /**
     * Prova login amb credencials d'administrador vàlides.
     * Verifica que es retornen totes les dades esperades per a l'usuari admin.
     */
    @Test
    void login_AmbCredencialsAdminValides_RetornaRespostaCompleta() throws ApiException {
        LoginRequest request = new LoginRequest("admin", "admin");

        LoginResponse resposta = authApi.login(request);

        assertNotNull(resposta, "La resposta no ha de ser null");
        assertEquals("MOCK-TOKEN-ADMIN-123456", resposta.getAccessToken(), "Token ha de coincidir");
        assertEquals("1", resposta.getUserId(), "UserId ha de ser '1' per admin");
        assertEquals(0, resposta.getRol(), "Rol ha de ser 0 (admin)");
        assertEquals("Sergio", resposta.getNom(), "Nom ha de coincidir");
        assertEquals("Sancho", resposta.getCognom1(), "Primer cognom ha de coincidir");
        assertEquals("Mock", resposta.getCognom2(), "Segon cognom ha de coincidir");
    }

    /**
     * Prova login amb credencials d'usuari normal vàlides.
     * Verifica les dades específiques de l'usuari normal.
     */
    @Test
    void login_AmbCredencialsUserValides_RetornaRespostaUsuari() throws ApiException {
        LoginRequest request = new LoginRequest("user", "user");

        LoginResponse resposta = authApi.login(request);

        assertNotNull(resposta, "La resposta no ha de ser null");
        assertEquals("MOCK-TOKEN-USER-ABCDEF", resposta.getAccessToken(), "Token ha de coincidir");
        assertEquals("2", resposta.getUserId(), "UserId ha de ser '2' per usuari");
        assertEquals(1, resposta.getRol(), "Rol ha de ser 1 (usuari)");
        assertEquals("Antoni", resposta.getNom(), "Nom ha de coincidir");
        assertEquals("Perez", resposta.getCognom1(), "Primer cognom ha de coincidir");
    }

    /**
     * Prova login amb credencials d'admin incorrectes.
     * Verifica que es llença l'excepció esperada amb el missatge correcte.
     */
    @Test
    void login_AmbCredencialsAdminIncorrectes_LlencaApiException() {
        LoginRequest request = new LoginRequest("admin", "contrasenyaIncorrecta");

        ApiException excepcio = assertThrows(ApiException.class,
                () -> authApi.login(request));

        assertEquals("Credencials invàlides (mock).", excepcio.getMessage());
        assertEquals(401, excepcio.getStatusCode());
    }

    /**
     * Prova login amb credencials d'usuari incorrectes.
     * Verifica el comportament amb errors en usuari normal.
     */
    @Test
    void login_AmbCredencialsUserIncorrectes_LlencaApiException() {
        LoginRequest request = new LoginRequest("user", "wrongpass");

        ApiException excepcio = assertThrows(ApiException.class,
                () -> authApi.login(request));

        assertEquals("Credencials invàlides (mock).", excepcio.getMessage());
    }

    /**
     * Prova login amb usuari inexistent.
     * Verifica el comportament amb credencials completament desconegudes.
     */
    @Test
    void login_AmbUsuariInexistent_LlencaApiException() {
        LoginRequest request = new LoginRequest("inexistent", "1234");

        ApiException excepcio = assertThrows(ApiException.class,
                () -> authApi.login(request));

        assertTrue(excepcio.getMessage().contains("Credencials invàlides"));
    }

    /**
     * Prova login amb contrasenya buida.
     * Verifica el comportament amb dades incompletes.
     */
    @Test
    void login_AmbContrasenyaBuida_LlencaApiException() {
        LoginRequest request = new LoginRequest("admin", "");

        assertThrows(ApiException.class, () -> authApi.login(request));
    }

    /**
     * Prova login amb usuari buit.
     * Verifica el comportament amb usuari absent.
     */
    @Test
    void login_AmbUsuariBuit_LlencaApiException() {
        LoginRequest request = new LoginRequest("", "admin");

        assertThrows(ApiException.class, () -> authApi.login(request));
    }

    /**
     * Prova que el logout s'executa sense errors.
     * Verifica que el mètode és idempotent i no llença excepcions.
     */
    @Test
    void logout_QuanCridat_NoLlencaExcepcions() {
        assertDoesNotThrow(() -> authApi.logout());

        assertDoesNotThrow(() -> authApi.logout());
        assertDoesNotThrow(() -> authApi.logout());
    }
}