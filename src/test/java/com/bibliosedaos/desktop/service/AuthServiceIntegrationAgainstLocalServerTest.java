package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpAuthApi;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.security.SessionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integració contra un servidor real.
 *
 * Aquesta prova verifica la comunicació del client amb el servidor real
 * mitjançant les capes HttpAuthApi i AuthService.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class AuthServiceIntegrationAgainstLocalServerTest {

    private AuthService authService;

    /**
     * Configuració inicial abans de cada test.
     * Inicialitza els components necessaris per a la comunicació amb el servidor real.
     */
    @BeforeEach
    void setUp() {
        HttpAuthApi httpAuthApi = new HttpAuthApi();
        authService = new AuthService(httpAuthApi);
        SessionStore.getInstance().clear();
    }

    /**
     * Neteja la sessió després de cada test.
     * Assegura que no queden dades de sessió entre proves consecutives.
     */
    @AfterEach
    void tearDown() {
        SessionStore.getInstance().clear();
    }

    /**
     * Prova d'integració que verifica l'autenticació contra el servidor real.
     *
     * Aquest test valida que el client pot comunicar-se correctament amb el servidor,
     * realitzar un procés d'autenticació i emmagatzemar la sessió resultant.
     *
     * @throws ApiException si hi ha errors en la comunicació amb el servidor
     */
    @Test
    void login_ContraServidorReal_AutenticacioCorrecta() throws ApiException {
        String usuari = "admin";
        String contrasenya = "admin";

        LoginResponse resposta = authService.login(usuari, contrasenya);

        assertNotNull(resposta, "La resposta del servidor no ha de ser null");
        assertNotNull(resposta.getAccessToken(), "El token d'accés ha d'estar present");
        assertNotNull(resposta.getUserId(), "L'ID d'usuari ha d'estar present");

        assertEquals(resposta.getAccessToken(), SessionStore.getInstance().getToken(),
                "El token ha d'haver estat emmagatzemat a la sessió");
        assertEquals(resposta.getUserId(), SessionStore.getInstance().getUserId(),
                "L'ID d'usuari ha d'haver estat emmagatzemat a la sessió");
    }
}