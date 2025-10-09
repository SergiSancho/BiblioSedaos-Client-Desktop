package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.*;
import com.bibliosedaos.desktop.model.dto.LoginRequest;
import com.bibliosedaos.desktop.model.dto.LoginResponse;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * HttpAuthApi: implementació real que fa crides HTTP a /auth/login.
 * Els mètodes són bloquejants i han d'executar-se en un fil de fons.
 */
public class HttpAuthApi implements AuthApi {

    @Override
    public LoginResponse login(LoginRequest req) throws ApiException {
        try {
            // Serialitzar DTO a JSON
            String reqJson = ApiClient.MAPPER.writeValueAsString(req);

            // Construir petició POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                    .build();

            // Enviar i rebre resposta (bloquejant)
            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            if (code == 200) {
                return ApiClient.MAPPER.readValue(body, LoginResponse.class);
            } else {
                return handleErrorResponse(code, body);
            }


        } catch (InterruptedException e) {
            // Re-interrompre el fil si s'interromp
            Thread.currentThread().interrupt();
            throw new ApiException("El fil ha estat interromput durant la crida HTTP", e);
        } catch (ApiException a) {
            throw a;
        } catch (Exception e) {
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Gestiona errors 400/401 i llança ApiException amb el missatge adequat.
     *
     * @param code codi HTTP rebut
     * @param body cos de la resposta
     * @return no retorna mai; sempre llença ApiException
     * @throws ApiException amb informació de l'error
     */
    private LoginResponse handleErrorResponse(int code, String body) throws ApiException {
        String defaultMsg;
        switch (code) {
            case 400 -> defaultMsg = "Dades incorrectes";
            case 401 -> defaultMsg = "Credencials invàlides";
            case 403 -> defaultMsg = "No autoritzat";
            default -> {
                if (code >= 500) defaultMsg = "Error intern del servidor";
                else defaultMsg = "Error del servidor: " + code;
            }
        }

        String msg = defaultMsg;
        try {
            ApiClient.ErrorResponse err = ApiClient.MAPPER.readValue(body, ApiClient.ErrorResponse.class);
            if (err != null && err.getMessage() != null && !err.getMessage().isBlank()) {
                msg = err.getMessage();
            }
        } catch (Exception ignored) {
            // Si no se puede parsear, usamos el defaultMsg
        }

        // Normalizar a una sola línea y longitud razonable
        msg = msg.replaceAll("\\s+", " ").trim();
        if (msg.length() > 200) msg = defaultMsg;

        // Lanzar ApiException con el prefijo "Codi X:" para que la UI muestre exactamente eso
        throw new ApiException("Codi " + code + ": " + msg, code);
    }


    /**
     * Tanca la sessió de l'usuari.
     * Encara no implementat al servidor; llença UnsupportedOperationException.
     *
     * @throws ApiException si hi ha errors (encara no implementat)
     */
    @Override
    public void logout() throws ApiException {
        throw new UnsupportedOperationException("logout no implementat en HttpAuthApi");
    }
}
