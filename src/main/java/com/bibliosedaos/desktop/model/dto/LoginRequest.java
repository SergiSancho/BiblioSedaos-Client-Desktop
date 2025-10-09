package com.bibliosedaos.desktop.model.dto;

/**
 * DTO per a les credencials d'autenticació.
 *
 * Conté les dades necessàries per sol·licitar un login al servidor.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class LoginRequest {
    private String nick;
    private String password;

    /**
     * Constructor per defecte necessari per la deserialització JSON.
     */
    public LoginRequest() {}

    /**
     * Constructor principal amb totes les credencials.
     *
     * @param nick nom d'usuari
     * @param password contrasenya
     */
    public LoginRequest(String nick, String password) {
        this.nick = nick;
        this.password = password;
    }

    /** @return nom d'usuari per autenticació */
    public String getNick() { return nick; }

    /** @param nick nom d'usuari per autenticació */
    public void setNick(String nick) { this.nick = nick; }

    /** @return contrasenya de l'usuari */
    public String getPassword() { return password; }

    /** @param password contrasenya de l'usuari */
    public void setPassword(String password) { this.password = password; }
}