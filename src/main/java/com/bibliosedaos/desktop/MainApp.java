package com.bibliosedaos.desktop;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiFactory;
import com.bibliosedaos.desktop.api.AuthApi;
import com.bibliosedaos.desktop.controller.DashboardController;
import com.bibliosedaos.desktop.controller.LoginController;
import com.bibliosedaos.desktop.service.AuthService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client escriptori JavaFX de BiblioSedaos.
 * Configura l'stage principal, el Navigator i les dependències.
 * Gestiona la injecció de dependències als controladors.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static final String CONFIG_FILE = "app.properties";
    private static final String DEFAULT_ICON = "/com/bibliosedaos/desktop/images/logo2.png";
    private static final String APP_TITLE = "BiblioSedaos - Login";
    private static final String LOGIN_VIEW = "/com/bibliosedaos/desktop/login-view.fxml";
    private static final String DASHBOARD_VIEW = "/com/bibliosedaos/desktop/dashboard-view.fxml";
    private static final boolean DEFAULT_USE_MOCK = true;
    private static final double DEFAULT_WIDTH = 1000.0;
    private static final double DEFAULT_HEIGHT = 600.0;
    private static final double MIN_WIDTH = 800.0;
    private static final double MIN_HEIGHT = 480.0;

    /**
     * Inicialitza la configuració de l'aplicació abans de mostrar la interfície.
     * Llegeix la configuració des d'un fitxer extern
     *
     * @throws Exception en cas d'errors
     */
    @Override
    public void init() throws Exception {
        super.init();

        boolean useMock = readUseMockFromProperties(Path.of(CONFIG_FILE));
        ApiFactory.setUseMock(useMock);
    }

    /**
     * Inicia la interfície gràfica.
     * Configura el Navigator, els estils CSS i la injecció de dependències
     * als controladors mitjançant una ControllerFactory.
     *
     * @param stage Stage principal de JavaFX
     */
    @Override
    public void start(Stage stage) {
        Navigator nav = new Navigator();
        nav.init(stage);

        // Registre d'estils CSS
        nav.registerGlobalCss("/styles/app.css");
        nav.registerViewCss(LOGIN_VIEW, "/styles/login.css");
        nav.registerViewCss(DASHBOARD_VIEW, "/styles/dashboard.css");

        setupStage(stage);

        // Composition root: creació de dependències compartides
        AuthApi authApi = ApiFactory.createAuthApi();
        AuthService authService = new AuthService(authApi);

        // ControllerFactory per a injecció de dependències en controladors
        nav.setControllerFactory(clazz -> {
            try {
                if (clazz == LoginController.class) return new LoginController(authService, nav);
                if (clazz == DashboardController.class) return new DashboardController(authService, nav);
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error creando controller {0}", clazz.getName());
                throw new RuntimeException("Error creando controller " + clazz.getName(), e);
            }
        });

        nav.goTo(LOGIN_VIEW, APP_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT, false, null);
    }

    /**
     * Estableix títol, icona, dimensió i redimensionament de l'Stage.
     *
     * @param stage l'Stage a configurar
     */
    private void setupStage(Stage stage) {
        try (InputStream is = getClass().getResourceAsStream(DEFAULT_ICON)) {
            if (is != null) {
                stage.getIcons().add(new Image(is));
            } else {
                LOGGER.warning("Icona no trobada: " + DEFAULT_ICON);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error carregant icona: {0}", e.getMessage());
        }

        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setResizable(true);
    }

    /**
     * Tanca recursos en aturar l'aplicació.
     * Allibera els recursos del executor d'API i finalitza l'aplicació.
     *
     * @throws Exception si hi ha errors
     */
    @Override
    public void stop() throws Exception {
        try {
            ApiClient.shutdownExecutor();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error tancant executor", e);
        } finally {
            super.stop();
        }
    }

    /**
     * Llegeix la configuració 'app.useMock' des de properties.
     *
     * @param external Ruta al fitxer de configuració extern
     * @return true si s'ha d'usar mock, false per a connexió real
     */
    public static boolean readUseMockFromProperties(Path external) {
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

        try (InputStream in = MainApp.class.getResourceAsStream("/" + CONFIG_FILE)) {
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
     * Punt d'entrada Java.
     *
     * @param args arguments de línia de comandes
     */
    public static void main(String[] args) {
        launch(args);
    }
}
