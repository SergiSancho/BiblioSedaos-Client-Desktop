package com.bibliosedaos.desktop.service;

import com.bibliosedaos.desktop.api.UserApi;
import com.bibliosedaos.desktop.api.ApiException;
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
 * Proves unitaries per a UserService.
 *
 * Verifica que les operacions d'usuari es delegen correctament a l'API
 * i que les excepcions es propaguen adequadament.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserApi userApi;

    private UserService userService;

    /**
     * Configuracio abans de cada test: crea el servei amb el mock.
     */
    @BeforeEach
    void setUp() {
        userService = new UserService(userApi);
    }

    /**
     * Test: constructor ha de llençar NullPointerException si userApi es null.
     */
    @Test
    void constructor_WhenUserApiIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UserService(null));
    }

    /**
     * Test: getUserById retorna l'usuari quan existeix.
     */
    @Test
    void getUserById_WhenUserExists_ReturnsUser() throws ApiException {
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        when(userApi.getUserById(userId)).thenReturn(expectedUser);

        User actualUser = userService.getUserById(userId);

        assertSame(expectedUser, actualUser);
        verify(userApi).getUserById(userId);
    }

    /**
     * Test: getUserById propaga ApiException quan l'API la llença.
     */
    @Test
    void getUserById_WhenUserDoesNotExist_ThrowsApiException() throws ApiException {
        Long userId = 999L;
        when(userApi.getUserById(userId)).thenThrow(new ApiException("Usuari no trobat"));

        assertThrows(ApiException.class, () -> userService.getUserById(userId));
    }

    /**
     * Test: getAllUsers delega a l'API i retorna la llista.
     */
    @Test
    void getAllUsers_WhenCalled_ReturnsListOfUsers() throws ApiException {
        when(userApi.getAllUsers()).thenReturn(List.of());

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        verify(userApi).getAllUsers();
    }

    /**
     * Test: deleteUser delega a l'API quan l'usuari existeix.
     */
    @Test
    void deleteUser_WhenUserExists_DeletesUser() throws ApiException {
        Long userId = 2L;
        doNothing().when(userApi).deleteUser(userId);

        userService.deleteUser(userId);

        verify(userApi).deleteUser(userId);
    }

    /**
     * Test: deleteUser propaga ApiException quan l'API llença.
     */
    @Test
    void deleteUser_WhenUserDoesNotExist_ThrowsApiException() throws ApiException {
        Long userId = 999L;
        doThrow(new ApiException("Usuari no trobat")).when(userApi).deleteUser(userId);

        assertThrows(ApiException.class, () -> userService.deleteUser(userId));
    }

    /**
     * Test: createUser delega i retorna l'usuari creat.
     */
    @Test
    void createUser_WhenUserIsValid_ReturnsCreatedUser() throws ApiException {
        User userToCreate = new User();
        User createdUser = new User();
        createdUser.setId(1L);
        when(userApi.createUser(userToCreate)).thenReturn(createdUser);

        User result = userService.createUser(userToCreate);

        assertSame(createdUser, result);
        verify(userApi).createUser(userToCreate);
    }

    /**
     * Test: updateUser delega i retorna l'usuari actualitzat.
     */
    @Test
    void updateUser_WhenUserIsValid_ReturnsUpdatedUser() throws ApiException {
        Long userId = 1L;
        User userToUpdate = new User();
        User updatedUser = new User();
        updatedUser.setId(userId);
        when(userApi.updateUser(userId, userToUpdate)).thenReturn(updatedUser);

        User result = userService.updateUser(userId, userToUpdate);

        assertSame(updatedUser, result);
        verify(userApi).updateUser(userId, userToUpdate);
    }

    /**
     * Test: getUserByNick delega i retorna l'usuari per nick.
     */
    @Test
    void getUserByNick_WhenNickExists_ReturnsUser() throws ApiException {
        String nick = "testnick";
        User expectedUser = new User();
        expectedUser.setNick(nick);
        when(userApi.getUserByNick(nick)).thenReturn(expectedUser);

        User result = userService.getUserByNick(nick);

        assertSame(expectedUser, result);
        verify(userApi).getUserByNick(nick);
    }

    /**
     * Test: getUserByNif delega i retorna l'usuari per NIF.
     */
    @Test
    void getUserByNif_WhenNifExists_ReturnsUser() throws ApiException {
        String nif = "12345678A";
        User expectedUser = new User();
        expectedUser.setNif(nif);
        when(userApi.getUserByNif(nif)).thenReturn(expectedUser);

        User result = userService.getUserByNif(nif);

        assertSame(expectedUser, result);
        verify(userApi).getUserByNif(nif);
    }
}
