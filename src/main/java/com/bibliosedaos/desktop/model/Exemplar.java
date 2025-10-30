package com.bibliosedaos.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model principal d'exemplar per a tota l'aplicacio.
 * Representa una copia fisica d'un llibre amb la seva ubicacio i estat.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Exemplar {
    private Long id;
    private String lloc;
    private String reservat;
    private Llibre llibre;

    /**
     * Constructor per defecte necessari per a la deserialitzacio JSON.
     */
    public Exemplar() {}

    /** @return ID unic de l'exemplar */
    public Long getId() { return id; }

    /** @param id ID unic de l'exemplar */
    public void setId(Long id) { this.id = id; }

    /** @return lloc fisic on es troba l'exemplar */
    public String getLloc() { return lloc; }

    /** @param lloc lloc fisic on es troba l'exemplar */
    public void setLloc(String lloc) { this.lloc = lloc; }

    /** @return estat de reserva de l'exemplar */
    public String getReservat() { return reservat; }

    /** @param reservat estat de reserva de l'exemplar */
    public void setReservat(String reservat) { this.reservat = reservat; }

    /** @return llibre al que pertany l'exemplar */
    public Llibre getLlibre() { return llibre; }

    /** @param llibre llibre al que pertany l'exemplar */
    public void setLlibre(Llibre llibre) { this.llibre = llibre; }
}