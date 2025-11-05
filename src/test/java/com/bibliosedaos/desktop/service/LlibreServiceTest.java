package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.LlibreApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Llibre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

        /**
 * Proves unitaries per a LlibreService.
 *
 * Comprova la delegació de les operacions CRUD a LlibreApi
 * i el maneig d'excepcions.
 */
@ExtendWith(MockitoExtension.class)
class LlibreServiceTest {

    @Mock
    private LlibreApi llibreApi;

    private LlibreService llibreService;

    /**
     * Setup: crea el servei amb el mock.
     */
    @BeforeEach
    void setUp() {
        llibreService = new LlibreService(llibreApi);
    }

    /**
     * Test: constructor ha de llençar NullPointerException si l'api es null.
     */
    @Test
    void constructor_WhenLlibreApiIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LlibreService(null));
    }

    /**
     * Test: getAllBooks delega a l'API i retorna llista.
     */
    @Test
    void getAllBooks_WhenCalled_ReturnsListOfBooks() throws ApiException {
        when(llibreApi.getAllLlibres()).thenReturn(List.of());

        List<Llibre> books = llibreService.getAllBooks();

        assertNotNull(books);
        verify(llibreApi).getAllLlibres();
    }

    /**
     * Test: getBookById delega i retorna llibre existent.
     */
    @Test
    void getBookById_WhenBookExists_ReturnsBook() throws ApiException {
        Long bookId = 1L;
        Llibre expectedBook = new Llibre();
        expectedBook.setId(bookId);
        when(llibreApi.findLlibreById(bookId)).thenReturn(expectedBook);

        Llibre actualBook = llibreService.getBookById(bookId);

        assertSame(expectedBook, actualBook);
        verify(llibreApi).findLlibreById(bookId);
    }

    /**
     * Test: createBook delega i retorna llibre creat.
     */
    @Test
    void createBook_WhenBookIsValid_ReturnsCreatedBook() throws ApiException {
        Llibre bookToCreate = new Llibre();
        Llibre createdBook = new Llibre();
        createdBook.setId(1L);
        when(llibreApi.createLlibre(bookToCreate)).thenReturn(createdBook);

        Llibre result = llibreService.createBook(bookToCreate);

        assertSame(createdBook, result);
        verify(llibreApi).createLlibre(bookToCreate);
    }

    /**
     * Test: updateBook delega i retorna llibre actualitzat.
     */
    @Test
    void updateBook_WhenBookIsValid_ReturnsUpdatedBook() throws ApiException {
        Long bookId = 1L;
        Llibre bookToUpdate = new Llibre();
        Llibre updatedBook = new Llibre();
        updatedBook.setId(bookId);
        when(llibreApi.updateLlibre(bookId, bookToUpdate)).thenReturn(updatedBook);

        Llibre result = llibreService.updateBook(bookId, bookToUpdate);

        assertSame(updatedBook, result);
        verify(llibreApi).updateLlibre(bookId, bookToUpdate);
    }

    /**
     * Test: deleteBook delega a l'API.
     */
    @Test
    void deleteBook_WhenBookExists_DeletesBook() throws ApiException {
        Long bookId = 1L;
        doNothing().when(llibreApi).deleteLlibre(bookId);

        llibreService.deleteBook(bookId);

        verify(llibreApi).deleteLlibre(bookId);
    }

    /**
     * Test: deleteBook propaga ApiException si no existeix.
     */
    @Test
    void deleteBook_WhenBookDoesNotExist_ThrowsApiException() throws ApiException {
        Long bookId = 999L;
        doThrow(new ApiException("Llibre no trobat")).when(llibreApi).deleteLlibre(bookId);

        assertThrows(ApiException.class, () -> llibreService.deleteBook(bookId));
    }
}


