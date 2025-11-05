package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.service.UserService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.shape.SVGPath;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per al formulari de gestio d'usuaris.
 * Gestiona la creacio, edicio i visualitzacio d'usuaris en diferents modes (CREATE, EDIT, VIEW).
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class UserFormController {

    private static final Logger LOGGER = Logger.getLogger(UserFormController.class.getName());
    private static final String ROLE_USER = "Usuari/a";
    private static final String ROLE_ADMIN = "Administrador/a";
    private static final String MODE_CREATE = "CREATE";
    private static final String MODE_EDIT = "EDIT";
    private static final String MODE_VIEW = "VIEW";
    private static final String USERS_LIST_PATH = "/com/bibliosedaos/desktop/users-list-view.fxml";

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

    @FXML private Label titleLabel;
    @FXML private Label userIdLabel;
    @FXML private Label rolLabel;
    @FXML private ComboBox<String> roleCombo;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label errorLabel;
    @FXML private SVGPath formIcon;

    private final UserService userService;
    private final Navigator navigator;
    private String mode;
    private User currentUser;
    private boolean initialized = false;

    /**
     * Constructor amb injeccio de dependencies.
     *
     * @param userService servei de gestio d'usuaris
     * @param navigator gestor de navegacio
     * @throws NullPointerException si alguna dependencia es null
     */
    public UserFormController(UserService userService, Navigator navigator) {
        this.userService = Objects.requireNonNull(userService, "UserService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador despres de carregar el FXML.
     * Configura efectes visuals, modes d'operacio i dades del formulari.
     */
    @FXML
    private void initialize() {
        AnimationUtils.safeApplyClick(saveButton);
        AnimationUtils.safeApplyClick(cancelButton);
        AnimationUtils.safeApplyClick(backButton);
        AnimationUtils.safeApplyClick(editButton);
        AnimationUtils.safeApplyClick(deleteButton);

        errorLabel.setVisible(false);
        errorLabel.setManaged(true);

        roleCombo.setItems(FXCollections.observableArrayList(ROLE_USER, ROLE_ADMIN));
        roleCombo.setValue(ROLE_USER);

        if (mode == null) mode = MODE_VIEW;

        setupForm();
        loadUserData();

        initialized = true;
    }

    /**
     * Configura la visibilitat i gestio d'un node al layout.
     *
     * @param node node a configurar
     * @param visible true per mostrar, false per amagar
     */
    private void setVisibleManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Configura el formulari segons el mode d'operacio.
     */
    private void setupForm() {
        final String iconEye = "M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z";
        final String iconPencil = "M14.078 4.232l-12.64 12.639-1.438 7.129 7.127-1.438 12.641-12.64-5.69-5.69zm-10.369 14.893l-.85-.85 11.141-11.125.849.849-11.14 11.126zm2.008 2.008l-.85-.85 11.141-11.125.85.850-11.141 11.125zm18.283-15.444l-2.816 2.818-5.691-5.691 2.816-2.816 5.691 5.689z";
        final String iconPlus = "M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm6 13h-5v5h-2v-5h-5v-2h5v-5h2v5h5v2z";

        setVisibleManaged(saveButton, false);
        setVisibleManaged(cancelButton, false);
        setVisibleManaged(backButton, false);
        setVisibleManaged(editButton, false);
        setVisibleManaged(deleteButton, false);
        setVisibleManaged(roleCombo, false);
        setVisibleManaged(rolLabel, true);

        formIcon.getStyleClass().removeAll("icon-eye", "icon-pencil", "icon-plus");
        formIcon.setVisible(true);
        formIcon.setManaged(true);

        switch (mode) {
            case MODE_VIEW:
                titleLabel.setText("Veure " + ROLE_USER);
                setFieldsEditable(false);

                setVisibleManaged(backButton, true);
                setVisibleManaged(editButton, true);
                setVisibleManaged(deleteButton, true);

                setVisibleManaged(rolLabel, true);
                setVisibleManaged(roleCombo, false);

                formIcon.setContent(iconEye);
                formIcon.getStyleClass().add("icon-eye");
                break;

            case MODE_EDIT:
                titleLabel.setText("Editar " + ROLE_USER);
                setFieldsEditable(true);

                setVisibleManaged(saveButton, true);
                setVisibleManaged(cancelButton, true);

                setVisibleManaged(rolLabel, true);
                setVisibleManaged(roleCombo, false);

                formIcon.setContent(iconPencil);
                formIcon.getStyleClass().add("icon-pencil");
                break;

            case MODE_CREATE:
                titleLabel.setText("Crear Nova " + ROLE_USER);
                setFieldsEditable(true);

                setVisibleManaged(saveButton, true);
                setVisibleManaged(cancelButton, true);

                setVisibleManaged(userIdLabel, false);
                setVisibleManaged(rolLabel, false);
                setVisibleManaged(roleCombo, true);
                roleCombo.setValue(ROLE_USER);

                formIcon.setContent(iconPlus);
                formIcon.getStyleClass().add("icon-plus");
                break;

            default:
                titleLabel.setText("Veure " + ROLE_USER);
                setFieldsEditable(false);
                setVisibleManaged(backButton, true);
                setVisibleManaged(editButton, true);
                setVisibleManaged(deleteButton, true);
                formIcon.setContent(iconEye);
                formIcon.getStyleClass().add("icon-eye");
                break;
        }

        editButton.setOnAction(e -> {
            if (currentUser != null) {
                setUserData(currentUser, MODE_EDIT);
            }
        });

        deleteButton.setOnAction(e -> {
            if (currentUser != null) {
                onDelete();
            }
        });
    }

    /**
     * Estableix l'edicio dels camps del formulari.
     *
     * @param editable true per permetre edicio, false per bloquejar
     */
    private void setFieldsEditable(boolean editable) {
        nickField.setEditable(editable);
        nifField.setEditable(editable);
        nomField.setEditable(editable);
        cognom1Field.setEditable(editable);
        cognom2Field.setEditable(editable);
        localitatField.setEditable(editable);
        provinciaField.setEditable(editable);
        carrerField.setEditable(editable);
        cpField.setEditable(editable);
        tlfField.setEditable(editable);
        emailField.setEditable(editable);
        passwordField.setEditable(editable);
        passwordConfirmField.setEditable(editable);

        boolean disable = !editable;
        nickField.setDisable(disable);
        nifField.setDisable(disable);
        nomField.setDisable(disable);
        cognom1Field.setDisable(disable);
        cognom2Field.setDisable(disable);
        localitatField.setDisable(disable);
        provinciaField.setDisable(disable);
        carrerField.setDisable(disable);
        cpField.setDisable(disable);
        tlfField.setDisable(disable);
        emailField.setDisable(disable);
        passwordField.setDisable(disable);
        passwordConfirmField.setDisable(disable);
        roleCombo.setDisable(disable);
    }

    /**
     * Carrega les dades de l'usuari al formulari.
     */
    private void loadUserData() {
        if (MODE_CREATE.equals(mode)) {
            clearForm();
            return;
        }

        if (currentUser != null) {
            populateFormWithUserData(currentUser);
        } else {
            showError("No se han carregat les dades del usuari");
        }
    }

    /**
     * Omple el formulari amb les dades de l'usuari.
     *
     * @param user usuari amb les dades a mostrar
     */
    private void populateFormWithUserData(User user) {
        userIdLabel.setText(user.getId() != null ? user.getId().toString() : "");
        rolLabel.setText(user.getRol() == 2 ? ROLE_ADMIN : ROLE_USER);

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

        roleCombo.setValue(user.getRol() == 2 ? ROLE_ADMIN : ROLE_USER);

        passwordField.setText("");
        passwordConfirmField.setText("");
    }

    /**
     * Neteja tots els camps del formulari.
     */
    private void clearForm() {
        nickField.setText("");
        nifField.setText("");
        nomField.setText("");
        cognom1Field.setText("");
        cognom2Field.setText("");
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
     * Estableix les dades de l'usuari i el mode d'operacio.
     *
     * @param user usuari a mostrar/editar
     * @param mode mode d'operacio (CREATE, EDIT, VIEW)
     */
    public void setUserData(User user, String mode) {
        this.currentUser = user;
        this.mode = mode != null ? mode : MODE_VIEW;

        if (initialized) {
            setupForm();
            loadUserData();
        }
    }

    /**
     * Retorna una cadena segura sense valors null.
     *
     * @param value cadena a verificar
     * @return cadena original o cadena buida si es null
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

        User user = createUserFromForm();
        if (MODE_CREATE.equals(mode)) {
            createUser(user);
        } else if (MODE_EDIT.equals(mode)) {
            updateUser(user);
        }
    }

    /**
     * Gestiona l'accio de cancel·lar i tornar a la llista d'usuaris.
     */
    @FXML
    private void onCancel() {
        navigator.showMainView(USERS_LIST_PATH);
    }

    /**
     * Gestiona l'accio de tornar enrere a la llista d'usuaris.
     */
    @FXML
    private void onBack() {
        navigator.showMainView(USERS_LIST_PATH);
    }

    /**
     * Gestiona l'accio d'editar l'usuari actual.
     */
    @FXML
    private void onEdit() {
        if (currentUser != null) setUserData(currentUser, MODE_EDIT);
    }

    /**
     * Gestiona l'accio d'eliminar l'usuari actual.
     */
    @FXML
    private void onDelete() {
        if (currentUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminació");
        alert.setHeaderText("Vols eliminar aquest usuari?");
        alert.setContentText(String.format("ID: %d%nNick: %s%nNombre: %s %s",
                currentUser.getId(), currentUser.getNick(), currentUser.getNom(), currentUser.getCognom1()));

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) performDelete(currentUser);
        });
    }

    /**
     * Valida tots els camps del formulari.
     *
     * @return true si tots els camps son valids, false altrament
     */
    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (nickField.getText().trim().isEmpty()) errorMessage.append("El nick és obligatori\n");
        if (nifField.getText().trim().isEmpty()) errorMessage.append("El NIF és obligatori\n");
        if (nomField.getText().trim().isEmpty()) errorMessage.append("El nom és obligatori\n");
        if (cognom1Field.getText().trim().isEmpty()) errorMessage.append("El primer cognom és obligatori\n");
        if (localitatField.getText().trim().isEmpty()) errorMessage.append("La localitat és obligatoria\n");
        if (provinciaField.getText().trim().isEmpty()) errorMessage.append("La provincia és obligatoria\n");
        if (carrerField.getText().trim().isEmpty()) errorMessage.append("El carrer és obligatori\n");
        if (cpField.getText().trim().isEmpty()) errorMessage.append("El codi postal és obligatori\n");
        if (tlfField.getText().trim().isEmpty()) errorMessage.append("El telèfon és obligatori\n");
        if (emailField.getText().trim().isEmpty()) errorMessage.append("El email és obligatori\n");

        if (MODE_CREATE.equals(mode) || MODE_EDIT.equals(mode)) {
            if (passwordField.getText().trim().isEmpty()) errorMessage.append("La contrasenya és obligatoria\n");
            if (passwordConfirmField.getText().trim().isEmpty()) errorMessage.append("Torna a escriure la contrasenya\n");
        }

        String nick = nickField.getText().trim();
        String nif = nifField.getText().trim();
        String telefon = tlfField.getText().trim();
        String email = emailField.getText().trim();
        String codiPostal = cpField.getText().trim();
        String password = passwordField.getText();

        if (nick.length() > 10) errorMessage.append("El nick ha de tenir maxim 10 caracters\n");
        if (nif.length() != 9) errorMessage.append("El NIF ha de tenir 9 caracters\n");
        if (telefon.length() != 9) errorMessage.append("El telèfon ha de tenir 9 digits\n");
        if (codiPostal.length() != 5) errorMessage.append("El codi postal ha de tenir 5 digits\n");

        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) errorMessage.append("Format de email invalid\n");

        if (!password.isEmpty() && !password.equals(passwordConfirmField.getText())) {
            errorMessage.append("Les contrasenyes no coincideixen\n");
        }

        if (!errorMessage.isEmpty()) {
            showError(errorMessage.toString().trim());
            return false;
        }

        return true;
    }

    /**
     * Crea un objecte User amb les dades del formulari.
     *
     * @return objecte User amb les dades del formulari
     */
    private User createUserFromForm() {
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

        if (MODE_CREATE.equals(mode) && roleCombo.isVisible()) {
            String val = roleCombo.getValue();
            if (ROLE_ADMIN.equals(val)) {
                user.setRol(2);
            } else {
                user.setRol(1);
            }
        } else if (currentUser != null) {
            user.setRol(currentUser.getRol());
        } else {
            user.setRol(1);
        }

        return user;
    }

    /**
     * Crea un nou usuari al sistema.
     *
     * @param user usuari a crear
     */
    private void createUser(User user) {
        Task<User> createTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return userService.createUser(user);
            }
        };

        createTask.setOnSucceeded(e -> showSuccessAndNavigate("Usuario creat correctament"));

        createTask.setOnFailed(e -> {
            Throwable exception = createTask.getException();
            String errorMsg = "Error al crear usuari: " +
                    (exception instanceof ApiException ? exception.getMessage() : "Error de connexió");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error al crear usuari", exception);
        });

        setVisibleManaged(saveButton, false);
        errorLabel.setVisible(false);
        ApiClient.BG_EXEC.submit(createTask);
    }

    /**
     * Actualitza un usuari existent al sistema.
     *
     * @param user usuari amb les dades actualitzades
     */
    private void updateUser(User user) {
        if (currentUser == null) {
            showError("Usuari actual no disponible per actualitzar");
            return;
        }

        Task<User> updateTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return userService.updateUser(currentUser.getId(), user);
            }
        };

        updateTask.setOnSucceeded(e -> showSuccessAndNavigate("Usuari actualitzat correctament"));

        updateTask.setOnFailed(e -> {
            Throwable exception = updateTask.getException();
            String errorMsg = "Error actualitzant usuari: " +
                    (exception instanceof ApiException ? exception.getMessage() : "Error de connexió");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error actualitzant usuari", exception);
        });

        setVisibleManaged(saveButton, false);
        errorLabel.setVisible(false);
        ApiClient.BG_EXEC.submit(updateTask);
    }

    /**
     * Executa l'eliminacio d'un usuari.
     *
     * @param user usuari a eliminar
     */
    private void performDelete(User user) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                userService.deleteUser(user.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> navigator.showMainView(USERS_LIST_PATH));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error eliminant usuari", ex);
            showError("Error eliminant usuari: " + (ex != null ? ex.getMessage() : "Error desconegut"));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Mostra un missatge d'exit i navega a la llista d'usuaris.
     *
     * @param message missatge d'exit a mostrar
     */
    private void showSuccessAndNavigate(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("error-label");
        errorLabel.getStyleClass().add("success-label");
        errorLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(1200);
                Platform.runLater(() -> navigator.showMainView(USERS_LIST_PATH));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Mostra un missatge d'error al formulari.
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