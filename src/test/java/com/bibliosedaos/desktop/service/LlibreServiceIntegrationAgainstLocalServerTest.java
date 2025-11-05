package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpAuthApi;
import com.bibliosedaos.desktop.api.http.HttpLlibreApi;
import com.bibliosedaos.desktop.model.Llibre;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.security.SessionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integració contra un servidor real per a la capa de llibres:
 * HttpAuthApi + AuthService + HttpLlibreApi + LlibreService.
 *
 * Aquesta prova autentica (login) abans d'executar les crides i s'omet si no
 * es pot autenticar (evita fallades 403 per falta de token/rol).
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class LlibreServiceIntegrationAgainstLocalServerTest {

    private static final String DEFAULT_USER = "admin4";
    private static final String DEFAULT_PASS = "admin4";

    private AuthService authService;
    private LlibreService llibreService;

    /**
     * Inicialitza AuthService (i fa login) i LlibreService abans de cada test.
     * Les credencials es poden sobreescriure amb les propietats de sistema:
     * -Dtests.real.user=eluser -Dtests.real.pass=elpass
     */
    @BeforeEach
    void setUp() {
        authService = new AuthService(new HttpAuthApi());
        llibreService = new LlibreService(new HttpLlibreApi());

        String user = System.getProperty("tests.real.user", DEFAULT_USER);
        String pass = System.getProperty("tests.real.pass", DEFAULT_PASS);

        try {
            LoginResponse lr = authService.login(user, pass);
            Assumptions.assumeTrue(lr != null && lr.getAccessToken() != null,
                    "No s'ha pogut autenticar al servidor real — s'ometen les proves");
        } catch (ApiException e) {
            Assumptions.assumeTrue(false, "No s'ha pogut autenticar al servidor real: " + e.getMessage());
        }
    }

    /**
     * Neteja la sessió després de cada test.
     */
    @AfterEach
    void tearDown() {
        SessionStore.getInstance().clear();
    }

    /**
     * Prova d'integració que verifica que GET /llistarLlibres funciona contra el servidor real.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getAllBooks_ContraServidorReal_RetornaLlista() throws ApiException {
        List<Llibre> list = llibreService.getAllBooks();
        assertNotNull(list, "La llista de llibres no ha de ser null");
        System.out.println(">> getAllBooks - size: " + list.size());
    }

    /**
     * Prova d'integració que obté el primer llibre retornat per la llista i comprova getBookById.
     *
     * S'executa només si el servidor retorna almenys un llibre; en cas contrari, s'ignora la prova.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getBookById_ContraServidorReal_SiHiHaLlibre_RetornaLlibre() throws ApiException {
        List<Llibre> list = llibreService.getAllBooks();
        Assumptions.assumeTrue(list != null && !list.isEmpty(), "No hi ha llibres al servidor — s'ometeix la comprovació getBookById");

        Llibre first = list.get(0);
        assertNotNull(first, "El primer llibre de la llista no ha de ser null");

        Llibre fetched = llibreService.getBookById(first.getId());
        assertNotNull(fetched, "findLlibreById no ha de retornar null per un ID existent");
        assertEquals(first.getId(), fetched.getId(), "L'ID obtingut ha de coincidir amb l'ID sol·licitat");
    }
}
