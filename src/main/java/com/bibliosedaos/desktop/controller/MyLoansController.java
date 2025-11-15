package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.model.Prestec;
import com.bibliosedaos.desktop.security.SessionStore;
import com.bibliosedaos.desktop.service.PrestecService;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per a la llista de prestecs del usuari actual.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class MyLoansController {

    private static final Logger LOGGER = Logger.getLogger(MyLoansController.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String ERROR_TITLE = "Error";

    @FXML private TableView<Prestec> prestecsTable;
    @FXML private TableColumn<Prestec, Long> idColumn;
    @FXML private TableColumn<Prestec, String> titolColumn;
    @FXML private TableColumn<Prestec, String> autorColumn;
    @FXML private TableColumn<Prestec, String> llocColumn;
    @FXML private TableColumn<Prestec, LocalDate> dataPrestecColumn;
    @FXML private TableColumn<Prestec, LocalDate> dataDevolucioColumn;
    @FXML private ScrollPane mainScrollPane;

    @FXML private CheckBox historicCheck;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final PrestecService prestecService;

    private final ObservableList<Prestec> masterList = FXCollections.observableArrayList();
    private final ObservableList<Prestec> filteredList = FXCollections.observableArrayList();
    private final ObservableList<Prestec> currentPageList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;

    /**
     * Constructor del controlador.
     *
     * @param prestecService Servei per a operacions amb prestecs
     */
    public MyLoansController(PrestecService prestecService) {
        this.prestecService = Objects.requireNonNull(prestecService, "PrestecService no pot ser null");
    }

    /**
     * Inicialitza el controlador despres de carregar el FXML.
     * Configura la taula, listeners i carrega les dades inicials.
     */
    @FXML
    private void initialize() {
        applyButtonEffects();
        setupTable();
        setupListeners();
        loadMyPrestecs();
    }

    /**
     * Aplica efectes de clic als botons de la interficie.
     */
    private void applyButtonEffects() {
        AnimationUtils.safeApplyClick(prevPageButton);
        AnimationUtils.safeApplyClick(nextPageButton);
    }

    /**
     * Configura la taula de prestecs amb les columnes corresponents.
     */
    private void setupTable() {
        configureTableColumns();
        prestecsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        prestecsTable.setItems(currentPageList);
    }

    /**
     * Configura les columnes de la taula amb les propietats de l'objecte Prestec.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titolColumn.setCellValueFactory(this::getTitolValue);
        autorColumn.setCellValueFactory(this::getAutorValue);
        llocColumn.setCellValueFactory(this::getLlocValue);
        dataPrestecColumn.setCellValueFactory(new PropertyValueFactory<>("dataPrestec"));
        dataDevolucioColumn.setCellValueFactory(new PropertyValueFactory<>("dataDevolucio"));
    }

    /**
     * Obte el valor per a la columna del titol.
     *
     * @param cell Dades de la cel·la
     * @return Valor de la columna titol
     */
    private ReadOnlyStringWrapper getTitolValue(TableColumn.CellDataFeatures<Prestec, String> cell) {
        Prestec prestec = cell.getValue();
        if (prestec != null && prestec.getExemplar() != null && prestec.getExemplar().getLlibre() != null) {
            String titol = prestec.getExemplar().getLlibre().getTitol();
            return new ReadOnlyStringWrapper(titol != null ? titol : "");
        }
        return new ReadOnlyStringWrapper("");
    }

    /**
     * Obte el valor per a la columna de l'autor.
     *
     * @param cell Dades de la cel·la
     * @return Valor de la columna autor
     */
    private ReadOnlyStringWrapper getAutorValue(TableColumn.CellDataFeatures<Prestec, String> cell) {
        Prestec prestec = cell.getValue();
        if (prestec != null && prestec.getExemplar() != null &&
                prestec.getExemplar().getLlibre() != null && prestec.getExemplar().getLlibre().getAutor() != null) {
            String autor = prestec.getExemplar().getLlibre().getAutor().getNom();
            return new ReadOnlyStringWrapper(autor != null ? autor : "");
        }
        return new ReadOnlyStringWrapper("");
    }

    /**
     * Obte el valor per a la columna del lloc.
     *
     * @param cell Dades de la cel·la
     * @return Valor de la columna lloc
     */
    private ReadOnlyStringWrapper getLlocValue(TableColumn.CellDataFeatures<Prestec, String> cell) {
        Prestec prestec = cell.getValue();
        if (prestec != null && prestec.getExemplar() != null) {
            String lloc = prestec.getExemplar().getLloc();
            return new ReadOnlyStringWrapper(lloc != null ? lloc : "");
        }
        return new ReadOnlyStringWrapper("");
    }

    /**
     * Configura els listeners per als controls de la interficie.
     */
    private void setupListeners() {
        historicCheck.selectedProperty().addListener((obs, oldVal, newVal) -> loadMyPrestecs());
    }

    /**
     * Carrega els prestecs del usuari actual.
     */
    private void loadMyPrestecs() {
        Long userId = SessionStore.getInstance().getUserId();
        if (userId == null) {
            showError("No s'ha pogut identificar l'usuari");
            return;
        }

        Task<List<Prestec>> task = createLoadPrestecsTask(userId);

        task.setOnSucceeded(e -> onLoadPrestecsSucceeded(task.getValue()));
        task.setOnFailed(e -> onLoadPrestecsFailed(task.getException()));

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Crea una tasca per carregar els prestecs de l'usuari.
     *
     * @param userId ID de l'usuari
     * @return Tasca per carregar prestecs
     */
    private Task<List<Prestec>> createLoadPrestecsTask(Long userId) {
        return new Task<>() {
            @Override
            protected List<Prestec> call() throws Exception {
                boolean showHistoric = historicCheck.isSelected();
                return showHistoric ?
                        prestecService.getAllPrestecs(userId) :
                        prestecService.getPrestecsActius(userId);
            }
        };
    }

    /**
     * Gestiona l'exit de la carrega de prestecs.
     *
     * @param prestecs Llista de prestecs carregats
     */
    private void onLoadPrestecsSucceeded(List<Prestec> prestecs) {
        masterList.setAll(prestecs);
        applyFilterAndPagination();
    }

    /**
     * Gestiona l'error en la carrega de prestecs.
     *
     * @param exception Excepcio produida
     */
    private void onLoadPrestecsFailed(Throwable exception) {
        LOGGER.log(Level.WARNING, "Error carregant els meus prestecs", exception);
        showError("No s'han pogut carregar els teus prestecs");
    }

    /**
     * Aplica els filtres i la paginacio a la llista de prestecs.
     */
    private void applyFilterAndPagination() {
        filteredList.setAll(masterList);
        currentPage = 0;
        updatePagination();
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
        scrollToTop();
    }

    /**
     * Desplaça la vista al principi de la taula.
     */
    private void scrollToTop() {
        Platform.runLater(() -> {
            try {
                mainScrollPane.setVvalue(0.0);
                if (!currentPageList.isEmpty()) {
                    prestecsTable.scrollTo(0);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "No s'ha pogut fer scroll al principi de la taula", ex);
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
     * Mostra un dialeg d'error.
     *
     * @param message Missatge de l'error
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_TITLE);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}