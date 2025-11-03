package com.bibliosedaos.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model principal d'usuari per a tota l'aplicacio.
 * S'utilitza per a edicio, visualitzacio i qualsevol operacio d'usuari.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Long id;
    private String nick;
    private String nif;
    private String nom;
    private String cognom1;
    private String cognom2;
    private String localitat;
    private String provincia;
    private String carrer;
    private String cp;
    private String tlf;
    private String email;
    private int rol;
    private String password;

    /**
     * Constructor per defecte necessari per a la deserialitzacio JSON.
     */
    public User() {}

    /** @return ID unic de l'usuari */
    public Long getId() { return id; }

    /** @param id ID unic de l'usuari */
    public void setId(Long id) { this.id = id; }

    /** @return nick d'usuari */
    public String getNick() { return nick; }

    /** @param nick nick d'usuari */
    public void setNick(String nick) { this.nick = nick; }

    /** @return NIF de l'usuari */
    public String getNif() { return nif; }

    /** @param nif NIF de l'usuari */
    public void setNif(String nif) { this.nif = nif; }

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

    /** @return localitat de l'usuari */
    public String getLocalitat() { return localitat; }

    /** @param localitat localitat de l'usuari */
    public void setLocalitat(String localitat) { this.localitat = localitat; }

    /** @return provincia de l'usuari */
    public String getProvincia() { return provincia; }

    /** @param provincia provincia de l'usuari */
    public void setProvincia(String provincia) { this.provincia = provincia; }

    /** @return carrer de l'usuari */
    public String getCarrer() { return carrer; }

    /** @param carrer carrer de l'usuari */
    public void setCarrer(String carrer) { this.carrer = carrer; }

    /** @return codi postal de l'usuari */
    public String getCp() { return cp; }

    /** @param cp codi postal de l'usuari */
    public void setCp(String cp) { this.cp = cp; }

    /** @return telefon de l'usuari */
    public String getTlf() { return tlf; }

    /** @param tlf telefon de l'usuari */
    public void setTlf(String tlf) { this.tlf = tlf; }

    /** @return email de l'usuari */
    public String getEmail() { return email; }

    /** @param email email de l'usuari */
    public void setEmail(String email) { this.email = email; }

    /** @return rol de l'usuari (1=usuari, 2=admin) */
    public int getRol() { return rol; }

    /**
     * Retorna l'etiqueta del rol per la UI.
     * 2 -> "Admin", qualsevol altre valor -> "Usuari"
     *
     * @return "Admin" o "Usuari" o cadena buida si no hi ha rol
     */
    public String getRolLabel() {
        Integer rol = getRol();
        if (rol == null) return "";
        return rol == 2 ? "Admin" : "Usuari";
    }

    /** @param rol rol de l'usuari (1=usuari, 2=admin) */
    public void setRol(int rol) { this.rol = rol; }

    /** @return contrasenya de l'usuari */
    public String getPassword() { return password; }

    /** @param password contrasenya de l'usuari */
    public void setPassword(String password) { this.password = password; }
}