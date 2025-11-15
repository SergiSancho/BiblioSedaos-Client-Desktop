package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.Prestec;
import java.util.List;

/**
 * Interficie per a les operacions de prestecs.
 * Gestiona la recuperacio i actualitzacio de dades de prestecs.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface PrestecApi {

    /**
     * Obte tots els prestecs del sistema.
     *
     * @param usuariId ID de l'usuari per filtrar (opcional, pot ser null)
     * @return llista de tots els prestecs
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    List<Prestec> getAllPrestecs(Long usuariId) throws ApiException;

    /**
     * Obte els prestecs actius (sense data de devolucio).
     *
     * @param usuariId ID de l'usuari per filtrar (opcional, pot ser null)
     * @return llista de prestecs actius
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    List<Prestec> getPrestecsActius(Long usuariId) throws ApiException;

    /**
     * Crea un nou prestec al sistema.
     *
     * @param prestec dades del nou prestec (amb exemplar.id i usuari.id)
     * @return prestec creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    Prestec createPrestec(Prestec prestec) throws ApiException;

    /**
     * Marca un prestec com retornat.
     *
     * @param prestecId ID del prestec a retornar
     * @throws ApiException si el prestec no existeix o hi ha errors de permisos
     */
    void retornarPrestec(Long prestecId) throws ApiException;

    /**
     * Obte un prestec pel seu ID.
     *
     * @param id ID del prestec a obtenir
     * @return prestec amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o el prestec no es troba
     */
    Prestec getPrestecById(Long id) throws ApiException;
}