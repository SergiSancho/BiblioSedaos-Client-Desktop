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

    /**
     * Prova d'integració que verifica el logout contra el servidor real.
     *
     * Aquest test valida que es pot fer logout correctament després del login,
     * netejant la sessió local i notificant al servidor.
     */
    @Test
    void logout_DespresDeLogin_NetrejaSessioLocal() throws ApiException {
        String usuari = "admin";
        String contrasenya = "admin";

        LoginResponse resposta = authService.login(usuari, contrasenya);
        String tokenOriginal = resposta.getAccessToken();

        assertNotNull(tokenOriginal, "Ha d'haver-hi un token després del login");
        assertNotNull(SessionStore.getInstance().getToken(),
                "El token ha d'estar emmagatzemat a la sessió");

        authService.logout();

        assertNull(SessionStore.getInstance().getToken(),
                "El token ha d'haver estat eliminat de la sessió local");
        assertNull(SessionStore.getInstance().getUserId(),
                "L'ID d'usuari ha d'haver estat eliminat de la sessió local");
    }

    /**
     * Prova d'integració que verifica que el logout funciona sense sessió prèvia.
     *
     * Aquest test valida que el logout no falla quan no hi ha sessió activa.
     */
    @Test
    void logout_SenseSessioPrevia_NoLlancaExcepcio() {
        SessionStore.getInstance().clear();

        assertDoesNotThrow(() -> authService.logout(),
                "El logout sense sessió no ha de llançar excepcions");
    }

    /**
     * Prova d'integració que verifica el cicle complet login-logout-login.
     *
     * Aquest test valida que es pot fer login després d'un logout sense problemes.
     */
    @Test
    void login_DespresDeLogout_FuncionaCorrectament() throws ApiException {
        LoginResponse resposta1 = authService.login("admin", "admin");
        String token1 = resposta1.getAccessToken();
        assertNotNull(token1, "Primer token ha d'existir");

        authService.logout();
        assertNull(SessionStore.getInstance().getToken(),
                "Sessió ha d'estar neta després del logout");

        LoginResponse resposta2 = authService.login("admin", "admin");
        String token2 = resposta2.getAccessToken();
        assertNotNull(token2, "Segon token ha d'existir");

        assertEquals(token2, SessionStore.getInstance().getToken(),
                "El nou token ha d'estar emmagatzemat a la sessió");
    }

    /**
     * Prova d'integració que verifica múltiples logouts consecutius.
     *
     * Aquest test valida que múltiples crides a logout no causen errors.
     */
    @Test
    void logout_MultiplesCrides_NoCausenErrors() throws ApiException {
        authService.login("admin", "admin");
        assertNotNull(SessionStore.getInstance().getToken(),
                "Ha d'haver-hi sessió després del login");

        assertDoesNotThrow(() -> {
            authService.logout(); // Primer logout
            authService.logout(); // Segon logout (sessió ja neta)
            authService.logout(); // Tercer logout (sessió ja neta)
        }, "Múltiples logouts no han de llançar excepcions");

        assertNull(SessionStore.getInstance().getToken(),
                "La sessió ha d'estar neta després de múltiples logouts");
    }
}