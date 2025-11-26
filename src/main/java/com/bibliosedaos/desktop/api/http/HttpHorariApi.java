package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.HorariApi;
import com.bibliosedaos.desktop.model.Horari;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Implementacio HTTP real de HorariApi.
 * Gestiona les crides al servidor per a operacions d'horaris.
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class HttpHorariApi implements HorariApi {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    /**
     * Obte tots els horaris del sistema.
     *
     * @return llista de tots els horaris
     * @throws ApiException si hi ha errors de comunicacio o permisos
     */
    @Override
    public List<Horari> getAllHoraris() throws ApiException {
        try {
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/horaris/llistarHorarisSales"))
                            .timeout(TIMEOUT)
                            .header("Content-Type", "application/json")
                            .GET()
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Horari[] arr = ApiClient.MAPPER.readValue(resp.body(), Horari[].class);
                return Arrays.asList(arr);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error obtenint horaris");
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
     * Crea un nou horari al sistema.
     *
     * @param horari dades del nou horari
     * @return horari creat amb el ID assignat
     * @throws ApiException si hi ha errors de validacio o conflictes
     */
    @Override
    public Horari createHorari(Horari horari) throws ApiException {
        try {
            String json = ApiClient.MAPPER.writeValueAsString(horari);
            HttpRequest request = ApiClient.withAuth(
                    HttpRequest.newBuilder()
                            .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/horaris/afegirHorari"))
                            .timeout(TIMEOUT)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
            ).build();

            HttpResponse<String> resp = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 200 || code == 201) {
                return ApiClient.MAPPER.readValue(resp.body(), Horari.class);
            } else {
                String msg = ApiClient.extractErrorMessage(resp.body(), "Error creant horari");
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