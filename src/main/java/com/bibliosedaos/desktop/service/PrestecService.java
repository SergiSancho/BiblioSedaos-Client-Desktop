package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.PrestecApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Prestec;

import java.util.List;
import java.util.Objects;

/**
 * Servei per a les operacions de prestecs.
 * Capa intermèdia entre els controladors i l'API de prestecs.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class PrestecService {
    private final PrestecApi prestecApi;

    /**
     * Constructor principal amb injeccio de dependencies.
     *
     * @param prestecApi implementacio de PrestecApi (mock o real)
     * @throws NullPointerException si prestecApi es null
     */
    public PrestecService(PrestecApi prestecApi) {
        this.prestecApi = Objects.requireNonNull(prestecApi, "PrestecApi no pot ser null");
    }

    /**
     * Obte tots els prestecs del sistema.
     *
     * @param usuariId ID de l'usuari per filtrar (opcional)
     * @return llista de tots els prestecs
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Prestec> getAllPrestecs(Long usuariId) throws ApiException {
        return prestecApi.getAllPrestecs(usuariId);
    }

    /**
     * Obte els prestecs actius del sistema.
     *
     * @param usuariId ID de l'usuari per filtrar (opcional)
     * @return llista de prestecs actius
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Prestec> getPrestecsActius(Long usuariId) throws ApiException {
        return prestecApi.getPrestecsActius(usuariId);
    }

    /**
     * Crea un nou prestec al sistema.
     *
     * @param prestec dades del nou prestec
     * @return prestec creat amb el ID assignat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Prestec createPrestec(Prestec prestec) throws ApiException {
        return prestecApi.createPrestec(prestec);
    }

    /**
     * Marca un prestec com retornat.
     *
     * @param prestecId ID del prestec a retornar
     * @throws ApiException si falla la comunicacio o el prestec no es troba
     */
    public void retornarPrestec(Long prestecId) throws ApiException {
        prestecApi.retornarPrestec(prestecId);
    }

    /**
     * Obte un prestec pel seu ID.
     *
     * @param id ID del prestec a obtenir
     * @return prestec amb totes les seves dades
     * @throws ApiException si falla la comunicacio o el prestec no es troba
     */
    public Prestec getPrestecById(Long id) throws ApiException {
        return prestecApi.getPrestecById(id);
    }
}