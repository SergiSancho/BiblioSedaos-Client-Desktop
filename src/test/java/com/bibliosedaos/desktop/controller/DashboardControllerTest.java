package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.service.AuthService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.security.SessionStore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per a DashboardController.
 *
 * Verifica la lògica de negoci.
 * S'utilitzen mocks per aïllar el comportament del controlador.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private AuthService authServiceMock;

    @Mock
    private Navigator navigatorMock;

    private static final Logger DC_LOG = Logger.getLogger(DashboardController.class.getName());
    private Level previousLogLevel;

    /**
     * Configuració inicial abans de cada test.
     * Desactiva els logs del DashboardController per a sortida més neta.
     */
    @BeforeEach
    void setUp() {
        previousLogLevel = DC_LOG.getLevel();
        DC_LOG.setLevel(Level.OFF);
    }

    /**
     * Neteja i restaura l'estat després de cada test.
     * Restaura el nivell de log i neteja SessionStore.
     */
    @AfterEach
    void tearDown() {
        DC_LOG.setLevel(previousLogLevel);

        SessionStore.getInstance().clear();
    }


    /**
     * Prova que navigateToLoginAfterLogout navega correctament a la vista de login.
     * Verifica els paràmetres exactes passats al Navigator.
     */
    @Test
    void navigateToLoginAfterLogout_QuanCridat_NavegaALogin() {
        DashboardController controller = new DashboardController(authServiceMock, navigatorMock);

        controller.navigateToLoginAfterLogout();

        verify(navigatorMock, times(1)).goTo(
                eq("/com/bibliosedaos/desktop/login-view.fxml"),
                eq("BiblioSedaos - Login"),
                eq(1000.0),
                eq(600.0),
                eq(false),
                isNull()
        );
    }

    /**
     * Prova que performLogout executa el logout del servei d'autenticació.
     * Verifica la integració amb AuthService.
     */
    @Test
    void performLogout_QuanCridat_ExecutaLogout() {
        DashboardController controller = new DashboardController(authServiceMock, navigatorMock);

        controller.performLogout();

        verify(authServiceMock, times(1)).logout();
    }

    /**
     * Prova que performLogout maneja correctament les excepcions del servei.
     * Verifica que les excepcions no es propaguin i es registrin internament.
     */
    @Test
    void performLogout_QuanLogoutLlansaExcepcio_NoPropagaExcepcio() {
        DashboardController controller = new DashboardController(authServiceMock, navigatorMock);
        doThrow(new RuntimeException("Error de xarxa")).when(authServiceMock).logout();

        assertDoesNotThrow(controller::performLogout);
    }

    /**
     * Prova que el constructor valida les dependències i rebutja valors null.
     * Verifica el comportament defensiu del constructor.
     */
    @Test
    void constructor_AmbDependenciesNulles_LlensaExcepcio() {
        assertThrows(NullPointerException.class,
                () -> new DashboardController(null, navigatorMock));
        assertThrows(NullPointerException.class,
                () -> new DashboardController(authServiceMock, null));
    }

    /**
     * Prova que buildDisplayName construeix correctament el nom complet amb nom i cognoms.
     * Verifica la lògica de formatació de noms amb dades completes.
     */
    @Test
    void buildDisplayName_withNameAndSurnames_returnsFullName() {
        DashboardController controller = new DashboardController(authServiceMock, navigatorMock);
        SessionStore s = SessionStore.getInstance();
        s.setNom("Joan");
        s.setCognom1("Garcia");
        s.setCognom2("Lopez");
        s.setUserId("42");

        String name = controller.buildDisplayName(s);

        assertEquals("Joan Garcia Lopez", name);
    }

    /**
     * Prova que buildDisplayName utilitza l'ID d'usuari quan el nom no està disponible.
     * Verifica el comportament de fallback amb dades incompletes.
     */
    @Test
    void buildDisplayName_withMissingName_usesUserId() {
        DashboardController controller = new DashboardController(authServiceMock, navigatorMock);
        SessionStore s = SessionStore.getInstance();
        s.setNom(null);
        s.setUserId("42");

        String name = controller.buildDisplayName(s);

        assertEquals("42", name);
    }
}
