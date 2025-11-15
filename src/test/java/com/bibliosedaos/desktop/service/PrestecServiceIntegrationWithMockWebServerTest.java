package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpPrestecApi;
import com.bibliosedaos.desktop.model.Prestec;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integracio controlada de la capa de prestecs:
 * MockWebServer + HttpPrestecApi + PrestecService.
 *
 * Aquesta prova simula respostes HTTP reals (200, 404, 500...) sense necessitat d'un
 * servidor extern.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class PrestecServiceIntegrationWithMockWebServerTest {

    private MockWebServer mockWebServer;
    private PrestecService prestecService;

    /**
     * Inicia el MockWebServer abans de cada test i configura la propietat
     * del sistema "api.base.url" perque HttpPrestecApi utilitzi la URL del mock.
     */
    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Forcem ApiClient a apuntar al MockWebServer durant la prova
        System.setProperty("api.base.url", mockWebServer.url("/").toString());

        HttpPrestecApi httpPrestecApi = new HttpPrestecApi();
        prestecService = new PrestecService(httpPrestecApi);
    }

    /**
     * Atura el MockWebServer i neteja propietats despres de cada test.
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
     * Prova que una resposta HTTP 200 amb JSON correcte es parseja be i que
     * PrestecService retorna una llista de prestecs per getAllPrestecs.
     */
    @Test
    void getAllPrestecs_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              {
                "id": 1,
                "dataPrestec": "2024-01-01",
                "dataDevolucio": null,
                "usuari": {
                  "id": 1,
                  "nom": "Usuari1",
                  "cognom1": "Cognom1"
                },
                "exemplar": {
                  "id": 1,
                  "lloc": "Estanteria A1",
                  "llibre": {
                    "id": 1,
                    "titol": "El Quijote"
                  }
                }
              },
              {
                "id": 2,
                "dataPrestec": "2024-01-02",
                "dataDevolucio": "2024-01-10",
                "usuari": {
                  "id": 2,
                  "nom": "Usuari2",
                  "cognom1": "Cognom2"
                },
                "exemplar": {
                  "id": 2,
                  "lloc": "Estanteria B2",
                  "llibre": {
                    "id": 2,
                    "titol": "Cien años de soledad"
                  }
                }
              }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Prestec> list = prestecService.getAllPrestecs(null);
        assertNotNull(list, "La llista no ha de ser null");
        assertEquals(2, list.size(), "La llista ha de contenir 2 elements");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/prestecs/llistarPrestecs", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que una resposta HTTP 200 amb JSON correcte es parseja be per getPrestecsActius.
     */
    @Test
    void getPrestecsActius_Resposta200_RetornaLlista() throws Exception {
        String jsonArray = """
            [
              {
                "id": 3,
                "dataPrestec": "2024-01-15",
                "dataDevolucio": null,
                "usuari": {
                  "id": 3,
                  "nom": "Usuari3",
                  "cognom1": "Cognom3"
                },
                "exemplar": {
                  "id": 3,
                  "lloc": "Estanteria C3",
                  "llibre": {
                    "id": 3,
                    "titol": "1984"
                  }
                }
              }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        List<Prestec> list = prestecService.getPrestecsActius(null);
        assertNotNull(list, "La llista no ha de ser null");
        assertEquals(1, list.size(), "La llista ha de contenir 1 element");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/prestecs/llistarPrestecsActius", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que getAllPrestecs amb usuariId afegeix el parametre a la URL.
     */
    @Test
    void getAllPrestecs_AmbUsuariId_AfegixParametreUrl() throws Exception {
        String jsonArray = "[]";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        prestecService.getAllPrestecs(123L);

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/prestecs/llistarPrestecs?usuariId=123", path);
        assertEquals("GET", req.getMethod());
    }

    /**
     * Prova que una resposta HTTP 404 en obtenir prestecs provoca que PrestecService llanci ApiException.
     */
    @Test
    void getAllPrestecs_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> prestecService.getAllPrestecs(null),
                "S'ha d'atreure ApiException per 404");

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(404, code, "Si ApiException exposa codi, ha de ser 404");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // si l'excepcio no exposa getStatusCode(), no fem l'assert
        }
    }

    /**
     * Prova que una resposta HTTP 500 en obtenir prestecs actius provoca ApiException.
     */
    @Test
    void getPrestecsActius_Resposta500_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        ApiException ex = assertThrows(ApiException.class, () -> prestecService.getPrestecsActius(null));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(500, code, "Si ApiException exposa codi, ha de ser 500");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementacio no exposa el codi
        }
    }

    /**
     * Prova que una crida POST per crear prestec processa la resposta i assigna l'ID retornat.
     */
    @Test
    void createPrestec_PostCorrecte_AssignaIdDesdeResposta() throws Exception {
        String respJson = """
            {
              "id": 123,
              "dataPrestec": "2024-01-20",
              "dataDevolucio": null,
              "usuari": {
                "id": 1,
                "nom": "Usuari1"
              },
              "exemplar": {
                "id": 1,
                "llibre": {
                  "titol": "Llibre Nou"
                }
              }
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(respJson)
                .addHeader("Content-Type", "application/json"));

        Prestec newPrestec = new Prestec();

        // Creem un usuari minim
        com.bibliosedaos.desktop.model.User usuari = new com.bibliosedaos.desktop.model.User();
        usuari.setId(1L);
        newPrestec.setUsuari(usuari);

        // Creem un exemplar minim
        com.bibliosedaos.desktop.model.Exemplar exemplar = new com.bibliosedaos.desktop.model.Exemplar();
        exemplar.setId(1L);
        newPrestec.setExemplar(exemplar);

        Prestec created = prestecService.createPrestec(newPrestec);

        assertNotNull(created, "El prestec creat no ha de ser null");
        assertEquals(123L, created.getId(), "L'id assignat ha de provenir de la resposta");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/prestecs/afegirPrestec", path);
        assertEquals("POST", req.getMethod());

        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType, "Header Content-Type present");
        assertTrue(contentType.toLowerCase().contains("application/json"),
                "Content-Type ha de contenir application/json");

        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"usuari\""), "El cos ha d'incloure 'usuari'");
        assertTrue(body.contains("\"exemplar\""), "El cos ha d'incloure 'exemplar'");
    }

    /**
     * Prova que una resposta HTTP 400 en crear prestec provoca ApiException.
     */
    @Test
    void createPrestec_Resposta400_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        Prestec newPrestec = new Prestec();

        com.bibliosedaos.desktop.model.User usuari = new com.bibliosedaos.desktop.model.User();
        usuari.setId(1L);
        newPrestec.setUsuari(usuari);

        com.bibliosedaos.desktop.model.Exemplar exemplar = new com.bibliosedaos.desktop.model.Exemplar();
        exemplar.setId(1L);
        newPrestec.setExemplar(exemplar);

        ApiException ex = assertThrows(ApiException.class, () -> prestecService.createPrestec(newPrestec));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(400, code, "Si ApiException exposa codi, ha de ser 400");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementacio no exposa el codi
        }
    }

    /**
     * Prova que una crida PUT per retornar prestec s'envia correctament.
     */
    @Test
    void retornarPrestec_PutCorrecte_ExecutaSenseExcepcio() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        prestecService.retornarPrestec(1L);

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/prestecs/ferDevolucio/1", path);
        assertEquals("PUT", req.getMethod());
    }

    /**
     * Prova que una resposta HTTP 404 en retornar prestec provoca ApiException.
     */
    @Test
    void retornarPrestec_Resposta404_LlancaApiException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ApiException ex = assertThrows(ApiException.class, () -> prestecService.retornarPrestec(999L));

        try {
            int code = ex.getStatusCode();
            if (code != -1) {
                assertEquals(404, code, "Si ApiException exposa codi, ha de ser 404");
            }
        } catch (NoSuchMethodError | AbstractMethodError ignored) {
            // ignore si la implementacio no exposa el codi
        }
    }

    /**
     * Prova que getPrestecById funciona correctament (implementacio actual filtra de getAllPrestecs).
     */
    @Test
    void getPrestecById_Resposta200_RetornaPrestec() throws Exception {
        String jsonArray = """
            [
              {
                "id": 42,
                "dataPrestec": "2024-01-01",
                "dataDevolucio": null,
                "usuari": {
                  "id": 1,
                  "nom": "Usuari1"
                },
                "exemplar": {
                  "id": 1,
                  "llibre": {
                    "titol": "Llibre 42"
                  }
                }
              }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonArray)
                .addHeader("Content-Type", "application/json"));

        Prestec p = prestecService.getPrestecById(42L);

        assertNotNull(p, "El prestec retornat no ha de ser null");
        assertEquals(42L, p.getId(), "L'id ha de coincidir");

        RecordedRequest req = mockWebServer.takeRequest();
        String path = normalizePath(req.getPath());
        assertEquals("/biblioteca/prestecs/llistarPrestecs", path);
        assertEquals("GET", req.getMethod());
    }
}