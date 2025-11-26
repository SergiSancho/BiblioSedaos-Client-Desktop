package com.bibliosedaos.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model principal d'horari per a tota l'aplicacio.
 * S'utilitza per a creacio, visualitzacio i qualsevol operacio d'horari.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Horari {
    private Long id;
    private String sala;
    private String dia;
    private String hora;
    private String estat;

    /**
     * Constructor per defecte necessari per a la deserialitzacio JSON.
     */
    public Horari() {}

    /** @return ID unic de l'horari */
    public Long getId() { return id; }

    /** @param id ID unic de l'horari */
    public void setId(Long id) { this.id = id; }

    /** @return sala de l'horari */
    public String getSala() { return sala; }

    /** @param sala sala de l'horari */
    public void setSala(String sala) { this.sala = sala; }

    /** @return dia de l'horari */
    public String getDia() { return dia; }

    /** @param dia dia de l'horari */
    public void setDia(String dia) { this.dia = dia; }

    /** @return hora de l'horari */
    public String getHora() { return hora; }

    /** @param hora hora de l'horari */
    public void setHora(String hora) { this.hora = hora; }

    /** @return estat de l'horari ("reservat" o "lliure") */
    public String getEstat() { return estat; }

    /** @param estat estat de l'horari ("reservat" o "lliure") */
    public void setEstat(String estat) { this.estat = estat; }

    @Override
    public String toString() {
        return String.format("%s - %s %s (%s)",
                sala != null ? sala : "",
                dia != null ? dia : "",
                hora != null ? hora : "",
                estat != null ? estat : "");
    }
}