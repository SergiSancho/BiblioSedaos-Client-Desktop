package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.AutorApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Autor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a AutorService.
 *
 * Verifica la delegació de les operacions d'autors a AutorApi
 * i la propagació d'excepcions.
 */
@ExtendWith(MockitoExtension.class)
class AutorServiceTest {

    @Mock
    private AutorApi autorApi;

    private AutorService autorService;

    /**
     * Setup: crea el servei amb el mock.
     */
    @BeforeEach
    void setUp() {
        autorService = new AutorService(autorApi);
    }

    /**
     * Test: constructor ha de llençar NullPointerException si l'api es null.
     */
    @Test
    void constructor_WhenAutorApiIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AutorService(null));
    }

    /**
     * Test: getAllAutors delega i retorna llista.
     */
    @Test
    void getAllAutors_WhenCalled_ReturnsListOfAutors() throws ApiException {
        when(autorApi.getAllAutors()).thenReturn(List.of());

        List<Autor> autors = autorService.getAllAutors();

        assertNotNull(autors);
        verify(autorApi).getAllAutors();
    }

    /**
     * Test: createAutor delega i retorna autor creat.
     */
    @Test
    void createAutor_WhenAutorIsValid_ReturnsCreatedAutor() throws ApiException {
        Autor autorToCreate = new Autor();
        Autor createdAutor = new Autor();
        createdAutor.setId(1L);
        when(autorApi.createAutor(autorToCreate)).thenReturn(createdAutor);

        Autor result = autorService.createAutor(autorToCreate);

        assertSame(createdAutor, result);
        verify(autorApi).createAutor(autorToCreate);
    }

    /**
     * Test: deleteAutor delega a l'API.
     */
    @Test
    void deleteAutor_WhenAutorExists_DeletesAutor() throws ApiException {
        Long autorId = 1L;
        doNothing().when(autorApi).deleteAutor(autorId);

        autorService.deleteAutor(autorId);

        verify(autorApi).deleteAutor(autorId);
    }

    /**
     * Test: deleteAutor propaga ApiException si no existeix.
     */
    @Test
    void deleteAutor_WhenAutorDoesNotExist_ThrowsApiException() throws ApiException {
        Long autorId = 999L;
        doThrow(new ApiException("Autor no trobat")).when(autorApi).deleteAutor(autorId);

        assertThrows(ApiException.class, () -> autorService.deleteAutor(autorId));
    }

    /**
     * Test: getAutorById delega i retorna l'autor.
     */
    @Test
    void getAutorById_WhenAutorExists_ReturnsAutor() throws ApiException {
        Long autorId = 1L;
        Autor expectedAutor = new Autor();
        expectedAutor.setId(autorId);
        when(autorApi.findAutorById(autorId)).thenReturn(expectedAutor);

        Autor actualAutor = autorService.getAutorById(autorId);

        assertSame(expectedAutor, actualAutor);
        verify(autorApi).findAutorById(autorId);
    }
}