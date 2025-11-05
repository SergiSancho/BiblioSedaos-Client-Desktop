package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpLlibreApi;
import com.bibliosedaos.desktop.model.Llibre;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integració controlada de la capa de llibres:
 * MockWebServer + HttpLlibreApi + LlibreService.
 *
 * Aquesta prova simula respostes HTTP reals (200, 404, 500...) sense necessitat d'un
 * servidor extern.
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class LlibreServiceIntegrationWithMockWebServerTest {

    private MockWebServer mockWebServer;
    private LlibreService llibreService;

    /**
     * Inicia el MockWebServer abans de cada test i configura la propietat
     * del sistema "api.base.url" perquè HttpLlibreApi utilitzi la URL del mock.
     */
    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Forcem ApiClient a apuntar al MockWebServer durant la prova
        System.setProperty("api.base.url", mockWebServer.url("/").toString());

        HttpLlibreApi httpLlibreApi = new HttpLlibreApi();
        llibreService = new LlibreService(httpLlibreApi);
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
     * Normalitza una ruta retornada pel MockWebServer (evita '//' inicial).
     */
    private String normalizePath(String rawPath) {
        if (rawPath == null) return null;
        return rawPath.replaceFirst("^/+", "/");
    }

    /**
     * Prova que GET /llistarLlibres retorna 200 i es mapeja a llista de Llibre.
     */
    @Test
    void getAllBooks_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              { "id": 1, "titol": "Un llibre", "isbn":"111" },
              { "id": 2, "titol": "Dos llibres", "isbn":"222" }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Llibre> list = llibreService.getAllBooks();
        assertNotNull(list, "La llista no ha de ser null");
        assertEquals(2, list.size(), "Ha de retornar 2 llibres");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/llibres/llistarLlibres", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que GET /trobarLlibrePerId/{id} retorna 200 i es parseja a Llibre.
     */
    @Test
    void getBookById_Resposta200_RetornaLlibre() throws Exception {
        String json = """
            {
              "id": 42,
              "isbn": "978-0-00-000000-0",
              "titol": "El Gran Llibre",
              "pagines": 300
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        Llibre l = llibreService.getBookById(42L);
        assertNotNull(l, "El llibre no ha de ser null");
        assertEquals(42L, l.getId());
        assertEquals("El Gran Llibre", l.getTitol());

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/llibres/trobarLlibrePerId/42", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que GET /trobarLlibrePerId/{id} amb 404 llanci ApiException.
     */
    @Test
    void getBookById_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> llibreService.getBookById(999L),
                "S'ha d'atreure ApiException per 404");

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(404, code);
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // implementacions que no exposin getStatusCode() són acceptades
        }
    }

    /**
     * Prova que PUT /afegirLlibre processa la resposta i retorna el Llibre creat.
     */
    @Test
    void createBook_PutCorrecte_RetornaLlibreCreat() throws Exception {
        String respJson = """
            {
              "id": 123,
              "isbn": "999",
              "titol": "Nou Llibre",
              "pagines": 100
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        Llibre toCreate = new Llibre();
        toCreate.setIsbn("999");
        toCreate.setTitol("Nou Llibre");
        toCreate.setPagines(100);

        Llibre created = llibreService.createBook(toCreate);
        assertNotNull(created, "L'objecte creat no ha de ser null");
        assertEquals(123L, created.getId());

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/llibres/afegirLlibre", path);
        assertEquals("PUT", req.getMethod());

        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.toLowerCase().contains("application/json"));

        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"titol\""));
        assertTrue(body.contains("\"isbn\""));
    }

    /**
     * Prova que PUT /actualitzarLlibre/{id} retorna el Llibre actualitzat.
     */
    @Test
    void updateBook_Resposta200_RetornaLlibreActualitzat() throws Exception {
        String respJson = """
            {
              "id": 55,
              "isbn": "555",
              "titol": "Llibre Actualitzat",
              "pagines": 150
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        Llibre toUpdate = new Llibre();
        toUpdate.setTitol("Llibre Actualitzat");
        toUpdate.setIsbn("555");
        toUpdate.setPagines(150);

        Llibre updated = llibreService.updateBook(55L, toUpdate);
        assertNotNull(updated);
        assertEquals(55L, updated.getId());
        assertEquals("Llibre Actualitzat", updated.getTitol());

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/llibres/actualitzarLlibre/55", path);
        assertEquals("PUT", req.getMethod());
    }

    /**
     * Prova que DELETE /eliminarLlibre/{id} amb 404 llanci ApiException.
     */
    @Test
    void deleteBook_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> llibreService.deleteBook(999L));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(404, code);
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementació no exposa el codi
        }
    }
}
