package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.UserApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.User;
import java.util.List;
import java.util.Objects;

/**
 * Servei per a les operacions d'usuari.
 * Capa intermèdia entre els controladors i l'API d'usuari.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class UserService {

    private final UserApi userApi;

    /**
     * Constructor principal amb injeccio de dependencies.
     *
     * @param userApi implementacio de UserApi (mock o real)
     * @throws NullPointerException si userApi es null
     */
    public UserService(UserApi userApi) {
        this.userApi = Objects.requireNonNull(userApi, "UserApi no pot ser null");
    }

    /**
     * Obte un usuari pel seu ID des del servidor.
     *
     * @param userId ID de l'usuari a obtenir
     * @return usuari amb totes les seves dades
     * @throws ApiException si falla la comunicacio o l'usuari no es troba
     */
    public User getUserById(Long userId) throws ApiException {
        return userApi.getUserById(userId);
    }

    /**
     * Actualitza un usuari al servidor.
     *
     * @param userId ID de l'usuari a actualitzar
     * @param user dades actualitzades de l'usuari
     * @return usuari actualitzat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public User updateUser(Long userId, User user) throws ApiException {
        return userApi.updateUser(userId, user);
    }

    /**
     * Obte tots els usuaris del sistema.
     *
     * @return llista de tots els usuaris
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    public List<User> getAllUsers() throws ApiException {
        return userApi.getAllUsers();
    }

    /**
     * Crea un nou usuari al sistema.
     *
     * @param user dades del nou usuari
     * @return usuari creat amb el ID assignat
     * @throws ApiException si falla la comunicacio o hi ha errors de validacio
     */
    public User createUser(User user) throws ApiException {
        return userApi.createUser(user);
    }

    /**
     * Elimina un usuari del sistema.
     *
     * @param userId ID de l'usuari a eliminar
     * @throws ApiException si falla la comunicacio o l'usuari no es troba
     */
    public void deleteUser(Long userId) throws ApiException {
        userApi.deleteUser(userId);
    }

    /**
     * Cerca un usuari pel seu nick.
     *
     * @param nick nick a buscar
     * @return usuari trobat
     * @throws ApiException si falla la comunicacio o l'usuari no es troba
     */
    public User getUserByNick(String nick) throws ApiException {
        return userApi.getUserByNick(nick);
    }

    /**
     * Cerca un usuari pel seu NIF.
     *
     * @param nif NIF a buscar
     * @return usuari trobat
     * @throws ApiException si falla la comunicacio o l'usuari no es troba
     */
    public User getUserByNif(String nif) throws ApiException {
        return userApi.getUserByNif(nif);
    }
}