package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.service.UserService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a UsersListController.
 *
 * Comprova validacio d'entrada (ID i NIF) i el comportament segur davant valors nuls i case-insensitive de safeContains.
 */
@ExtendWith(MockitoExtension.class)
class UsersListControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Navigator navigator;

    private UsersListController controller;

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
        controller = new UsersListController(userService, navigator);
    }

    /**
     * Prova classes d'equivalencia i valors limit per a IDs.
     */
    @Test
    void validateSearchInput_IdEquivalenciaYLimites() {
        assertTrue(controller.validateSearchInput("12345", "ID"), "ID numeric valid ha de ser true");
        assertFalse(controller.validateSearchInput("12a34", "ID"), "ID amb lletres ha de ser false");
        assertFalse(controller.validateSearchInput("", "ID"), "ID buit ha de ser false");
        assertFalse(controller.validateSearchInput(null, "ID"), "ID null ha de ser false");
        assertTrue(controller.validateSearchInput("0", "ID"), "ID '0' ha de ser valid");
        assertFalse(controller.validateSearchInput("-1", "ID"), "ID negatiu ha de ser invalid");
    }

    /**
     * Prova classes d'equivalencia i valors limit per a NIFs.
     */
    @Test
    void validateSearchInput_NifEquivalenciaYLimites() {
        assertTrue(controller.validateSearchInput("12345678A", "NIF"), "NIF amb lletra majuscula ha de ser valid");
        assertTrue(controller.validateSearchInput("12345678a", "NIF"), "NIF amb lletra minuscula ha de ser valid");
        assertFalse(controller.validateSearchInput("1234567A", "NIF"), "NIF amb 7 digits ha de ser invalid");
        assertFalse(controller.validateSearchInput("123456789A", "NIF"), "NIF amb 9 digits ha de ser invalid");
        assertFalse(controller.validateSearchInput("12345678AB", "NIF"), "NIF amb dues lletres ha de ser invalid");
        assertFalse(controller.validateSearchInput("", "NIF"), "NIF buit ha de ser invalid");
        assertFalse(controller.validateSearchInput(null, "NIF"), "NIF null ha de ser invalid");
    }

    /**
     * Prova les condicions essencials de safeContains.
     */
    @Test
    void safeContains_CasosEssencials() {
        assertTrue(UsersListController.safeContains("Hola Mundo", "mundo"), "Ha de trobar ignore case");
        assertTrue(UsersListController.safeContains("Hola Mundo", ""), "Query buit ha de coincidir");
        assertFalse(UsersListController.safeContains("Hola Mundo", "adios"), "No ha de trobar-se quan no existeix");
        assertFalse(UsersListController.safeContains(null, "x"), "Camp null ha de retornar false");
        assertFalse(UsersListController.safeContains("Hola Mundo", null), "Query null ha de retornar false");
    }
}
