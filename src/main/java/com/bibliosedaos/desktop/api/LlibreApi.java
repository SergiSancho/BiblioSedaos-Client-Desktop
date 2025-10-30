package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.Llibre;
import java.util.List;

/**
 * Interficie per a les operacions de llibres.
 * Gestiona la recuperacio i actualitzacio de dades de llibres.
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface LlibreApi {

    /**
     * Obte tots els llibres del sistema.
     *
     * @return llista de tots els llibres
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    List<Llibre> getAllLlibres() throws ApiException;

    /**
     * Crea un nou llibre al sistema.
     *
     * @param llibre dades del nou llibre
     * @return llibre creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    Llibre createLlibre(Llibre llibre) throws ApiException;

    /**
     * Actualitza les dades d'un llibre.
     *
     * @param id ID del llibre a actualitzar
     * @param llibre dades actualitzades del llibre
     * @return llibre actualitzat
     * @throws ApiException si hi ha errors de comunicacio o validacio
     */
    Llibre updateLlibre(Long id, Llibre llibre) throws ApiException;

    /**
     * Elimina un llibre del sistema.
     *
     * @param id ID del llibre a eliminar
     * @throws ApiException si el llibre no existeix o hi ha errors de permisos
     */
    void deleteLlibre(Long id) throws ApiException;

    /**
     * Obte un llibre pel seu ID.
     *
     * @param id ID del llibre a obtenir
     * @return llibre amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o el llibre no es troba
     */
    Llibre findLlibreById(Long id) throws ApiException;
}