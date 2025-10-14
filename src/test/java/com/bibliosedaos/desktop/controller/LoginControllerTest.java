package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.service.AuthService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per a LoginController.
 *
 * Verifica la lògica de negoci i validació
 * S'utilitzen mocks per aïllar el comportament del controlador.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private AuthService authServiceMock;

    @Mock
    private Navigator navigatorMock;

    /**
     * Prova que la validació accepta credencials vàlides.
     */
    @Test
    void validateLoginLogic_AmbCredencialsValides_RetornaNull() {
        LoginController controller = new LoginController(authServiceMock, navigatorMock);
        assertNull(controller.validateLoginLogic("usuari", "contrasenya"));
    }

    /**
     * Prova que la validació rebutja nick buit.
     */
    @Test
    void validateLoginLogic_AmbNickBuit_RetornaMissatgeError() {
        LoginController controller = new LoginController(authServiceMock, navigatorMock);
        String resultat = controller.validateLoginLogic("", "contrasenya");
        assertEquals("Omple l'usuari/a i la contrasenya", resultat);
    }

    /**
     * Prova que la validació rebutja contrasenya buida.
     */
    @Test
    void validateLoginLogic_AmbContrasenyaBuida_RetornaMissatgeError() {
        LoginController controller = new LoginController(authServiceMock, navigatorMock);
        String resultat = controller.validateLoginLogic("usuari", "");
        assertEquals("Omple l'usuari/a i la contrasenya", resultat);
    }

    /**
     * Prova que la validació rebutja nick massa llarg.
     */
    @Test
    void validateLoginLogic_AmbNickMassaLlarg_RetornaMissatgeError() {
        LoginController controller = new LoginController(authServiceMock, navigatorMock);
        String resultat = controller.validateLoginLogic("usuari1234", "pass");
        assertEquals("El nick ha de tenir un màxim de 8 caràcters.", resultat);
    }

    /**
     * Prova que la validació rebutja contrasenya massa llarga.
     */
    @Test
    void validateLoginLogic_AmbContrasenyaMassaLlarga_RetornaMissatgeError() {
        LoginController controller = new LoginController(authServiceMock, navigatorMock);
        String contrasenyaLlarga = "a".repeat(21);
        String resultat = controller.validateLoginLogic("usuari", contrasenyaLlarga);
        assertEquals("La contrasenya ha de tenir un màxim de 20 caràcters.", resultat);
    }

    /**
     * Prova que el constructor rebutja AuthService null.
     */
    @Test
    void constructor_AmbAuthServiceNull_LlensaExcepcio() {
        assertThrows(NullPointerException.class,
                () -> new LoginController(null, navigatorMock));
    }

    /**
     * Prova que el constructor rebutja Navigator null.
     */
    @Test
    void constructor_AmbNavigatorNull_LlensaExcepcio() {
        assertThrows(NullPointerException.class,
                () -> new LoginController(authServiceMock, null));
    }
}