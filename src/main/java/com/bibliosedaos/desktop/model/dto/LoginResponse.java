package com.bibliosedaos.desktop.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per a la resposta d'autenticació del servidor.
 *
 * Conté el token JWT i les dades de l'usuari autenticat.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    @JsonProperty("token")
    private String accessToken;

    @JsonProperty("id")
    private String userId;

    private int rol; // 2 admin, 1 normal
    private String nom;
    private String cognom1;
    private String cognom2;

    /**
     * Constructor buit per a la deserialització JSON.
     */
    public LoginResponse() {}

    // --- Getters i Setters ---

    /** @return token d'accés JWT per a autenticació */
    public String getAccessToken() { return accessToken; }

    /** @param accessToken token d'accés vàlid */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /** @return identificador únic de l'usuari */
    public String getUserId() { return userId; }

    /** @param userId identificador únic de l'usuari */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return rol de l'usuari (2=admin, 1=normal) */
    public int getRol() { return rol; }

    /** @param rol rol de l'usuari (2=admin, 1=normal) */
    public void setRol(int rol) { this.rol = rol; }

    /** @return nom de l'usuari */
    public String getNom() { return nom; }

    /** @param nom nom de l'usuari */
    public void setNom(String nom) { this.nom = nom; }

    /** @return primer cognom de l'usuari */
    public String getCognom1() { return cognom1; }

    /** @param cognom1 primer cognom de l'usuari */
    public void setCognom1(String cognom1) { this.cognom1 = cognom1; }

    /** @return segon cognom de l'usuari */
    public String getCognom2() { return cognom2; }

    /** @param cognom2 segon cognom de l'usuari */
    public void setCognom2(String cognom2) { this.cognom2 = cognom2; }
}