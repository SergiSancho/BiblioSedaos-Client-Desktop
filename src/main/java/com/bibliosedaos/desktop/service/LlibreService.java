package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.LlibreApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Llibre;

import java.util.List;
import java.util.Objects;

/**
 * Servei per a les operacions de llibres.
 * Capa intermèdia entre els controladors i l'API de llibres.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class LlibreService {
    private final LlibreApi llibreApi;

    /**
     * Constructor principal amb injeccio de dependencies.
     *
     * @param llibreApi implementacio de LlibreApi (mock o real)
     * @throws NullPointerException si llibreApi es null
     */
    public LlibreService(LlibreApi llibreApi) {
        this.llibreApi = Objects.requireNonNull(llibreApi, "LlibreApi no pot ser null");
    }

    /**
     * Obte tots els llibres del sistema.
     *
     * @return llista de tots els llibres
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Llibre> getAllBooks() throws ApiException { return llibreApi.getAllLlibres(); }

    /**
     * Obte un llibre pel seu ID.
     *
     * @param id ID del llibre a obtenir
     * @return llibre amb totes les seves dades
     * @throws ApiException si falla la comunicacio o el llibre no es troba
     */
    public Llibre getBookById(Long id) throws ApiException { return llibreApi.findLlibreById(id); }

    /**
     * Crea un nou llibre al sistema.
     *
     * @param llibre dades del nou llibre
     * @return llibre creat amb el ID assignat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Llibre createBook(Llibre llibre) throws ApiException { return llibreApi.createLlibre(llibre); }

    /**
     * Actualitza un llibre al servidor.
     *
     * @param id ID del llibre a actualitzar
     * @param llibre dades actualitzades del llibre
     * @return llibre actualitzat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Llibre updateBook(Long id, Llibre llibre) throws ApiException { return llibreApi.updateLlibre(id, llibre); }

    /**
     * Elimina un llibre del sistema.
     *
     * @param id ID del llibre a eliminar
     * @throws ApiException si falla la comunicacio o el llibre no es troba
     */
    public void deleteBook(Long id) throws ApiException { llibreApi.deleteLlibre(id); }
}