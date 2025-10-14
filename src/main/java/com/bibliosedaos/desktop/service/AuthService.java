package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.AuthApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.dto.LoginRequest;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.security.SessionStore;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servei d'autenticació.
 * Capa intermèdia entre els controladors i l'API d'autenticació.
 * Gestiona el procés de login/logout i l'emmagatzematge de la sessió.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    /** Instància de AuthApi (injectada via constructor). */
    private final AuthApi authApi;

    /**
     * Constructor principal amb injecció de dependències.
     *
     * @param authApi implementació d'AuthApi (mock o real)
     * @throws NullPointerException si authApi és null
     */
    public AuthService(AuthApi authApi) {
        this.authApi = Objects.requireNonNull(authApi, "AuthApi no pot ser null");
    }

    /**
     * Autentica un usuari amb les credencials proporcionades.
     * En cas d'èxit, emmagatzema el token i les dades de l'usuari a la sessió.
     * Aquest mètode és bloquejant i s'ha d'executar en un fil de fons.
     *
     * @param nick nom d'usuari
     * @param password contrasenya
     * @return resposta amb token i dades d'usuari
     * @throws ApiException si falla la comunicació o les credencials són incorrectes
     */
    public LoginResponse login(String nick, String password) throws ApiException {
        LoginRequest req = new LoginRequest(nick, password);
        LoginResponse resp = authApi.login(req);

        if (resp != null && resp.getAccessToken() != null && !resp.getAccessToken().isEmpty()) {
            SessionStore store = SessionStore.getInstance();
            store.setToken(resp.getAccessToken());
            store.setUserId(resp.getUserId());
            store.setRol(resp.getRol());
            store.setNom(resp.getNom());
            store.setCognom1(resp.getCognom1());
            store.setCognom2(resp.getCognom2());
        }

        return resp;
    }

    /**
     * Tanca la sessió de l'usuari actual.
     * Neteja totes les dades de sessió emmagatzemades en memòria.
     * Petició de logout al servidor (posarà token a blacklist)
     */
    public void logout() {
        String token = SessionStore.getInstance().getToken();

        SessionStore.getInstance().clear();

        try {
            authApi.logout(token);
        } catch (ApiException e) {
            LOGGER.log(Level.INFO, "Error al revocar token en servidor: {0}", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Excepció inesperada al fer logout: {0}", e.getMessage());
        }
    }
}