package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.PrestecApi;
import com.bibliosedaos.desktop.model.Prestec;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacio HTTP real de PrestecApi.
 * Gestiona les crides al servidor per a operacions de prestecs.
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HttpPrestecApi implements PrestecApi {

    /**
     * Obte tots els prestecs del sistema.
     *
     * @param usuariId ID de l'usuari per filtrar (opcional, pot ser null)
     * @return llista de tots els prestecs
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Prestec> getAllPrestecs(Long usuariId) throws ApiException {
        try {
            String uri = ApiClient.getBaseUrl() + "/biblioteca/prestecs/llistarPrestecs";
            if (usuariId != null) {
                uri += "?usuariId=" + URLEncoder.encode(String.valueOf(usuariId), StandardCharsets.UTF_8);
            }
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 200) {
                Prestec[] arr = ApiClient.MAPPER.readValue(resp.body(), Prestec[].class);
                return Arrays.asList(arr);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error obtenint prestecs");
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
     * Obte els prestecs actius (sense data de devolucio).
     *
     * @param usuariId ID de l'usuari per filtrar (opcional, pot ser null)
     * @return llista de prestecs actius
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Prestec> getPrestecsActius(Long usuariId) throws ApiException {
        try {
            String uri = ApiClient.getBaseUrl() + "/biblioteca/prestecs/llistarPrestecsActius";
            if (usuariId != null) {
                uri += "?usuariId=" + URLEncoder.encode(String.valueOf(usuariId), StandardCharsets.UTF_8);
            }
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 200) {
                Prestec[] arr = ApiClient.MAPPER.readValue(resp.body(), Prestec[].class);
                return Arrays.asList(arr);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error obtenint prestecs actius");
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
     * Crea un nou prestec al sistema.
     *
     * @param prestec dades del nou prestec (amb exemplar.id i usuari.id)
     * @return prestec creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    @Override
    public Prestec createPrestec(Prestec prestec) throws ApiException {
        try {
            // Enviem un JSON lleuger amb els ids
            Map<String,Object> payload = new HashMap<>();
            if (prestec.getDataPrestec() != null) {
                payload.put("dataPrestec", prestec.getDataPrestec().toString());
            }
            // enviar usuari mínim
            Map<String,Object> usuariMap = new HashMap<>();
            if (prestec.getUsuari() != null && prestec.getUsuari().getId() != null) {
                usuariMap.put("id", prestec.getUsuari().getId());
            } else {
                throw new ApiException("Usuari o usuari.id no poden ser null per crear Prestec");
            }
            payload.put("usuari", usuariMap);

            // enviar exemplar mínim
            Map<String,Object> exemplarMap = new HashMap<>();
            if (prestec.getExemplar() != null && prestec.getExemplar().getId() != null) {
                exemplarMap.put("id", prestec.getExemplar().getId());
            } else {
                throw new ApiException("Exemplar o exemplar.id no poden ser null per crear Prestec");
            }
            payload.put("exemplar", exemplarMap);

            String json = ApiClient.MAPPER.writeValueAsString(payload);

            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/prestecs/afegirPrestec"))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String body = resp.body();

            if (code == 200 || code == 201) {
                return ApiClient.MAPPER.readValue(body, Prestec.class);
            } else {
                String msg = ApiClient.extractErrorMessage(body, "Error creant prestec");
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
     * Marca un prestec com retornat.
     *
     * @param prestecId ID del prestec a retornar
     * @throws ApiException si el prestec no existeix o hi ha errors de permisos
     */
    @Override
    public void retornarPrestec(Long prestecId) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/prestecs/ferDevolucio/" + prestecId))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.noBody())
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code != 200) {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error marcant devolucio");
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
     * Obte un prestec pel seu ID.
     *
     * @param id ID del prestec a obtenir
     * @return prestec amb totes les seves dades
     * @throws ApiException si hi ha errors de comunicacio o el prestec no es troba
     */
    @Override
    public Prestec getPrestecById(Long id) throws ApiException {
        try {
            // Reutilitzem un endpoint existent i filtrem al costat-client
            List<Prestec> all = getAllPrestecs(null);
            return all.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        } catch (ApiException a) {
            throw a;
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }
}