package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.api.http.HttpAuthApi;
import com.bibliosedaos.desktop.api.http.HttpUserApi;
import com.bibliosedaos.desktop.api.mock.MockAuthApi;

/**
 * Fàbrica per a la creació d'implementacions d'API.
 *
 * Permet alternar entre mode mock i mode real segons la configuració,
 * facilitant el desenvolupament i les proves.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public final class ApiFactory {

    /** Indicador per utilitzar implementacions mock o reals. */
    private static boolean useMock;

    /**
     * Constructor privat per a classe d'utilitats.
     * No s'ha d'instanciar.
     */
    private ApiFactory() {
        // Classe només amb mètodes estàtics
    }

    /**
     * Estableix el mode d'operació de les APIs.
     *
     * @param value true per utilitzar mocks, false per implementacions reals
     */
    public static void setUseMock(boolean value) {
        useMock = value;
    }

    /**
     * Indica si s'estan utilitzant implementacions mock.
     *
     * @return true si s'utilitzen mocks, false si s'utilitzen implementacions reals
     */
    public static boolean isUseMock() {
        return useMock;
    }

    /**
     * Crea una implementació d'AuthApi segons la configuració actual.
     *
     * En mode mock retorna una implementació amb dades fictícies,
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementació configurada d'AuthApi
     */
    public static AuthApi createAuthApi() {
        if (useMock) {
            return new MockAuthApi();
        } else {
            return new HttpAuthApi();
        }
    }

    /**
     * Crea una implementació d'UserApi segons la configuració actual.
     *
     * En mode mock no implementat
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementació configurada d'UserApi
     */
    public static UserApi createUserApi() {
        return new HttpUserApi();
    }
}