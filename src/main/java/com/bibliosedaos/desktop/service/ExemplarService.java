package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ExemplarApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Exemplar;

import java.util.List;
import java.util.Objects;

/**
 * Servei per a les operacions d'exemplars.
 * Capa intermèdia entre els controladors i l'API d'exemplars.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class ExemplarService {
    private final ExemplarApi exemplarApi;

    /**
     * Constructor principal amb injeccio de dependencies.
     *
     * @param exemplarApi implementacio de ExemplarApi (mock o real)
     * @throws NullPointerException si exemplarApi es null
     */
    public ExemplarService(ExemplarApi exemplarApi) {
        this.exemplarApi = Objects.requireNonNull(exemplarApi, "ExemplarApi no pot ser null");
    }

    /**
     * Obte tots els exemplars del sistema.
     *
     * @return llista de tots els exemplars
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Exemplar> getAllExemplars() throws ApiException { return exemplarApi.getAllExemplars(); }

    /**
     * Obte tots els exemplars lliures (disponibles) del sistema.
     *
     * @return llista d'exemplars disponibles
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Exemplar> getExemplarsLliures() throws ApiException { return exemplarApi.getExemplarsLliures(); }

    /**
     * Cerca exemplars lliures pel titol del llibre.
     *
     * @param titol titol del llibre a buscar
     * @return llista d'exemplars lliures que coincideixen amb el titol
     * @throws ApiException si falla la comunicacio o no es troben resultats
     */
    public List<Exemplar> findExemplarsByTitol(String titol) throws ApiException { return exemplarApi.findExemplarsLliuresByTitol(titol); }

    /**
     * Cerca exemplars lliures pel nom de l'autor.
     *
     * @param autorNom nom de l'autor a buscar
     * @return llista d'exemplars lliures que coincideixen amb l'autor
     * @throws ApiException si falla la comunicacio o no es troben resultats
     */
    public List<Exemplar> findExemplarsByAutor(String autorNom) throws ApiException { return exemplarApi.findExemplarsLliuresByAutorNom(autorNom); }

    /**
     * Crea un nou exemplar al sistema.
     *
     * @param ex dades del nou exemplar
     * @return exemplar creat amb el ID assignat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Exemplar createExemplar(Exemplar ex) throws ApiException { return exemplarApi.createExemplar(ex); }

    /**
     * Actualitza un exemplar al servidor.
     *
     * @param id ID de l'exemplar a actualitzar
     * @param ex dades actualitzades de l'exemplar
     * @return exemplar actualitzat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Exemplar updateExemplar(Long id, Exemplar ex) throws ApiException { return exemplarApi.updateExemplar(id, ex); }

    /**
     * Elimina un exemplar del sistema.
     *
     * @param id ID de l'exemplar a eliminar
     * @throws ApiException si falla la comunicacio o l'exemplar no es troba
     */
    public void deleteExemplar(Long id) throws ApiException { exemplarApi.deleteExemplar(id); }

    /**
     * Obte un exemplar pel seu ID.
     *
     * @param id ID de l'exemplar a obtenir
     * @return exemplar amb totes les seves dades
     * @throws ApiException si falla la comunicacio o l'exemplar no es troba
     */
    public Exemplar getExemplarById(Long id) throws ApiException { return exemplarApi.findExemplarById(id); }
}