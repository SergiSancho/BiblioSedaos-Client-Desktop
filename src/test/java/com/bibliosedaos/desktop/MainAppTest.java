package com.bibliosedaos.desktop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per al mètode {@link MainApp#readUseMockFromProperties(Path)}.
 * Cada test comprova un cas concret de lectura de la propietat {@code app.useMock}
 * (fitxer amb valor true, fitxer amb valor false, fitxer inexistent -> valor per defecte).
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
class MainAppTest {

    private Path tempFile;

    /**
     * Neteja el fitxer temporal després de cada test.
     *
     * @throws IOException si hi ha errors eliminant el fitxer
     */
    @AfterEach
    void tearDown() throws IOException {
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    /**
     * Prova que readUseMockFromProperties retorna true quan el fitxer té app.useMock=true.
     *
     * Escenari: S'crea un fitxer temporal amb la propietat configurada a true.
     * Resultat esperat: El mètode retorna true.
     *
     * @throws IOException si hi ha errors creant el fitxer temporal
     */
    @Test
    void readUseMockFromProperties_WhenFileHasTrue_ReturnsTrue() throws IOException {
        tempFile = Files.createTempFile("test-app", ".properties");
        Files.writeString(tempFile, "app.useMock=true\n");

        boolean result = MainApp.readUseMockFromProperties(tempFile);

        assertTrue(result, "Ha de retornar true quan el fitxer té app.useMock=true");
    }

    /**
     * Prova que readUseMockFromProperties retorna false quan el fitxer té app.useMock=false.
     *
     * Escenari: S'crea un fitxer temporal amb la propietat configurada a false.
     * Resultat esperat: El mètode retorna false.
     *
     * @throws IOException si hi ha errors creant el fitxer temporal
     */
    @Test
    void readUseMockFromProperties_WhenFileHasFalse_ReturnsFalse() throws IOException {
        tempFile = Files.createTempFile("test-app", ".properties");
        Files.writeString(tempFile, "app.useMock=false\n");

        boolean result = MainApp.readUseMockFromProperties(tempFile);

        assertFalse(result, "Ha de retornar false quan el fitxer té app.useMock=false");
    }

    /**
     * Prova que readUseMockFromProperties retorna el valor per defecte quan no hi ha fitxer.
     *
     * Escenari: Es passa una ruta a un fitxer que no existeix.
     * Resultat esperat: El mètode retorna el valor per defecte (true).
     */
    @Test
    void readUseMockFromProperties_WhenFileNotExists_ReturnsDefault() {
        Path nonExistentFile = Path.of("no_existe.properties");

        boolean result = MainApp.readUseMockFromProperties(nonExistentFile);

        assertTrue(result, "Ha de retornar el valor per defecte true quan no es troba fitxer");
    }

    /**
     * Prova que readUseMockFromProperties retorna el valor per defecte quan la propietat no existeix al fitxer.
     *
     * Escenari: El fitxer existeix però no conté la propietat app.useMock.
     * Resultat esperat: El mètode retorna el valor per defecte (true).
     *
     * @throws IOException si hi ha errors creant el fitxer temporal
     */
    @Test
    void readUseMockFromProperties_WhenPropertyMissing_ReturnsDefault() throws IOException {
        tempFile = Files.createTempFile("test-app", ".properties");
        Files.writeString(tempFile, "otra.propiedad=valor\n");

        boolean result = MainApp.readUseMockFromProperties(tempFile);

        assertTrue(result, "Ha de retornar el valor per defecte true quan la propietat no existeix");
    }
}