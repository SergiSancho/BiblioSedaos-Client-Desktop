package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.AuthApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.dto.LoginRequest;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.security.SessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Proves unitàries per AuthService.
 *
 * Verifica el comportament del servei d'autenticació amb mocks.
 * No es requereix connexió real amb el servidor.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class AuthServiceTest {

    private AuthApi authApiMock;
    private AuthService authService;

    /**
     * Configuració inicial abans de cada test.
     * Crea els mocks i neteja l'estat de la sessió.
     */
    @BeforeEach
    void setUp() {
        authApiMock = mock(AuthApi.class);
        authService = new AuthService(authApiMock);
        SessionStore.getInstance().clear();
    }

    /**
     * Prova login correcte amb credencials vàlides.
     * Verifica que es retorna la resposta i s'emmagatzema la sessió.
     */
    @Test
    void login_AmbCredencialsValides_RetornaRespostaIEmmagatzemaSessio() throws ApiException {
        LoginResponse respostaMock = new LoginResponse();
        respostaMock.setAccessToken("token123");
        respostaMock.setUserId(1L);
        respostaMock.setRol(1);
        respostaMock.setNom("Usuari");
        respostaMock.setCognom1("Test");

        when(authApiMock.login(any(LoginRequest.class))).thenReturn(respostaMock);

        LoginResponse resultat = authService.login("usuari", "pass");

        assertNotNull(resultat);
        assertEquals("token123", resultat.getAccessToken());
        assertEquals("token123", SessionStore.getInstance().getToken());
        assertEquals(1L, SessionStore.getInstance().getUserId());
    }

    /**
     * Prova login fallit amb credencials incorrectes.
     * Verifica que es llença l'excepció esperada.
     */
    @Test
    void login_AmbCredencialsInvalides_LlencaApiException() throws ApiException {
        when(authApiMock.login(any(LoginRequest.class)))
                .thenThrow(new ApiException("Credencials invàlides"));

        ApiException excepcio = assertThrows(ApiException.class,
                () -> authService.login("usuari", "contrasenyaIncorrecta"));

        assertEquals("Credencials invàlides", excepcio.getMessage());
    }

    /**
     * Prova que el logout neteja correctament la sessió.
     * Verifica que després del logout no hi ha dades de sessió.
     */
    @Test
    void logout_QuanSessioActiva_NetejaTotesLesDades() {
        SessionStore.getInstance().setToken("token123");
        SessionStore.getInstance().setUserId(1L);
        SessionStore.getInstance().setNom("Usuari");

        authService.logout();

        assertNull(SessionStore.getInstance().getToken());
        assertNull(SessionStore.getInstance().getUserId());
        assertNull(SessionStore.getInstance().getNom());
    }
}