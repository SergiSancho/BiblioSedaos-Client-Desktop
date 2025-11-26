package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.GrupApi;
import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.User;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Implementacio HTTP real de GrupApi.
 * Gestiona les crides al servidor per a operacions de grups.
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HttpGrupApi implements GrupApi {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    /**
     * Obte tots els grups del sistema.
     *
     * @return llista de tots els grups
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Grup> getAllGrups() throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/grups/llistarGrups"))
                            .timeout(TIMEOUT)
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Grup[] arr = ApiClient.MAPPER.readValue(resp.body(), Grup[].class);
                return Arrays.asList(arr);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error obtenint llista de grups");
                throw new ApiException("Codi " + resp.statusCode() + ": " + msg, resp.statusCode());
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
     * Crea un nou grup al sistema.
     *
     * @param grup dades del nou grup
     * @return grup creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    @Override
    public Grup createGrup(Grup grup) throws ApiException {
        try {
            String json = ApiClient.MAPPER.writeValueAsString(grup);
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/grups/afegirGrup"))
                            .timeout(TIMEOUT)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 201 || code == 200) {
                return ApiClient.MAPPER.readValue(resp.body(), Grup.class);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error creant grup");
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
     * Elimina un grup del sistema.
     *
     * @param grupId ID del grup a eliminar
     * @throws ApiException si el grup no existeix o hi ha errors de permisos
     */
    @Override
    public void deleteGrup(Long grupId) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/grups/eliminarGrup/" + grupId))
                            .timeout(TIMEOUT)
                            .DELETE()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code != 200) {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error eliminant grup");
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
     * Afegeix un usuari a un grup.
     *
     * @param grupId ID del grup al que s'afegira l'usuari
     * @param membreId ID de l'usuari a afegir
     * @return grup actualitzat amb el nou membre
     * @throws ApiException si hi ha errors de comunicacio o validacio
     */
    @Override
    public Grup afegirUsuariGrup(Long grupId, Long membreId) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/grups/" + grupId + "/afegirUsuariGrup/" + membreId))
                            .timeout(TIMEOUT)
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.noBody())
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 200) {
                return ApiClient.MAPPER.readValue(resp.body(), Grup.class);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error afegint membre al grup");
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
     * Obte tots els membres d'un grup.
     *
     * @param grupId ID del grup del que obtenir els membres
     * @return llista de membres del grup
     * @throws ApiException si hi ha errors de comunicacio o el grup no es troba
     */
    @Override
    public List<User> getMembresGrup(Long grupId) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/grups/llistarUsuarisGrup/" + grupId))
                            .timeout(TIMEOUT)
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 200) {
                User[] arr = ApiClient.MAPPER.readValue(resp.body(), User[].class);
                return Arrays.asList(arr);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error obtenint membres del grup");
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
     * Elimina un usuari d'un grup.
     *
     * @param grupId ID del grup del que sortir
     * @param membreId ID de l'usuari a eliminar del grup
     * @throws ApiException si l'usuari no es troba al grup o hi ha errors de permisos
     */
    @Override
    public void sortirUsuari(Long grupId, Long membreId) throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/grups/" + grupId + "/sortirUsuari/" + membreId))
                            .timeout(TIMEOUT)
                            .DELETE()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code != 200) {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error eliminant usuari del grup");
                throw new ApiException("Codi " + code + ": " + msg, code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Operacio interrompuda", e);
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }
}