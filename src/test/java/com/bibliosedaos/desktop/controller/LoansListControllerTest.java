package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.model.Exemplar;
import com.bibliosedaos.desktop.model.Llibre;
import com.bibliosedaos.desktop.model.Prestec;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.service.PrestecService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a LoansListController.
 * Comprova el comportament de cerca (safeContains) i criteris de cerca per diferents camps.
 */
@ExtendWith(MockitoExtension.class)
class LoansListControllerTest {

    @Mock
    private PrestecService prestecService;

    @Mock
    private Navigator navigator;

    private LoansListController controller;

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
        controller = new LoansListController(prestecService, navigator);
    }

    /**
     * Prova les condicions essencials de safeContains.
     */
    @Test
    void safeContains_CasosEssencials() {
        assertAll("Proves de safeContains",
                () -> assertTrue(LoansListController.safeContains("Hola Mundo", "mundo"),
                        "Ha de trobar ignore case"),
                () -> assertTrue(LoansListController.safeContains("Hola Mundo", ""),
                        "Query buit ha de coincidir"),
                () -> assertFalse(LoansListController.safeContains("Hola Mundo", "adios"),
                        "No ha de trobar-se quan no existeix"),
                () -> assertFalse(LoansListController.safeContains(null, "x"),
                        "Camp null ha de retornar false"),
                () -> assertFalse(LoansListController.safeContains("Hola Mundo", null),
                        "Query null ha de retornar false")
        );
    }

    /**
     * Prova safeContains amb diferentes casos de cerca.
     */
    @Test
    void safeContains_CasosCerca() {
        assertAll("Proves de cerca case insensitive",
                () -> assertTrue(LoansListController.safeContains("Gabriel García Márquez", "gabriel"),
                        "Ha de trobar al principi"),
                () -> assertTrue(LoansListController.safeContains("Gabriel García Márquez", "márquez"),
                        "Ha de trobar al final"),
                () -> assertTrue(LoansListController.safeContains("Gabriel García Márquez", "GARCÍA"),
                        "Ha de ser case insensitive"),
                () -> assertFalse(LoansListController.safeContains("", "algo"),
                        "Cadena buida no ha de trobar res")
        );
    }

    /**
     * Prova els criteris de cerca per ID Prestec.
     */
    @Test
    void matchesSearchCriteria_IDPrestec_CercaPerID() throws Exception {
        Prestec prestec = new Prestec();
        prestec.setId(123L);

        Method metode = LoansListController.class.getDeclaredMethod("matchesSearchCriteria",
                Prestec.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves de cerca per ID Prestec",
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "123", "ID Prestec"),
                        "Ha de trobar per ID coincident"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "999", "ID Prestec"),
                        "No ha de trobar per ID no coincident"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "12", "ID Prestec"),
                        "Ha de trobar per ID parcial (comportament actual)"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "23", "ID Prestec"),
                        "Ha de trobar per ID parcial en mig")
        );
    }

    /**
     * Prova els criteris de cerca per ID Exemplar.
     */
    @Test
    void matchesSearchCriteria_IDExemplar_CercaPerID() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        exemplar.setId(456L);
        prestec.setExemplar(exemplar);

        Method metode = LoansListController.class.getDeclaredMethod("matchesSearchCriteria",
                Prestec.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves de cerca per ID Exemplar",
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "456", "ID Exemplar"),
                        "Ha de trobar per ID coincident"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "999", "ID Exemplar"),
                        "No ha de trobar per ID no coincident"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "45", "ID Exemplar"),
                        "Ha de trobar per ID parcial")
        );
    }

    /**
     * Prova els criteris de cerca per ID Usuari.
     */
    @Test
    void matchesSearchCriteria_IDUsuari_CercaPerID() throws Exception {
        Prestec prestec = new Prestec();
        User usuari = new User();
        usuari.setId(789L);
        prestec.setUsuari(usuari);

        Method metode = LoansListController.class.getDeclaredMethod("matchesSearchCriteria",
                Prestec.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves de cerca per ID Usuari",
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "789", "ID Usuari"),
                        "Ha de trobar per ID coincident"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "999", "ID Usuari"),
                        "No ha de trobar per ID no coincident"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "78", "ID Usuari"),
                        "Ha de trobar per ID parcial")
        );
    }

    /**
     * Prova els criteris de cerca per Titol.
     */
    @Test
    void matchesSearchCriteria_Titol_CercaPerTitol() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        Llibre llibre = new Llibre();
        llibre.setTitol("El Quijote de la Mancha");
        exemplar.setLlibre(llibre);
        prestec.setExemplar(exemplar);

        Method metode = LoansListController.class.getDeclaredMethod("matchesSearchCriteria",
                Prestec.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves de cerca per Titol",
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "quijote", "Titol"),
                        "Ha de trobar per titol"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "mancha", "Titol"),
                        "Ha de trobar per paraula del titol"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "hamlet", "Titol"),
                        "No ha de trobar quan no coincideix"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "quijote", "ID Prestec"),
                        "No ha de trobar per titol en camp incorrecte")
        );
    }

    /**
     * Prova els criteris de cerca per Usuari.
     */
    @Test
    void matchesSearchCriteria_Usuari_CercaPerNomCognom() throws Exception {
        Prestec prestec = new Prestec();
        User usuari = new User();
        usuari.setNom("Miguel");
        usuari.setCognom1("Cervantes");
        usuari.setNick("cervantes");
        prestec.setUsuari(usuari);

        Method metode = LoansListController.class.getDeclaredMethod("matchesSearchCriteria",
                Prestec.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves de cerca per Usuari",
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "miguel", "Usuari"),
                        "Ha de trobar per nom"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "cervantes", "Usuari"),
                        "Ha de trobar per cognom"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "Miguel Cervantes", "Usuari"),
                        "Ha de trobar per nom complet"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "shakespeare", "Usuari"),
                        "No ha de trobar quan no coincideix")
        );
    }

    /**
     * Prova els criteris per defecte (cerca en tots els camps).
     */
    @Test
    void matchesSearchCriteria_Tots_CercaGlobal() throws Exception {
        Prestec prestec = new Prestec();
        prestec.setId(1L);

        Exemplar exemplar = new Exemplar();
        exemplar.setId(100L);

        Llibre llibre = new Llibre();
        llibre.setTitol("Cien años de soledad");
        exemplar.setLlibre(llibre);
        prestec.setExemplar(exemplar);

        User usuari = new User();
        usuari.setNom("Gabriel");
        usuari.setCognom1("García Márquez");
        usuari.setNick("gabriel");
        prestec.setUsuari(usuari);

        Method metode = LoansListController.class.getDeclaredMethod("matchesSearchCriteria",
                Prestec.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves de cerca global",
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "1", "Tots"),
                        "Ha de trobar per ID Prestec"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "100", "Tots"),
                        "Ha de trobar per ID Exemplar"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "gabriel", "Tots"),
                        "Ha de trobar per nom usuari"),
                () -> assertTrue((boolean) metode.invoke(controller, prestec, "soledad", "Tots"),
                        "Ha de trobar per titol"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "hamlet", "Tots"),
                        "No ha de trobar quan no hi ha coincidencia")
        );
    }

    /**
     * Prova el comportament quan el prestec te camps null.
     */
    @Test
    void matchesSearchCriteria_CampsNull_ComportamentCorrecte() throws Exception {
        Prestec prestec = new Prestec();
        // Sense exemplar ni usuari

        Method metode = LoansListController.class.getDeclaredMethod("matchesSearchCriteria",
                Prestec.class, String.class, String.class);
        metode.setAccessible(true);

        assertAll("Proves amb camps null",
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "1", "ID Exemplar"),
                        "No ha de trobar quan exemplar es null"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "1", "ID Usuari"),
                        "No ha de trobar quan usuari es null"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "titol", "Titol"),
                        "No ha de trobar quan exemplar es null"),
                () -> assertFalse((boolean) metode.invoke(controller, prestec, "nom", "Usuari"),
                        "No ha de trobar quan usuari es null")
        );
    }

    /**
     * Prova la validacio d'ID numerica utilitzada en la cerca.
     */
    @Test
    void idRegex_ValidacioFormatNumerica() {
        assertAll("Proves de regex per validacio ID",
                () -> assertTrue("12345".matches("^\\d+$"), "ID numeric valid"),
                () -> assertTrue("0".matches("^\\d+$"), "ID zero valid"),
                () -> assertFalse("12a34".matches("^\\d+$"), "ID amb lletres invalid"),
                () -> assertFalse("-123".matches("^\\d+$"), "ID negatiu invalid"),
                () -> assertFalse(" 123 ".matches("^\\d+$"), "ID amb espais invalid"),
                () -> assertFalse("".matches("^\\d+$"), "ID buit invalid")
        );
    }

}