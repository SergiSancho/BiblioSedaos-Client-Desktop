package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.Autor;
import java.util.List;

/**
 * Interficie per a les operacions d'autors.
 * Gestiona la recuperacio i actualitzacio de dades d'autors.
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface AutorApi {

    /**
     * Obte tots els autors del sistema.
     *
     * @return llista de tots els autors
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    List<Autor> getAllAutors() throws ApiException;

    /**
     * Crea un nou autor al sistema.
     *
     * @param autor dades del nou autor
     * @return autor creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    Autor createAutor(Autor autor) throws ApiException;

    /**
     * Elimina un autor del sistema.
     *
     * @param id ID de l'autor a eliminar
     * @throws ApiException si l'autor no existeix o hi ha errors de permisos
     */
    void deleteAutor(Long id) throws ApiException;

    /**
     * Obte un autor pel seu ID.
     *
     * @param id ID de l'autor a obtenir
     * @return autor amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o l'autor no es troba
     */
    Autor findAutorById(Long id) throws ApiException;
}