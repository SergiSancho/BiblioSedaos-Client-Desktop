package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.model.User;
import com.bibliosedaos.desktop.service.UserService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per a la llista d'usuaris.
 *
 * Gestiona la visualitzacio, cerca i navegacio entre usuaris del sistema.
 * Implementa paginacio i cerca per ID/NIF.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class UsersListController {

    private static final Logger LOGGER = Logger.getLogger(UsersListController.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String USER_FORM_VIEW_PATH = "/com/bibliosedaos/desktop/user-form-view.fxml";

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> idColumn;
    @FXML private TableColumn<User, String> nickColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> cognom1Column;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> rolColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private ScrollPane mainScrollPane;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private TextField searchByIdField;
    @FXML private Button searchByIdButton;
    @FXML private Button newUserButton;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final UserService userService;
    private final Navigator navigator;

    private final ObservableList<User> masterList = FXCollections.observableArrayList();
    private final ObservableList<User> filteredList = FXCollections.observableArrayList();
    private final ObservableList<User> currentPageList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;

    /**
     * Constructor del controlador.
     *
     * @param userService Servei per a operacions amb usuaris
     * @param navigator Sistema de navegacio entre vistes
     */
    public UsersListController(UserService userService, Navigator navigator) {
        this.userService = Objects.requireNonNull(userService, "UserService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador despres de carregar el FXML.
     * Configura la taula, combos, listeners i carrega les dades inicials.
     */
    @FXML
    private void initialize() {
        try {
            applyButtonEffects();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error aplicant efectes d'animació", e);
        }

        setupTable();
        initSearchCombo();
        initSearchTypeCombo();
        setupListeners();
        loadUsers();
        updateSearchByIdPrompt();
    }

    /**
     * Aplica efectes de clic als botons de la interficie.
     */
    private void applyButtonEffects() {
        if (newUserButton != null) AnimationUtils.applyClickEffect(newUserButton);
        if (searchByIdButton != null) AnimationUtils.applyClickEffect(searchByIdButton);
        if (prevPageButton != null) AnimationUtils.applyClickEffect(prevPageButton);
        if (nextPageButton != null) AnimationUtils.applyClickEffect(nextPageButton);
    }

    /**
     * Configura la taula d'usuaris amb les columnes i accions.
     */
    private void setupTable() {
        configureTableColumns();
        setupActionsColumn();
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        usersTable.setItems(currentPageList);
    }

    /**
     * Configura les columnes de la taula amb les propietats de l'objecte User.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nickColumn.setCellValueFactory(new PropertyValueFactory<>("nick"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        cognom1Column.setCellValueFactory(new PropertyValueFactory<>("cognom1"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("rolLabel"));
    }

    /**
     * Configura la columna d'accions amb botons per veure, editar i eliminar.
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> createActionsTableCell());
    }

    /**
     * Crea una cel·la de taula amb botons d'accio.
     *
     * @return TableCell configurada amb botons d'accio
     */
    private TableCell<User, Void> createActionsTableCell() {
        return new TableCell<>() {
            private final Button viewBtn = createActionButton("M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z",
                    "view-btn", "Veure detalls", "Veure");
            private final Button editBtn = createActionButton("M14.078 4.232l-12.64 12.639-1.438 7.129 7.127-1.438 12.641-12.64-5.69-5.69zm-10.369 14.893l-.85-.85 11.141-11.125.849.849-11.14 11.126zm2.008 2.008l-.85-.85 11.141-11.125.85.85-11.141 11.125zm18.283-15.444l-2.816 2.818-5.691-5.691 2.816-2.816 5.691 5.689z",
                    "edit-btn", "Editar usuari", "Editar");
            private final Button delBtn = createActionButton("M3 6v18h18v-18h-18zm5 14c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm4-18v2h-20v-2h5.711c.9 0 1.631-1.099 1.631-2h5.315c0 .901.73 2 1.631 2h5.712z",
                    "delete-btn", "Eliminar usuari", "Eliminar");
            private final HBox box = new HBox(6, viewBtn, editBtn, delBtn);

            {
                configureButtonActions();
            }

            /**
             * Configura les accions dels botons d'acció.
             */
            private void configureButtonActions() {
                viewBtn.setOnAction(e -> handleViewAction());
                editBtn.setOnAction(e -> handleEditAction());
                delBtn.setOnAction(e -> handleDeleteAction());
            }

            private void handleViewAction() {
                User user = getCurrentUser();
                if (user != null) viewUser(user);
            }

            private void handleEditAction() {
                User user = getCurrentUser();
                if (user != null) editUser(user);
            }

            private void handleDeleteAction() {
                User user = getCurrentUser();
                if (user != null) deleteUser(user);
            }

            private User getCurrentUser() {
                int idx = getIndex();
                return isValidIndex(idx) ? getTableView().getItems().get(idx) : null;
            }

            private boolean isValidIndex(int index) {
                return index >= 0 && index < getTableView().getItems().size();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    /**
     * Crea un boto d'accio amb icona.
     *
     * @param svgContent Contingut SVG de l'icona
     * @param styleClass Classe CSS del botó
     * @param tooltipText Text del tooltip
     * @param buttonText Text del botó
     * @return Botó configurat
     */
    private Button createActionButton(String svgContent, String styleClass, String tooltipText, String buttonText) {
        Button button = new Button();
        SVGPath svg = new SVGPath();
        svg.setContent(svgContent);
        svg.getStyleClass().add("action-icon");
        button.setGraphic(svg);
        button.setText(buttonText);
        button.getStyleClass().addAll("action-btn", styleClass);
        button.setTooltip(new Tooltip(tooltipText));

        try {
            AnimationUtils.applyClickEffect(button);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error aplicant efecte de clic", e);
        }

        return button;
    }

    /**
     * Inicialitza el combo de cerca per camps.
     */
    private void initSearchCombo() {
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Tots", "Nick", "Nom", "Cognom", "Email"));
            searchFieldCombo.setValue("Tots");
        }
    }

    /**
     * Inicialitza el combo de tipus de cerca (ID/NIF).
     */
    private void initSearchTypeCombo() {
        if (searchTypeCombo != null) {
            searchTypeCombo.setItems(FXCollections.observableArrayList("ID", "NIF"));
            searchTypeCombo.setValue("ID");
        }
    }

    /**
     * Configura els listeners per als camps de cerca.
     */
    private void setupListeners() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyFilterAndPagination());
        }
        if (searchFieldCombo != null) {
            searchFieldCombo.valueProperty().addListener((obs, o, n) -> applyFilterAndPagination());
        }
        if (searchByIdButton != null) searchByIdButton.setOnAction(e -> onSearchById());
        if (searchByIdField != null) searchByIdField.setOnAction(e -> onSearchById());
        if (searchTypeCombo != null) {
            searchTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> updateSearchByIdPrompt());
        }
    }

    /**
     * Actualitza el prompt text del camp de cerca segons el tipus seleccionat.
     */
    private void updateSearchByIdPrompt() {
        if (searchByIdField != null && searchTypeCombo != null) {
            String searchType = searchTypeCombo.getValue();
            if ("NIF".equals(searchType)) {
                searchByIdField.setPromptText("Introdueix NIF");
            } else {
                searchByIdField.setPromptText("Introdueix ID");
            }
        }
    }

    /**
     * Carrega tots els usuaris del sistema.
     */
    private void loadUsers() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return userService.getAllUsers();
            }
        };

        task.setOnSucceeded(e -> {
            masterList.setAll(task.getValue());
            applyFilterAndPagination();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant usuaris", ex);
            showError("Error carregant usuaris", ex != null ? ex.getMessage() : "Error desconegut");
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Aplica els filtres i la paginacio a la llista d'usuaris.
     */
    private void applyFilterAndPagination() {
        final String query = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
        final String field = (searchFieldCombo == null || searchFieldCombo.getValue() == null) ? "Tots" : searchFieldCombo.getValue();

        filteredList.setAll(masterList.filtered(user -> matchesSearchCriteria(user, query, field)));

        currentPage = 0;
        updatePagination();
    }

    /**
     * Comprova si un usuari compleix amb els criteris de cerca.
     *
     * @param user Usuari a verificar
     * @param query Text de cerca
     * @param field Camp on cercar
     * @return true si l'usuari compleix els criteris
     */
    private boolean matchesSearchCriteria(User user, String query, String field) {
        if (query.isEmpty()) return true;

        return switch (field) {
            case "Nick" -> safeContains(user.getNick(), query);
            case "Nom" -> safeContains(user.getNom(), query);
            case "Cognom" -> safeContains(user.getCognom1(), query) || safeContains(user.getCognom2(), query);
            case "Email" -> safeContains(user.getEmail(), query);
            default -> safeContains(user.getNick(), query) || safeContains(user.getNom(), query)
                    || safeContains(user.getCognom1(), query) || safeContains(user.getCognom2(), query)
                    || safeContains(user.getEmail(), query);
        };
    }

    /**
     * Actualitza la informacio de paginacio.
     */
    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredList.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        updateCurrentPage();
        updatePageButtons();
    }

    /**
     * Actualitza la pagina actual.
     */
    private void updateCurrentPage() {
        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filteredList.size());

        if (from < filteredList.size()) {
            currentPageList.setAll(filteredList.subList(from, to));
        } else {
            currentPageList.clear();
        }

        if (pageInfoLabel != null) {
            pageInfoLabel.setText(String.format("Pàgina %d de %d", currentPage + 1, totalPages));
        }

        Platform.runLater(() -> {
            try {
                if (mainScrollPane != null) {
                    mainScrollPane.setVvalue(0.0);
                }
                if (usersTable != null && !currentPageList.isEmpty()) {
                    usersTable.scrollTo(0);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "No sha pogut fer scroll al principi de la taula", ex);
            }
        });
    }

    /**
     * Actualitza l'estat dels botons de paginacio.
     */
    private void updatePageButtons() {
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 0);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages - 1 || filteredList.isEmpty());
    }

    /**
     * Gestiona la navegacio a la pàgina anterior.
     */
    @FXML
    private void onPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateCurrentPage();
            updatePageButtons();
        }
    }

    /**
     * Gestiona la navegacio a la pagina següent.
     */
    @FXML
    private void onNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateCurrentPage();
            updatePageButtons();
        }
    }

    /**
     * Comprova si un camp conte la cadena de cerca.
     *
     * @param field Camp a verificar
     * @param query Text a cercar
     * @return true si el camp conté la cerca
     */
    private static boolean safeContains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    /**
     * Gestiona la cerca per ID o NIF.
     */
    @FXML
    private void onSearchById() {
        if (searchByIdField == null || searchTypeCombo == null) return;

        String raw = searchByIdField.getText();
        if (raw == null || raw.isBlank()) {
            showSearchError("Error de cerca", "Introdueix un ID o NIF.");
            return;
        }

        final String query = raw.trim();
        final String type = searchTypeCombo.getValue();

        if (!validateSearchInput(query, type)) {
            return;
        }

        Task<User> task = createUserSearchTask(query, type);
        configureTaskHandlers(task, type, query);
        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Valida l'entrada de cerca per ID o NIF.
     *
     * @param query Text de cerca
     * @param type Tipus de cerca (ID/NIF)
     * @return true si l'entrada és vàlida
     */
    boolean validateSearchInput(String query, String type) {
        if ("ID".equals(type) && !query.matches("^\\d+$")) {
            showSearchError("Error de format", "L'ID ha de ser numeric.");
            return false;
        } else if ("NIF".equals(type) && !query.matches("^\\d{8}[A-Za-z]$")) {
            showSearchError("Error de format", "El NIF ha de tenir 8 numeros i 1 lletra.\nExemple: 12345678A");
            return false;
        }
        return true;
    }

    /**
     * Crea una tasca per cercar un usuari per ID o NIF.
     *
     * @param query Text de cerca
     * @param type Tipus de cerca (ID/NIF)
     * @return Tasca de cerca d'usuari
     */
    private Task<User> createUserSearchTask(String query, String type) {
        return new Task<>() {
            @Override
            protected User call() throws Exception {
                return "NIF".equals(type)
                        ? userService.getUserByNif(query.toUpperCase())
                        : userService.getUserById(Long.parseLong(query));
            }
        };
    }

    /**
     * Configura els manejadors d'èxit i error per a una tasca de cerca.
     *
     * @param task Tasca a configurar
     * @param type Tipus de cerca (ID/NIF)
     * @param query Text de cerca
     */
    private void configureTaskHandlers(Task<User> task, String type, String query) {
        task.setOnSucceeded(e -> {
            User user = task.getValue();
            if (searchByIdField != null) searchByIdField.clear();
            if (searchField != null) searchField.clear();

            if (user == null) {
                showUserNotFound(type, query);
            } else {
                navigator.showMainView(USER_FORM_VIEW_PATH,
                        (UserFormController controller) -> controller.setUserData(user, "VIEW"));
            }
        });

        task.setOnFailed(e -> {
            if (searchByIdField != null) searchByIdField.clear();
            showUserNotFound(type, query);
        });
    }


    /**
     * Mostra missatge d'usuari no trobat.
     *
     * @param searchType Tipus de cerca realitzada
     * @param query Text de cerca utilitzat
     */
    private void showUserNotFound(String searchType, String query) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Usuari no trobat");
            alert.setHeaderText(null);

            String searchTypeDisplay = "ID".equals(searchType) ? "ID" : "NIF";
            String content = String.format("%s: %s%nUsuari no trobat", searchTypeDisplay, query);
            alert.setContentText(content);

            alert.showAndWait();
        });
    }

    /**
     * Mostra errors de validació o de servidor.
     *
     * @param title Títol de l'error
     * @param message Missatge de l'error
     */
    private void showSearchError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.getDialogPane().getStyleClass().add("search-error-dialog");
            alert.showAndWait();
        });
    }

    /**
     * Navega a la vista de detalls d'usuari en mode visualització.
     *
     * @param user Usuari a visualitzar
     */
    private void viewUser(User user) {
        navigator.showMainView(USER_FORM_VIEW_PATH,
                (UserFormController controller) -> controller.setUserData(user, "VIEW"));
    }

    /**
     * Navega a la vista d'edició d'usuari en mode edició.
     *
     * @param user Usuari a editar
     */
    private void editUser(User user) {
        navigator.showMainView(USER_FORM_VIEW_PATH,
                (UserFormController controller) -> controller.setUserData(user, "EDIT"));
    }

    /**
     * Gestiona l'eliminació d'un usuari.
     *
     * @param user Usuari a eliminar
     */
    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminació");
        alert.setHeaderText("Vols eliminar aquest usuari?");
        alert.setContentText(String.format("%nID: %d%nNick: %s%nNom: %s %s",
                user.getId(), user.getNick(), user.getNom(), user.getCognom1()));
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) performDelete(user);
        });
    }

    /**
     * Executa l'eliminacio d'un usuari.
     *
     * @param user Usuari a eliminar
     */
    private void performDelete(User user) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                userService.deleteUser(user.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            masterList.remove(user);
            applyFilterAndPagination();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error eliminant usuari", ex);
            showError("Error eliminant usuari", ex != null ? ex.getMessage() : "Error desconegut");
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Mostra un dialeg d'error.
     *
     * @param title Títol de l'error
     * @param message Missatge de l'error
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Gestiona la creacio d'un nou usuari.
     */
    @FXML
    private void onNewUser() {
        navigator.showMainView(USER_FORM_VIEW_PATH,
                (UserFormController controller) -> controller.setUserData(null, "CREATE"));
    }
}