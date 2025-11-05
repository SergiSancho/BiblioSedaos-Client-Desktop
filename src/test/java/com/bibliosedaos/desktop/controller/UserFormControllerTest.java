package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.model.User;
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
 * Proves del comportament null-safe de safeGet i la configuracio de modes amb setUserData.
 */
@ExtendWith(MockitoExtension.class)
class UserFormControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Navigator navigator;

    private UserFormController controller;

    @BeforeAll
    static void globalSetup() {
        System.setProperty("tests.noDialog", "true");
    }

    @BeforeEach
    void setUp() {
        controller = new UserFormController(userService, navigator);
    }

    /**
     * Comprova el comportament null-safe de safeGet amb diferents valors.
     */
    @Test
    void safeGet_ComportamentNullSafe() {
        assertAll("Proves de safeGet amb valors null i buits",
                () -> assertEquals("", controller.safeGet(null), "Ha de retornar buit amb null"),
                () -> assertEquals("", controller.safeGet(""), "Ha de retornar buit amb cadena buida"),
                () -> assertEquals("test", controller.safeGet("test"), "Ha de retornar el text original"),
                () -> assertEquals("  espais  ", controller.safeGet("  espais  "), "Ha de preservar espais")
        );
    }

    /**
     * Comprova la configuracio de modes amb setUserData.
     */
    @Test
    void setUserData_ConfiguracioModes() {
        User usuari = crearUsuariBasico();

        assertAll("Proves de configuracio de modes",
                () -> assertDoesNotThrow(() -> controller.setUserData(null, "CREATE"), "Mode CREATE amb null"),
                () -> assertDoesNotThrow(() -> controller.setUserData(usuari, "VIEW"), "Mode VIEW amb usuari"),
                () -> assertDoesNotThrow(() -> controller.setUserData(usuari, "EDIT"), "Mode EDIT amb usuari"),
                () -> assertDoesNotThrow(() -> controller.setUserData(usuari, null), "Mode null usa per defecte"),
                () -> assertDoesNotThrow(() -> controller.setUserData(null, "VIEW"), "Mode VIEW amb null")
        );
    }

    private User crearUsuariBasico() {
        User usuari = new User();
        usuari.setId(1L);
        usuari.setNick("prova");
        usuari.setNom("Nom");
        usuari.setCognom1("Cognom");
        usuari.setEmail("prova@exemple.cat");
        usuari.setRol(1);
        return usuari;
    }
}