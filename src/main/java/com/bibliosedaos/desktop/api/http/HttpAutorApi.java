package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.AutorApi;
import com.bibliosedaos.desktop.model.Autor;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Implementacio HTTP real de AutorApi.
 * Gestiona les crides al servidor per a operacions d'autors.
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HttpAutorApi implements AutorApi {

    /**
     * Obte tots els autors del sistema.
     *
     * @return llista de tots els autors
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Autor> getAllAutors() throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/autors/llistarAutors"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Autor[] arr = ApiClient.MAPPER.readValue(resp.body(), Autor[].class);
                return Arrays.asList(arr);
            } else {
                throw new ApiException("Error obtenint autors: Codi " + resp.statusCode(), resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nou autor al sistema.
     *
     * @param autor dades del nou autor
     * @return autor creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    @Override
    public Autor createAutor(Autor autor) throws ApiException {
        try {
            String json = ApiClient.MAPPER.writeValueAsString(autor);
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/autors/afegirAutor"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String body = resp.body();
            if (code == 200 || code == 201) {
                return ApiClient.MAPPER.readValue(body, Autor.class);
            } else {
                throw new ApiException("Error creant autor: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un autor del sistema.
     *
     * @param id ID de l'autor a eliminar
     * @throws ApiException si l'autor no existeix o hi ha errors de permisos
     */
    @Override
    public void deleteAutor(Long id) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/autors/eliminarAutor/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .DELETE()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error eliminant autor");
                throw new ApiException("Codi " + resp.statusCode() + ": " + msg, resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Obte un autor pel seu ID.
     *
     * @param id ID de l'autor a obtenir
     * @return autor amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o l'autor no es troba
     */
    @Override
    public Autor findAutorById(Long id) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/autors/trobarAutorPerId/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return ApiClient.MAPPER.readValue(resp.body(), Autor.class);
            } else {
                throw new ApiException("Autor no trobat: Codi " + resp.statusCode(), resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }
}