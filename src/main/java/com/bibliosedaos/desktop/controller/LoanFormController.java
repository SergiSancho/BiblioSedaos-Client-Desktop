package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Exemplar;
import com.bibliosedaos.desktop.model.Prestec;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.service.ExemplarService;
import com.bibliosedaos.desktop.service.PrestecService;
import com.bibliosedaos.desktop.service.UserService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per al formulari de gestio de prestecs.
 * Gestiona la creacio i visualitzacio de prestecs en diferents modes (CREATE, VIEW).
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class LoanFormController {

    private static final Logger LOGGER = Logger.getLogger(LoanFormController.class.getName());
    private static final String LOANS_LIST_PATH = "/com/bibliosedaos/desktop/loans-list-view.fxml";
    private static final String MODE_CREATE = "CREATE";
    private static final String MODE_VIEW = "VIEW";
    private static final String ERROR_DESCONEGUT = "Error desconegut";

    @FXML private Label loanIdLabel;
    @FXML private Label dataPrestecLabel;
    @FXML private Label dataDevolucioLabel;
    @FXML private Label exemplarIdLabel;
    @FXML private Label titolLlibreLabel;
    @FXML private Label llocExemplarLabel;
    @FXML private Label usuariIdLabel;
    @FXML private Label nomUsuariLabel;
    @FXML private Label emailUsuariLabel;
    @FXML private TextField exemplarIdField;
    @FXML private TextField usuariIdField;
    @FXML private VBox createContainer;
    @FXML private VBox viewContainer;
    @FXML private Button createButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button devolucioButton;
    @FXML private Button searchExemplarButton;
    @FXML private Button searchUserButton;
    @FXML private Label titleLabel;
    @FXML private Label errorLabel;
    @FXML private SVGPath formIcon;
    @FXML private Label requiredNoteLabel;

    private final PrestecService prestecService;
    private final ExemplarService exemplarService;
    private final UserService userService;
    private final Navigator navigator;
    private String mode;
    private Prestec currentPrestec;
    private boolean initialized = false;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Constructor amb injeccio de dependencies.
     *
     * @param prestecService servei de gestio de prestecs
     * @param exemplarService servei de gestio d'exemplars
     * @param userService servei de gestio d'usuaris
     * @param navigator gestor de navegacio
     * @throws NullPointerException si alguna dependencia es null
     */
    public LoanFormController(PrestecService prestecService,
                              ExemplarService exemplarService,
                              UserService userService,
                              Navigator navigator) {
        this.prestecService = Objects.requireNonNull(prestecService, "PrestecService no pot ser null");
        this.exemplarService = Objects.requireNonNull(exemplarService, "ExemplarService no pot ser null");
        this.userService = Objects.requireNonNull(userService, "UserService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador despres de carregar el FXML.
     * Configura efectes visuals i modes d'operacio.
     */
    @FXML
    private void initialize() {
        aplicarEfectesBotons();
        configurarErrorLabel();

        if (mode == null) mode = MODE_VIEW;

        setupForm();
        loadLoanData();

        initialized = true;
    }

    /**
     * Aplica efectes visuals als botons.
     */
    private void aplicarEfectesBotons() {
        AnimationUtils.safeApplyClick(createButton);
        AnimationUtils.safeApplyClick(cancelButton);
        AnimationUtils.safeApplyClick(backButton);
        AnimationUtils.safeApplyClick(devolucioButton);
        AnimationUtils.safeApplyClick(searchExemplarButton);
        AnimationUtils.safeApplyClick(searchUserButton);
    }

    /**
     * Configura el label d'errors.
     */
    private void configurarErrorLabel() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(true);
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
        formIcon.getStyleClass().removeAll("icon-eye", "icon-plus");
        formIcon.setVisible(true);
        formIcon.setManaged(true);

        switch (mode) {
            case MODE_VIEW:
                configurarModeView();
                break;
            case MODE_CREATE:
                configurarModeCreate();
                break;
            default:
                configurarModeView();
                break;
        }
    }

    /**
     * Configura el formulari en mode VIEW.
     */
    private void configurarModeView() {
        titleLabel.setText("Veure Prestec");
        setVisibleManaged(createContainer, false);
        setVisibleManaged(viewContainer, true);
        setVisibleManaged(createButton, false);
        setVisibleManaged(backButton, true);
        setVisibleManaged(cancelButton, false);
        setVisibleManaged(requiredNoteLabel, false);
        formIcon.setContent("M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z");
        formIcon.getStyleClass().add("icon-eye");
    }

    /**
     * Configura el formulari en mode CREATE.
     */
    private void configurarModeCreate() {
        titleLabel.setText("Crear Nou Prestec");
        setVisibleManaged(createContainer, true);
        setVisibleManaged(viewContainer, false);
        setVisibleManaged(createButton, true);
        setVisibleManaged(backButton, false);
        setVisibleManaged(cancelButton, true);
        setVisibleManaged(devolucioButton, false);
        setVisibleManaged(requiredNoteLabel, true);
        formIcon.setContent("M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm6 13h-5v5h-2v-5h-5v-2h5v-5h2v5h5v2z");
        formIcon.getStyleClass().add("icon-plus");
    }

    /**
     * Carrega les dades del prestec al formulari.
     */
    private void loadLoanData() {
        if (MODE_CREATE.equals(mode)) {
            clearForm();
            return;
        }

        if (currentPrestec != null) {
            populateFormWithLoanData(currentPrestec);
        } else {
            showError("No s'han carregat les dades del prestec");
        }
    }

    /**
     * Omple el formulari amb les dades del prestec.
     *
     * @param prestec prestec amb les dades a mostrar
     */
    private void populateFormWithLoanData(Prestec prestec) {
        // Dades del prestec
        loanIdLabel.setText(prestec.getId() != null ? prestec.getId().toString() : "");
        dataPrestecLabel.setText(prestec.getDataPrestec() != null ?
                prestec.getDataPrestec().format(dateFormatter) : "");
        dataDevolucioLabel.setText(prestec.getDataDevolucio() != null ?
                prestec.getDataDevolucio().format(dateFormatter) : "No retornat");

        boolean isRetornat = prestec.getDataDevolucio() != null;
        if (MODE_VIEW.equals(mode)) {
            devolucioButton.setVisible(!isRetornat);
            devolucioButton.setManaged(!isRetornat);
        }

        if (prestec.getExemplar() != null) {
            exemplarIdLabel.setText(prestec.getExemplar().getId() != null ?
                    prestec.getExemplar().getId().toString() : "");
            llocExemplarLabel.setText(safeGet(prestec.getExemplar().getLloc()));

            if (prestec.getExemplar().getLlibre() != null) {
                titolLlibreLabel.setText(safeGet(prestec.getExemplar().getLlibre().getTitol()));
            } else {
                titolLlibreLabel.setText("");
            }
        } else {
            exemplarIdLabel.setText("");
            titolLlibreLabel.setText("");
            llocExemplarLabel.setText("");
        }

        if (prestec.getUsuari() != null) {
            usuariIdLabel.setText(prestec.getUsuari().getId() != null ?
                    prestec.getUsuari().getId().toString() : "");
            nomUsuariLabel.setText(safeGet(prestec.getUsuari().getNom()) + " " +
                    safeGet(prestec.getUsuari().getCognom1()));
            emailUsuariLabel.setText(safeGet(prestec.getUsuari().getEmail()));
        } else {
            usuariIdLabel.setText("");
            nomUsuariLabel.setText("");
            emailUsuariLabel.setText("");
        }
    }

    /**
     * Neteja tots els camps del formulari.
     */
    private void clearForm() {
        exemplarIdField.setText("");
        usuariIdField.setText("");
        errorLabel.setVisible(false);
    }

    /**
     * Estableix les dades del prestec i el mode d'operacio.
     *
     * @param prestec prestec a mostrar/editar
     * @param mode mode d'operacio (CREATE, VIEW)
     */
    public void setLoanData(Prestec prestec, String mode) {
        this.currentPrestec = prestec;
        this.mode = mode != null ? mode : MODE_VIEW;

        if (initialized) {
            setupForm();
            loadLoanData();
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
     * Gestiona l'accio de crear un nou prestec.
     */
    @FXML
    private void onCreate() {
        if (!validateCreateFields()) return;

        Prestec nouPrestec = createPrestecFromForm();
        createLoan(nouPrestec);
    }

    /**
     * Gestiona l'accio de cancel·lar i tornar a la llista de prestecs.
     */
    @FXML
    private void onCancel() {
        navigator.showMainView(LOANS_LIST_PATH);
    }

    /**
     * Gestiona l'accio de tornar enrere a la llista de prestecs.
     */
    @FXML
    private void onBack() {
        navigator.showMainView(LOANS_LIST_PATH);
    }

    /**
     * Valida els camps del formulari per a la creacio.
     *
     * @return true si tots els camps son valids, false altrament
     */
    private boolean validateCreateFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (exemplarIdField.getText().trim().isEmpty()) {
            errorMessage.append("L'ID de l'exemplar es obligatori\n");
        } else if (!exemplarIdField.getText().trim().matches("^\\d+$")) {
            errorMessage.append("L'ID de l'exemplar ha de ser numeric\n");
        }

        if (usuariIdField.getText().trim().isEmpty()) {
            errorMessage.append("L'ID de l'usuari es obligatori\n");
        } else if (!usuariIdField.getText().trim().matches("^\\d+$")) {
            errorMessage.append("L'ID de l'usuari ha de ser numeric\n");
        }

        if (!errorMessage.isEmpty()) {
            showError(errorMessage.toString().trim());
            return false;
        }

        return true;
    }

    /**
     * Crea un objecte Prestec amb les dades del formulari.
     *
     * @return objecte Prestec amb les dades del formulari
     */
    private Prestec createPrestecFromForm() {
        Prestec prestec = new Prestec();

        try {
            Long exemplarId = Long.parseLong(exemplarIdField.getText().trim());
            Long usuariId = Long.parseLong(usuariIdField.getText().trim());

            Exemplar exemplar = new Exemplar();
            exemplar.setId(exemplarId);
            prestec.setExemplar(exemplar);

            User usuari = new User();
            usuari.setId(usuariId);
            prestec.setUsuari(usuari);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error parsejant IDs a Long", e);
            throw new IllegalArgumentException("Error en el format dels IDs");
        }

        return prestec;
    }

    /**
     * Crea un nou prestec al sistema.
     *
     * @param prestec prestec a crear
     */
    private void createLoan(Prestec prestec) {
        Task<Prestec> createTask = new Task<>() {
            @Override
            protected Prestec call() throws Exception {
                Prestec createdPrestec = prestecService.createPrestec(prestec);
                return prestecService.getPrestecById(createdPrestec.getId());
            }
        };

        createTask.setOnSucceeded(e -> {
            Prestec createdPrestec = createTask.getValue();
            showSuccessAndNavigate(createdPrestec);
        });

        createTask.setOnFailed(e -> {
            Throwable exception = createTask.getException();
            String errorMsg = "Error al crear prestec: " +
                    (exception instanceof ApiException ? exception.getMessage() : "Error de connexio");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error al crear prestec", exception);
        });

        setVisibleManaged(createButton, false);
        errorLabel.setVisible(false);
        ApiClient.BG_EXEC.submit(createTask);
    }

    /**
     * Mostra un missatge d'exit i navega a la vista del prestec creat.
     *
     * @param prestec prestec creat
     */
    private void showSuccessAndNavigate(Prestec prestec) {
        errorLabel.setText("Prestec creat correctament");
        errorLabel.getStyleClass().removeAll("error-label");
        errorLabel.getStyleClass().add("success-label");
        errorLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(1200);
                Platform.runLater(() -> setLoanData(prestec, MODE_VIEW));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Gestiona l'accio de retornar el prestec.
     */
    @FXML
    private void onDevolucio() {
        if (currentPrestec == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar devolucio");
        alert.setHeaderText("Vols marcar aquest prestec com a retornat?");
        alert.setContentText(String.format("ID: %d%nTitol: %s%nUsuari: %s",
                currentPrestec.getId(),
                currentPrestec.getExemplar() != null && currentPrestec.getExemplar().getLlibre() != null ?
                        currentPrestec.getExemplar().getLlibre().getTitol() : "",
                currentPrestec.getUsuari() != null ?
                        (safeGet(currentPrestec.getUsuari().getNom()) + " " + safeGet(currentPrestec.getUsuari().getCognom1())) : ""
        ));
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) performReturnPrestec(currentPrestec.getId());
        });
    }

    /**
     * Executa el retorn d'un prestec i torna a la llista.
     *
     * @param prestecId ID del prestec a retornar
     */
    private void performReturnPrestec(Long prestecId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                prestecService.retornarPrestec(prestecId);
                return null;
            }
        };

        task.setOnSucceeded(e -> navigator.showMainView(LOANS_LIST_PATH));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error retornant prestec", ex);
            showError("Error retornant prestec: " + (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona la cerca d'exemplars.
     */
    @FXML
    private void onSearchExemplars() {
        String idText = exemplarIdField.getText().trim();

        if (!idText.isEmpty()) {
            try {
                Long id = Long.parseLong(idText);
                searchExemplarById(id);
            } catch (NumberFormatException ex) {
                showError("L'ID ha de ser un numero valid");
            }
        } else {
            showAllExemplars();
        }
    }

    /**
     * Gestiona la cerca d'usuaris.
     */
    @FXML
    private void onSearchUsers() {
        String idText = usuariIdField.getText().trim();

        if (!idText.isEmpty()) {
            try {
                Long id = Long.parseLong(idText);
                searchUserById(id);
            } catch (NumberFormatException ex) {
                showError("L'ID ha de ser un numero valid");
            }
        } else {
            showAllUsers();
        }
    }

    /**
     * Cerca un exemplar per ID.
     *
     * @param id ID de l'exemplar a cercar
     */
    private void searchExemplarById(Long id) {
        Task<Exemplar> task = new Task<>() {
            @Override
            protected Exemplar call() throws Exception {
                return exemplarService.getExemplarById(id);
            }
        };

        task.setOnSucceeded(e -> {
            Exemplar exemplar = task.getValue();
            if (exemplar != null) {
                showSingleExemplarDialog(exemplar);
            } else {
                showError("No s'ha trobat cap exemplar amb ID: " + id);
            }
        });

        task.setOnFailed(e -> showError("Error cercant exemplar: " + task.getException().getMessage()));

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Cerca un usuari per ID.
     *
     * @param id ID de l'usuari a cercar
     */
    private void searchUserById(Long id) {
        Task<User> task = new Task<>() {
            @Override
            protected User call() throws Exception {
                return userService.getUserById(id);
            }
        };

        task.setOnSucceeded(e -> {
            User user = task.getValue();
            if (user != null) {
                showSingleUserDialog(user);
            } else {
                showError("No s'ha trobat cap usuari amb ID: " + id);
            }
        });

        task.setOnFailed(e -> showError("Error cercant usuari: " + task.getException().getMessage()));

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Mostra tots els exemplars disponibles.
     */
    private void showAllExemplars() {
        Task<List<Exemplar>> task = new Task<>() {
            @Override
            protected List<Exemplar> call() throws Exception {
                return exemplarService.getExemplarsLliures();
            }
        };

        task.setOnSucceeded(e -> {
            List<Exemplar> exemplars = task.getValue();
            showExemplarsDialog(exemplars);
        });

        task.setOnFailed(e -> showError("Error carregant exemplars: " + task.getException().getMessage()));

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Mostra tots els usuaris.
     */
    private void showAllUsers() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return userService.getAllUsers();
            }
        };

        task.setOnSucceeded(e -> {
            List<User> users = task.getValue();
            showUsersDialog(users);
        });

        task.setOnFailed(e -> showError("Error carregant usuaris: " + task.getException().getMessage()));

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Mostra el dialeg amb un unic exemplar.
     *
     * @param exemplar exemplar a mostrar
     */
    private void showSingleExemplarDialog(Exemplar exemplar) {
        showExemplarsDialog(List.of(exemplar));
    }

    /**
     * Mostra el dialeg amb un unic usuari.
     *
     * @param user usuari a mostrar
     */
    private void showSingleUserDialog(User user) {
        showUsersDialog(List.of(user));
    }

    /**
     * Mostra el dialeg de seleccio d'exemplars.
     *
     * @param exemplars llista d'exemplars a mostrar
     */
    private void showExemplarsDialog(List<Exemplar> exemplars) {
        Dialog<Long> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Exemplar");
        dialog.setHeaderText(exemplars.size() == 1 ? "Exemplar trobat" : "Exemplars disponibles");

        TableView<Exemplar> table = crearTaulaExemplars(exemplars);
        configurarDialegSeleccio(dialog, table, exemplars);

        Platform.runLater(() -> {
            Optional<Long> result = dialog.showAndWait();
            result.ifPresent(exemplarId -> exemplarIdField.setText(exemplarId.toString()));
        });
    }

    /**
     * Crea la taula d'exemplars per al dialeg.
     *
     * @param exemplars llista d'exemplars
     * @return taula configurada
     */
    private TableView<Exemplar> crearTaulaExemplars(List<Exemplar> exemplars) {
        TableView<Exemplar> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(exemplars));
        table.setPrefSize(400, 200);

        TableColumn<Exemplar, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);

        TableColumn<Exemplar, String> titolColumn = new TableColumn<>("Titol");
        titolColumn.setCellValueFactory(cell -> {
            Exemplar ex = cell.getValue();
            if (ex.getLlibre() != null && ex.getLlibre().getTitol() != null) {
                return new SimpleStringProperty(ex.getLlibre().getTitol());
            }
            return new SimpleStringProperty("Sense titol");
        });
        titolColumn.setPrefWidth(200);

        TableColumn<Exemplar, String> llocColumn = new TableColumn<>("Lloc");
        llocColumn.setCellValueFactory(new PropertyValueFactory<>("lloc"));
        llocColumn.setPrefWidth(120);

        Collections.addAll(table.getColumns(), idColumn, titolColumn, llocColumn);
        return table;
    }

    /**
     * Mostra el dialeg de seleccio d'usuaris.
     *
     * @param users llista d'usuaris a mostrar
     */
    private void showUsersDialog(List<User> users) {
        Dialog<Long> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Usuari");
        dialog.setHeaderText(users.size() == 1 ? "Usuari trobat" : "Tots els usuaris");

        TableView<User> table = crearTaulaUsuaris(users);
        configurarDialegSeleccio(dialog, table, users);

        Platform.runLater(() -> {
            Optional<Long> result = dialog.showAndWait();
            result.ifPresent(userId -> usuariIdField.setText(userId.toString()));
        });
    }

    /**
     * Crea la taula d'usuaris per al dialeg.
     *
     * @param users llista d'usuaris
     * @return taula configurada
     */
    private TableView<User> crearTaulaUsuaris(List<User> users) {
        TableView<User> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(users));
        table.setPrefSize(400, 200);

        TableColumn<User, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);

        TableColumn<User, String> nomColumn = new TableColumn<>("Nom");
        nomColumn.setCellValueFactory(cell -> {
            User user = cell.getValue();
            String nomComplet = (user.getNom() != null ? user.getNom() : "") + " " +
                    (user.getCognom1() != null ? user.getCognom1() : "");
            return new SimpleStringProperty(nomComplet.trim());
        });
        nomColumn.setPrefWidth(150);

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(170);

        Collections.addAll(table.getColumns(), idColumn, nomColumn, emailColumn);
        return table;
    }

    /**
     * Configura el dialeg de seleccio.
     *
     * @param dialog dialeg a configurar
     * @param table taula de dades
     * @param items llista d'items
     * @param <T> tipus dels items
     */
    private <T> void configurarDialegSeleccio(Dialog<Long> dialog, TableView<T> table, List<T> items) {
        ButtonType selectButton = new ButtonType("Seleccionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButton, ButtonType.CANCEL);

        Button selectBtn = (Button) dialog.getDialogPane().lookupButton(selectButton);
        selectBtn.setDisable(items.size() > 1);

        if (items.size() == 1) {
            table.getSelectionModel().select(0);
        }

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                selectBtn.setDisable(newVal == null)
        );

        dialog.getDialogPane().setContent(new VBox(10, table));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == selectButton) {
                T selected = table.getSelectionModel().getSelectedItem();
                if (selected instanceof Exemplar exemplar) {
                    return exemplar.getId();
                } else if (selected instanceof User user) {
                    return user.getId();
                }
            }
            return null;
        });
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
        createButton.setDisable(false);
    }
}