package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.UserApi;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementacio HTTP real de UserApi.
 * Gestiona les crides al servidor per a operacions d'usuari.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HttpUserApi implements UserApi {

    /**
     * Obte les dades completes d'un usuari pel seu ID.
     *
     * @param userId ID de l'usuari
     * @return usuari amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o l'usuari no es troba
     */
    @Override
    public User getUserById(Long userId) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/usuaris/trobarUsuariPerId/" + userId))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            if (code == 200) {
                return ApiClient.MAPPER.readValue(body, User.class);
            } else {
                throw new ApiException("Error obtenint dades: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Actualitza les dades d'un usuari.
     *
     * @param userId ID de l'usuari a actualitzar
     * @param user dades actualitzades de l'usuari
     * @return usuari actualitzat
     * @throws ApiException si hi ha errors de comunicacio o validacio
     */
    @Override
    public User updateUser(Long userId, User user) throws ApiException {
        try {
            String reqJson = ApiClient.MAPPER.writeValueAsString(user);

            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/usuaris/actualitzarUsuari/" + userId))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(reqJson))
            ).build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            if (code == 200) {
                return ApiClient.MAPPER.readValue(body, User.class);
            } else {
                throw new ApiException("Error actualitzant: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Obte tots els usuaris del sistema.
     *
     * @return llista de tots els usuaris
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<User> getAllUsers() throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/usuaris/llistarUsuaris"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            if (code == 200) {
                User[] usersArray = ApiClient.MAPPER.readValue(body, User[].class);
                return Arrays.asList(usersArray);
            } else {
                throw new ApiException("Error obtenint llista d'usuaris: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nou usuari al sistema.
     *
     * @param user dades del nou usuari
     * @return usuari creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    @Override
    public User createUser(User user) throws ApiException {
        try {
            // Creem un Map amb les dades que espera el servidor
            Map<String, Object> registerData = new HashMap<>();
            registerData.put("nick", user.getNick());
            registerData.put("nif", user.getNif());
            registerData.put("nom", user.getNom());
            registerData.put("cognom1", user.getCognom1());
            registerData.put("cognom2", user.getCognom2());
            registerData.put("localitat", user.getLocalitat());
            registerData.put("provincia", user.getProvincia());
            registerData.put("carrer", user.getCarrer());
            registerData.put("cp", user.getCp());
            registerData.put("tlf", user.getTlf());
            registerData.put("email", user.getEmail());
            registerData.put("password", user.getPassword());
            registerData.put("rol", user.getRol());

            String reqJson = ApiClient.MAPPER.writeValueAsString(registerData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/auth/afegirUsuari"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                    .build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            if (code == 200) {
                // El servidor retorna LoginResponse amb l'ID del nou usuari
                LoginResponse loginResp = ApiClient.MAPPER.readValue(body, LoginResponse.class);

                // SIMPLIFICADO: Ya es Long, asignación directa
                user.setId(loginResp.getUserId());
                return user;
            } else {
                throw new ApiException("Error creant usuari: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un usuari del sistema.
     *
     * @param userId ID de l'usuari a eliminar
     * @throws ApiException si l'usuari no existeix o hi ha errors de permisos
     */
    @Override
    public void deleteUser(Long userId) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/usuaris/eliminarUsuari/" + userId))
                            .timeout(Duration.ofSeconds(10))
                            .DELETE()
            ).build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();

            if (code != 200) {
                throw new ApiException("Error eliminant usuari: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Cerca un usuari pel seu nick.
     *
     * @param nick nick a buscar
     * @return usuari trobat
     * @throws ApiException si hi ha errors de comunicacio
     */
    @Override
    public User getUserByNick(String nick) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/usuaris/trobarUsuariPerNick/" + nick))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            if (code == 200) {
                return ApiClient.MAPPER.readValue(body, User.class);
            } else {
                throw new ApiException("Error obtenint usuari per nick: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Cerca un usuari pel seu NIF.
     *
     * @param nif NIF a buscar
     * @return usuari trobat
     * @throws ApiException si hi ha errors de comunicacio
     */
    @Override
    public User getUserByNif(String nif) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/usuaris/trobarUsuariPerNif/" + nif))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            if (code == 200) {
                return ApiClient.MAPPER.readValue(body, User.class);
            } else {
                throw new ApiException("Error obtenint usuari per NIF: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }
}