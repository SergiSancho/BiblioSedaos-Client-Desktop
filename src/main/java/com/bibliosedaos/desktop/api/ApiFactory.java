package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.api.http.*;
import com.bibliosedaos.desktop.api.mock.MockAuthApi;

/**
 * Fabrica per a la creacio d'implementacions d'API.
 *
 * Permet alternar entre mode mock i mode real segons la configuracio,
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
     * Estableix el mode d'operacio de les APIs.
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
     * Crea una implementacio d'AuthApi segons la configuració actual.
     *
     * En mode mock retorna una implementació amb dades ficticies,
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementacio configurada d'AuthApi
     */
    public static AuthApi createAuthApi() {
        if (useMock) {
            return new MockAuthApi();
        } else {
            return new HttpAuthApi();
        }
    }

    /**
     * Crea una implementacio d'UserApi segons la configuracio actual.
     *
     * En mode mock no implementat,
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementacio configurada d'UserApi
     */
    public static UserApi createUserApi() {
        return new HttpUserApi();
    }

    /**
     * Crea una implementacio de LlibreApi segons la configuracio actual.
     *
     * En mode mock no implementat,
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementacio configurada de LlibreApi
     */
    public static LlibreApi createLlibreApi() {
        return new HttpLlibreApi();
    }

    /**
     * Crea una implementacio d'AutorApi segons la configuracio actual.
     *
     * En mode mock no implementat,
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementacio configurada d'AutorApi
     */
    public static AutorApi createAutorApi() {
        return new HttpAutorApi();
    }

    /**
     * Crea una implementacio d'ExemplarApi segons la configuracio actual.
     *
     * En mode mock no implementat,
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementacio configurada d'ExemplarApi
     */
    public static ExemplarApi createExemplarApi() {
        return new HttpExemplarApi();
    }

    /**
     * Crea una implementacio de PrestecApi segons la configuracio actual.
     *
     * En mode mock no implementat,
     * en mode real retorna un client HTTP que es comunica amb el servidor.
     *
     * @return implementacio configurada de PrestecApi
     */
    public static PrestecApi createPrestecApi() {
        return new HttpPrestecApi();
    }
}