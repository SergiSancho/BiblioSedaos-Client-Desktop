package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.model.Autor;
import com.bibliosedaos.desktop.model.Exemplar;
import com.bibliosedaos.desktop.model.Llibre;
import com.bibliosedaos.desktop.model.Prestec;
import com.bibliosedaos.desktop.service.PrestecService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a MyLoansController.
 * Comprova la configuracio de les columnes de la taula i el comportament amb dades null.
 */
@ExtendWith(MockitoExtension.class)
class MyLoansControllerTest {

    @Mock
    private PrestecService prestecService;

    private MyLoansController controller;

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
        controller = new MyLoansController(prestecService);
    }

    /**
     * Prova que getTitolValue retorna el titol quan totes les dependencies estan presents.
     */
    @Test
    void getTitolValue_QuanTitolPresent_RetornaTitol() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        Llibre llibre = new Llibre();
        llibre.setTitol("El Quijote");
        exemplar.setLlibre(llibre);
        prestec.setExemplar(exemplar);

        Method metode = MyLoansController.class.getDeclaredMethod("getTitolValue", TableColumn.CellDataFeatures.class);
        metode.setAccessible(true);

        @SuppressWarnings("unchecked")
        TableColumn.CellDataFeatures<Prestec, String> celda =
                new TableColumn.CellDataFeatures<>(null, null, prestec);

        ReadOnlyStringWrapper resultat = (ReadOnlyStringWrapper) metode.invoke(controller, celda);
        assertEquals("El Quijote", resultat.get());
    }

    /**
     * Prova que getTitolValue retorna cadena buida quan el llibre es null.
     */
    @Test
    void getTitolValue_QuanLlibreNull_RetornaBuit() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        // Sense llibre
        prestec.setExemplar(exemplar);

        Method metode = MyLoansController.class.getDeclaredMethod("getTitolValue", TableColumn.CellDataFeatures.class);
        metode.setAccessible(true);

        @SuppressWarnings("unchecked")
        TableColumn.CellDataFeatures<Prestec, String> celda =
                new TableColumn.CellDataFeatures<>(null, null, prestec);

        ReadOnlyStringWrapper resultat = (ReadOnlyStringWrapper) metode.invoke(controller, celda);
        assertEquals("", resultat.get());
    }

    /**
     * Prova que getAutorValue retorna el nom de l'autor quan tot esta present.
     */
    @Test
    void getAutorValue_QuanAutorPresent_RetornaNomAutor() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        Llibre llibre = new Llibre();
        Autor autor = new Autor();
        autor.setNom("Miguel de Cervantes");
        llibre.setAutor(autor);
        exemplar.setLlibre(llibre);
        prestec.setExemplar(exemplar);

        Method metode = MyLoansController.class.getDeclaredMethod("getAutorValue", TableColumn.CellDataFeatures.class);
        metode.setAccessible(true);

        @SuppressWarnings("unchecked")
        TableColumn.CellDataFeatures<Prestec, String> celda =
                new TableColumn.CellDataFeatures<>(null, null, prestec);

        ReadOnlyStringWrapper resultat = (ReadOnlyStringWrapper) metode.invoke(controller, celda);
        assertEquals("Miguel de Cervantes", resultat.get());
    }

    /**
     * Prova que getAutorValue retorna cadena buida quan l'autor es null.
     */
    @Test
    void getAutorValue_QuanAutorNull_RetornaBuit() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        Llibre llibre = new Llibre();
        // Sense autor
        exemplar.setLlibre(llibre);
        prestec.setExemplar(exemplar);

        Method metode = MyLoansController.class.getDeclaredMethod("getAutorValue", TableColumn.CellDataFeatures.class);
        metode.setAccessible(true);

        @SuppressWarnings("unchecked")
        TableColumn.CellDataFeatures<Prestec, String> celda =
                new TableColumn.CellDataFeatures<>(null, null, prestec);

        ReadOnlyStringWrapper resultat = (ReadOnlyStringWrapper) metode.invoke(controller, celda);
        assertEquals("", resultat.get());
    }

    /**
     * Prova que getLlocValue retorna el lloc quan esta present.
     */
    @Test
    void getLlocValue_QuanLlocPresent_RetornaLloc() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        exemplar.setLloc("Estanteria A-5");
        prestec.setExemplar(exemplar);

        Method metode = MyLoansController.class.getDeclaredMethod("getLlocValue", TableColumn.CellDataFeatures.class);
        metode.setAccessible(true);

        @SuppressWarnings("unchecked")
        TableColumn.CellDataFeatures<Prestec, String> celda =
                new TableColumn.CellDataFeatures<>(null, null, prestec);

        ReadOnlyStringWrapper resultat = (ReadOnlyStringWrapper) metode.invoke(controller, celda);
        assertEquals("Estanteria A-5", resultat.get());
    }

    /**
     * Prova que getLlocValue retorna cadena buida quan el lloc es null.
     */
    @Test
    void getLlocValue_QuanLlocNull_RetornaBuit() throws Exception {
        Prestec prestec = new Prestec();
        Exemplar exemplar = new Exemplar();
        // Sense lloc
        prestec.setExemplar(exemplar);

        Method metode = MyLoansController.class.getDeclaredMethod("getLlocValue", TableColumn.CellDataFeatures.class);
        metode.setAccessible(true);

        @SuppressWarnings("unchecked")
        TableColumn.CellDataFeatures<Prestec, String> celda =
                new TableColumn.CellDataFeatures<>(null, null, prestec);

        ReadOnlyStringWrapper resultat = (ReadOnlyStringWrapper) metode.invoke(controller, celda);
        assertEquals("", resultat.get());
    }
}