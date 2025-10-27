package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.mock.MockAuthApi;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.security.SessionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d’integració per AuthService utilitzant MockAuthApi i SessionStore.
 *
 * Comprova que tots tres components treballen junts correctament:
 * - AuthService: lògica principal d’autenticació.
 * - MockAuthApi: simulació de l’API REST.
 * - SessionStore: emmagatzematge de la sessió de l’usuari.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class AuthServiceIntegrationWithMockApiTest {

    private AuthService authService;

    /**
     * Inicialitza els components abans de cada prova.
     */
    @BeforeEach
    void setUp() {
        SessionStore.getInstance().clear();
        authService = new AuthService(new MockAuthApi());
    }

    /**
     * Neteja la sessió després de cada prova.
     */
    @AfterEach
    void tearDown() {
        SessionStore.getInstance().clear();
    }

    /**
     * Prova d’integració: login amb credencials d’administrador vàlides.
     *
     * Verifica que:
     * - Es retorna una resposta correcta amb l’ID d’usuari esperat.
     * - Es guarda el token de sessió al SessionStore.
     */
    @Test
    void login_admin_emmagatzemaSessio_i_retornaResposta() throws ApiException {
        LoginResponse resp = authService.login("admin", "admin");

        assertNotNull(resp);
        assertEquals(1L, resp.getUserId());
        assertNotNull(SessionStore.getInstance().getToken());
        assertTrue(SessionStore.getInstance().getToken().startsWith("MOCK-TOKEN-ADMIN"));
    }

    /**
     * Prova d’integració: login amb credencials d’usuari normals.
     *
     * Verifica que el servei permet autenticar un usuari
     * i que el token guardat correspon al rol d’usuari.
     */
    @Test
    void login_user_emmagatzemaSessio_i_retornaResposta() throws ApiException {
        LoginResponse resp = authService.login("user", "user");

        assertNotNull(resp);
        assertEquals(2L, resp.getUserId());
        assertTrue(SessionStore.getInstance().getToken().startsWith("MOCK-TOKEN-USER"));
    }

    /**
     * Prova d’integració: login amb credencials invàlides.
     *
     * Verifica que:
     * - Es llença una ApiException amb el missatge i codi esperats.
     * - No es guarda cap token de sessió a SessionStore.
     */
    @Test
    void login_credencialsInvalides_llancaApiException_i_noEmmagatzemaSessio() {
        ApiException ex = assertThrows(ApiException.class,
                () -> authService.login("noexisteix", "badpass"));

        assertEquals("Credencials invàlides (mock).", ex.getMessage());
        assertEquals(401, ex.getStatusCode());
        assertNull(SessionStore.getInstance().getToken());
    }

    /**
     * Prova d’integració: login amb contrasenya buida.
     *
     * Verifica que:
     * - Es llença una ApiException.
     * - La sessió queda neta després de l’error.
     */
    @Test
    void login_contrasenyaBuida_llancaApiException_i_sessioNeta() {
        assertThrows(ApiException.class, () -> authService.login("admin", ""));
        assertNull(SessionStore.getInstance().getToken());
    }
}
