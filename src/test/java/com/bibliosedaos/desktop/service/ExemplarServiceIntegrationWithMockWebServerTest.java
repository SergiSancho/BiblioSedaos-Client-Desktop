package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpExemplarApi;
import com.bibliosedaos.desktop.model.Exemplar;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integració controlada de la capa d'exemplars:
 * MockWebServer + HttpExemplarApi + ExemplarService.
 *
 * Aquesta prova simula respostes HTTP reals (200, 404, 500...) sense necessitat d'un
 * servidor extern.
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class ExemplarServiceIntegrationWithMockWebServerTest {

    private MockWebServer mockWebServer;
    private ExemplarService exemplarService;

    /**
     * Inicia el MockWebServer abans de cada test i configura la propietat
     * del sistema "api.base.url" perquè HttpExemplarApi utilitzi la URL del mock.
     */
    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        System.setProperty("api.base.url", mockWebServer.url("/").toString());

        HttpExemplarApi httpExemplarApi = new HttpExemplarApi();
        exemplarService = new ExemplarService(httpExemplarApi);
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
     * Prova que GET /llistarExemplars retorna 200 i es mapeja a llista d'Exemplar.
     */
    @Test
    void getAllExemplars_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              { "id": 1, "lloc": "A1" },
              { "id": 2, "lloc": "B2" }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Exemplar> list = exemplarService.getAllExemplars();
        assertNotNull(list, "La llista no ha de ser null");
        assertEquals(2, list.size(), "Ha de retornar 2 exemplars");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/exemplars/llistarExemplars", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que GET /llistarExemplarsLliures retorna 200 i es mapeja a llista d'Exemplar.
     */
    @Test
    void getExemplarsLliures_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              { "id": 3, "lloc": "C3" }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Exemplar> list = exemplarService.getExemplarsLliures();
        assertNotNull(list);
        assertEquals(1, list.size());

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/exemplars/llistarExemplarsLliures", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que la cerca per títol construeix la query i retorna resultats.
     */
    @Test
    void findExemplarsByTitol_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              { "id": 4, "lloc": "D4" }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Exemplar> list = exemplarService.findExemplarsByTitol("El títol");
        assertNotNull(list);
        assertEquals(1, list.size());

        RecordedRequest req = mockWebServer.takeRequest();
        String rawPath = req.getPath(); // conté query
        assertTrue(rawPath.contains("/biblioteca/exemplars/llistarExemplarsLliures"), "Ruta base esperada");
        assertTrue(rawPath.contains("titol="), "Ha d'incloure el paràmetre titol");
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que la cerca per autor construeix la query i retorna resultats.
     */
    @Test
    void findExemplarsByAutor_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              { "id": 5, "lloc": "E5" }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Exemplar> list = exemplarService.findExemplarsByAutor("Autor Nom");
        assertNotNull(list);
        assertEquals(1, list.size());

        RecordedRequest req = mockWebServer.takeRequest();
        String rawPath = req.getPath();
        assertTrue(rawPath.contains("/biblioteca/exemplars/llistarExemplarsLliures"), "Ruta base esperada");
        assertTrue(rawPath.contains("autor="), "Ha d'incloure el paràmetre autor");
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que PUT /afegirExemplar processa la resposta i retorna l'Exemplar creat.
     */
    @Test
    void createExemplar_PutCorrecte_RetornaExemplarCreat() throws Exception {
        String respJson = """
            {
              "id": 77,
              "lloc": "Z7"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        Exemplar toCreate = new Exemplar();
        toCreate.setLloc("Z7");

        Exemplar created = exemplarService.createExemplar(toCreate);
        assertNotNull(created);
        assertEquals(77L, created.getId());

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/exemplars/afegirExemplar", path);
        assertEquals("PUT", req.getMethod());

        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.toLowerCase().contains("application/json"));

        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"lloc\""));
    }

    /**
     * Prova que PUT /actualitzarExemplar/{id} retorna l'Exemplar actualitzat.
     */
    @Test
    void updateExemplar_Resposta200_RetornaExemplarActualitzat() throws Exception {
        String respJson = """
            {
              "id": 88,
              "lloc": "Updated"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        Exemplar toUpdate = new Exemplar();
        toUpdate.setLloc("Updated");

        Exemplar updated = exemplarService.updateExemplar(88L, toUpdate);
        assertNotNull(updated);
        assertEquals(88L, updated.getId());
        assertEquals("Updated", updated.getLloc());

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/exemplars/actualitzarExemplar/88", path);
        assertEquals("PUT", req.getMethod());
    }

    /**
     * Prova que DELETE /eliminarExemplar/{id} amb 404 llanci ApiException.
     */
    @Test
    void deleteExemplar_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> exemplarService.deleteExemplar(999L));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(404, code, "Si ApiException exposa codi, ha de ser 404");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementació no exposa el codi
        }
    }

    /**
     * Prova que GET /trobarExemplarPerId/{id} retorna 200 i es parseja a Exemplar.
     */
    @Test
    void findExemplarById_Resposta200_RetornaExemplar() throws Exception {
        String json = """
            {
              "id": 200,
              "lloc": "F200"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        Exemplar e = exemplarService.getExemplarById(200L);
        assertNotNull(e);
        assertEquals(200L, e.getId());
        assertEquals("F200", e.getLloc());

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/exemplars/trobarExemplarPerId/200", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que GET /trobarExemplarPerId/{id} amb 404 llanci ApiException.
     */
    @Test
    void findExemplarById_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> exemplarService.getExemplarById(9999L));

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
