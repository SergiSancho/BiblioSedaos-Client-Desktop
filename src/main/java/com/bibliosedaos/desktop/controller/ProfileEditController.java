package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.security.SessionStore;
import com.bibliosedaos.desktop.service.UserService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per a l'edicio de perfil d'usuari.
 * Gestiona l'actualitzacio de dades personals i credencials.
 *
 * Assistencia d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class ProfileEditController {

    private static final Logger LOGGER = Logger.getLogger(ProfileEditController.class.getName());

    @FXML private TextField nickField;
    @FXML private TextField nifField;
    @FXML private TextField nomField;
    @FXML private TextField cognom1Field;
    @FXML private TextField cognom2Field;
    @FXML private TextField localitatField;
    @FXML private TextField provinciaField;
    @FXML private TextField carrerField;
    @FXML private TextField cpField;
    @FXML private TextField tlfField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordConfirmField;

    @FXML private Label userIdLabel;
    @FXML private Label rolLabel;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private final UserService userService;
    private final Navigator navigator;
    private Long currentUserId;

    /**
     * Constructor amb injeccio de dependencies.
     *
     * @param userService servei d'usuari
     * @param navigator gestor de navegacio
     * @throws NullPointerException si alguna dependencia es null
     */
    public ProfileEditController(UserService userService, Navigator navigator) {
        this.userService = Objects.requireNonNull(userService, "UserService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador despres de carregar el FXML.
     * Configura efectes visuals i carrega les dades de l'usuari.
     */
    @FXML
    private void initialize() {
        AnimationUtils.applyClickEffect(saveButton);
        AnimationUtils.applyClickEffect(cancelButton);
        errorLabel.setVisible(false);
        loadUserDataFromServer();
    }

    /**
     * Carrega les dades completes de l'usuari des del servidor.
     */
    private void loadUserDataFromServer() {
        SessionStore store = SessionStore.getInstance();
        currentUserId = store.getUserId();

        if (currentUserId == null) {
            showError("No s'ha pogut identificar l'usuari. Torneu a iniciar sessio.");
            return;
        }

        Task<User> loadTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                LOGGER.log(Level.FINE, "Carregant dades de lusuari amb ID: {0}", currentUserId);
                return userService.getUserById(currentUserId);
            }
        };

        loadTask.setOnSucceeded(e -> {
            User user = loadTask.getValue();
            if (user != null) {
                populateFormWithUserData(user);
                LOGGER.log(Level.INFO, "Dades de lusuari carregades correctament: {0}", user.getNom());
            } else {
                showError("No s'han pogut carregar les dades de l'usuari.");
                loadUserDataFromSession();
            }
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            String errorMsg = "Error carregant les dades: " +
                    (ex instanceof ApiException ? ex.getMessage() : "Error de connexio");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error carregant dades de l'usuari des del servidor", ex);
            loadUserDataFromSession();
        });

        ApiClient.BG_EXEC.submit(loadTask);
    }

    /**
     * Omple el formulari amb TOTES les dades de l'usuari des del servidor.
     *
     * @param user usuari amb les dades a mostrar
     */
    private void populateFormWithUserData(User user) {
        userIdLabel.setText(user.getId() != null ? user.getId().toString() : "");
        rolLabel.setText(user.getRol() == 2 ? "Administrador/a" : "Usuari/a");

        nickField.setText(safeGet(user.getNick()));
        nifField.setText(safeGet(user.getNif()));
        nomField.setText(safeGet(user.getNom()));
        cognom1Field.setText(safeGet(user.getCognom1()));
        cognom2Field.setText(safeGet(user.getCognom2()));
        localitatField.setText(safeGet(user.getLocalitat()));
        provinciaField.setText(safeGet(user.getProvincia()));
        carrerField.setText(safeGet(user.getCarrer()));
        cpField.setText(safeGet(user.getCp()));
        tlfField.setText(safeGet(user.getTlf()));
        emailField.setText(safeGet(user.getEmail()));

        passwordField.setText("");
        passwordConfirmField.setText("");
    }

    /**
     * Metode de fallback - carrega dades basiques des de SessionStore.
     */
    private void loadUserDataFromSession() {
        SessionStore store = SessionStore.getInstance();
        currentUserId = store.getUserId();

        userIdLabel.setText(currentUserId != null ? currentUserId.toString() : "");
        nomField.setText(safeGet(store.getNom()));
        cognom1Field.setText(safeGet(store.getCognom1()));
        cognom2Field.setText(safeGet(store.getCognom2()));
        rolLabel.setText(store.getRol() == 2 ? "Administrador/a" : "Usuari/a");

        clearEditableFields();
        LOGGER.info("Dades carregades des de SessionStore (mode fallback)");
    }

    /**
     * Neteja els camps editables del formulari.
     */
    private void clearEditableFields() {
        nickField.setText("");
        nifField.setText("");
        localitatField.setText("");
        provinciaField.setText("");
        carrerField.setText("");
        cpField.setText("");
        tlfField.setText("");
        emailField.setText("");
        passwordField.setText("");
        passwordConfirmField.setText("");
    }

    /**
     * Obte un valor segur (no nul).
     *
     * @param value valor a verificar
     * @return valor no nul o cadena buida
     */
    String safeGet(String value) {
        return value != null ? value : "";
    }

    /**
     * Gestiona l'accio de guardar el formulari.
     */
    @FXML
    private void onSave() {
        if (!validateFields()) return;

        User updatedUser = createFullUser();
        executeUpdateTask(updatedUser);
    }

    /**
     * Gestiona l'accio de cancel·lar l'edicio.
     */
    @FXML
    private void onCancel() {
        navigator.showMainView("/com/bibliosedaos/desktop/welcome-view.fxml");
    }

    /**
     * Valida tots els camps del formulari.
     *
     * @return true si tots els camps son valids, false altrament
     */
    boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        validateRequiredFields(errorMessage);
        validateFieldFormats(errorMessage);

        if (!errorMessage.isEmpty()) {
            showError(errorMessage.toString().trim());
            return false;
        }

        return true;
    }

    /**
     * Valida camps obligatoris.
     *
     * @param errorMessage constructor per acumular missatges d'error
     */
    private void validateRequiredFields(StringBuilder errorMessage) {
        if (nickField.getText().trim().isEmpty()) errorMessage.append("El nick es obligatori\n");
        if (nifField.getText().trim().isEmpty()) errorMessage.append("El NIF es obligatori\n");
        if (nomField.getText().trim().isEmpty()) errorMessage.append("El nom es obligatori\n");
        if (cognom1Field.getText().trim().isEmpty()) errorMessage.append("El primer cognom es obligatori\n");
        if (localitatField.getText().trim().isEmpty()) errorMessage.append("La localitat es obligatoria\n");
        if (provinciaField.getText().trim().isEmpty()) errorMessage.append("La provincia es obligatoria\n");
        if (carrerField.getText().trim().isEmpty()) errorMessage.append("El carrer es obligatori\n");
        if (cpField.getText().trim().isEmpty()) errorMessage.append("El codi postal es obligatori\n");
        if (tlfField.getText().trim().isEmpty()) errorMessage.append("El telefon es obligatori\n");
        if (emailField.getText().trim().isEmpty()) errorMessage.append("L'email es obligatori\n");
        if (passwordField.getText().trim().isEmpty()) errorMessage.append("La contrasenya es obligatoria\n");
        if (passwordConfirmField.getText().trim().isEmpty()) errorMessage.append("Confirma la contrasenya\n");
    }

    /**
     * Valida formats de camps.
     *
     * @param errorMessage constructor per acumular missatges d'error
     */
    private void validateFieldFormats(StringBuilder errorMessage) {
        String nick = nickField.getText().trim();
        String nif = nifField.getText().trim();
        String tlf = tlfField.getText().trim();
        String email = emailField.getText().trim();
        String cp = cpField.getText().trim();
        String pwd = passwordField.getText();

        if (nick.length() > 10) errorMessage.append("El nick ha de tenir com a maxim 10 caracters\n");
        if (nif.length() != 9) errorMessage.append("El NIF ha de tenir 9 caracters (8 numeros i 1 lletra)\n");
        if (tlf.length() != 9) errorMessage.append("El telefon ha de tenir 9 digits\n");
        if (cp.length() != 5) errorMessage.append("El codi postal ha de tenir 5 digits\n");
        if (!pwd.equals(passwordConfirmField.getText())) errorMessage.append("Les contrasenyes no coincideixen\n");

        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            errorMessage.append("Format d'email invalid\n");
        }
    }

    /**
     * Crea l'objecte User amb les dades del formulari.
     *
     * @return usuari amb les dades del formulari
     */
    User createFullUser() {
        User user = new User();
        user.setNick(nickField.getText().trim());
        user.setNif(nifField.getText().trim());
        user.setNom(nomField.getText().trim());
        user.setCognom1(cognom1Field.getText().trim());
        user.setCognom2(cognom2Field.getText().trim());
        user.setLocalitat(localitatField.getText().trim());
        user.setProvincia(provinciaField.getText().trim());
        user.setCarrer(carrerField.getText().trim());
        user.setCp(cpField.getText().trim());
        user.setTlf(tlfField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPassword(passwordField.getText());

        return user;
    }

    /**
     * Executa la tasca d'actualitzacio en segon pla.
     *
     * @param updatedUser usuari amb les dades actualitzades
     */
    private void executeUpdateTask(User updatedUser) {
        Task<User> updateTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                LOGGER.log(Level.FINE, "Actualitzant usuari amb ID: {0}", currentUserId);
                return userService.updateUser(currentUserId, updatedUser); // CAMBIO: Ahora es Long
            }
        };

        updateTask.setOnSucceeded(e -> {
            User updatedUserResult = updateTask.getValue();
            updateSessionStore(updatedUserResult);
            showSuccessAndNavigate();
            LOGGER.log(Level.INFO, "Usuari actualitzat correctament: {0}", updatedUserResult.getNom());
        });

        updateTask.setOnFailed(e -> {
            Throwable exception = updateTask.getException();
            String errorMsg = "Error actualitzant perfil: " +
                    (exception instanceof ApiException ? exception.getMessage() : "Error de connexio");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error actualitzant usuari", exception);
        });

        saveButton.setDisable(true);
        errorLabel.setVisible(false);
        ApiClient.BG_EXEC.submit(updateTask);
    }

    /**
     * Actualitza les dades a SessionStore.
     *
     * @param updatedUser usuari amb les dades actualitzades
     */
    void updateSessionStore(User updatedUser) {
        SessionStore store = SessionStore.getInstance();
        store.setNom(updatedUser.getNom());
        store.setCognom1(updatedUser.getCognom1());
        store.setCognom2(updatedUser.getCognom2());
    }

    /**
     * Mostra exit i navega a la vista principal.
     */
    private void showSuccessAndNavigate() {
        errorLabel.setText("Perfil actualitzat correctament!");
        errorLabel.getStyleClass().removeAll("error-label");
        errorLabel.getStyleClass().add("success-label");
        errorLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() ->
                        navigator.showMainView("/com/bibliosedaos/desktop/welcome-view.fxml"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Mostra un missatge d'error.
     *
     * @param message missatge d'error a mostrar
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("success-label");
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(true);
        saveButton.setDisable(false);
    }
}