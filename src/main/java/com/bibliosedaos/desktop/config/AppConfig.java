package com.bibliosedaos.desktop.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la configuracio de l'aplicacio.
 * Llegeix la configuracio des de app.properties per determinar si s'ha d'usar mock.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String CONFIG_FILE = "app.properties";
    private static final boolean DEFAULT_USE_MOCK = true;

    private final boolean useMock;

    /**
     * Constructor que inicialitza la configuracio llegint des del fitxer.
     */
    public AppConfig() {
        this.useMock = readUseMockFromProperties(Path.of(CONFIG_FILE));
    }

    /**
     * Retorna si s'ha d'usar la implementacio mock de l'API.
     *
     * @return true si s'ha d'usar mock, false si s'ha de connectar a l'API real
     */
    public boolean isUseMock() {
        return useMock;
    }

    /**
     * Llegeix la propietat app.useMock des d'un fitxer de configuracio.
     * Primer intenta llegir des d'un fitxer extern, i si no existeix, del classpath.
     *
     * @param external Ruta al fitxer de configuracio extern
     * @return valor de app.useMock o el valor per defecte si no es troba
     */
    private boolean readUseMockFromProperties(Path external) {
        boolean useMock = DEFAULT_USE_MOCK;
        Properties props = new Properties();

        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
                String v = props.getProperty("app.useMock");
                if (v != null) useMock = Boolean.parseBoolean(v.trim());
                LOGGER.log(Level.INFO, "[CONFIG] Llegit {0} -> app.useMock={1}", new Object[]{external, useMock});
                return useMock;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "[CONFIG] Error llegint fitxer extern {0}: {1}", new Object[]{external, e.getMessage()});
            }
        }

        try (InputStream in = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                String v = props.getProperty("app.useMock");
                if (v != null) useMock = Boolean.parseBoolean(v.trim());
                LOGGER.log(Level.INFO, "[CONFIG] Llegit app.properties del classpath -> app.useMock={0}", useMock);
            } else {
                LOGGER.log(Level.INFO, "[CONFIG] No s'ha trobat app.properties; s'utilitza valor per defecte app.useMock={0}", useMock);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[CONFIG] Error llegint fitxer de classpath: {0}", e.getMessage());
        }

        return useMock;
    }

    /**
     * Carrega propietats de configuració i, opcionalment, les estableix com a system properties
     * útils per a la inicialització de l'aplicació (trustStore, javax.net.debug, api.base.url).
     */
    public static void loadSystemPropertiesFromFile() {
        Path external = Path.of(CONFIG_FILE);
        Properties props = new Properties();

        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
                LOGGER.log(Level.INFO, "[CONFIG] cargado app.properties externo: {0}", external.toAbsolutePath());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[CONFIG] no se pudo leer app.properties externo: {0}", e.getMessage());
            }
        } else {
            try (InputStream in = AppConfig.class.getResourceAsStream("/app.properties")) {
                if (in != null) {
                    props.load(in);
                    LOGGER.log(Level.INFO, "[CONFIG] cargado app.properties del classpath");
                } else {
                    LOGGER.log(Level.INFO, "[CONFIG] no se encontró app.properties en classpath ni en el working dir");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[CONFIG] error reading app.properties from classpath: {0}", e.getMessage());
            }
        }

        String[] keys = new String[] {
                "api.base.url",
                "api.ssl.trustStore",
                "api.ssl.trustStorePassword",
                "javax.net.ssl.trustStore",
                "javax.net.ssl.trustStorePassword",
                "javax.net.debug"
        };

        for (String k : keys) {
            String v = props.getProperty(k);
            if (v != null && System.getProperty(k) == null) {
                System.setProperty(k, v.trim());
                LOGGER.log(Level.INFO, "[CONFIG] System property set: {0}={1}", new Object[]{k, v});
            }
        }

        String passEnv = System.getenv("API_SSL_TRUSTSTORE_PASSWORD");
        if (passEnv != null && System.getProperty("api.ssl.trustStorePassword") == null) {
            System.setProperty("api.ssl.trustStorePassword", passEnv);
            System.setProperty("javax.net.ssl.trustStorePassword", passEnv);
            LOGGER.log(Level.INFO, "[CONFIG] Loaded truststore password from env API_SSL_TRUSTSTORE_PASSWORD");
        }
    }
}