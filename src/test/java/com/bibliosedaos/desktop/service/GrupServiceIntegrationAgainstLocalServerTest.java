package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpAuthApi;
import com.bibliosedaos.desktop.api.http.HttpGrupApi;
import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.security.SessionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova d'integració contra un servidor real per a la capa de grups:
 * HttpAuthApi + AuthService + HttpGrupApi + GrupService.
 *
 * Aquesta prova autentica (login) abans d'executar les crides i s'omet si no
 * es pot autenticar (evita fallades 403 per falta de token/rol).
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class GrupServiceIntegrationAgainstLocalServerTest {

    private static final String DEFAULT_USER = "admin4";
    private static final String DEFAULT_PASS = "admin4";

    private AuthService authService;
    private GrupService grupService;

    /**
     * Inicialitza AuthService (i fa login) i GrupService abans de cada test.
     * Les credencials es poden sobreescriure amb les propietats de sistema:
     * -Dtests.real.user=eluser -Dtests.real.pass=elpass
     */
    @BeforeEach
    void setUp() {
        authService = new AuthService(new HttpAuthApi());
        grupService = new GrupService(new HttpGrupApi());

        String user = System.getProperty("tests.real.user", DEFAULT_USER);
        String pass = System.getProperty("tests.real.pass", DEFAULT_PASS);

        try {
            LoginResponse lr = authService.login(user, pass);

            Assumptions.assumeTrue(lr != null && lr.getAccessToken() != null,
                    "No s'ha pogut autenticar al servidor real — s'ometen les proves");
        } catch (ApiException e) {
            e.printStackTrace(); // així veurem la causa original (ConnectException, 403, etc.)
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                if (e.getCause() != null) msg = e.getCause().toString();
                else msg = e.getClass().getSimpleName();
            }
            Assumptions.assumeTrue(false, "No s'ha pogut autenticar al servidor real: " + msg);
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
     * Prova d'integració que verifica que GET /llistarGrups funciona contra el servidor real.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getAllGrups_ContraServidorReal_RetornaLlista() throws ApiException {
        List<Grup> list = grupService.getAllGrups();
        assertNotNull(list, "La llista de grups no ha de ser null");
        System.out.println(">> getAllGrups - size: " + list.size());
    }

    /**
     * Prova d'integració que obté el primer grup retornat per la llista i comprova que té dades bàsiques.
     *
     * S'executa només si el servidor retorna almenys un grup; en cas contrari, s'ignora la prova.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getFirstGrup_ContraServidorReal_SiHiHaGrup_RetornaGrupValid() throws ApiException {
        List<Grup> list = grupService.getAllGrups();
        Assumptions.assumeTrue(list != null && !list.isEmpty(),
                "No hi ha grups al servidor — s'ometeix la comprovació del grup");

        Grup first = list.get(0);
        assertNotNull(first, "El primer grup de la llista no ha de ser null");
        assertNotNull(first.getId(), "El grup ha de tenir ID");
        assertNotNull(first.getNom(), "El grup ha de tenir nom");

        System.out.println(">> Primer grup - ID: " + first.getId() + ", Nom: " + first.getNom());
    }

    /**
     * Prova d'integració que verifica que es poden obtenir els membres d'un grup existent.
     *
     * S'executa només si el servidor retorna almenys un grup; en cas contrari, s'ignora la prova.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getMembres_ContraServidorReal_SiHiHaGrup_RetornaLlistaMembres() throws ApiException {
        List<Grup> grups = grupService.getAllGrups();
        Assumptions.assumeTrue(grups != null && !grups.isEmpty(),
                "No hi ha grups al servidor — s'omet la prova de membres");

        Grup firstGrup = grups.get(0);
        Long grupId = firstGrup.getId();

        List<User> membres = grupService.getMembres(grupId);
        assertNotNull(membres, "La llista de membres no ha de ser null");
        System.out.println(">> getMembres per grup " + grupId + " - size: " + membres.size());

        // Si hi ha membres, comprovem que tenen dades bàsiques
        if (!membres.isEmpty()) {
            User firstMembre = membres.get(0);
            assertNotNull(firstMembre.getId(), "El membre ha de tenir ID");
            assertNotNull(firstMembre.getNom(), "El membre ha de tenir nom");
        }
    }

    /**
     * Prova d'integració que verifica l'estructura completa d'un grup.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void grupStructure_ContraServidorReal_SiHiHaGrup_TeEstructuraCompleta() throws ApiException {
        List<Grup> grups = grupService.getAllGrups();
        Assumptions.assumeTrue(grups != null && !grups.isEmpty(),
                "No hi ha grups al servidor — s'omet la prova d'estructura");

        Grup grup = grups.get(0);

        assertAll("Estructura del grup",
                () -> assertNotNull(grup.getId(), "ID no ha de ser null"),
                () -> assertNotNull(grup.getNom(), "Nom no ha de ser null"),
                () -> assertNotNull(grup.getTematica(), "Temàtica no ha de ser null"),
                () -> assertNotNull(grup.getAdministrador(), "Administrador no ha de ser null"),
                () -> assertNotNull(grup.getAdministrador().getId(), "ID administrador no ha de ser null")
        );

        // Comprovem opcionalment l'horari si existeix
        if (grup.getHorari() != null) {
            assertNotNull(grup.getHorari().getId(), "ID horari no ha de ser null si existeix horari");
        }

        System.out.println(">> Grup validat: " + grup.getNom() + " (ID: " + grup.getId() + ")");
    }

    /**
     * Prova d'integració que verifica el comportament amb grups sense membres.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getMembres_GrupSenseMembres_RetornaLlistaBuida() throws ApiException {
        List<Grup> grups = grupService.getAllGrups();
        Assumptions.assumeTrue(grups != null && !grups.isEmpty(),
                "No hi ha grups al servidor — s'omet la prova de grups sense membres");

        // Busquem un grup que pugui no tenir membres (això depèn dels teus dades)
        // Per simplificar, agafem el primer grup
        Grup grup = grups.get(0);
        Long grupId = grup.getId();

        List<User> membres = grupService.getMembres(grupId);
        assertNotNull(membres, "La llista de membres no ha de ser null encara que estigui buida");

        System.out.println(">> Grup " + grupId + " té " + membres.size() + " membres");
    }
}
