package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.api.GrupApi;
import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitaries per a GrupService.
 *
 * Verifica que les operacions de grups es delegen correctament a l'API
 * i que les excepcions es propaguen adequadament.
 */
@ExtendWith(MockitoExtension.class)
class GrupServiceTest {

    @Mock
    private GrupApi grupApi;

    private GrupService grupService;

    /**
     * Configuracio abans de cada test: crea el servei amb el mock.
     */
    @BeforeEach
    void setUp() {
        grupService = new GrupService(grupApi);
    }

    /**
     * Test: constructor ha de llençar NullPointerException si grupApi es null.
     */
    @Test
    void constructor_WhenGrupApiIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GrupService(null));
    }

    /**
     * Test: getAllGrups retorna la llista de grups quan existeixen.
     */
    @Test
    void getAllGrups_WhenGrupsExist_ReturnsListOfGrups() throws ApiException {
        List<Grup> expectedGrups = List.of(new Grup(), new Grup());
        when(grupApi.getAllGrups()).thenReturn(expectedGrups);

        List<Grup> actualGrups = grupService.getAllGrups();

        assertSame(expectedGrups, actualGrups);
        verify(grupApi).getAllGrups();
    }

    /**
     * Test: getAllGrups propaga ApiException quan l'API la llença.
     */
    @Test
    void getAllGrups_WhenApiFails_ThrowsApiException() throws ApiException {
        when(grupApi.getAllGrups()).thenThrow(new ApiException("Error de connexio"));

        assertThrows(ApiException.class, () -> grupService.getAllGrups());
    }

    /**
     * Test: createGrup delega i retorna el grup creat.
     */
    @Test
    void createGrup_WhenGrupIsValid_ReturnsCreatedGrup() throws ApiException {
        Grup grupToCreate = new Grup();
        Grup createdGrup = new Grup();
        createdGrup.setId(1L);
        when(grupApi.createGrup(grupToCreate)).thenReturn(createdGrup);

        Grup result = grupService.createGrup(grupToCreate);

        assertSame(createdGrup, result);
        verify(grupApi).createGrup(grupToCreate);
    }

    /**
     * Test: createGrup propaga ApiException quan hi ha errors de validacio.
     */
    @Test
    void createGrup_WhenValidationFails_ThrowsApiException() throws ApiException {
        Grup grupToCreate = new Grup();
        when(grupApi.createGrup(grupToCreate))
                .thenThrow(new ApiException("Dades de grup invalides"));

        assertThrows(ApiException.class, () -> grupService.createGrup(grupToCreate));
    }

    /**
     * Test: deleteGrup delega correctament a l'API.
     */
    @Test
    void deleteGrup_WhenGrupExists_DelegatesToApi() throws ApiException {
        Long grupId = 1L;
        doNothing().when(grupApi).deleteGrup(grupId);

        grupService.deleteGrup(grupId);

        verify(grupApi).deleteGrup(grupId);
    }

    /**
     * Test: deleteGrup propaga ApiException quan el grup no es troba.
     */
    @Test
    void deleteGrup_WhenGrupDoesNotExist_ThrowsApiException() throws ApiException {
        Long grupId = 999L;
        doThrow(new ApiException("Grup no trobat")).when(grupApi).deleteGrup(grupId);

        assertThrows(ApiException.class, () -> grupService.deleteGrup(grupId));
    }

    /**
     * Test: joinGrup delega i retorna el grup actualitzat.
     */
    @Test
    void joinGrup_WhenGrupAndUserExist_ReturnsUpdatedGrup() throws ApiException {
        Long grupId = 1L;
        Long userId = 1L;
        Grup updatedGrup = new Grup();
        updatedGrup.setId(grupId);
        when(grupApi.afegirUsuariGrup(grupId, userId)).thenReturn(updatedGrup);

        Grup result = grupService.joinGrup(grupId, userId);

        assertSame(updatedGrup, result);
        verify(grupApi).afegirUsuariGrup(grupId, userId);
    }

    /**
     * Test: joinGrup propaga ApiException quan no es pot afegir l'usuari.
     */
    @Test
    void joinGrup_WhenUserCannotBeAdded_ThrowsApiException() throws ApiException {
        Long grupId = 1L;
        Long userId = 1L;
        when(grupApi.afegirUsuariGrup(grupId, userId))
                .thenThrow(new ApiException("No s'ha pogut afegir l'usuari"));

        assertThrows(ApiException.class, () -> grupService.joinGrup(grupId, userId));
    }

    /**
     * Test: getMembres delega i retorna la llista de membres.
     */
    @Test
    void getMembres_WhenGrupExists_ReturnsListOfMembers() throws ApiException {
        Long grupId = 1L;
        List<User> expectedMembers = List.of(new User(), new User());
        when(grupApi.getMembresGrup(grupId)).thenReturn(expectedMembers);

        List<User> actualMembers = grupService.getMembres(grupId);

        assertSame(expectedMembers, actualMembers);
        verify(grupApi).getMembresGrup(grupId);
    }

    /**
     * Test: getMembres propaga ApiException quan el grup no es troba.
     */
    @Test
    void getMembres_WhenGrupDoesNotExist_ThrowsApiException() throws ApiException {
        Long grupId = 999L;
        when(grupApi.getMembresGrup(grupId)).thenThrow(new ApiException("Grup no trobat"));

        assertThrows(ApiException.class, () -> grupService.getMembres(grupId));
    }

    /**
     * Test: getMembres amb grupId null delega correctament.
     */
    @Test
    void getMembres_WhenGrupIdIsNull_DelegatesCorrectly() throws ApiException {
        List<User> expectedMembers = List.of(new User());
        when(grupApi.getMembresGrup(null)).thenReturn(expectedMembers);

        List<User> actualMembers = grupService.getMembres(null);

        assertSame(expectedMembers, actualMembers);
        verify(grupApi).getMembresGrup(null);
    }

    /**
     * Test: sortirDelGrup delega correctament a l'API.
     */
    @Test
    void sortirDelGrup_WhenGrupAndUserExist_DelegatesToApi() throws ApiException {
        Long grupId = 1L;
        Long membreId = 1L;
        doNothing().when(grupApi).sortirUsuari(grupId, membreId);

        grupService.sortirDelGrup(grupId, membreId);

        verify(grupApi).sortirUsuari(grupId, membreId);
    }

    /**
     * Test: sortirDelGrup propaga ApiException quan l'usuari no es troba al grup.
     */
    @Test
    void sortirDelGrup_WhenUserNotInGroup_ThrowsApiException() throws ApiException {
        Long grupId = 1L;
        Long membreId = 999L;
        doThrow(new ApiException("Usuari no trobat al grup")).when(grupApi).sortirUsuari(grupId, membreId);

        assertThrows(ApiException.class, () -> grupService.sortirDelGrup(grupId, membreId));
    }

    /**
     * Test: sortirDelGrup amb IDs null delega correctament.
     */
    @Test
    void sortirDelGrup_WhenIdsAreNull_DelegatesCorrectly() throws ApiException {
        doNothing().when(grupApi).sortirUsuari(null, null);

        grupService.sortirDelGrup(null, null);

        verify(grupApi).sortirUsuari(null, null);
    }

    /**
     * Test: joinGrup amb IDs zero delega correctament.
     */
    @Test
    void joinGrup_WhenIdsAreZero_DelegatesCorrectly() throws ApiException {
        Long grupId = 0L;
        Long userId = 0L;
        Grup expectedGrup = new Grup();
        when(grupApi.afegirUsuariGrup(grupId, userId)).thenReturn(expectedGrup);

        Grup result = grupService.joinGrup(grupId, userId);

        assertSame(expectedGrup, result);
        verify(grupApi).afegirUsuariGrup(grupId, userId);
    }
}