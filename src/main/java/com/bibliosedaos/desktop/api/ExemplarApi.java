package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.Exemplar;
import java.util.List;

/**
 * Interficie per a les operacions d'exemplars.
 * Gestiona la recuperacio i actualitzacio de dades d'exemplars.
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface ExemplarApi {

    /**
     * Obte tots els exemplars del sistema.
     *
     * @return llista de tots els exemplars
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    List<Exemplar> getAllExemplars() throws ApiException;

    /**
     * Obte tots els exemplars lliures del sistema.
     *
     * @return llista d'exemplars disponibles
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    List<Exemplar> getExemplarsLliures() throws ApiException;

    /**
     * Cerca exemplars lliures pel titol del llibre.
     *
     * @param titol titol del llibre a buscar
     * @return llista d'exemplars lliures que coincideixen amb el titol
     * @throws ApiException si hi ha errors de comunicacio
     */
    List<Exemplar> findExemplarsLliuresByTitol(String titol) throws ApiException;

    /**
     * Cerca exemplars lliures pel nom de l'autor.
     *
     * @param autorNom nom de l'autor a buscar
     * @return llista d'exemplars lliures que coincideixen amb l'autor
     * @throws ApiException si hi ha errors de comunicacio
     */
    List<Exemplar> findExemplarsLliuresByAutorNom(String autorNom) throws ApiException;

    /**
     * Crea un nou exemplar al sistema.
     *
     * @param exemplar dades del nou exemplar
     * @return exemplar creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    Exemplar createExemplar(Exemplar exemplar) throws ApiException;

    /**
     * Actualitza les dades d'un exemplar.
     *
     * @param id ID de l'exemplar a actualitzar
     * @param exemplar dades actualitzades de l'exemplar
     * @return exemplar actualitzat
     * @throws ApiException si hi ha errors de comunicacio o validacio
     */
    Exemplar updateExemplar(Long id, Exemplar exemplar) throws ApiException;

    /**
     * Elimina un exemplar del sistema.
     *
     * @param id ID de l'exemplar a eliminar
     * @throws ApiException si l'exemplar no existeix o hi ha errors de permisos
     */
    void deleteExemplar(Long id) throws ApiException;

    /**
     * Obte un exemplar pel seu ID.
     *
     * @param id ID de l'exemplar a obtenir
     * @return exemplar amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o l'exemplar no es troba
     */
    Exemplar findExemplarById(Long id) throws ApiException;
}