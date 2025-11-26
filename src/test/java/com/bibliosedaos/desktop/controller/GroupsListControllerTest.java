package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.Horari;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.security.SessionStore;
import com.bibliosedaos.desktop.service.GrupService;
import com.bibliosedaos.desktop.service.HorariService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Proves unitaries per a GroupsListController.
 *
 * Comprova el comportament dels mètodes de verificació d'administració i membres,
 * validació d'horaris i el comportament segur davant valors nuls.
 */
@ExtendWith(MockitoExtension.class)
class GroupsListControllerTest {

    @Mock
    private GrupService grupService;

    @Mock
    private HorariService horariService;

    @Mock
    private Navigator navigator;

    private GroupsListController controller;

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
        controller = new GroupsListController(grupService, horariService, navigator);
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
     * Prova la verificació d'administrador global amb diferents rols.
     */
    @Test
    void isAdmin_RolsDiferents_ComportamentCorrecte() {
        try (MockedStatic<SessionStore> mockedSessionStore = mockStatic(SessionStore.class)) {
            SessionStore sessionStore = mock(SessionStore.class);

            when(sessionStore.getRol()).thenReturn(2);
            mockedSessionStore.when(SessionStore::getInstance).thenReturn(sessionStore);
            assertTrue(controller.isAdmin(), "Rol 2 ha de ser administrador");

            when(sessionStore.getRol()).thenReturn(1);
            assertFalse(controller.isAdmin(), "Rol 1 no ha de ser administrador");

            when(sessionStore.getRol()).thenThrow(new RuntimeException("Error"));
            assertFalse(controller.isAdmin(), "Excepció ha de retornar false");
        }
    }

    /**
     * Prova la verificació d'administrador de grup amb diferents escenaris.
     */
    @Test
    void isUserAdminOfGroup_EscenarisAdministracio() {
        try (MockedStatic<SessionStore> mockedSessionStore = mockStatic(SessionStore.class)) {
            SessionStore sessionStore = mock(SessionStore.class);
            when(sessionStore.getUserId()).thenReturn(123L);
            mockedSessionStore.when(SessionStore::getInstance).thenReturn(sessionStore);

            Grup grup = new Grup();
            User admin = new User();
            admin.setId(123L);
            grup.setAdministrador(admin);

            assertAll("Proves administrador grup",
                    () -> assertTrue(controller.isUserAdminOfGroup(grup),
                            "Ha de ser admin quan l'ID coincideix"),
                    () -> {
                        admin.setId(456L);
                        assertFalse(controller.isUserAdminOfGroup(grup),
                                "No ha de ser admin quan l'ID no coincideix");
                    },
                    () -> {
                        grup.setAdministrador(null);
                        assertFalse(controller.isUserAdminOfGroup(grup),
                                "No ha de ser admin quan no hi ha administrador");
                    },
                    () -> {
                        when(sessionStore.getUserId()).thenReturn(null);
                        assertFalse(controller.isUserAdminOfGroup(grup),
                                "No ha de ser admin quan l'usuari és null");
                    }
            );
        }
    }

    /**
     * Prova la verificació de membres de grup amb diferents escenaris.
     */
    @Test
    void isUserMemberOfGroup_EscenarisMembres() {
        try (MockedStatic<SessionStore> mockedSessionStore = mockStatic(SessionStore.class)) {
            SessionStore sessionStore = mock(SessionStore.class);
            when(sessionStore.getUserId()).thenReturn(123L);
            mockedSessionStore.when(SessionStore::getInstance).thenReturn(sessionStore);

            Grup grup = new Grup();
            User membre = new User();
            membre.setId(123L);
            grup.setMembres(Arrays.asList(membre, new User()));

            assertAll("Proves membres grup",
                    () -> assertTrue(controller.isUserMemberOfGroup(grup),
                            "Ha de ser membre quan l'ID està a la llista"),
                    () -> {
                        membre.setId(456L);
                        assertFalse(controller.isUserMemberOfGroup(grup),
                                "No ha de ser membre quan l'ID no està a la llista");
                    },
                    () -> {
                        grup.setMembres(null);
                        assertFalse(controller.isUserMemberOfGroup(grup),
                                "No ha de ser membre quan la llista és null");
                    },
                    () -> {
                        when(sessionStore.getUserId()).thenReturn(null);
                        assertFalse(controller.isUserMemberOfGroup(grup),
                                "No ha de ser membre quan l'usuari és null");
                    }
            );
        }
    }

