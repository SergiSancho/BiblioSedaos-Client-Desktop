package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpGrupApi;
import com.bibliosedaos.desktop.model.Grup;
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
 * Prova d'integració controlada de la capa de grups:
 * MockWebServer + HttpGrupApi + GrupService.
 *
 * Aquesta prova simula respostes HTTP reals (200, 404, 500...) sense necessitat d'un
 * servidor extern.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class GrupServiceIntegrationWithMockWebServerTest {

    private MockWebServer mockWebServer;
    private GrupService grupService;

    /**
     * Inicia el MockWebServer abans de cada test i configura la propietat
     * del sistema "api.base.url" perquè HttpGrupApi utilitzi la URL del mock.
     */
    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Forcem ApiClient a apuntar al MockWebServer durant la prova
        System.setProperty("api.base.url", mockWebServer.url("/").toString());

        HttpGrupApi httpGrupApi = new HttpGrupApi();
        grupService = new GrupService(httpGrupApi);
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
     * GrupService retorna una llista de grups per getAllGrups.
     */
    @Test
    void getAllGrups_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              {
                "id": 1,
                "nom": "Grup de Lectura",
                "tematica": "Literatura Clàssica",
                "horari": {
                  "id": 1,
                  "sala": "Sala A",
                  "dia": "Dilluns",
                  "hora": "18:00"
                },
                "administrador": {
                  "id": 1,
                  "nom": "Anna",
                  "cognom1": "Garcia"
                }
              },
              {
                "id": 2,
                "nom": "Club de Poesia",
                "tematica": "Poesia Contemporània",
                "horari": {
                  "id": 2,
                  "sala": "Sala B",
                  "dia": "Dimarts",
                  "hora": "20:00"
                },
                "administrador": {
                  "id": 2,
                  "nom": "Pere",
                  "cognom1": "Martinez"
                }
              }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Grup> list = grupService.getAllGrups();
        assertNotNull(list, "La llista no ha de ser null");
        assertEquals(2, list.size(), "La llista ha de contenir 2 elements");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/grups/llistarGrups", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que una resposta HTTP 404 en obtenir grups provoca que GrupService llanci ApiException.
     */
    @Test
    void getAllGrups_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> grupService.getAllGrups(),
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
     * Prova que una crida POST per crear grup processa la resposta i assigna l'ID retornat.
     */
    @Test
    void createGrup_PostCorrecte_AssignaIdDesdeResposta() throws Exception {
        String respJson = """
            {
              "id": 123,
              "nom": "Nou Grup de Prova",
              "tematica": "Nova Temàtica",
              "horari": {
                "id": 1,
                "sala": "Sala C",
                "dia": "Dijous",
                "hora": "19:00"
              },
              "administrador": {
                "id": 3,
                "nom": "Maria",
                "cognom1": "Lopez"
              }
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        Grup newGrup = new Grup();
        newGrup.setNom("Nou Grup de Prova");
        newGrup.setTematica("Nova Temàtica");

        Grup created = grupService.createGrup(newGrup);

        assertNotNull(created, "El grup creat no ha de ser null");
        assertEquals(123L, created.getId(), "L'id assignat ha de provenir de la resposta");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/grups/afegirGrup", path);
        assertEquals("POST", req.getMethod());

        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType, "Header Content-Type present");
        assertTrue(contentType.toLowerCase().contains("application/json"),
                "Content-Type ha de contenir application/json");

        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"nom\""), "El cos ha d'incloure 'nom'");
        assertTrue(body.contains("\"tematica\""), "El cos ha d'incloure 'tematica'");
    }

    /**
     * Prova que una crida PUT per afegir usuari a grup s'envia correctament.
     */
    @Test
    void joinGrup_PutCorrecte_RetornaGrupActualitzat() throws Exception {
        String respJson = """
            {
              "id": 1,
              "nom": "Grup Actualitzat",
              "tematica": "Temàtica",
              "horari": {
                "id": 1,
                "sala": "Sala A",
                "dia": "Dilluns",
                "hora": "18:00"
              },
              "administrador": {
                "id": 1,
                "nom": "Admin",
                "cognom1": "Principal"
              }
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        Grup updated = grupService.joinGrup(1L, 2L);

        assertNotNull(updated, "El grup actualitzat no ha de ser null");
        assertEquals(1L, updated.getId(), "L'id ha de coincidir");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/grups/1/afegirUsuariGrup/2", path);
        assertEquals("PUT", req.getMethod());
    }

    /**
     * Prova que una crida GET per obtenir membres d'un grup retorna llista d'usuaris.
     */
    @Test
    void getMembres_Resposta200_RetornaLlistaUsuaris() throws Exception {
        String jsonArray = """
            [
              {
                "id": 1,
                "nom": "Usuari1",
                "cognom1": "Cognom1",
                "email": "usuari1@example.com"
              },
              {
                "id": 2,
                "nom": "Usuari2",
                "cognom1": "Cognom2",
                "email": "usuari2@example.com"
              }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<User> membres = grupService.getMembres(1L);
        assertNotNull(membres, "La llista de membres no ha de ser null");
        assertEquals(2, membres.size(), "La llista ha de contenir 2 usuaris");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/grups/llistarUsuarisGrup/1", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que una crida DELETE per eliminar usuari de grup s'envia correctament.
     */
    @Test
    void sortirDelGrup_DeleteCorrecte_ExecutaSenseExcepcio() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        grupService.sortirDelGrup(1L, 2L);

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/grups/1/sortirUsuari/2", path);
        assertEquals("DELETE", req.getMethod());
    }

    /**
     * Prova que una crida DELETE per eliminar grup s'envia correctament.
     */
    @Test
    void deleteGrup_DeleteCorrecte_ExecutaSenseExcepcio() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        grupService.deleteGrup(1L);

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/grups/eliminarGrup/1", path);
        assertEquals("DELETE", req.getMethod());
    }

    /**
     * Prova que una resposta HTTP 500 en crear grup provoca ApiException.
     */
    @Test
    void createGrup_Resposta500_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Grup newGrup = new Grup();
        newGrup.setNom("Grup de Prova");

        ApiException ex = assertThrows(ApiException.class, () -> grupService.createGrup(newGrup));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(500, code, "Si ApiException exposa codi, ha de ser 500");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementació no exposa el codi
        }
    }

    /**
     * Prova que una resposta HTTP 400 en afegir usuari a grup provoca ApiException.
     */
    @Test
    void joinGrup_Resposta400_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        ApiException ex = assertThrows(ApiException.class, () -> grupService.joinGrup(1L, 2L));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(400, code, "Si ApiException exposa codi, ha de ser 400");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementació no exposa el codi
        }
    }
}
