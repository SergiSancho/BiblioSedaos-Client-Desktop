package com.bibliosedaos.desktop.api.http;

import com.bibliosedaos.desktop.api.*;
import com.bibliosedaos.desktop.model.dto.LoginRequest;
import com.bibliosedaos.desktop.model.dto.LoginResponse;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * HttpAuthApi: implementació real que fa crides HTTP a /auth/login i /auth/logout.
 * Els mètodes són bloquejants.
 * Login han d'executar-se en un fil de fons.
 * Logout pot exdcutar-se en el fil principal
 */
public class HttpAuthApi implements AuthApi {

    private static final Logger LOGGER = Logger.getLogger(HttpAuthApi.class.getName());

    /**
     * Autentica un usuari amb el servidor.
     *
     * @param req DTO amb nick/password
     * @return amb el token i dades d'usuari si codi 200
     * @throws ApiException si hi ha error de comunicació o el servidor retorna un codi != 200
     */
    @Override
    public LoginResponse login(LoginRequest req) throws ApiException {
        try {
            // Serialitzar DTO a JSON
            String reqJson = ApiClient.MAPPER.writeValueAsString(req);

            // Construir petició POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.getBaseUrl() + "/biblioteca/auth/login"))
                    .timeout(Duration.ofSeconds(10))
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
     * Gestiona errors (p. ex. 400/401/403/5xx) i llança ApiException amb un missatge
     * llegible per la UI.
     *
     * @param code codi HTTP rebut
     * @param body cos de la resposta (pot ser text pla o JSON { "message": "..." })
     * @return no retorna mai; sempre llença
     * @throws ApiException amb informació de l'error (codi i missatge)
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

        // Utilitza l'utilitat central d'ApiClient per extraure un missatge llegible.
        String msg = ApiClient.extractErrorMessage(body, defaultMsg);
        msg = msg.replaceAll("\\s+", " ").trim();
        if (msg.length() > 200) msg = defaultMsg;

        // Prefixar amb "Codi X:" per a la UI
        throw new ApiException("Codi " + code + ": " + msg, code);
    }

    /**
     * Tanca la sessió de l'usuari informant al servidor.
     * Implementació que rep el token (pot ser null o buit). Si el token és null/empty
     * s'assumeix que la sessió ja s'ha esborrat localment i no es fa cap crida.
     *
     * @param token token JWT previ (pot ser null/empty)
     * @throws ApiException si hi ha errors en la crida al servidor
     */
    @Override
    public void logout(String token) throws ApiException {
        try {
            if (token == null || token.isBlank()) {
                LOGGER.fine("Logout: token vacío, no se llama al servidor");
                return;
            }

            String url = ApiClient.getBaseUrl() + "/biblioteca/auth/logout";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = ApiClient.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();
            String shortBody = (body == null) ? "" : body.trim();

            if (code == 200) {
                LOGGER.info(() -> "Logout exitós. status=" + code + ", body=\"" + shortBody + "\"");
                return;
            } else {
                LOGGER.warning(() -> "Logout falló. status=" + code + ", body=\"" + shortBody + "\"");
            }

            String fallback = (code == 400 || code == 401) ? "Token no trobat o invàlid." : "Error del servidor: " + code;
            String msg = ApiClient.extractErrorMessage(body, fallback);
            throw new ApiException("Codi " + code + ": " + msg, code);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Logout interrumpido: " + e.getMessage());
            throw new ApiException("Operació interrompuda", e);
        } catch (Exception e) {
            LOGGER.severe("Error de conexión en logout: " + e.getMessage());
            throw new ApiException("Error connectant amb el servidor: " + e.getMessage(), e);
        }
    }
}