    /**
     * Prova la validació de camps d'horari amb diferents combinacions.
     */
    @Test
    void validateHorariFields_CombinacionsCamps() throws Exception {
        Method metode = GroupsListController.class.getDeclaredMethod(
                "validateHorariFields", String.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves validació horaris",
                () -> assertTrue((boolean) metode.invoke(controller, "Sala A", "Dilluns", "18:00"),
                        "Camps complets han de ser vàlids"),
                () -> assertFalse((boolean) metode.invoke(controller, "", "Dilluns", "18:00"),
                        "Sala buida ha de ser invàlid"),
                () -> assertFalse((boolean) metode.invoke(controller, "Sala A", "", "18:00"),
                        "Dia buit ha de ser invàlid"),
                () -> assertFalse((boolean) metode.invoke(controller, "Sala A", "Dilluns", ""),
                        "Hora buida ha de ser invàlida"),
                () -> assertFalse((boolean) metode.invoke(controller, "", "", ""),
                        "Tots els camps buits han de ser invàlids"),
                () -> assertFalse((boolean) metode.invoke(controller, "  ", "Dilluns", "18:00"),
                        "Sala amb espais ha de ser invàlida"),
                () -> assertTrue((boolean) metode.invoke(controller, "Sala B", "Dimarts", "09:00"),
                        "Altres valors vàlids han de funcionar")
        );
    }

    /**
     * Prova el format de cerca per ID utilitzat en les validacions.
     */
    @Test
    void idRegex_ValidacioFormatNumerica() {
        assertAll("Proves de regex per validació ID",
                () -> assertTrue("12345".matches("^\\d+$"), "ID numèric vàlid"),
                () -> assertTrue("0".matches("^\\d+$"), "ID zero vàlid"),
                () -> assertFalse("12a34".matches("^\\d+$"), "ID amb lletres invàlid"),
                () -> assertFalse("-123".matches("^\\d+$"), "ID negatiu invàlid"),
                () -> assertFalse(" 123 ".matches("^\\d+$"), "ID amb espais invàlid"),
                () -> assertFalse("".matches("^\\d+$"), "ID buit invàlid")
        );
    }

    /**
     * Prova la creació d'objectes Horari amb dades completes.
     */
    @Test
    void horariObject_CreacioAmbDadesCompletes() {
        Horari horari = new Horari();
        horari.setSala("Sala Principal");
        horari.setDia("Dijous");
        horari.setHora("15:30");
        horari.setEstat("lliure");

        assertAll("Proves objecte Horari",
                () -> assertEquals("Sala Principal", horari.getSala(),
                        "Sala ha de coincidir"),
                () -> assertEquals("Dijous", horari.getDia(),
                        "Dia ha de coincidir"),
                () -> assertEquals("15:30", horari.getHora(),
                        "Hora ha de coincidir"),
                () -> assertEquals("lliure", horari.getEstat(),
                        "Estat ha de coincidir")
        );
    }

    /**
     * Prova la creació d'objectes Grup amb relacions completes.
     */
    @Test
    void grupObject_CreacioAmbRelacions() {
        Grup grup = new Grup();
        grup.setId(1L);
        grup.setNom("Grup de Test");
        grup.setTematica("Literatura");

        Horari horari = new Horari();
        horari.setSala("Sala A");
        grup.setHorari(horari);

        User admin = new User();
        admin.setId(100L);
        admin.setNom("Admin");
        grup.setAdministrador(admin);

        User membre = new User();
        membre.setId(200L);
        membre.setNom("Membre");
        grup.setMembres(Arrays.asList(membre));

        assertAll("Proves objecte Grup",
                () -> assertEquals(1L, grup.getId(), "ID ha de coincidir"),
                () -> assertEquals("Grup de Test", grup.getNom(), "Nom ha de coincidir"),
                () -> assertEquals("Literatura", grup.getTematica(), "Temàtica ha de coincidir"),
                () -> assertNotNull(grup.getHorari(), "Horari no ha de ser null"),
                () -> assertEquals("Sala A", grup.getHorari().getSala(), "Sala de l'horari ha de coincidir"),
                () -> assertNotNull(grup.getAdministrador(), "Administrador no ha de ser null"),
                () -> assertEquals(100L, grup.getAdministrador().getId(), "ID administrador ha de coincidir"),
                () -> assertNotNull(grup.getMembres(), "Llista membres no ha de ser null"),
                () -> assertEquals(1, grup.getMembres().size(), "Ha de tenir 1 membre"),
                () -> assertEquals(200L, grup.getMembres().get(0).getId(), "ID membre ha de coincidir")
        );
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

        grup.setMembres(Arrays.asList());
        assertNotNull(grup.getMembres(), "Llista membres no ha de ser null després d'establir");
        assertEquals(0, grup.getMembres().size(), "Llista buida ha de tenir 0 elements");

        List<User> membres = Arrays.asList(new User(), new User(), new User());
        grup.setMembres(membres);
        assertEquals(3, grup.getMembres().size(), "Llista ha de tenir 3 elements");
    }
}
