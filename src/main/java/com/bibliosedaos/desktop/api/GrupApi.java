package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.User;
import java.util.List;

/**
 * Interfície per a les operacions remotes relacionades amb Grups.
 *
 * Segueix l'estil de les altres API del client: mètodes bloquejants,
 * han d'executar-se en fil de fons.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface GrupApi {

    /**
     * Llista tots els grups disponibles al servidor.
     *
     * @return llista de grups
     * @throws ApiException si hi ha errors de comunicació o permisos
     */
    List<Grup> getAllGrups() throws ApiException;

    /**
     * Crea un nou grup al servidor.
     *
     * @param grup dades del grup (nom, tematica, administrador.id, horari.id)
     * @return grup creat amb l'id assignat
     * @throws ApiException si hi ha errors de validació o conflictes (horari reservat, etc.)
     */
    Grup createGrup(Grup grup) throws ApiException;

    /**
     * Elimina un grup pel seu id.
     *
     * @param grupId id del grup a eliminar
     * @throws ApiException si no existeix o no s'hi té permís
     */
    void deleteGrup(Long grupId) throws ApiException;

    /**
     * Afegeix (inscriu) l'usuari identificat per membreId al grup grupId.
     *
     * @param grupId id del grup
     * @param membreId id de l'usuari que s'afegeix (ha de coincidir amb l'usuari autenticat)
     * @return grup actualitzat
     * @throws ApiException si hi ha errors (grup/usari no trobats, límit de membres, etc.)
     */
    Grup afegirUsuariGrup(Long grupId, Long membreId) throws ApiException;

    /**
     * Obté la llista de membres d'un grup.
     *
     * @param grupId id del grup
     * @return llista d'usuaris membres
     * @throws ApiException si el grup no existeix
     */
    List<User> getMembresGrup(Long grupId) throws ApiException;

    /**
     * Elimina (fa sortir o expulsa) l'usuari membreId del grup grupId.
     *
     * @param grupId id del grup
     * @param membreId id de l'usuari a eliminar
     * @throws ApiException si hi ha errors (no trobat o sense permisos)
     */
    void sortirUsuari(Long grupId, Long membreId) throws ApiException;
}

