package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.http.HttpAuthApi;
import com.bibliosedaos.desktop.api.http.HttpUserApi;
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
 * Prova d'integració contra un servidor real per a la capa d'usuaris:
 * HttpAuthApi + AuthService + HttpUserApi + UserService.
 *
 * Aquesta prova autentica (login) abans d'executar les crides i s'omet si no
 * es pot autenticar (evita fallades 403 per falta de token/rol).
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class UserServiceIntegrationAgainstLocalServerTest {

    private static final String DEFAULT_USER = "admin4";
    private static final String DEFAULT_PASS = "admin4";

    private AuthService authService;
    private UserService userService;

    /**
     * Inicialitza AuthService (i fa login) i UserService abans de cada test.
     * Les credencials es poden sobreescriure amb les propietats de sistema:
     * -Dtests.real.user=eluser -Dtests.real.pass=elpass
     */
    @BeforeEach
    void setUp() {
        authService = new AuthService(new HttpAuthApi());
        userService = new UserService(new HttpUserApi());

        String user = System.getProperty("tests.real.user", DEFAULT_USER);
        String pass = System.getProperty("tests.real.pass", DEFAULT_PASS);

        try {
            LoginResponse lr = authService.login(user, pass);
            // assumeTrue fa skip de la prova si no s'ha pogut autenticar
            Assumptions.assumeTrue(lr != null && lr.getAccessToken() != null,
                    "No s'ha pogut autenticar al servidor real — s'ometen les proves");
        } catch (ApiException e) {
            // <-- CAMBIO MÍNIMO: imprimimos la traza y construimos mensaje seguro
            e.printStackTrace(); // así verás la causa original (ConnectException, 403, etc.)
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
     * Prova d'integració que verifica que GET /llistarUsuaris funciona contra el servidor real.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getAllUsers_ContraServidorReal_RetornaLlista() throws ApiException {
        List<User> list = userService.getAllUsers();
        assertNotNull(list, "La llista retornada no ha de ser null");
        System.out.println(">> getAllUsers - size: " + list.size());
    }

    /**
     * Prova d'integració que obté el primer usuari retornat per la llista i comprova getUserById.
     *
     * S'executa només si el servidor retorna almenys un usuari; en cas contrari, s'ignora la prova.
     *
     * @throws ApiException si falla la comunicació amb el servidor
     */
    @Test
    void getUserById_ContraServidorReal_SiHiHaUsuari_RetornaUsuari() throws ApiException {
        List<User> list = userService.getAllUsers();
        Assumptions.assumeTrue(list != null && !list.isEmpty(), "No hi ha usuaris al servidor — s'ometeix la comprovació getUserById");

        User first = list.get(0);
        assertNotNull(first, "El primer usuari de la llista no ha de ser null");

        User fetched = userService.getUserById(first.getId());
        assertNotNull(fetched, "getUserById no ha de retornar null per un ID existent");
        assertEquals(first.getId(), fetched.getId(), "L'ID obtingut ha de coincidir amb l'ID sol·licitat");
    }
}
