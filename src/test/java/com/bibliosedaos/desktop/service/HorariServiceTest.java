package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.HorariApi;
import com.bibliosedaos.desktop.model.Horari;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a HorariService.
 *
 * Verifica que les operacions d'horaris es delegen correctament a l'API
 * i que les excepcions es propaguen adequadament.
 */
@ExtendWith(MockitoExtension.class)
class HorariServiceTest {

    @Mock
    private HorariApi horariApi;

    private HorariService horariService;

    /**
     * Configuracio abans de cada test: crea el servei amb el mock.
     */
    @BeforeEach
    void setUp() {
        horariService = new HorariService(horariApi);
    }

    /**
     * Test: constructor ha de llençar NullPointerException si horariApi es null.
     */
    @Test
    void constructor_WhenHorariApiIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HorariService(null));
    }

    /**
     * Test: getAllHoraris retorna la llista d'horaris quan existeixen.
     */
    @Test
    void getAllHoraris_WhenHorarisExist_ReturnsListOfHoraris() throws ApiException {
        List<Horari> expectedHoraris = List.of(new Horari(), new Horari());
        when(horariApi.getAllHoraris()).thenReturn(expectedHoraris);

        List<Horari> actualHoraris = horariService.getAllHoraris();

        assertSame(expectedHoraris, actualHoraris);
        verify(horariApi).getAllHoraris();
    }

    /**
     * Test: getAllHoraris propaga ApiException quan l'API la llença.
     */
    @Test
    void getAllHoraris_WhenApiFails_ThrowsApiException() throws ApiException {
        when(horariApi.getAllHoraris()).thenThrow(new ApiException("Error de connexio"));

        assertThrows(ApiException.class, () -> horariService.getAllHoraris());
    }

    /**
     * Test: getAllHoraris retorna llista buida quan no hi ha horaris.
     */
    @Test
    void getAllHoraris_WhenNoHorarisExist_ReturnsEmptyList() throws ApiException {
        when(horariApi.getAllHoraris()).thenReturn(List.of());

        List<Horari> actualHoraris = horariService.getAllHoraris();

        assertNotNull(actualHoraris);
        assertTrue(actualHoraris.isEmpty());
        verify(horariApi).getAllHoraris();
    }

    /**
     * Test: createHorari delega i retorna l'horari creat.
     */
    @Test
    void createHorari_WhenHorariIsValid_ReturnsCreatedHorari() throws ApiException {
        Horari horariToCreate = new Horari();
        Horari createdHorari = new Horari();
        createdHorari.setId(1L);
        when(horariApi.createHorari(horariToCreate)).thenReturn(createdHorari);

        Horari result = horariService.createHorari(horariToCreate);

        assertSame(createdHorari, result);
        verify(horariApi).createHorari(horariToCreate);
    }

    /**
     * Test: createHorari propaga ApiException quan hi ha errors de validacio.
     */
    @Test
    void createHorari_WhenValidationFails_ThrowsApiException() throws ApiException {
        Horari horariToCreate = new Horari();
        when(horariApi.createHorari(horariToCreate))
                .thenThrow(new ApiException("Dades d'horari invalides"));

        assertThrows(ApiException.class, () -> horariService.createHorari(horariToCreate));
    }

    /**
     * Test: createHorari amb horari null delega correctament.
     */
    @Test
    void createHorari_WhenHorariIsNull_DelegatesCorrectly() throws ApiException {
        Horari createdHorari = new Horari();
        when(horariApi.createHorari(null)).thenReturn(createdHorari);

        Horari result = horariService.createHorari(null);

        assertSame(createdHorari, result);
        verify(horariApi).createHorari(null);
    }

    /**
     * Test: createHorari propaga altres tipus d'excepcions.
     */
    @Test
    void createHorari_WhenApiThrowsRuntimeException_PropagatesException() throws ApiException {
        Horari horariToCreate = new Horari();
        when(horariApi.createHorari(horariToCreate))
                .thenThrow(new RuntimeException("Error inesperat"));

        assertThrows(RuntimeException.class, () -> horariService.createHorari(horariToCreate));
    }

    /**
     * Test: servei es crea correctament amb dependencia valida.
     */
    @Test
    void constructor_WhenHorariApiIsValid_CreatesService() {
        assertNotNull(horariService);
    }

    /**
     * Test: mètodes criden l'API exactament una vegada.
     */
    @Test
    void allMethods_CallApiExactlyOnce() throws ApiException {
        when(horariApi.getAllHoraris()).thenReturn(List.of());

        Horari horari = new Horari();
        when(horariApi.createHorari(horari)).thenReturn(horari);

        horariService.getAllHoraris();
        horariService.createHorari(horari);

        verify(horariApi, times(1)).getAllHoraris();
        verify(horariApi, times(1)).createHorari(horari);
    }

    /**
     * Test: servei maneja correctament llista null de l'API.
     */
    @Test
    void getAllHoraris_WhenApiReturnsNull_ReturnsNull() throws ApiException {
        when(horariApi.getAllHoraris()).thenReturn(null);

        List<Horari> result = horariService.getAllHoraris();

        assertNull(result);
        verify(horariApi).getAllHoraris();
    }

    /**
     * Test: createHorari amb horari buit es delega correctament.
     */
    @Test
    void createHorari_WithEmptyHorari_DelegatesCorrectly() throws ApiException {
        Horari emptyHorari = new Horari();
        Horari createdHorari = new Horari();
        createdHorari.setId(1L);
        when(horariApi.createHorari(emptyHorari)).thenReturn(createdHorari);

        Horari result = horariService.createHorari(emptyHorari);

        assertSame(createdHorari, result);
        verify(horariApi).createHorari(emptyHorari);
    }
}