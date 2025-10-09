package com.bibliosedaos.desktop.api;

/**
 * Excepció per errors en les operacions d'API.
 *
 * Pot contenir informació addicional com codis d'estat HTTP
 * per a una millor gestió d'errors.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class ApiException extends Exception {

    /** Codi d'estat HTTP associat a l'error. */
    private final int statusCode;

    /**
     * Crea una excepció amb missatge descriptiu.
     *
     * @param message missatge que descriu l'error ocorregut
     */
    public ApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    /**
     * Crea una excepció amb missatge i causa subjacent.
     *
     * @param message missatge que descriu l'error ocorregut
     * @param cause excepció original que va provocar aquest error
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    /**
     * Crea una excepció amb missatge i codi d'estat HTTP.
     *
     * @param message missatge que descriu l'error ocorregut
     * @param statusCode codi d'estat HTTP retornat pel servidor
     */
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Retorna el codi d'estat HTTP associat a l'error.
     *
     * Retorna -1 si l'error no està associat a una resposta HTTP.
     *
     * @return codi d'estat HTTP o -1 si no aplica
     */
    public int getStatusCode() {
        return statusCode;
    }
}