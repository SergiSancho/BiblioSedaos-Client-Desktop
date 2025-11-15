package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.PrestecApi;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Prestec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a PrestecService.
 *
 * Verifica que les operacions de prestecs es delegen correctament a l'API
 * i que les excepcions es propaguen adequadament.
 */
@ExtendWith(MockitoExtension.class)
class PrestecServiceTest {

    @Mock
    private PrestecApi prestecApi;

    private PrestecService prestecService;

    /**
     * Configuracio abans de cada test: crea el servei amb el mock.
     */
    @BeforeEach
    void setUp() {
        prestecService = new PrestecService(prestecApi);
    }

    /**
     * Test: constructor ha de llençar NullPointerException si prestecApi es null.
     */
    @Test
    void constructor_WhenPrestecApiIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PrestecService(null));
    }

    /**
     * Test: getAllPrestecs retorna la llista de prestecs quan existeixen.
     */
    @Test
    void getAllPrestecs_WhenPrestecsExist_ReturnsListOfPrestecs() throws ApiException {
        Long usuariId = 1L;
        List<Prestec> expectedPrestecs = List.of(new Prestec(), new Prestec());
        when(prestecApi.getAllPrestecs(usuariId)).thenReturn(expectedPrestecs);

        List<Prestec> actualPrestecs = prestecService.getAllPrestecs(usuariId);

        assertSame(expectedPrestecs, actualPrestecs);
        verify(prestecApi).getAllPrestecs(usuariId);
    }

    /**
     * Test: getAllPrestecs amb usuariId null delega correctament.
     */
    @Test
    void getAllPrestecs_WhenUsuariIdIsNull_DelegatesCorrectly() throws ApiException {
        List<Prestec> expectedPrestecs = List.of(new Prestec());
        when(prestecApi.getAllPrestecs(null)).thenReturn(expectedPrestecs);

        List<Prestec> actualPrestecs = prestecService.getAllPrestecs(null);

        assertSame(expectedPrestecs, actualPrestecs);
        verify(prestecApi).getAllPrestecs(null);
    }

    /**
     * Test: getAllPrestecs propaga ApiException quan l'API la llença.
     */
    @Test
    void getAllPrestecs_WhenApiFails_ThrowsApiException() throws ApiException {
        Long usuariId = 1L;
        when(prestecApi.getAllPrestecs(usuariId)).thenThrow(new ApiException("Error de connexio"));

        assertThrows(ApiException.class, () -> prestecService.getAllPrestecs(usuariId));
    }

    /**
     * Test: getPrestecsActius retorna la llista de prestecs actius.
     */
    @Test
    void getPrestecsActius_WhenActivePrestecsExist_ReturnsListOfActivePrestecs() throws ApiException {
        Long usuariId = 1L;
        List<Prestec> expectedPrestecs = List.of(new Prestec(), new Prestec());
        when(prestecApi.getPrestecsActius(usuariId)).thenReturn(expectedPrestecs);

        List<Prestec> actualPrestecs = prestecService.getPrestecsActius(usuariId);

        assertSame(expectedPrestecs, actualPrestecs);
        verify(prestecApi).getPrestecsActius(usuariId);
    }

    /**
     * Test: getPrestecsActius amb usuariId null delega correctament.
     */
    @Test
    void getPrestecsActius_WhenUsuariIdIsNull_DelegatesCorrectly() throws ApiException {
        List<Prestec> expectedPrestecs = List.of(new Prestec());
        when(prestecApi.getPrestecsActius(null)).thenReturn(expectedPrestecs);

        List<Prestec> actualPrestecs = prestecService.getPrestecsActius(null);

        assertSame(expectedPrestecs, actualPrestecs);
        verify(prestecApi).getPrestecsActius(null);
    }

    /**
     * Test: getPrestecsActius propaga ApiException quan l'API la llença.
     */
    @Test
    void getPrestecsActius_WhenApiFails_ThrowsApiException() throws ApiException {
        Long usuariId = 1L;
        when(prestecApi.getPrestecsActius(usuariId)).thenThrow(new ApiException("Error de permisos"));

        assertThrows(ApiException.class, () -> prestecService.getPrestecsActius(usuariId));
    }

    /**
     * Test: createPrestec delega i retorna el prestec creat.
     */
    @Test
    void createPrestec_WhenPrestecIsValid_ReturnsCreatedPrestec() throws ApiException {
        Prestec prestecToCreate = new Prestec();
        Prestec createdPrestec = new Prestec();
        createdPrestec.setId(1L);
        when(prestecApi.createPrestec(prestecToCreate)).thenReturn(createdPrestec);

        Prestec result = prestecService.createPrestec(prestecToCreate);

        assertSame(createdPrestec, result);
        verify(prestecApi).createPrestec(prestecToCreate);
    }

    /**
     * Test: createPrestec propaga ApiException quan hi ha errors de validacio.
     */
    @Test
    void createPrestec_WhenValidationFails_ThrowsApiException() throws ApiException {
        Prestec prestecToCreate = new Prestec();
        when(prestecApi.createPrestec(prestecToCreate))
                .thenThrow(new ApiException("Dades de prestec invalides"));

        assertThrows(ApiException.class, () -> prestecService.createPrestec(prestecToCreate));
    }

    /**
     * Test: retornarPrestec delega correctament a l'API.
     */
    @Test
    void retornarPrestec_WhenPrestecExists_DelegatesToApi() throws ApiException {
        Long prestecId = 1L;
        doNothing().when(prestecApi).retornarPrestec(prestecId);

        prestecService.retornarPrestec(prestecId);

        verify(prestecApi).retornarPrestec(prestecId);
    }

    /**
     * Test: retornarPrestec propaga ApiException quan el prestec no es troba.
     */
    @Test
    void retornarPrestec_WhenPrestecDoesNotExist_ThrowsApiException() throws ApiException {
        Long prestecId = 999L;
        doThrow(new ApiException("Prestec no trobat")).when(prestecApi).retornarPrestec(prestecId);

        assertThrows(ApiException.class, () -> prestecService.retornarPrestec(prestecId));
    }

    /**
     * Test: getPrestecById retorna el prestec quan existeix.
     */
    @Test
    void getPrestecById_WhenPrestecExists_ReturnsPrestec() throws ApiException {
        Long prestecId = 1L;
        Prestec expectedPrestec = new Prestec();
        expectedPrestec.setId(prestecId);
        when(prestecApi.getPrestecById(prestecId)).thenReturn(expectedPrestec);

        Prestec actualPrestec = prestecService.getPrestecById(prestecId);

        assertSame(expectedPrestec, actualPrestec);
        verify(prestecApi).getPrestecById(prestecId);
    }

    /**
     * Test: getPrestecById propaga ApiException quan el prestec no es troba.
     */
    @Test
    void getPrestecById_WhenPrestecDoesNotExist_ThrowsApiException() throws ApiException {
        Long prestecId = 999L;
        when(prestecApi.getPrestecById(prestecId)).thenThrow(new ApiException("Prestec no trobat"));

        assertThrows(ApiException.class, () -> prestecService.getPrestecById(prestecId));
    }

    /**
     * Test: getPrestecById amb ID zero delega correctament.
     */
    @Test
    void getPrestecById_WhenIdIsZero_DelegatesCorrectly() throws ApiException {
        Long prestecId = 0L;
        Prestec expectedPrestec = new Prestec();
        when(prestecApi.getPrestecById(prestecId)).thenReturn(expectedPrestec);

        Prestec actualPrestec = prestecService.getPrestecById(prestecId);

        assertSame(expectedPrestec, actualPrestec);
        verify(prestecApi).getPrestecById(prestecId);
    }
}