package com.bibliosedaos.desktop;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiFactory;
import com.bibliosedaos.desktop.config.AppConfig;
import com.bibliosedaos.desktop.config.ControllerRegistry;
import com.bibliosedaos.desktop.config.StyleManager;
import com.bibliosedaos.desktop.service.*;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aplicacio principal JavaFX per a BiblioSedaos.
 * Configura l'stage principal, el Navigator i les dependencies.
 * Gestiona la injeccio de dependencies als controladors.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static final String DEFAULT_ICON = "/com/bibliosedaos/desktop/images/logo2.png";
    private static final String APP_TITLE = "BiblioSedaos - Login";
    private static final String LOGIN_VIEW = "/com/bibliosedaos/desktop/login-view.fxml";
    private static final double DEFAULT_WIDTH = 1000.0;
    private static final double DEFAULT_HEIGHT = 600.0;
    private static final double MIN_WIDTH = 800.0;
    private static final double MIN_HEIGHT = 480.0;

    /**
     * Inicialitza la configuracio de l'aplicacio abans de mostrar la interficie.
     * Llegeix la configuracio des d'un fitxer extern.
     *
     * @throws Exception en cas d'errors
     */
    @Override
    public void init() throws Exception {
        super.init();
        AppConfig appConfig = new AppConfig();
        ApiFactory.setUseMock(appConfig.isUseMock());
    }

    /**
     * Inicia la interficie grafica.
     * Configura el Navigator, els estils CSS i la injeccio de dependencies
     * als controladors mitjancant una ControllerFactory.
     *
     * @param stage Stage principal de JavaFX
     */
    @Override
    public void start(Stage stage) {
        Navigator navigator = new Navigator();
        navigator.init(stage);

        // Registre d'estils CSS
        StyleManager.registerStyles(navigator);

        setupStage(stage);

        // Composition root: creacio de dependencies compartides
        AuthService authService = new AuthService(ApiFactory.createAuthApi());
        UserService userService = new UserService(ApiFactory.createUserApi());
        LlibreService llibreService = new LlibreService(ApiFactory.createLlibreApi());
        AutorService autorService = new AutorService(ApiFactory.createAutorApi());
        ExemplarService exemplarService = new ExemplarService(ApiFactory.createExemplarApi());
        PrestecService prestecService = new PrestecService(ApiFactory.createPrestecApi());
        GrupService grupService = new GrupService(ApiFactory.createGrupApi());
        HorariService horariService = new HorariService(ApiFactory.createHorariApi());

        // ControllerFactory per a injeccio de dependencies en controladors
        ControllerRegistry registry = new ControllerRegistry(
                authService, userService, llibreService, autorService, exemplarService, prestecService, grupService, horariService, navigator
        );
        navigator.setControllerFactory(registry::createController);

        // Anar a la vista de login
        navigator.goTo(LOGIN_VIEW, APP_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT, false, null);
    }

    /**
     * Configura l'stage principal de l'aplicacio.
     * Estableix la icona, dimensions i redimensionament de l'stage.
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
     * Tanca recursos en aturar l'aplicacio.
     * Allibera els recursos del executor d'API i finalitza l'aplicacio.
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
     * Punt d'entrada Java.
     *
     * @param args arguments de linia de comandes
     */
    public static void main(String[] args) {
        AppConfig.loadSystemPropertiesFromFile();
        launch(args);
    }
}