package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.HorariApi;
import com.bibliosedaos.desktop.model.Horari;

import java.util.List;
import java.util.Objects;

/**
 * Servei per a les operacions d'horaris.
 * Capa intermèdia entre els controladors i l'API d'horaris.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HorariService {
    private final HorariApi horariApi;

    /**
     * Constructor principal amb injeccio de dependencies.
     *
     * @param horariApi implementacio de HorariApi (mock o real)
     * @throws NullPointerException si horariApi es null
     */
    public HorariService(HorariApi horariApi) {
        this.horariApi = Objects.requireNonNull(horariApi, "HorariApi no pot ser null");
    }

    /**
     * Obte tots els horaris del sistema.
     *
     * @return llista de tots els horaris
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Horari> getAllHoraris() throws ApiException {
        return horariApi.getAllHoraris();
    }

    /**
     * Crea un nou horari al sistema.
     *
     * @param horari dades del nou horari
     * @return horari creat amb el ID assignat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Horari createHorari(Horari horari) throws ApiException {
        return horariApi.createHorari(horari);
    }
}