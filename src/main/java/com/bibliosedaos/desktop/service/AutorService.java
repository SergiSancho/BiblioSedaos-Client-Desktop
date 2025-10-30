package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.AutorApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Autor;

import java.util.List;
import java.util.Objects;

/**
 * Servei per a les operacions d'autors.
 * Capa intermèdia entre els controladors i l'API d'autors.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class AutorService {
    private final AutorApi autorApi;

    /**
     * Constructor principal amb injeccio de dependencies.
     *
     * @param autorApi implementacio de AutorApi (mock o real)
     * @throws NullPointerException si autorApi es null
     */
    public AutorService(AutorApi autorApi) {
        this.autorApi = Objects.requireNonNull(autorApi, "AutorApi no pot ser null");
    }

    /**
     * Obte tots els autors del sistema.
     *
     * @return llista de tots els autors
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Autor> getAllAutors() throws ApiException { return autorApi.getAllAutors(); }

    /**
     * Crea un nou autor al sistema.
     *
     * @param autor dades del nou autor
     * @return autor creat amb el ID assignat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Autor createAutor(Autor autor) throws ApiException { return autorApi.createAutor(autor); }

    /**
     * Elimina un autor del sistema.
     *
     * @param id ID de l'autor a eliminar
     * @throws ApiException si falla la comunicacio o l'autor no es troba
     */
    public void deleteAutor(Long id) throws ApiException { autorApi.deleteAutor(id); }

    /**
     * Obte un autor pel seu ID.
     *
     * @param id ID de l'autor a obtenir
     * @return autor amb totes les seves dades
     * @throws ApiException si falla la comunicacio o l'autor no es troba
     */
    public Autor getAutorById(Long id) throws ApiException { return autorApi.findAutorById(id); }
}