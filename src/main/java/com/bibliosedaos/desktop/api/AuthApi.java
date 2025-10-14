package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.dto.LoginRequest;
import com.bibliosedaos.desktop.model.dto.LoginResponse;

/**
 * Interfície que defineix les operacions d'autenticació.
 *
 * Les implementacions poden ser mocks per a desenvolupament
 * o clients HTTP reals per a producció.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface AuthApi {

    /**
     * Autentica un usuari amb les credencials proporcionades.
     *
     * Aquest mètode és bloquejant i s'ha d'executar en un fil de fons
     * per no bloquejar la interfície d'usuari.
     *
     * @param req objecte amb les credencials d'autenticació
     * @return resposta amb el token i les dades de l'usuari
     * @throws ApiException si falla la comunicació o les credencials són incorrectes
     */
    LoginResponse login(LoginRequest req) throws ApiException;

    /**
     * Tanca la sessió de l'usuari actual.
     *
     * En implementacions reals, pot notificar al servidor el tancament de sessió.
     * En mocks, normalment no realitza cap acció.
     *
     * @throws ApiException si hi ha errors en el procés de tancament
     */
    void logout(String token) throws ApiException;
}