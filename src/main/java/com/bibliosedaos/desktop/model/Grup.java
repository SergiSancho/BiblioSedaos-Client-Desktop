package com.bibliosedaos.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Model principal de grup per a tota l'aplicacio.
 * S'utilitza per a creacio, visualitzacio i qualsevol operacio de grup.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Grup {
    private Long id;
    private String nom;
    private String tematica;
    private User administrador;
    private Horari horari;
    private List<User> membres;

    /**
     * Constructor per defecte necessari per a la deserialitzacio JSON.
     */
    public Grup() {}

    /** @return ID unic del grup */
    public Long getId() { return id; }

    /** @param id ID unic del grup */
    public void setId(Long id) { this.id = id; }

    /** @return nom del grup */
    public String getNom() { return nom; }

    /** @param nom nom del grup */
    public void setNom(String nom) { this.nom = nom; }

    /** @return tematica del grup */
    public String getTematica() { return tematica; }

    /** @param tematica tematica del grup */
    public void setTematica(String tematica) { this.tematica = tematica; }

    /** @return administrador del grup */
    public User getAdministrador() { return administrador; }

    /** @param administrador administrador del grup */
    public void setAdministrador(User administrador) { this.administrador = administrador; }

    /** @return horari del grup */
    public Horari getHorari() { return horari; }

    /** @param horari horari del grup */
    public void setHorari(Horari horari) { this.horari = horari; }

    /** @return llista de membres del grup */
    public List<User> getMembres() { return membres; }

    /** @param membres llista de membres del grup */
    public void setMembres(List<User> membres) { this.membres = membres; }
}