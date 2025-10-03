package com.bibliosedaos.desktop.model.dto;

public class LoginResponse {
    private String accessToken;
    private String userId;
    private int rol; // 0 admin, 1 normal
    private String nom;
    private String cognom1;
    private String cognom2;
    private long expiracio; // epoch seconds (o milis, según se acuerde)

    public LoginResponse() {}

    // getters / setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getRol() { return rol; }
    public void setRol(int rol) { this.rol = rol; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCognom1() { return cognom1; }
    public void setCognom1(String cognom1) { this.cognom1 = cognom1; }

    public String getCognom2() { return cognom2; }
    public void setCognom2(String cognom2) { this.cognom2 = cognom2; }

    public long getExpiracio() { return expiracio; }
    public void setExpiracio(long expiracio) { this.expiracio = expiracio; }
}

