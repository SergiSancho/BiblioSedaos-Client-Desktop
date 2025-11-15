package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.model.Prestec;
import com.bibliosedaos.desktop.service.ExemplarService;
import com.bibliosedaos.desktop.service.PrestecService;
import com.bibliosedaos.desktop.service.UserService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a LoanFormController.
 * Comprova validacio de dependencies, comportament null-safe i configuracio de modes.
 */
@ExtendWith(MockitoExtension.class)
class LoanFormControllerTest {

    @Mock
    private PrestecService prestecService;

    @Mock
    private ExemplarService exemplarService;

    @Mock
    private UserService userService;

    @Mock
    private Navigator navigator;

    private LoanFormController controller;

    /**
     * Configuracio global per a tests: evitem que els dialegs bloquegin.
     */
    @BeforeAll
    static void globalSetup() {
        System.setProperty("tests.noDialog", "true");
    }

    /**
     * Instancia el controlador amb mocks abans de cada prova.
     */
    @BeforeEach
    void setUp() {
        controller = new LoanFormController(prestecService, exemplarService, userService, navigator);
    }

    /**
     * Prova el comportament null-safe de safeGet amb diferents valors.
     */
    @Test
    void safeGet_ComportamentNullSafe() {
        assertAll("Proves de safeGet amb valors null i buits",
                () -> assertEquals("", controller.safeGet(null),
                        "Ha de retornar buit amb null"),
                () -> assertEquals("", controller.safeGet(""),
                        "Ha de retornar buit amb cadena buida"),
                () -> assertEquals("test", controller.safeGet("test"),
                        "Ha de retornar el text original"),
                () -> assertEquals("  espais  ", controller.safeGet("  espais  "),
                        "Ha de preservar espais"),
                () -> assertEquals("123", controller.safeGet("123"),
                        "Ha de retornar numerics correctament")
        );
    }

    /**
     * Prova la configuracio de modes amb setLoanData quan no esta inicialitzat.
     */
    @Test
    void setLoanData_QuanNoInicialitzat_EmmagatzemaPrestecIMode() throws Exception {
        Prestec prestec = new Prestec();
        prestec.setId(77L);

        controller.setLoanData(prestec, "CREATE");

        // Llegim els camps privats mitjancant reflexio
        Field campPrestec = LoanFormController.class.getDeclaredField("currentPrestec");
        campPrestec.setAccessible(true);
        Object prestecEmmagatzemat = campPrestec.get(controller);
        assertSame(prestec, prestecEmmagatzemat, "El prestec ha de ser el mateix");

        Field campMode = LoanFormController.class.getDeclaredField("mode");
        campMode.setAccessible(true);
        Object modeEmmagatzemat = campMode.get(controller);
        assertEquals("CREATE", modeEmmagatzemat, "El mode ha de ser CREATE");
    }

    /**
     * Prova la configuracio de modes amb setLoanData quan el mode es null.
     */
    @Test
    void setLoanData_QuanModeNull_UtilitzaModePerDefecte() throws Exception {
        Prestec prestec = new Prestec();
        prestec.setId(88L);

        controller.setLoanData(prestec, null);

        Field campMode = LoanFormController.class.getDeclaredField("mode");
        campMode.setAccessible(true);
        Object modeEmmagatzemat = campMode.get(controller);
        assertEquals("VIEW", modeEmmagatzemat, "Mode null ha d'usar VIEW per defecte");
    }

    /**
     * Prova la configuracio de modes amb setLoanData quan el prestec es null.
     */
    @Test
    void setLoanData_QuanPrestecNull_EmmagatzemaNull() throws Exception {
        controller.setLoanData(null, "VIEW");

        Field campPrestec = LoanFormController.class.getDeclaredField("currentPrestec");
        campPrestec.setAccessible(true);
        Object prestecEmmagatzemat = campPrestec.get(controller);
        assertNull(prestecEmmagatzemat, "El prestec ha de ser null");
    }

    /**
     * Prova que setLoanData es comporta correctament amb diferents combinacions.
     */
    @Test
    void setLoanData_DiferentsCombinacions_ComportamentCorrecte() {
        Prestec prestec = new Prestec();
        prestec.setId(99L);

        assertAll("Proves de setLoanData amb diferents combinacions",
                () -> assertDoesNotThrow(() -> controller.setLoanData(null, "CREATE"),
                        "Ha de permetre prestec null en mode CREATE"),
                () -> assertDoesNotThrow(() -> controller.setLoanData(prestec, "VIEW"),
                        "Ha de permetre prestec valid en mode VIEW"),
                () -> assertDoesNotThrow(() -> controller.setLoanData(null, null),
                        "Ha de permetre ambdos null"),
                () -> assertDoesNotThrow(() -> controller.setLoanData(prestec, "INVALID"),
                        "Ha de permetre mode invalid")
        );
    }
}