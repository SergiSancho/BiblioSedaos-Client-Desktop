package com.bibliosedaos.desktop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per a SessionStore.
 *
 * Verifica el comportament del patró Singleton i la gestió de dades de sessió.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class SessionStoreTest {

    private SessionStore sessionStore;

    /**
     * Configuració inicial abans de cada test.
     * Neteja la sessió per assegurar un estat conegut.
     */
    @BeforeEach
    void setUp() {
        sessionStore = SessionStore.getInstance();
        sessionStore.clear(); // Assegura estat net per cada test
    }

    /**
     * Prova que getInstance() sempre retorna la mateixa instància.
     * Verifica el patró Singleton.
     */
    @Test
    void getInstance_Always_ReturnsSameInstance() {
        SessionStore instance1 = SessionStore.getInstance();
        SessionStore instance2 = SessionStore.getInstance();

        assertSame(instance1, instance2, "Singleton ha de retornar la mateixa instància");
    }

    /**
     * Prova que els valors es poden establir i recuperar correctament.
     * Verifica els getters i setters bàsics.
     */
    @Test
    void settersAndGetters_WhenValuesSet_ReturnsCorrectValues() {
        String expectedToken = "test-token-123";
        Long expectedUserId = 456L;
        int expectedRol = 1;
        String expectedNom = "Test";
        String expectedCognom1 = "User";
        String expectedCognom2 = "Mock";

        sessionStore.setToken(expectedToken);
        sessionStore.setUserId(expectedUserId);
        sessionStore.setRol(expectedRol);
        sessionStore.setNom(expectedNom);
        sessionStore.setCognom1(expectedCognom1);
        sessionStore.setCognom2(expectedCognom2);

        assertEquals(expectedToken, sessionStore.getToken(), "Token ha de coincidir");
        assertEquals(expectedUserId, sessionStore.getUserId(), "UserId ha de coincidir");
        assertEquals(expectedRol, sessionStore.getRol(), "Rol ha de coincidir");
        assertEquals(expectedNom, sessionStore.getNom(), "Nom ha de coincidir");
        assertEquals(expectedCognom1, sessionStore.getCognom1(), "Cognom1 ha de coincidir");
        assertEquals(expectedCognom2, sessionStore.getCognom2(), "Cognom2 ha de coincidir");
    }

    /**
     * Prova que clear() neteja tots els camps de la sessió.
     * Verifica que després de clear(), tots els valors són null o zero.
     */
    @Test
    void clear_WhenCalled_ResetsAllFields() {
        sessionStore.setToken("some-token");
        sessionStore.setUserId(456L);
        sessionStore.setRol(2);
        sessionStore.setNom("Some");
        sessionStore.setCognom1("Name");

        sessionStore.clear();

        assertNull(sessionStore.getToken(), "Token ha de ser null després de clear()");
        assertNull(sessionStore.getUserId(), "UserId ha de ser null després de clear()");
        assertEquals(1, sessionStore.getRol(), "Rol ha de ser 1 després de clear()");
        assertNull(sessionStore.getNom(), "Nom ha de ser null després de clear()");
        assertNull(sessionStore.getCognom1(), "Cognom1 ha de ser null després de clear()");
        assertNull(sessionStore.getCognom2(), "Cognom2 ha de ser null després de clear()");
    }

    /**
     * Prova que els valors es poden sobreescriure correctament.
     * Verifica que nous valors reemplacen els antics.
     */
    @Test
    void setters_WhenCalledMultipleTimes_UpdatesValues() {
        sessionStore.setToken("first-token");
        sessionStore.setRol(1);

        sessionStore.setToken("second-token");
        sessionStore.setRol(2);

        assertEquals("second-token", sessionStore.getToken(), "Ha de retornar l'últim token establert");
        assertEquals(2, sessionStore.getRol(), "Ha de retornar l'últim rol establert");
    }

    /**
     * Prova el comportament amb valors null.
     * Verifica que els setters accepten null i els getters retornen null correctament.
     */
    @Test
    void setters_WithNullValues_HandlesCorrectly() {
        sessionStore.setToken(null);
        sessionStore.setUserId(null);
        sessionStore.setNom(null);

        assertNull(sessionStore.getToken(), "Ha de permetre token null");
        assertNull(sessionStore.getUserId(), "Ha de permetre userId null");
        assertNull(sessionStore.getNom(), "Ha de permetre nom null");
    }
}