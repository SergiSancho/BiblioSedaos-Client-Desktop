package com.bibliosedaos.desktop.controller;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button cancelButton;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);

        // Aplicar animación dinámica a botones
        applyClickAnimation(loginButton);
        applyClickAnimation(cancelButton);
    }

    // Animación de "rebote" al pulsar
    private void applyClickAnimation(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), button);

        button.setOnMousePressed(e -> {
            st.setToX(0.95);
            st.setToY(0.95);
            st.playFromStart();
        });

        button.setOnMouseReleased(e -> {
            st.setToX(1);
            st.setToY(1);
            st.playFromStart();
        });
    }

    @FXML
    private void onLogin() {
        errorLabel.setVisible(false);
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Omple l'usuari/a i la contrasenya");
            errorLabel.setVisible(true);
            return;
        }

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login simulado: " + username, ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }

    @FXML
    private void onCancel() {
        Platform.exit();
    }
}
