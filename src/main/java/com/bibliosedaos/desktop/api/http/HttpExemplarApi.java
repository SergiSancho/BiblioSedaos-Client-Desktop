package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.ExemplarApi;
import com.bibliosedaos.desktop.model.Exemplar;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Implementacio HTTP real de ExemplarApi.
 * Gestiona les crides al servidor per a operacions d'exemplars.
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HttpExemplarApi implements ExemplarApi {

    /**
     * Obte tots els exemplars del sistema.
     *
     * @return llista de tots els exemplars
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Exemplar> getAllExemplars() throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/exemplars/llistarExemplars"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Exemplar[] arr = ApiClient.MAPPER.readValue(resp.body(), Exemplar[].class);
                return Arrays.asList(arr);
            } else {
                throw new ApiException("Error obtenint exemplars: Codi " + resp.statusCode(), resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Obte tots els exemplars lliures del sistema.
     *
     * @return llista d'exemplars disponibles
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Exemplar> getExemplarsLliures() throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/exemplars/llistarExemplarsLliures"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Exemplar[] arr = ApiClient.MAPPER.readValue(resp.body(), Exemplar[].class);
                return Arrays.asList(arr);
            } else {
                throw new ApiException("Error obtenint exemplars lliures: Codi " + resp.statusCode(), resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Cerca exemplars lliures pel titol del llibre.
     *
     * @param titol titol del llibre a buscar
     * @return llista d'exemplars lliures que coincideixen amb el titol
     * @throws ApiException si hi ha errors de comunicacio
     */
    @Override
    public List<Exemplar> findExemplarsLliuresByTitol(String titol) throws ApiException {
        try {
            String q = URLEncoder.encode(titol == null ? "" : titol, StandardCharsets.UTF_8);
            String uri = ApiClient.getBaseUrl() + "/biblioteca/exemplars/llistarExemplarsLliures?titol=" + q;
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Exemplar[] arr = ApiClient.MAPPER.readValue(resp.body(), Exemplar[].class);
                return Arrays.asList(arr);
            } else {
                throw new ApiException("Error buscant exemplars per titol: Codi " + resp.statusCode(), resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Cerca exemplars lliures pel nom de l'autor.
     *
     * @param autorNom nom de l'autor a buscar
     * @return llista d'exemplars lliures que coincideixen amb l'autor
     * @throws ApiException si hi ha errors de comunicacio
     */
    @Override
    public List<Exemplar> findExemplarsLliuresByAutorNom(String autorNom) throws ApiException {
        try {
            String q = URLEncoder.encode(autorNom == null ? "" : autorNom, StandardCharsets.UTF_8);
            String uri = ApiClient.getBaseUrl() + "/biblioteca/exemplars/llistarExemplarsLliures?autor=" + q;
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Exemplar[] arr = ApiClient.MAPPER.readValue(resp.body(), Exemplar[].class);
                return Arrays.asList(arr);
            } else {
                throw new ApiException("Error buscant exemplars per autor: Codi " + resp.statusCode(), resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nou exemplar al sistema.
     *
     * @param exemplar dades del nou exemplar
     * @return exemplar creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    @Override
    public Exemplar createExemplar(Exemplar exemplar) throws ApiException {
        try {
            String json = ApiClient.MAPPER.writeValueAsString(exemplar);
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/exemplars/afegirExemplar"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 200 || code == 201) {
                return ApiClient.MAPPER.readValue(resp.body(), Exemplar.class);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error creant exemplar");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Actualitza les dades d'un exemplar.
     *
     * @param id ID de l'exemplar a actualitzar
     * @param exemplar dades actualitzades de l'exemplar
     * @return exemplar actualitzat
     * @throws ApiException si hi ha errors de comunicacio o validacio
     */
    @Override
    public Exemplar updateExemplar(Long id, Exemplar exemplar) throws ApiException {
        try {
            String json = ApiClient.MAPPER.writeValueAsString(exemplar);
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/exemplars/actualitzarExemplar/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 200) {
                return ApiClient.MAPPER.readValue(resp.body(), Exemplar.class);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error actualitzant exemplar");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un exemplar del sistema.
     *
     * @param id ID de l'exemplar a eliminar
     * @throws ApiException si l'exemplar no existeix o hi ha errors de permisos
     */
    @Override
    public void deleteExemplar(Long id) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/exemplars/eliminarExemplar/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .DELETE()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code != 200) {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error eliminant exemplar");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Obte un exemplar pel seu ID.
     *
     * @param id ID de l'exemplar a obtenir
     * @return exemplar amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o l'exemplar no es troba
     */
    @Override
    public Exemplar findExemplarById(Long id) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/exemplars/trobarExemplarPerId/" + id))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return ApiClient.MAPPER.readValue(resp.body(), Exemplar.class);
            } else {
                throw new ApiException("Exemplar no trobat: Codi " + resp.statusCode(), resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }
}