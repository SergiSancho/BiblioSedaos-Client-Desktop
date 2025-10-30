package com.bibliosedaos.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model principal de llibre per a tota l'aplicacio.
 * S'utilitza per a creacio, visualitzacio i qualsevol operacio de llibre.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Llibre {
    private Long id;
    private String isbn;
    private String titol;
    private int pagines;
    private String editorial;
    private Autor autor;

    /**
     * Constructor per defecte necessari per a la deserialitzacio JSON.
     */
    public Llibre() {}

    /** @return ID unic del llibre */
    public Long getId() { return id; }

    /** @param id ID unic del llibre */
    public void setId(Long id) { this.id = id; }

    /** @return ISBN del llibre */
    public String getIsbn() { return isbn; }

    /** @param isbn ISBN del llibre */
    public void setIsbn(String isbn) { this.isbn = isbn; }

    /** @return titol del llibre */
    public String getTitol() { return titol; }

    /** @param titol titol del llibre */
    public void setTitol(String titol) { this.titol = titol; }

    /** @return numero de pagines del llibre */
    public int getPagines() { return pagines; }

    /** @param pagines numero de pagines del llibre */
    public void setPagines(int pagines) { this.pagines = pagines; }

    /** @return editorial del llibre */
    public String getEditorial() { return editorial; }

    /** @param editorial editorial del llibre */
    public void setEditorial(String editorial) { this.editorial = editorial; }

    /** @return autor del llibre */
    public Autor getAutor() { return autor; }

    /** @param autor autor del llibre */
    public void setAutor(Autor autor) { this.autor = autor; }
}