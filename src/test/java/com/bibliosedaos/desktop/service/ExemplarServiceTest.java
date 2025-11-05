package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ExemplarApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Exemplar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a ExemplarService.
 *
 * Assegura que les operacions d'exemplars es delegen a ExemplarApi
 * i les excepcions es propaguen correctament.
 */
@ExtendWith(MockitoExtension.class)
class ExemplarServiceTest {

    @Mock
    private ExemplarApi exemplarApi;

    private ExemplarService exemplarService;

    /**
     * Setup: crea el servei amb el mock.
     */
    @BeforeEach
    void setUp() {
        exemplarService = new ExemplarService(exemplarApi);
    }

    /**
     * Test: constructor ha de llençar NullPointerException si l'api es null.
     */
    @Test
    void constructor_WhenExemplarApiIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ExemplarService(null));
    }

    /**
     * Test: getAllExemplars delega i retorna llista.
     */
    @Test
    void getAllExemplars_WhenCalled_ReturnsListOfExemplars() throws ApiException {
        when(exemplarApi.getAllExemplars()).thenReturn(List.of());

        List<Exemplar> exemplars = exemplarService.getAllExemplars();

        assertNotNull(exemplars);
        verify(exemplarApi).getAllExemplars();
    }

    /**
     * Test: getExemplarsLliures delega i retorna llista.
     */
    @Test
    void getExemplarsLliures_WhenCalled_ReturnsListOfFreeExemplars() throws ApiException {
        when(exemplarApi.getExemplarsLliures()).thenReturn(List.of());

        List<Exemplar> exemplars = exemplarService.getExemplarsLliures();

        assertNotNull(exemplars);
        verify(exemplarApi).getExemplarsLliures();
    }

    /**
     * Test: findExemplarsByTitol delega a l'API.
     */
    @Test
    void findExemplarsByTitol_WhenTitolExists_ReturnsListOfExemplars() throws ApiException {
        String titol = "titol";
        when(exemplarApi.findExemplarsLliuresByTitol(titol)).thenReturn(List.of());

        List<Exemplar> exemplars = exemplarService.findExemplarsByTitol(titol);

        assertNotNull(exemplars);
        verify(exemplarApi).findExemplarsLliuresByTitol(titol);
    }

    /**
     * Test: findExemplarsByAutor delega a l'API.
     */
    @Test
    void findExemplarsByAutor_WhenAutorExists_ReturnsListOfExemplars() throws ApiException {
        String autor = "autor";
        when(exemplarApi.findExemplarsLliuresByAutorNom(autor)).thenReturn(List.of());

        List<Exemplar> exemplars = exemplarService.findExemplarsByAutor(autor);

        assertNotNull(exemplars);
        verify(exemplarApi).findExemplarsLliuresByAutorNom(autor);
    }

    /**
     * Test: createExemplar delega i retorna exemplar creat.
     */
    @Test
    void createExemplar_WhenExemplarIsValid_ReturnsCreatedExemplar() throws ApiException {
        Exemplar exemplarToCreate = new Exemplar();
        Exemplar createdExemplar = new Exemplar();
        createdExemplar.setId(1L);
        when(exemplarApi.createExemplar(exemplarToCreate)).thenReturn(createdExemplar);

        Exemplar result = exemplarService.createExemplar(exemplarToCreate);

        assertSame(createdExemplar, result);
        verify(exemplarApi).createExemplar(exemplarToCreate);
    }

    /**
     * Test: updateExemplar delega i retorna exemplar actualitzat.
     */
    @Test
    void updateExemplar_WhenExemplarIsValid_ReturnsUpdatedExemplar() throws ApiException {
        Long exemplarId = 1L;
        Exemplar exemplarToUpdate = new Exemplar();
        Exemplar updatedExemplar = new Exemplar();
        updatedExemplar.setId(exemplarId);
        when(exemplarApi.updateExemplar(exemplarId, exemplarToUpdate)).thenReturn(updatedExemplar);

        Exemplar result = exemplarService.updateExemplar(exemplarId, exemplarToUpdate);

        assertSame(updatedExemplar, result);
        verify(exemplarApi).updateExemplar(exemplarId, exemplarToUpdate);
    }

    /**
     * Test: deleteExemplar delega a l'API.
     */
    @Test
    void deleteExemplar_WhenExemplarExists_DeletesExemplar() throws ApiException {
        Long exemplarId = 1L;
        doNothing().when(exemplarApi).deleteExemplar(exemplarId);

        exemplarService.deleteExemplar(exemplarId);

        verify(exemplarApi).deleteExemplar(exemplarId);
    }

    /**
     * Test: deleteExemplar propaga ApiException si no existeix.
     */
    @Test
    void deleteExemplar_WhenExemplarDoesNotExist_ThrowsApiException() throws ApiException {
        Long exemplarId = 999L;
        doThrow(new ApiException("Exemplar no trobat")).when(exemplarApi).deleteExemplar(exemplarId);

        assertThrows(ApiException.class, () -> exemplarService.deleteExemplar(exemplarId));
    }

    /**
     * Test: getExemplarById delega i retorna l'exemplar.
     */
    @Test
    void getExemplarById_WhenExemplarExists_ReturnsExemplar() throws ApiException {
        Long exemplarId = 1L;
        Exemplar expectedExemplar = new Exemplar();
        expectedExemplar.setId(exemplarId);
        when(exemplarApi.findExemplarById(exemplarId)).thenReturn(expectedExemplar);

        Exemplar actualExemplar = exemplarService.getExemplarById(exemplarId);

        assertSame(expectedExemplar, actualExemplar);
        verify(exemplarApi).findExemplarById(exemplarId);
    }
}