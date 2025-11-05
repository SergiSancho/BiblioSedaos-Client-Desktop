package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.service.ExemplarService;
import com.bibliosedaos.desktop.service.LlibreService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a BooksBrowseController.
 * Comprova el comportament segur davant valors nuls i case-insensitive de safeContains
 * i la validacio de dependencies al constructor.
 */
@ExtendWith(MockitoExtension.class)
class BooksBrowseControllerTest {

    @Mock
    private LlibreService llibreService;

    @Mock
    private ExemplarService exemplarService;

    @Mock
    private Navigator navigator;

    private BooksBrowseController controller;

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
        controller = new BooksBrowseController(llibreService, exemplarService, navigator);
    }

    /**
     * Prova les condicions essencials de safeContains.
     */
    @Test
    void safeContains_CasosEssencials() {
        assertTrue(BooksBrowseController.safeContains("El Quijote", "quijote"),
                "Ha de trobar ignore case");

        assertTrue(BooksBrowseController.safeContains("Cien años de soledad", ""),
                "Query buit ha de coincidir");

        assertFalse(BooksBrowseController.safeContains("El Quijote", "hamlet"),
                "No ha de trobar-se quan no existeix");

        assertFalse(BooksBrowseController.safeContains(null, "busqueda"),
                "Camp null ha de retornar false");

        assertFalse(BooksBrowseController.safeContains("El Quijote", null),
                "Query null ha de retornar false");
    }

    /**
     * Prova safeContains amb diferentes casos de cerca.
     */
    @Test
    void safeContains_CasosCerca() {
        assertTrue(BooksBrowseController.safeContains("Gabriel García Márquez", "gabriel"),
                "Ha de trobar al principi");

        assertTrue(BooksBrowseController.safeContains("Gabriel García Márquez", "márquez"),
                "Ha de trobar al final");

        assertTrue(BooksBrowseController.safeContains("Gabriel García Márquez", "GARCÍA"),
                "Ha de ser case insensitive");

        assertFalse(BooksBrowseController.safeContains("", "algo"),
                "Cadena buida no ha de trobar res");
    }

    /**
     * Prova que el constructor valida les dependencies i rebutja valors null.
     */
    @Test
    void constructor_AmbDependenciesNulles_LlencaExcepcio() {
        assertThrows(NullPointerException.class,
                () -> new BooksBrowseController(null, exemplarService, navigator),
                "LlibreService null ha de llençar excepcio");

        assertThrows(NullPointerException.class,
                () -> new BooksBrowseController(llibreService, null, navigator),
                "ExemplarService null ha de llençar excepcio");

        assertThrows(NullPointerException.class,
                () -> new BooksBrowseController(llibreService, exemplarService, null),
                "Navigator null ha de llençar excepcio");
    }

    /**
     * Prova que el constructor accepta dependencies valides.
     */
    @Test
    void constructor_AmbDependenciesValides_EsCreaCorrectament() {
        assertDoesNotThrow(() -> new BooksBrowseController(llibreService, exemplarService, navigator),
                "Controlador ha de crear-se amb dependencies valides");
    }
}
