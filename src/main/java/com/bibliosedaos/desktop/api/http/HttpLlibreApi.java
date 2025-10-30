package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.LlibreApi;
import com.bibliosedaos.desktop.model.Llibre;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Implementacio HTTP real de LlibreApi.
 * Gestiona les crides al servidor per a operacions de llibres.
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HttpLlibreApi implements LlibreApi {

    /**
     * Obte tots els llibres del sistema.
     *
     * @return llista de tots els llibres
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Llibre> getAllLlibres() throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/llibres/llistarLlibres"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String body = resp.body();
            if (code == 200) {
                Llibre[] arr = ApiClient.MAPPER.readValue(body, Llibre[].class);
                return Arrays.asList(arr);
            } else {
                throw new ApiException("Error obtenint llista de llibres: Codi " + code, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (ApiException a) {
            throw a;
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nou llibre al sistema.
     *
     * @param llibre dades del nou llibre
     * @return llibre creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    @Override
    public Llibre createLlibre(Llibre llibre) throws ApiException {
        try {
            String json = ApiClient.MAPPER.writeValueAsString(llibre);
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/llibres/afegirLlibre"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String body = resp.body();
            if (code == 200 || code == 201) {
                return ApiClient.MAPPER.readValue(body, Llibre.class);
            } else {
                String msg = ApiClient.extractErrorMessage(body, "Error creant llibre");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (ApiException a) {
            throw a;
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Actualitza les dades d'un llibre.
     *
     * @param id ID del llibre a actualitzar
     * @param llibre dades actualitzades del llibre
     * @return llibre actualitzat
     * @throws ApiException si hi ha errors de comunicacio o validacio
     */
    @Override
    public Llibre updateLlibre(Long id, Llibre llibre) throws ApiException {
        try {
            String json = ApiClient.MAPPER.writeValueAsString(llibre);
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/llibres/actualitzarLlibre/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String body = resp.body();
            if (code == 200) {
                return ApiClient.MAPPER.readValue(body, Llibre.class);
            } else {
                String msg = ApiClient.extractErrorMessage(body, "Error actualitzant llibre");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (ApiException a) {
            throw a;
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un llibre del sistema.
     *
     * @param id ID del llibre a eliminar
     * @throws ApiException si el llibre no existeix o hi ha errors de permisos
     */
    @Override
    public void deleteLlibre(Long id) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/llibres/eliminarLlibre/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .DELETE()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code != 200) {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error eliminant llibre");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (ApiException a) {
            throw a;
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Obte un llibre pel seu ID.
     *
     * @param id ID del llibre a obtenir
     * @return llibre amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o el llibre no es troba
     */
    @Override
    public Llibre findLlibreById(Long id) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/llibres/trobarLlibrePerId/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String body = resp.body();
            if (code == 200) {
                return ApiClient.MAPPER.readValue(body, Llibre.class);
            } else {
                String msg = ApiClient.extractErrorMessage(body, "Llibre no trobat");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (ApiException a) {
            throw a;
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }
}