package com.bibliosedaos.desktop.ui.navigator;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sistema centralitzat de navegacio per a aplicacions JavaFX.
 * Gestiona la carrega de vistes FXML, la injeccio de dependencies en controladors,
 * l'aplicacio d'estils CSS i la navegacio entre pantalles.
 * Assistencia d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class Navigator {

    private static final Logger LOGGER = Logger.getLogger(Navigator.class.getName());
    private Stage primaryStage;
    private StackPane mainContentArea;
    private final Map<String, List<String>> viewCssMap = new HashMap<>();
    private final List<String> globalCss = new ArrayList<>();
    private final Set<String> appliedSceneCss = new LinkedHashSet<>();
    private Callback<Class<?>, Object> controllerFactory;

    /** Constructor public: crea una instancia a MainApp. */
    public Navigator() {
        //Constructor public
    }

    /**
     * Inicialitza el Navigator amb l'Stage principal de JavaFX.
     * Aquest metode es necessari per configurar l'stage abans de qualsevol operacio de navegacio.
     *
     * @param stage Stage principal de l'aplicacio
     */
    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Configura el factory per a controllers amb constructor injection.
     *
     * @param factory Callback que crea instancies de controladors
     */
    public void setControllerFactory(Callback<Class<?>, Object> factory) {
        this.controllerFactory = factory;
    }

    /**
     * Verifica que el Navigator estigui inicialitzat.
     *
     * @throws IllegalStateException si no esta inicialitzat
     */
    private void ensureInit() {
        if (primaryStage == null) {
            throw new IllegalStateException("Navigator no inicialitzat. Crida init(stage).");
        }
    }

    /**
     * Registra un CSS global que s'aplicara a totes les vistes.
     *
     * @param resourcePath Ruta al fitxer CSS al classpath
     */
    public void registerGlobalCss(String resourcePath) {
        if (resourcePath != null && !globalCss.contains(resourcePath)) {
            globalCss.add(resourcePath);
        }
    }

    /**
     * Registra CSS especific per a una vista particular.
     *
     * @param fxmlPath Ruta al fitxer FXML de la vista
     * @param cssResourcePath Ruta al fitxer CSS especific
     */
    public void registerViewCss(String fxmlPath, String cssResourcePath) {
        if (fxmlPath == null || cssResourcePath == null) return;
        viewCssMap.computeIfAbsent(fxmlPath, k -> new ArrayList<>()).add(cssResourcePath);
    }

    /**
     * Carrega una vista amb opcions avançades i configuracio del controlador.
     *
     * @param fxmlPath Ruta al fitxer FXML
     * @param title Titol de la finestra
     * @param width Amplada de la finestra
     * @param height Alçada de la finestra
     * @param maximize Si la finestra s'ha de maximitzar
     * @param controllerSetup Consumer per configurar el controlador despres de la carrega
     */
    public void goTo(String fxmlPath, String title, Double width, Double height, boolean maximize, Consumer<Object> controllerSetup) {
        ensureInit();
        Parent root = loadFxml(fxmlPath);
        applyControllerSetup(root, controllerSetup);

        Scene scene = new Scene(root);
        applyCss(scene, globalCss);
        applyCss(scene, viewCssMap.getOrDefault(fxmlPath, Collections.emptyList()));

        appliedSceneCss.clear();
        appliedSceneCss.addAll(scene.getStylesheets());
        LOGGER.log(Level.FINE, "Applied scene stylesheets: {0}", appliedSceneCss);

        primaryStage.setScene(scene);
        if (title != null) primaryStage.setTitle(title);
        resizeStage(width, height, maximize);
        primaryStage.show();
    }

    /**
     * Assigna l'area de contingut principal per a vistes incrustades.
     *
     * @param centerStackPane StackPane que fara d'area de contingut principal
     */
    public void setMainContentArea(StackPane centerStackPane) {
        this.mainContentArea = centerStackPane;
    }

    /**
     * Mostra una vista dins de l'area de contingut principal sense configuracio del controlador.
     *
     * @param fxmlPath Ruta al fitxer FXML de la vista
     * @throws IllegalStateException si no s'ha assignat l'area principal
     */
    public void showMainView(String fxmlPath) {
        showMainView(fxmlPath, null);
    }

    /**
     * Mostra una vista dins de l'area de contingut principal amb configuracio del controlador.
     *
     * @param fxmlPath Ruta al fitxer FXML de la vista
     * @param controllerConfig Consumer per configurar el controlador abans de initialize()
     * @param <T> Tipus del controlador
     * @throws IllegalStateException si no s'ha assignat l'area principal
     */
    public <T> void showMainView(String fxmlPath, Consumer<T> controllerConfig) {
        if (mainContentArea == null) {
            throw new IllegalStateException("Main content area no assignat.");
        }
        loadViewInContainer(fxmlPath, mainContentArea, controllerConfig);
    }

    /**
     * Carrega un fitxer FXML i retorna el Parent resultant.
     *
     * @param fxmlPath Ruta al fitxer FXML
     * @return Parent carregat des del FXML
     * @throws FxmlNotFoundException si el fitxer no es troba
     * @throws FxmlLoadException si hi ha errors en la carrega
     */
    private Parent loadFxml(String fxmlPath) {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new FxmlNotFoundException("FXML no trobat: " + fxmlPath);
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            if (controllerFactory != null) {
                loader.setControllerFactory(c -> controllerFactory.call(c));
            }
            return loader.load();
        } catch (IOException e) {
            throw new FxmlLoadException("Error carregant FXML: " + fxmlPath, e);
        }
    }

    /**
     * Aplica la configuracio al controlador si s'ha especificat.
     *
     * @param root Parent que conte el controlador
     * @param setup Consumer per configurar el controlador
     */
    private void applyControllerSetup(Parent root, Consumer<Object> setup) {
        if (setup != null) {
            FXMLLoader loader = (FXMLLoader) root.getProperties().get("javafx.fxml.loader");
            if (loader != null) setup.accept(loader.getController());
        }
    }

    /**
     * Carrega una vista dins d'un container especific amb configuracio del controlador.
     *
     * @param fxmlPath Ruta al fitxer FXML
     * @param container Container on carregar la vista
     * @param controllerConfig Consumer per configurar el controlador abans de initialize()
     * @param <T> Tipus del controlador
     */
    private <T> void loadViewInContainer(String fxmlPath, StackPane container, Consumer<T> controllerConfig) {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new FxmlNotFoundException("FXML no trobat: " + fxmlPath);
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(clazz -> {
                Object controller;

                if (this.controllerFactory != null) {
                    controller = this.controllerFactory.call(clazz);
                } else {
                    try {
                        controller = clazz.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException |
                             InvocationTargetException | NoSuchMethodException e) {
                        throw new ControllerInstantiationException("No es va poder instanciar el controlador: " + clazz.getName(), e);
                    }
                }

                if (controllerConfig != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        T typedController = (T) controller;
                        controllerConfig.accept(typedController);
                    } catch (ClassCastException e) {
                        throw new ControllerInstantiationException("Type mismatch en controller: esperat " + clazz.getName(), e);
                    }
                }
                return controller;
            });

            Parent view = loader.load(); // initialize() s'executa DESPRES, amb les dades ja configurades
            container.getChildren().setAll(view);

            Scene scene = primaryStage.getScene();
            if (scene != null) {
                applyCss(scene, viewCssMap.getOrDefault(fxmlPath, Collections.emptyList()));
            }

        } catch (IOException e) {
            throw new FxmlLoadException("Error carregant FXML: " + fxmlPath, e);
        }
    }

    /**
     * Aplica una llista d'estils CSS a una escena.
     *
     * @param scene Escena on aplicar els estils
     * @param cssList Llista de rutes CSS
     */
    private void applyCss(Scene scene, List<String> cssList) {
        if (scene == null || cssList == null) return;
        for (String path : cssList) addCssIfExists(scene, path);
    }

    /**
     * Afegeix un fitxer CSS a una escena si existeix.
     *
     * @param scene Escena on afegir l'estil
     * @param resourcePath Ruta al fitxer CSS
     */
    private void addCssIfExists(Scene scene, String resourcePath) {
        if (scene == null || resourcePath == null) return;
        URL cssUrl = getClass().getResource(resourcePath);
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
        }
    }

    /**
     * Redimensiona l'stage segons els parametres especificats.
     *
     * @param width Amplada desitjada
     * @param height Alçada desitjada
     * @param maximize Si s'ha de maximitzar
     */
    private void resizeStage(Double width, Double height, boolean maximize) {
        if (maximize) primaryStage.setMaximized(true);
        else {
            primaryStage.setMaximized(false);
            if (width != null) primaryStage.setWidth(width);
            if (height != null) primaryStage.setHeight(height);
        }
    }

    /**
     * Excepcio llançada quan no es troba un fitxer FXML.
     */
    public static class FxmlNotFoundException extends RuntimeException {
        /**
         * Constructor amb missatge d'error.
         *
         * @param message Missatge descriptiu de l'error
         */
        public FxmlNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Excepcio llançada quan hi ha errors en carregar un fitxer FXML.
     */
    public static class FxmlLoadException extends RuntimeException {
        /**
         * Constructor amb missatge i causa de l'error.
         *
         * @param message Missatge descriptiu de l'error
         * @param cause Excepcio original que va causar l'error
         */
        public FxmlLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Excepcio llançada quan hi ha errors en instanciar un controlador.
     */
    public static class ControllerInstantiationException extends RuntimeException {
        /**
         * Constructor amb missatge i causa de l'error.
         *
         * @param message Missatge descriptiu de l'error
         * @param cause Excepcio original que va causar l'error
         */
        public ControllerInstantiationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}