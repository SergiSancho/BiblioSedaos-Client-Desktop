package com.bibliosedaos.desktop.api.mock;

import com.bibliosedaos.desktop.api.*;
import com.bibliosedaos.desktop.model.dto.LoginRequest;
import com.bibliosedaos.desktop.model.dto.LoginResponse;

/**
 * MockAuthApi: implementació d'AuthApi que retorna respostes fictícies.
 * No fa crides HTTP; útil per desenvolupar la UI mentre el servidor no existeix.
 *
 * Actualment admet els usuaris "admin/admin" i "user/user".
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class MockAuthApi implements AuthApi {

    /**
     * Simula el login amb credencials fictícies.
     *
     * @param req objecte LoginRequest amb nick i password
     * @return LoginResponse amb token i dades de l'usuari mock
     * @throws ApiException si les credencials no són vàlides
     */
    @Override
    public LoginResponse login(LoginRequest req) throws ApiException {
        String nick = req.getNick();
        String pwd = req.getPassword();

        // Usuari admin fictici
        if ("admin".equals(nick) && "admin".equals(pwd)) {
            LoginResponse r = new LoginResponse();
            r.setAccessToken("MOCK-TOKEN-ADMIN-123456");
            r.setUserId("1");
            r.setRol(0);
            r.setNom("Sergio");
            r.setCognom1("Sancho");
            r.setCognom2("Mock");
            return r;
        }

        // Usuari normal fictici
        if ("user".equals(nick) && "user".equals(pwd)) {
            LoginResponse r = new LoginResponse();
            r.setAccessToken("MOCK-TOKEN-USER-ABCDEF");
            r.setUserId("2");
            r.setRol(1);
            r.setNom("Antoni");
            r.setCognom1("Perez");
            r.setCognom2("Mock");
            return r;
        }

        throw new ApiException("Credencials invàlides (mock).", 401);
    }

    /**
     * Simula el logout (forma que pertany a la interfície AuthApi).
     * Com és un mock, no fa cap acció real; accepta el token però l'ignora.
     *
     * @param token pot ser null en el mock
     * @throws ApiException mai; present per compatibilitat d'interfície
     */
    @Override
    public void logout(String token) throws ApiException {
        // No-op en el mock. Acceptem token però no fem res.
    }
}
