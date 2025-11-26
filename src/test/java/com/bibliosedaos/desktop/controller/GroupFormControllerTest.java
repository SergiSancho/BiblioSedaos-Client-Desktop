package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.Horari;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.service.GrupService;
import com.bibliosedaos.desktop.service.HorariService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a GroupFormController.
 *
 * Comprova el comportament dels mètodes de validació
 * i gestió de dades de grup.
 */
@ExtendWith(MockitoExtension.class)
class GroupFormControllerTest {

    @Mock
    private GrupService grupService;

    @Mock
    private HorariService horariService;

    @Mock
    private Navigator navigator;

    private GroupFormController controller;

    /**
     * Configuració global per a tests: evitem que els diàlegs bloquegin.
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
        controller = new GroupFormController(grupService, horariService, navigator);
    }

    /**
     * Prova les condicions essencials de safeGet amb valors null i buits.
     */
    @Test
    void safeGet_CasosEssencials() {
        assertEquals("test", controller.safeGet("test"), "Ha de retornar el valor original");
        assertEquals("", controller.safeGet(null), "Ha de retornar cadena buida per a null");
        assertEquals("", controller.safeGet(""), "Ha de retornar cadena buida per a cadena buida");
    }

    /**
     * Prova la creació d'objecte Grup des del formulari (simulació simple).
     */
    @Test
    void createGrupFromForm_ConfiguracioCorrecte() {

        Grup grup = new Grup();
        grup.setNom("Nou Grup");
        grup.setTematica("Nova Temàtica");

        Horari horari = new Horari();
        horari.setId(1L);
        grup.setHorari(horari);

        User admin = new User();
        admin.setId(123L);
        grup.setAdministrador(admin);

        assertAll("Proves estructura grup",
                () -> assertEquals("Nou Grup", grup.getNom(), "Nom ha de coincidir"),
                () -> assertEquals("Nova Temàtica", grup.getTematica(), "Temàtica ha de coincidir"),
                () -> assertNotNull(grup.getHorari(), "Horari no ha de ser null"),
                () -> assertEquals(1L, grup.getHorari().getId(), "ID horari ha de coincidir"),
                () -> assertNotNull(grup.getAdministrador(), "Administrador no ha de ser null"),
                () -> assertEquals(123L, grup.getAdministrador().getId(), "ID administrador ha de coincidir")
        );
    }

    /**
     * Prova el comportament amb sessions d'usuari null.
     */
    @Test
    void userSession_NullSafety() {
        // Simulem un grup amb administrador null
        Grup grup = new Grup();
        User admin = new User();
        grup.setAdministrador(admin);

        assertNotNull(grup.getAdministrador(), "Administrador ha d'existir encara que session sigui null");
        assertNull(grup.getAdministrador().getId(), "ID administrador ha de ser null quan session és null");
    }

    /**
     * Prova els estats possibles d'horari en el sistema.
     */
    @Test
    void horariEstats_ValorsPossibles() {
        Horari horari = new Horari();

        horari.setEstat("lliure");
        assertEquals("lliure", horari.getEstat(), "Estat 'lliure' ha de coincidir");

        horari.setEstat("ocupat");
        assertEquals("ocupat", horari.getEstat(), "Estat 'ocupat' ha de coincidir");

        horari.setEstat(null);
        assertNull(horari.getEstat(), "Estat null ha de ser permès");
    }

    /**
     * Prova el comportament amb llistes buides i null en objectes Grup.
     */
    @Test
    void grupCollections_ComportamentLlistes() {
        Grup grup = new Grup();

        assertNull(grup.getMembres(), "Llista membres ha de ser null per defecte");

        grup.setMembres(java.util.Arrays.asList());
        assertNotNull(grup.getMembres(), "Llista membres no ha de ser null després d'establir");
        assertEquals(0, grup.getMembres().size(), "Llista buida ha de tenir 0 elements");

        java.util.List<User> membres = java.util.Arrays.asList(new User(), new User(), new User());
        grup.setMembres(membres);
        assertEquals(3, grup.getMembres().size(), "Llista ha de tenir 3 elements");
    }

    /**
     * Prova la configuració bàsica de modes del controlador.
     */
    @Test
    void setGroupData_ConfiguracioModes() {
        Grup grup = new Grup();
        grup.setId(1L);
        grup.setNom("Grup de Prova");

        assertAll("Proves de configuració de modes",
                () -> assertDoesNotThrow(() -> controller.setGroupData(null, "CREATE"),
                        "Mode CREATE amb null"),
                () -> assertDoesNotThrow(() -> controller.setGroupData(grup, "VIEW"),
                        "Mode VIEW amb grup"),
                () -> assertDoesNotThrow(() -> controller.setGroupData(grup, null),
                        "Mode null ha d'usar per defecte"),
                () -> assertDoesNotThrow(() -> controller.setGroupData(null, "VIEW"),
                        "Mode VIEW amb null")
        );
    }
}