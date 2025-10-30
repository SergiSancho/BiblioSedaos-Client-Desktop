package com.bibliosedaos.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model principal d'autor per a tota l'aplicacio.
 * S'utilitza per a associar autors als llibres del sistema.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Autor {
    private Long id;
    private String nom;

    /**
     * Constructor per defecte necessari per a la deserialitzacio JSON.
     */
    public Autor() {}

    /**
     * Constructor amb parametres per a crear instancies completes.
     *
     * @param id identificador de l'autor
     * @param nom nom complet de l'autor
     */
    public Autor(Long id, String nom) { this.id = id; this.nom = nom; }

    /** @return ID unic de l'autor */
    public Long getId() { return id; }

    /** @param id ID unic de l'autor */
    public void setId(Long id) { this.id = id; }

    /** @return nom complet de l'autor */
    public String getNom() { return nom; }

    /** @param nom nom complet de l'autor */
    public void setNom(String nom) { this.nom = nom; }

    /**
     * Retorna la representacio en string de l'autor (el seu nom).
     *
     * @return nom de l'autor o cadena buida si es null
     */
    @Override
    public String toString() {
        return nom != null ? nom : "";
    }
}