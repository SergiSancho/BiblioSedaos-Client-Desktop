package com.bibliosedaos.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Cargar FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bibliosedaos/desktop/login-view.fxml"));
        Scene scene = new Scene(loader.load());

        // Enlace a estilos globales
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        // Enlace a estilos específicos del login
        scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());

        // Añadir icono
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/com/bibliosedaos/desktop/images/logo2.png"))));

        stage.setTitle("BiblioSedaos - Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
