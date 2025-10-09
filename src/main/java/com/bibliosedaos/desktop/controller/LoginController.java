package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.dto.LoginResponse;
import com.bibliosedaos.desktop.service.AuthService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per a la vista de login.
 *
 * Gestiona la interfície d'autenticació, validació d'entrades i navegació.
 * Utilitza Tasks per a operacions en segon pla i manté la UI responsive.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;

    private static final int MAX_NICK_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 20;

    private final AuthService authService;
    private final Navigator navigator;

    private Task<LoginResponse> currentTask;

    /**
     * Constructor amb injecció de dependències.
     *
     * @param authService servei d'autenticació
     * @param navigator gestor de navegació
     * @throws NullPointerException si alguna dependència és null
     */
    public LoginController(AuthService authService, Navigator navigator) {
        this.authService = Objects.requireNonNull(authService, "AuthService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador després de carregar el FXML.
     * Configura els elements de la interfície i aplica efectes visuals.
     */
    @FXML
    private void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
        AnimationUtils.applyClickEffect(loginButton);
        AnimationUtils.applyClickEffect(cancelButton);
        cancelButton.setDisable(false);
    }

    /**
     * Gestiona l'acció del botó d'inici de sessió.
     * Valida les dades i executa l'autenticació en segon pla.
     */
    @FXML
    void onLogin() {
        if (errorLabel != null) errorLabel.setVisible(false);

        final String nick = usernameField.getText() == null ? "" : usernameField.getText().trim();
        final String password = passwordField.getText() == null ? "" : passwordField.getText();

        String validationError = validateLoginLogic(nick, password);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        Task<LoginResponse> task = new Task<>() {
            @Override
            protected LoginResponse call() throws Exception {
                LOGGER.log(Level.FINE, "Autenticant {0}", nick);
                return authService.login(nick, password);
            }
        };

        currentTask = task;

        task.setOnSucceeded(evt -> {
            LoginResponse resp = task.getValue();
            if (resp != null) {
                passwordField.clear();
                navigator.goTo("/com/bibliosedaos/desktop/dashboard-view.fxml",
                        "BiblioSedaos - " + resp.getNom(), null, null, true, null);
                LOGGER.log(Level.INFO, "Login ok: {0}", resp.getNom());
            } else {
                showError("Resposta inesperada del servidor.");
            }
            restoreUiState();
            currentTask = null;
        });

        task.setOnFailed(evt -> {
            Throwable ex = task.getException();
            showError(ex instanceof ApiException ? ex.getMessage() : "Error al connectar: " + (ex == null ? "desconegut" : ex.getMessage()));
            LOGGER.log(Level.WARNING, "Login fallit", ex);
            restoreUiState();
            currentTask = null;
        });

        task.setOnCancelled(evt -> {
            showError("Operació cancel·lada.");
            LOGGER.info("Tasca login cancel·lada");
            restoreUiState();
            currentTask = null;
        });

        disableUiDuringLogin();
        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona l'acció del botó de cancel·lació.
     * Atura l'operació en curs o tanca l'aplicació.
     */
    @FXML
    private void onCancel() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel(true);
            cancelButton.setDisable(true);
            showError("Operació cancel·lada.");
            return;
        }
        Platform.exit();
    }

    /**
     * Mostra un missatge d'error a l'usuari.
     *
     * @param msg missatge d'error a mostrar
     */
    private void showError(String msg) {
        if (errorLabel == null) return;
        if (Platform.isFxApplicationThread()) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        } else {
            Platform.runLater(() -> {
                errorLabel.setText(msg);
                errorLabel.setVisible(true);
            });
        }
    }

    /**
     * Valida la lògica de les credencials d'entrada.
     *
     * @param nick nom d'usuari
     * @param password contrasenya
     * @return missatge d'error o null si és vàlid
     */
    String validateLoginLogic(String nick, String password) {
        if (nick.isEmpty() || password.isEmpty()) return "Omple l'usuari/a i la contrasenya";
        if (nick.length() > MAX_NICK_LENGTH) return "El nick ha de tenir un màxim de " + MAX_NICK_LENGTH + " caràcters.";
        if (password.length() > MAX_PASSWORD_LENGTH) return "La contrasenya ha de tenir un màxim de " + MAX_PASSWORD_LENGTH + " caràcters.";
        return null;
    }

    /**
     * Deshabilita la interfície durant l'autenticació.
     */
    private void disableUiDuringLogin() {
        if (loginButton != null) loginButton.setDisable(true);
        if (cancelButton != null) cancelButton.setDisable(false);
    }

    /**
     * Restaura l'estat de la interfície després de l'autenticació.
     */
    private void restoreUiState() {
        if (loginButton != null) loginButton.setDisable(false);
        if (cancelButton != null) cancelButton.setDisable(false);
    }
}