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
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.SVGPath;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per al formulari de gestio d'usuaris.
 * Gestiona la creacio, edicio i visualitzacio d'usuaris en diferents modes (CREATE, EDIT, VIEW).
 *
 * Assistencia d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
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

    @SuppressWarnings("unused") // inyectado por FXML, análisis estático puede advertir
    @FXML private BorderPane userFormRoot;
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
        try {
            if (saveButton != null) AnimationUtils.applyClickEffect(saveButton);
            if (cancelButton != null) AnimationUtils.applyClickEffect(cancelButton);
            if (backButton != null) AnimationUtils.applyClickEffect(backButton);
            if (editButton != null) AnimationUtils.applyClickEffect(editButton);
            if (deleteButton != null) AnimationUtils.applyClickEffect(deleteButton);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error aplicant efectes d'animacio", e);
        }

        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(true);
        }

        if (roleCombo != null) {
            roleCombo.setItems(FXCollections.observableArrayList(ROLE_USER, ROLE_ADMIN));
            roleCombo.setValue(ROLE_USER);
        }

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
        if (node == null) return;
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

        if (userFormRoot != null && mode != null) {
            userFormRoot.getStyleClass().removeIf(s -> s.startsWith("mode-"));
            userFormRoot.getStyleClass().add("mode-" + mode.toLowerCase());
        }

        setVisibleManaged(saveButton, false);
        setVisibleManaged(cancelButton, false);
        setVisibleManaged(backButton, false);
        setVisibleManaged(editButton, false);
        setVisibleManaged(deleteButton, false);
        setVisibleManaged(roleCombo, false);
        setVisibleManaged(rolLabel, true);

        if (formIcon != null) {
            formIcon.getStyleClass().removeAll("icon-eye", "icon-pencil", "icon-plus");
            formIcon.setVisible(true);
            formIcon.setManaged(true);
        }

        switch (mode) {
            case MODE_VIEW:
                titleLabel.setText("Veure " + ROLE_USER);
                setFieldsEditable(false);

                setVisibleManaged(backButton, true);
                setVisibleManaged(editButton, true);
                setVisibleManaged(deleteButton, true);

                setVisibleManaged(rolLabel, true);
                setVisibleManaged(roleCombo, false);

                if (formIcon != null) {
                    formIcon.setContent(iconEye);
                    formIcon.getStyleClass().add("icon-eye");
                }
                break;

            case MODE_EDIT:
                titleLabel.setText("Editar " + ROLE_USER);
                setFieldsEditable(true);

                setVisibleManaged(saveButton, true);
                setVisibleManaged(cancelButton, true);

                setVisibleManaged(rolLabel, true);
                setVisibleManaged(roleCombo, false);

                if (formIcon != null) {
                    formIcon.setContent(iconPencil);
                    formIcon.getStyleClass().add("icon-pencil");
                }
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

                if (formIcon != null) {
                    formIcon.setContent(iconPlus);
                    formIcon.getStyleClass().add("icon-plus");
                }
                break;

            default:
                titleLabel.setText("Veure " + ROLE_USER);
                setFieldsEditable(false);
                setVisibleManaged(backButton, true);
                setVisibleManaged(editButton, true);
                setVisibleManaged(deleteButton, true);
                if (formIcon != null) {
                    formIcon.setContent(iconEye);
                    formIcon.getStyleClass().add("icon-eye");
                }
                break;
        }

        if (editButton != null) {
            editButton.setOnAction(e -> {
                if (currentUser != null) {
                    setUserData(currentUser, MODE_EDIT);
                }
            });
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> {
                if (currentUser != null) {
                    onDelete();
                }
            });
        }
    }

    /**
     * Estableix l'edicio dels camps del formulari.
     *
     * @param editable true per permetre edicio, false per bloquejar
     */
    private void setFieldsEditable(boolean editable) {
        if (nickField != null) nickField.setEditable(editable);
        if (nifField != null) nifField.setEditable(editable);
        if (nomField != null) nomField.setEditable(editable);
        if (cognom1Field != null) cognom1Field.setEditable(editable);
        if (cognom2Field != null) cognom2Field.setEditable(editable);
        if (localitatField != null) localitatField.setEditable(editable);
        if (provinciaField != null) provinciaField.setEditable(editable);
        if (carrerField != null) carrerField.setEditable(editable);
        if (cpField != null) cpField.setEditable(editable);
        if (tlfField != null) tlfField.setEditable(editable);
        if (emailField != null) emailField.setEditable(editable);
        if (passwordField != null) passwordField.setEditable(editable);
        if (passwordConfirmField != null) passwordConfirmField.setEditable(editable);

        boolean disable = !editable;
        if (nickField != null) nickField.setDisable(disable);
        if (nifField != null) nifField.setDisable(disable);
        if (nomField != null) nomField.setDisable(disable);
        if (cognom1Field != null) cognom1Field.setDisable(disable);
        if (cognom2Field != null) cognom2Field.setDisable(disable);
        if (localitatField != null) localitatField.setDisable(disable);
        if (provinciaField != null) provinciaField.setDisable(disable);
        if (carrerField != null) carrerField.setDisable(disable);
        if (cpField != null) cpField.setDisable(disable);
        if (tlfField != null) tlfField.setDisable(disable);
        if (emailField != null) emailField.setDisable(disable);
        if (passwordField != null) passwordField.setDisable(disable);
        if (passwordConfirmField != null) passwordConfirmField.setDisable(disable);
        if (roleCombo != null) roleCombo.setDisable(disable);
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
        if (userIdLabel != null) userIdLabel.setText(user.getId() != null ? user.getId().toString() : "");
        if (rolLabel != null) rolLabel.setText(user.getRol() == 2 ? ROLE_ADMIN : ROLE_USER);

        if (nickField != null) nickField.setText(safeGet(user.getNick()));
        if (nifField != null) nifField.setText(safeGet(user.getNif()));
        if (nomField != null) nomField.setText(safeGet(user.getNom()));
        if (cognom1Field != null) cognom1Field.setText(safeGet(user.getCognom1()));
        if (cognom2Field != null) cognom2Field.setText(safeGet(user.getCognom2()));
        if (localitatField != null) localitatField.setText(safeGet(user.getLocalitat()));
        if (provinciaField != null) provinciaField.setText(safeGet(user.getProvincia()));
        if (carrerField != null) carrerField.setText(safeGet(user.getCarrer()));
        if (cpField != null) cpField.setText(safeGet(user.getCp()));
        if (tlfField != null) tlfField.setText(safeGet(user.getTlf()));
        if (emailField != null) emailField.setText(safeGet(user.getEmail()));

        if (roleCombo != null) {
            roleCombo.setValue(user.getRol() == 2 ? ROLE_ADMIN : ROLE_USER);
        }

        if (passwordField != null) passwordField.setText("");
        if (passwordConfirmField != null) passwordConfirmField.setText("");
    }

    /**
     * Neteja tots els camps del formulari.
     */
    private void clearForm() {
        if (nickField != null) nickField.setText("");
        if (nifField != null) nifField.setText("");
        if (nomField != null) nomField.setText("");
        if (cognom1Field != null) cognom1Field.setText("");
        if (cognom2Field != null) cognom2Field.setText("");
        if (localitatField != null) localitatField.setText("");
        if (provinciaField != null) provinciaField.setText("");
        if (carrerField != null) carrerField.setText("");
        if (cpField != null) cpField.setText("");
        if (tlfField != null) tlfField.setText("");
        if (emailField != null) emailField.setText("");
        if (passwordField != null) passwordField.setText("");
        if (passwordConfirmField != null) passwordConfirmField.setText("");
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
    private String safeGet(String value) {
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
            if (passwordField.getText().trim().isEmpty()) errorMessage.append("La contrasenyaa és obligatoria\n");
            if (passwordConfirmField.getText().trim().isEmpty()) errorMessage.append("Torna a escriue la contraseña\n");
        }

        String nick = nickField.getText().trim();
        String nif = nifField.getText().trim();
        String tlf = tlfField.getText().trim();
        String email = emailField.getText().trim();
        String cp = cpField.getText().trim();
        String pwd = passwordField.getText();

        if (nick.length() > 10) errorMessage.append("El nick ha de tenir màxim 10 caràcters\n");
        if (nif.length() != 9) errorMessage.append("El NIF ha de tenir 9 caràcters\n");
        if (tlf.length() != 9) errorMessage.append("El telèfon ha de tenir 9 dígits\n");
        if (cp.length() != 5) errorMessage.append("El codi postal ha de tenir 5 dígits\n");

        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) errorMessage.append("Formato de email inválido\n");

        if (!pwd.isEmpty() && !pwd.equals(passwordConfirmField.getText())) {
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

        if (MODE_CREATE.equals(mode) && roleCombo != null && roleCombo.isVisible()) {
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

        createTask.setOnSucceeded(e -> showSuccessAndNavigate("Usuario creado correctamente"));

        createTask.setOnFailed(e -> {
            Throwable exception = createTask.getException();
            String errorMsg = "Error al crear usuari: " +
                    (exception instanceof ApiException ? exception.getMessage() : "Error de conexió");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error al crear usuari", exception);
        });

        setVisibleManaged(saveButton, false); // evitar doble pulsación
        if (errorLabel != null) errorLabel.setVisible(false);
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
            String errorMsg = "Error actualizant usuari: " +
                    (exception instanceof ApiException ? exception.getMessage() : "Error de conexió");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error actualizant usuari", exception);
        });

        setVisibleManaged(saveButton, false);
        if (errorLabel != null) errorLabel.setVisible(false);
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
        if (saveButton != null) saveButton.setDisable(false);
    }
}