package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.GrupApi;
import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.User;

import java.util.List;
import java.util.Objects;

/**
 * Servei per a les operacions de grups.
 * Capa intermèdia entre els controladors i l'API de grups.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class GrupService {
    private final GrupApi grupApi;

    /**
     * Constructor principal amb injeccio de dependencies.
     *
     * @param grupApi implementacio de GrupApi (mock o real)
     * @throws NullPointerException si grupApi es null
     */
    public GrupService(GrupApi grupApi) {
        this.grupApi = Objects.requireNonNull(grupApi, "GrupApi no pot ser null");
    }

    /**
     * Obte tots els grups del sistema.
     *
     * @return llista de tots els grups
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<Grup> getAllGrups() throws ApiException {
        return grupApi.getAllGrups();
    }

    /**
     * Crea un nou grup al sistema.
     *
     * @param grup dades del nou grup
     * @return grup creat amb el ID assignat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public Grup createGrup(Grup grup) throws ApiException {
        return grupApi.createGrup(grup);
    }

    /**
     * Elimina un grup del sistema.
     *
     * @param grupId ID del grup a eliminar
     * @throws ApiException si falla la comunicacio o el grup no es troba
     */
    public void deleteGrup(Long grupId) throws ApiException {
        grupApi.deleteGrup(grupId);
    }

    /**
     * Afegeix un usuari a un grup.
     *
     * @param grupId ID del grup al que s'afegira l'usuari
     * @param myUserId ID de l'usuari a afegir
     * @return grup actualitzat amb el nou membre
     * @throws ApiException si falla la comunicacio o no es pot afegir l'usuari
     */
    public Grup joinGrup(Long grupId, Long myUserId) throws ApiException {
        return grupApi.afegirUsuariGrup(grupId, myUserId);
    }

    /**
     * Obte tots els membres d'un grup.
     *
     * @param grupId ID del grup del que obtenir els membres
     * @return llista de membres del grup
     * @throws ApiException si falla la comunicacio o el grup no es troba
     */
    public List<User> getMembres(Long grupId) throws ApiException {
        return grupApi.getMembresGrup(grupId);
    }

    /**
     * Elimina un usuari d'un grup.
     *
     * @param grupId ID del grup del que sortir
     * @param membreId ID de l'usuari a eliminar del grup
     * @throws ApiException si falla la comunicacio o l'usuari no es troba al grup
     */
    public void sortirDelGrup(Long grupId, Long membreId) throws ApiException {
        grupApi.sortirUsuari(grupId, membreId);
    }
}