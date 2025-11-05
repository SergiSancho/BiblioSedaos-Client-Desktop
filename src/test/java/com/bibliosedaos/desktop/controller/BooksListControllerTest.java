package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.model.Autor;
import com.bibliosedaos.desktop.model.Llibre;
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
 * Proves unitaries per a BooksListController.
 *
 * Comprova el comportament segur davant valors nuls i case-insensitive de safeContains
 * i verifica la regla de format per a l'ID (nomes digits).
 */
@ExtendWith(MockitoExtension.class)
class BooksListControllerTest {

    @Mock
    private LlibreService llibreService;

    @Mock
    private ExemplarService exemplarService;

    @Mock
    private Navigator navigator;

    private BooksListController controller;

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
        controller = new BooksListController(llibreService, exemplarService, navigator);
    }

    /**
     * Prova casos essencials de safeContains: null-safe, case-insensitive i query buit.
     */
    @Test
    void safeContains_CasosEssencials() {
        assertTrue(BooksListController.safeContains("Hola Mundo", "mundo"));
        assertTrue(BooksListController.safeContains("Hola Mundo", ""));
        assertFalse(BooksListController.safeContains("Hola Mundo", "adios"));
        assertFalse(BooksListController.safeContains(null, "x"));
        assertFalse(BooksListController.safeContains("Hola Mundo", null));
    }

    /**
     * Prova safeContains aplicat a camps tipics d'un llibre (isbn, titol, autor).
     */
    @Test
    void safeContains_EntreCampsDeLlibre() {
        Llibre llibre = new Llibre();
        llibre.setIsbn("978-3-16-148410-0");
        llibre.setTitol("La recerca del temps perdut");
        llibre.setEditorial("Editorial Exemple");

        Autor autor = new Autor();
        autor.setNom("Joan Perez");
        llibre.setAutor(autor);

        assertTrue(BooksListController.safeContains(llibre.getIsbn(), "978"));
        assertTrue(BooksListController.safeContains(llibre.getTitol(), "recerca"));
        assertTrue(BooksListController.safeContains(llibre.getEditorial(), "exemple"));
        assertTrue(BooksListController.safeContains(llibre.getAutor().getNom(), "joan"));
    }

    /**
     * Verifica la regla de validacio d'ID usada en onSearchById (nomes digits).
     * Aquesta prova comprova la regex que fa la validacio sin executar UI.
     */
    @Test
    void searchById_InputValidation_Regex() {
        assertTrue("12345".matches("^\\d+$"));
        assertTrue("0".matches("^\\d+$"));
        assertFalse("12a34".matches("^\\d+$"));
        assertFalse("-123".matches("^\\d+$"));
        assertFalse(" 123 ".matches("^\\d+$"));
        assertFalse("".matches("^\\d+$"));
    }
}

