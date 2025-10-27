package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.User;
import java.util.List;

/**
 * Interficie per a les operacions d'usuari.
 * Gestiona la recuperacio i actualitzacio de dades d'usuari.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface UserApi {

    /**
     * Obte les dades completes d'un usuari pel seu ID.
     *
     * @param userId ID de l'usuari
     * @return usuari amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o l'usuari no es troba
     */
    User getUserById(Long userId) throws ApiException;

    /**
     * Actualitza les dades d'un usuari.
     *
     * @param userId ID de l'usuari a actualitzar
     * @param user dades actualitzades de l'usuari
     * @return usuari actualitzat
     * @throws ApiException si hi ha errors de comunicacio o validacio
     */
    User updateUser(Long userId, User user) throws ApiException;

    /**
     * Obte tots els usuaris del sistema.
     *
     * @return llista de tots els usuaris
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    List<User> getAllUsers() throws ApiException;

    /**
     * Crea un nou usuari al sistema.
     *
     * @param user dades del nou usuari
     * @return usuari creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    User createUser(User user) throws ApiException;

    /**
     * Elimina un usuari del sistema.
     *
     * @param userId ID de l'usuari a eliminar
     * @throws ApiException si l'usuari no existeix o hi ha errors de permisos
     */
    void deleteUser(Long userId) throws ApiException;

    /**
     * Cerca un usuari pel seu nick.
     *
     * @param nick nick a buscar
     * @return usuari trobat
     * @throws ApiException si hi ha errors de comunicacio
     */
    User getUserByNick(String nick) throws ApiException;

    /**
     * Cerca un usuari pel seu NIF.
     *
     * @param nif NIF a buscar
     * @return usuari trobat
     * @throws ApiException si hi ha errors de comunicacio
     */
    User getUserByNif(String nif) throws ApiException;
}