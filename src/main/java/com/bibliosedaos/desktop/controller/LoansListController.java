package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.model.Prestec;
import com.bibliosedaos.desktop.service.PrestecService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per a la llista de prestecs.
 * Gestiona la visualitzacio, cerca i navegacio entre prestecs del sistema.
 * Implementa paginacio, cerca per diversos camps i gestio de retorns.
 *
 * Assistencia d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class LoansListController {

    private static final Logger LOGGER = Logger.getLogger(LoansListController.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String PRESTEC_FORM_VIEW_PATH = "/com/bibliosedaos/desktop/loan-form-view.fxml";
    private static final String ERROR_TITLE = "Error";
    private static final String ERROR_DESCONEGUT = "Error desconegut";
    private static final String USUARI_SENSE_PRESTECS = "Aquest usuari no te prestecs";

    @FXML private TableView<Prestec> prestecsTable;
    @FXML private TableColumn<Prestec, Long> idColumn;
    @FXML private TableColumn<Prestec, String> titolColumn;
    @FXML private TableColumn<Prestec, String> usuariColumn;
    @FXML private TableColumn<Prestec, LocalDate> dataPrestecColumn;
    @FXML private TableColumn<Prestec, LocalDate> dataDevolucioColumn;
    @FXML private TableColumn<Prestec, Void> actionsColumn;
    @FXML private ScrollPane mainScrollPane;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchByIdField;
    @FXML private Button searchByIdButton;
    @FXML private Button newPrestecButton;
    @FXML private CheckBox historicCheck;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final PrestecService prestecService;
    private final Navigator navigator;

    private final ObservableList<Prestec> masterList = FXCollections.observableArrayList();
    private final ObservableList<Prestec> filteredList = FXCollections.observableArrayList();
    private final ObservableList<Prestec> currentPageList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;

    /**
     * Constructor del controlador.
     *
     * @param prestecService Servei per a operacions amb prestecs
     * @param navigator Sistema de navegacio entre vistes
     */
    public LoansListController(PrestecService prestecService,
                               Navigator navigator) {
        this.prestecService = Objects.requireNonNull(prestecService, "PrestecService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador despres de carregar el FXML.
     * Configura la taula, combos, listeners i carrega les dades inicials.
     */
    @FXML
    private void initialize() {
        applyButtonEffects();
        setupTable();
        initSearchCombo();
        setupListeners();
        loadPrestecs();
    }

    /**
     * Aplica efectes de clic als botons de la interficie.
     */
    private void applyButtonEffects() {
        AnimationUtils.safeApplyClick(newPrestecButton);
        AnimationUtils.safeApplyClick(searchByIdButton);
        AnimationUtils.safeApplyClick(prevPageButton);
        AnimationUtils.safeApplyClick(nextPageButton);
    }

    /**
     * Configura la taula de prestecs amb les columnes i accions.
     */
    private void setupTable() {
        configureTableColumns();
        setupActionsColumn();
        prestecsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        prestecsTable.setItems(currentPageList);
    }

    /**
     * Configura les columnes de la taula amb les propietats de l'objecte Prestec.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titolColumn.setCellValueFactory(cell -> {
            if (cell.getValue() != null && cell.getValue().getExemplar() != null
                    && cell.getValue().getExemplar().getLlibre() != null) {
                String titol = cell.getValue().getExemplar().getLlibre().getTitol();
                return new ReadOnlyStringWrapper(titol != null ? titol : "");
            }
            return new ReadOnlyStringWrapper("");
        });

        usuariColumn.setCellValueFactory(cell -> {
            if (cell.getValue() != null && cell.getValue().getUsuari() != null) {
                String nom = cell.getValue().getUsuari().getNom();
                String cognom = cell.getValue().getUsuari().getCognom1();
                String nomComplet = (nom != null ? nom : "") + " " + (cognom != null ? cognom : "");
                return new ReadOnlyStringWrapper(nomComplet.trim());
            }
            return new ReadOnlyStringWrapper("");
        });

        dataPrestecColumn.setCellValueFactory(new PropertyValueFactory<>("dataPrestec"));
        dataDevolucioColumn.setCellValueFactory(new PropertyValueFactory<>("dataDevolucio"));
    }

    /**
     * Configura la columna d'accions amb botons per veure i retornar.
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> createActionsTableCell());
    }

    /**
     * Crea una cel·la de taula amb botons d'accio.
     *
     * @return TableCell configurada amb botons d'accio
     */
    private TableCell<Prestec, Void> createActionsTableCell() {
        return new ActionsTableCell();
    }

    /**
     * Crea un boto d'accio amb icona.
     *
     * @param svgContent Contingut SVG de l'icona
     * @param styleClass Classe CSS del boto
     * @param tooltipText Text del tooltip
     * @param buttonText Text del boto
     * @return Boto configurat
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
        AnimationUtils.safeApplyClick(button);
        return button;
    }

    /**
     * Cel·la de la taula que mostra els botons d'accio (Veure / Retornar)
     * per a la fila actual i gestiona la creacio dels botons i els seus handlers.
     */
    private class ActionsTableCell extends TableCell<Prestec, Void> {
        private final Button viewBtn;
        private final Button returnBtn;
        private final HBox box;

        /**
         * Constructor: crea botons, contenidor i assigna handlers.
         */
        ActionsTableCell() {
            viewBtn = createActionButton(
                    "M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z",
                    "view-btn", "Veure detalls", "Veure"
            );

            returnBtn = createActionButton(
                    "M7 2v20H3V2h4zm12.005 0C20.107 2 21 2.898 21 3.99v16.02c0 1.099-.893 1.99-1.995 1.99H9V2h10.005zM15 8l-4 4h3v4h2v-4h3l-4-4zm9 4v4h-2v-4h2zm0-6v4h-2V6h2z",
                    "return-btn", "Marcar com retornat", "Devolució"
            );

            box = new HBox(6, viewBtn, returnBtn);

            configureButtonActions();
        }

        /**
         * Configura les accions dels botons d'accio.
         */
        private void configureButtonActions() {
            viewBtn.setOnAction(event -> {
                Prestec currentItem = getCurrentItem();
                if (currentItem != null) viewPrestec(currentItem);
            });

            returnBtn.setOnAction(event -> {
                Prestec currentItem = getCurrentItem();
                if (currentItem != null) confirmAndReturnPrestec(currentItem);
            });
        }

        /**
         * Retorna el prestec associat a la fila d'aquesta cel·la.
         *
         * @return Prestec o null si no hi ha item
         */
        private Prestec getCurrentItem() {
            int rowIndex = getIndex();
            return (rowIndex >= 0 && rowIndex < getTableView().getItems().size())
                    ? getTableView().getItems().get(rowIndex) : null;
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            Prestec prestec = getCurrentItem();
            if (prestec == null) {
                setGraphic(null);
                return;
            }

            boolean isActive = prestec.getDataDevolucio() == null;
            returnBtn.setVisible(isActive);
            returnBtn.setManaged(isActive);
            setGraphic(box);
        }
    }

    /**
     * Inicialitza el combo de cerca per camps.
     */
    private void initSearchCombo() {
        searchFieldCombo.setItems(FXCollections.observableArrayList(
                "Tots", "ID Prestec", "ID Exemplar", "ID Usuari", "Titol", "Usuari"
        ));
        searchFieldCombo.setValue("Tots");
    }

    /**
     * Configura els listeners per als camps de cerca.
     */
    private void setupListeners() {
        searchField.textProperty().addListener((observableValue, oldValue, newValue) -> applyFilterAndPagination());
        searchFieldCombo.valueProperty().addListener((observableValue, oldValue, newValue) -> applyFilterAndPagination());
        searchByIdButton.setOnAction(event -> onSearchByUserId());
        searchByIdField.setOnAction(event -> onSearchByUserId());
        historicCheck.selectedProperty().addListener((observableValue, oldValue, newValue) -> loadPrestecs());
    }

    /**
     * Carrega tots els prestecs del sistema.
     */
    private void loadPrestecs() {
        Task<List<Prestec>> loadTask = createLoadPrestecsTask();

        loadTask.setOnSucceeded(event -> onLoadPrestecsSucceeded(loadTask.getValue()));

        loadTask.setOnFailed(event -> {
            Throwable exception = loadTask.getException();
            LOGGER.log(Level.WARNING, "Error carregant prestecs", exception);
            showError("Error carregant prestecs", exception != null ? exception.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(loadTask);
    }

    private void onLoadPrestecsSucceeded(List<Prestec> prestecs) {
        masterList.setAll(prestecs);
        applyFilterAndPagination();
    }

    /**
     * Crea una tasca per carregar tots els prestecs.
     *
     * @return Tasca per carregar prestecs
     */
    private Task<List<Prestec>> createLoadPrestecsTask() {
        return new Task<>() {
            @Override
            protected List<Prestec> call() throws Exception {
                boolean showHistoric = historicCheck.isSelected();
                if (showHistoric) {
                    return prestecService.getAllPrestecs(null);
                } else {
                    return prestecService.getPrestecsActius(null);
                }
            }
        };
    }

    /**
     * Aplica els filtres i la paginacio a la llista de prestecs.
     */
    private void applyFilterAndPagination() {
        final String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        final String field = searchFieldCombo.getValue() == null ? "Tots" : searchFieldCombo.getValue();

        filteredList.setAll(masterList.filtered(prestec -> matchesSearchCriteria(prestec, query, field)));

        currentPage = 0;
        updatePagination();
    }

    /**
     * Comprova si un prestec compleix amb els criteris de cerca.
     *
     * @param prestec Prestec a verificar
     * @param query Text de cerca
     * @param field Camp on cercar
     * @return true si el prestec compleix els criteris
     */
    private boolean matchesSearchCriteria(Prestec prestec, String query, String field) {
        if (query.isEmpty()) return true;

        return switch (field) {
            case "ID Prestec" -> prestec.getId() != null && prestec.getId().toString().contains(query);
            case "ID Exemplar" -> prestec.getExemplar() != null && prestec.getExemplar().getId() != null
                    && prestec.getExemplar().getId().toString().contains(query);
            case "ID Usuari" -> prestec.getUsuari() != null && prestec.getUsuari().getId() != null
                    && prestec.getUsuari().getId().toString().contains(query);
            case "Titol" -> prestec.getExemplar() != null && prestec.getExemplar().getLlibre() != null
                    && safeContains(prestec.getExemplar().getLlibre().getTitol(), query);
            case "Usuari" -> matchesUsuariCriteria(prestec, query);
            default -> matchesDefaultCriteria(prestec, query);
        };
    }

    /**
     * Comprova criteris de cerca per l'usuari.
     */
    private boolean matchesUsuariCriteria(Prestec prestec, String query) {
        if (prestec.getUsuari() == null) return false;

        boolean matchNom = safeContains(prestec.getUsuari().getNom(), query);
        boolean matchCognom = safeContains(prestec.getUsuari().getCognom1(), query);

        String nomComplet = (prestec.getUsuari().getNom() != null ? prestec.getUsuari().getNom() : "") + " " +
                (prestec.getUsuari().getCognom1() != null ? prestec.getUsuari().getCognom1() : "");
        boolean matchNomComplet = safeContains(nomComplet.trim(), query);

        return matchNom || matchCognom || matchNomComplet;
    }

    /**
     * Comprova criteris per defecte (cerca en tots els camps).
     */
    private boolean matchesDefaultCriteria(Prestec prestec, String query) {
        if ((prestec.getId() != null && prestec.getId().toString().contains(query)) ||
                (prestec.getExemplar() != null && prestec.getExemplar().getId() != null &&
                        prestec.getExemplar().getId().toString().contains(query))) {
            return true;
        }

        if (prestec.getUsuari() != null && (
                safeContains(prestec.getUsuari().getNom(), query) ||
                        safeContains(prestec.getUsuari().getCognom1(), query) ||
                        safeContains(prestec.getUsuari().getNick(), query))) {
            return true;
        }

        return prestec.getExemplar() != null &&
                prestec.getExemplar().getLlibre() != null &&
                safeContains(prestec.getExemplar().getLlibre().getTitol(), query);
    }

    /**
     * Comprova si una cadena conte la cerca de manera segura.
     *
     * @param value Cadena on cercar
     * @param query Text a cercar
     * @return true si la cadena conte el text
     */
    static boolean safeContains(String value, String query) {
        if (value == null || query == null) return false;
        return value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
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

        pageInfoLabel.setText(String.format("Pagina %d de %d", currentPage + 1, totalPages));

        Platform.runLater(() -> {
            try {
                mainScrollPane.setVvalue(0.0);
                if (!currentPageList.isEmpty()) {
                    prestecsTable.scrollTo(0);
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
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1 || filteredList.isEmpty());
    }

    /**
     * Gestiona la navegacio a la pagina anterior.
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
     * Gestiona la navegacio a la pagina seguent.
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
     * Gestiona la cerca per ID d'usuari.
     */
    @FXML
    private void onSearchByUserId() {
        String raw = searchByIdField.getText();
        if (raw == null || raw.isBlank()) {
            showSearchError(ERROR_TITLE, "Introdueix un ID d'usuari.");
            return;
        }
        if (!raw.matches("^\\d+$")) {
            showSearchError("Error format", "L'ID d'usuari ha de ser numeric.");
            return;
        }

        long usuariId = Long.parseLong(raw.trim());
        Task<List<Prestec>> task = createLoadPrestecsByUserIdTask(usuariId);

        task.setOnSucceeded(e -> {
            List<Prestec> prestecs = task.getValue();
            if (prestecs == null || prestecs.isEmpty()) {
                showSearchError("Sense resultats", USUARI_SENSE_PRESTECS);
            } else {
                masterList.setAll(prestecs);
                applyFilterAndPagination();
            }
            searchByIdField.clear();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error cercant prestecs per ID d'usuari", ex);
            showSearchError(ERROR_TITLE, "Error cercant prestecs per ID d'usuari: " +
                    (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
            searchByIdField.clear();
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Crea una tasca per carregar els prestecs per ID d'usuari.
     *
     * @param usuariId ID de l'usuari
     * @return Tasca per carregar prestecs
     */
    private Task<List<Prestec>> createLoadPrestecsByUserIdTask(long usuariId) {
        return new Task<>() {
            @Override
            protected List<Prestec> call() throws Exception {
                boolean showHistoric = historicCheck.isSelected();
                if (showHistoric) {
                    return prestecService.getAllPrestecs(usuariId);
                } else {
                    return prestecService.getPrestecsActius(usuariId);
                }
            }
        };
    }

    /**
     * Gestiona la creacio d'un nou prestec.
     */
    @FXML
    private void onNewPrestec() {
        navigator.showMainView(PRESTEC_FORM_VIEW_PATH,
                (LoanFormController controller) -> controller.setLoanData(null, "CREATE"));
    }

    /**
     * Navega a la vista de detalls del prestec.
     *
     * @param prestec Prestec a visualitzar
     */
    private void viewPrestec(Prestec prestec) {
        navigator.showMainView(PRESTEC_FORM_VIEW_PATH,
                (LoanFormController controller) -> controller.setLoanData(prestec, "VIEW"));
    }

    /**
     * Confirma i gestiona el retorn d'un prestec.
     *
     * @param prestec Prestec a retornar
     */
    private void confirmAndReturnPrestec(Prestec prestec) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar devolució");
        alert.setHeaderText("Vols marcar aquest prestec com a retornat?");
        alert.setContentText(String.format("ID: %d%nTitol: %s%nUsuari: %s %s",
                prestec.getId(),
                prestec.getExemplar() != null && prestec.getExemplar().getLlibre() != null ? prestec.getExemplar().getLlibre().getTitol() : "",
                prestec.getUsuari() != null ? prestec.getUsuari().getNom() : "",
                prestec.getUsuari() != null ? prestec.getUsuari().getCognom1() : ""
        ));
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) performReturnPrestec(prestec.getId());
        });
    }

    /**
     * Executa el retorn d'un prestec.
     *
     * @param prestecId ID del prestec a retornar
     */
    private void performReturnPrestec(Long prestecId) {
        Task<Void> task = createReturnPrestecTask(prestecId);

        task.setOnSucceeded(e -> loadPrestecs());

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error retornant prestec", ex);
            showError("Error retornant prestec", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Crea una tasca per retornar un prestec.
     *
     * @param prestecId ID del prestec a retornar
     * @return Tasca de retorn
     */
    private Task<Void> createReturnPrestecTask(Long prestecId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                prestecService.retornarPrestec(prestecId);
                return null;
            }
        };
    }

    /**
     * Mostra un error de cerca.
     *
     * @param title Titol de l'error
     * @param message Missatge de l'error
     */
    private void showSearchError(String title, String message) {
        if (Boolean.getBoolean("tests.noDialog")) return;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Mostra un dialeg d'error.
     *
     * @param title Titol de l'error
     * @param message Missatge de l'error
     */
    private void showError(String title, String message) {
        if (Boolean.getBoolean("tests.noDialog")) return;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}