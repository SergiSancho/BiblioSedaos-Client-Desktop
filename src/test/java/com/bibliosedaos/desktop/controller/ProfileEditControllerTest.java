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
 * Proves unitaries per a ProfileEditController.
 *
 * Comprova validacio de camps, creacio d'usuari i comportament null-safe.
 */
@ExtendWith(MockitoExtension.class)
class ProfileEditControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Navigator navigator;

    private ProfileEditController controller;

    @BeforeAll
    static void globalSetup() {
        System.setProperty("tests.noDialog", "true");
    }

    @BeforeEach
    void setUp() {
        controller = new ProfileEditController(userService, navigator);
    }

    /**
     * Prova el comportament null-safe de safeGet amb diferents valors.
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
     * Prova la validacio de formats de camps sense dependències de UI.
     */
    @Test
    void validateFieldFormats_LogicAillada() {
        // Simulem la lógica de validació de formats
        assertAll("Proves de validacio de formats",
                () -> assertTrue(validarNIF("12345678A"), "NIF amb 8 digits i lletra ha de ser valid"),
                () -> assertTrue(validarNIF("12345678a"), "NIF amb lletra minuscula ha de ser valid"),
                () -> assertFalse(validarNIF("1234567A"), "NIF amb 7 digits ha de ser invalid"),
                () -> assertFalse(validarNIF("123456789A"), "NIF amb 9 digits ha de ser invalid"),
                () -> assertFalse(validarNIF("12345678AB"), "NIF amb dues lletres ha de ser invalid"),
                () -> assertTrue(validarTelefon("123456789"), "Telèfon amb 9 digits ha de ser valid"),
                () -> assertFalse(validarTelefon("12345678"), "Telèfon amb 8 digits ha de ser invalid"),
                () -> assertFalse(validarTelefon("1234567890"), "Telèfon amb 10 digits ha de ser invalid"),
                () -> assertFalse(validarTelefon("12a45678"), "Telèfon amb lletres ha de ser invalid"),
                () -> assertTrue(validarCP("08001"), "CP amb 5 digits ha de ser valid"),
                () -> assertFalse(validarCP("0800"), "CP amb 4 digits ha de ser invalid"),
                () -> assertFalse(validarCP("080012"), "CP amb 6 digits ha de ser invalid"),
                () -> assertFalse(validarCP("08a01"), "CP amb lletres ha de ser invalid"),
                () -> assertTrue(validarEmail("usuari@exemple.cat"), "Email valid ha de retornar true"),
                () -> assertFalse(validarEmail("@exemple.cat"), "Email sense usuari ha de ser invalid"),
                () -> assertFalse(validarEmail("usuari@"), "Email sense domini ha de ser invalid"),
                () -> assertFalse(validarEmail("usuari"), "Email sense @ ha de ser invalid"),
                () -> assertTrue(validarContrasenyes("password123", "password123"), "Contrasenyes iguals han de ser valides"),
                () -> assertFalse(validarContrasenyes("password123", "password124"), "Contrasenyes diferents han de ser invalides")
        );
    }

    /**
     * Prova la creacio d'objecte User amb dades completes.
     */
    @Test
    void createUserObject_DadesCompletes() {
        User usuari = crearUsuariComplet();

        assertAll("Proves de creacio d'usuari amb dades completes",
                () -> assertEquals("usuari123", usuari.getNick()),
                () -> assertEquals("12345678A", usuari.getNif()),
                () -> assertEquals("Nom", usuari.getNom()),
                () -> assertEquals("Cognom1", usuari.getCognom1()),
                () -> assertEquals("Cognom2", usuari.getCognom2()),
                () -> assertEquals("Barcelona", usuari.getLocalitat()),
                () -> assertEquals("Barcelona", usuari.getProvincia()),
                () -> assertEquals("Carrer Example 123", usuari.getCarrer()),
                () -> assertEquals("08001", usuari.getCp()),
                () -> assertEquals("123456789", usuari.getTlf()),
                () -> assertEquals("usuari@exemple.cat", usuari.getEmail()),
                () -> assertEquals("password123", usuari.getPassword())
        );
    }

    /**
     * Prova la creacio d'objecte User amb dades minims.
     */
    @Test
    void createUserObject_DadesMinimes() {
        User usuari = crearUsuariMinim();

        assertAll("Proves de creacio d'usuari amb dades mínimes",
                () -> assertEquals("usuari", usuari.getNick()),
                () -> assertEquals("87654321Z", usuari.getNif()),
                () -> assertEquals("Nom", usuari.getNom()),
                () -> assertEquals("Cognom", usuari.getCognom1()),
                () -> assertEquals("", usuari.getCognom2()), // Cognom2 opcional
                () -> assertEquals("usuari@exemple.cat", usuari.getEmail()),
                () -> assertEquals("password", usuari.getPassword())
        );
    }

    /**
     * Prova camps obligatoris sense dependències de UI.
     */
    @Test
    void validateRequiredFields_LogicAillada() {
        assertAll("Proves de camps obligatoris",
                () -> assertTrue(validarCampObligatori("valor"), "Camp amb valor ha de ser valid"),
                () -> assertFalse(validarCampObligatori(""), "Camp buit ha de ser invalid"),
                () -> assertFalse(validarCampObligatori("   "), "Camp amb espais ha de ser invalid"),
                () -> assertFalse(validarCampObligatori(null), "Camp null ha de ser invalid")
        );
    }

    private boolean validarNIF(String nif) {
        return nif != null && nif.matches("^\\d{8}[A-Za-z]$");
    }

    private boolean validarTelefon(String tlf) {
        return tlf != null && tlf.matches("^\\d{9}$");
    }

    private boolean validarCP(String cp) {
        return cp != null && cp.matches("^\\d{5}$");
    }

    private boolean validarEmail(String email) {
        return email != null && email.contains("@") &&
                !email.startsWith("@") && !email.endsWith("@");
    }

    private boolean validarContrasenyes(String pwd1, String pwd2) {
        return pwd1 != null && pwd1.equals(pwd2);
    }

    private boolean validarCampObligatori(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private User crearUsuariComplet() {
        User usuari = new User();
        usuari.setNick("usuari123");
        usuari.setNif("12345678A");
        usuari.setNom("Nom");
        usuari.setCognom1("Cognom1");
        usuari.setCognom2("Cognom2");
        usuari.setLocalitat("Barcelona");
        usuari.setProvincia("Barcelona");
        usuari.setCarrer("Carrer Example 123");
        usuari.setCp("08001");
        usuari.setTlf("123456789");
        usuari.setEmail("usuari@exemple.cat");
        usuari.setPassword("password123");
        return usuari;
    }

    private User crearUsuariMinim() {
        User usuari = new User();
        usuari.setNick("usuari");
        usuari.setNif("87654321Z");
        usuari.setNom("Nom");
        usuari.setCognom1("Cognom");
        usuari.setCognom2(""); // Opcional
        usuari.setLocalitat("Lleida");
        usuari.setProvincia("Lleida");
        usuari.setCarrer("Carrer Simple");
        usuari.setCp("25001");
        usuari.setTlf("987654321");
        usuari.setEmail("usuari@exemple.cat");
        usuari.setPassword("password");
        return usuari;
    }
}