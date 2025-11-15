package com.bibliosedaos.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

/**
 * Model de Prestec per a tota l'aplicacio.
 * S'utilitza per a creacio, visualitzacio i qualsevol operacio de prestecs.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Prestec {
    private Long id;
    private LocalDate dataPrestec;
    private LocalDate dataDevolucio;
    private User usuari;
    private Exemplar exemplar;

    /**
     * Constructor per defecte necessari per a la deserialitzacio JSON.
     */
    public Prestec() {}

    /** @return ID unic del prestec */
    public Long getId() { return id; }

    /** @param id ID unic del prestec */
    public void setId(Long id) { this.id = id; }

    /** @return data de realitzacio del prestec */
    public LocalDate getDataPrestec() { return dataPrestec; }

    /** @param dataPrestec data de realitzacio del prestec */
    public void setDataPrestec(LocalDate dataPrestec) { this.dataPrestec = dataPrestec; }

    /** @return data de devolucio del prestec */
    public LocalDate getDataDevolucio() { return dataDevolucio; }

    /** @param dataDevolucio data de devolucio del prestec */
    public void setDataDevolucio(LocalDate dataDevolucio) { this.dataDevolucio = dataDevolucio; }

    /** @return usuari associat al prestec */
    public User getUsuari() { return usuari; }

    /** @param usuari usuari associat al prestec */
    public void setUsuari(User usuari) { this.usuari = usuari; }

    /** @return exemplar associat al prestec */
    public Exemplar getExemplar() { return exemplar; }

    /** @param exemplar exemplar associat al prestec */
    public void setExemplar(Exemplar exemplar) { this.exemplar = exemplar; }
}