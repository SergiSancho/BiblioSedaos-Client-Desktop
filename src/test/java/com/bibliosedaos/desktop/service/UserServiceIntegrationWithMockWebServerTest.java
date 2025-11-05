package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpUserApi;
import com.bibliosedaos.desktop.model.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integració controlada de la capa d'usuaris:
 * MockWebServer + HttpUserApi + UserService.
 *
 * Aquesta prova simula respostes HTTP reals (200, 404, 500...) sense necessitat d'un
 * servidor extern.
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class UserServiceIntegrationWithMockWebServerTest {

    private MockWebServer mockWebServer;
    private UserService userService;

    /**
     * Inicia el MockWebServer abans de cada test i configura la propietat
     * del sistema "api.base.url" perquè HttpUserApi utilitzi la URL del mock.
     */
    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Forcem ApiClient a apuntar al MockWebServer durant la prova
        System.setProperty("api.base.url", mockWebServer.url("/").toString());

        HttpUserApi httpUserApi = new HttpUserApi();
        userService = new UserService(httpUserApi);
    }

    /**
     * Atura el MockWebServer i neteja propietats després de cada test.
     */
    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty("api.base.url");
        if (mockWebServer != null) mockWebServer.shutdown();
    }

    /**
     * Helper per normalitzar una ruta retornada pel MockWebServer.
     * Elimina duplicats de slash inicials ("//...") i deixa una sola barra inicial.
     */
    private String normalizePath(String rawPath) {
        if (rawPath == null) return null;
        return rawPath.replaceFirst("^/+", "/");
    }

    /**
     * Prova que una resposta HTTP 200 amb JSON correcte es parseja bé i que
     * UserService retorna un User amb els camps esperats.
     */
    @Test
    void getUserById_Resposta200_RetornaUsuari() throws Exception {
        String userJson = """
            {
              "id": 42,
              "nick": "usr42",
              "nom": "Nom",
              "cognom1": "Cognom",
              "email": "usr@example.com"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(userJson)
                .addHeader("Content-Type", "application/json"));

        User u = userService.getUserById(42L);

        assertNotNull(u, "L'usuari retornat no ha de ser null");
        assertEquals(42L, u.getId(), "L'id ha de coincidir");
        assertEquals("usr42", u.getNick(), "El nick ha de coincidir");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/usuaris/trobarUsuariPerId/42", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que una resposta HTTP 404 provoca que UserService llanci ApiException.
     */
    @Test
    void getUserById_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> userService.getUserById(999L),
                "S'ha d'atreure ApiException per 404");

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(404, code, "Si ApiException exposa codi, ha de ser 404");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // si l'excepció no exposa getStatusCode(), no fem l'assert
        }
    }

    /**
     * Prova que una crida POST per crear usuari processa la resposta i assigna l'ID retornat.
     */
    @Test
    void createUser_PostCorrecte_AssignaIdDesdeResposta() throws Exception {
        String respJson = """
            {
              "id": 123,
              "token": "irrelevant",
              "rol": 1,
              "nom": "Nou"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        User newUser = new User();
        newUser.setNick("new");
        newUser.setNif("12345678A");
        newUser.setNom("Nom");
        newUser.setCognom1("Cognom");
        newUser.setEmail("new@example.com");
        newUser.setPassword("pwd");
        newUser.setRol(1);

        User created = userService.createUser(newUser);

        assertNotNull(created, "L'usuari creat no ha de ser null");
        assertEquals(123L, created.getId(), "L'id assignat ha de provenir de la resposta");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/auth/afegirUsuari", path);
        assertEquals("POST", req.getMethod());

        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType, "Header Content-Type present");
        assertTrue(contentType.toLowerCase().contains("application/json"), "Content-Type ha de contenir application/json");

        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"nick\""), "El cos ha d'incloure 'nick'");
        assertTrue(body.contains("\"nif\""), "El cos ha d'incloure 'nif'");
    }

    /**
     * Prova que GET /llistarUsuaris retorna un array JSON i que UserService el mapeja a llista.
     */
    @Test
    void getAllUsers_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              { "id": 1, "nick": "u1" },
              { "id": 2, "nick": "u2" }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<User> list = userService.getAllUsers();
        assertNotNull(list, "La llista no ha de ser null");
        assertEquals(2, list.size(), "La llista ha de contenir 2 elements");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/usuaris/llistarUsuaris", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que una resposta HTTP 404 en eliminar usuari provoca ApiException.
     */
    @Test
    void deleteUser_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> userService.deleteUser(999L));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(404, code, "Si ApiException exposa codi, ha de ser 404");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementació no exposa el codi
        }
    }
}
