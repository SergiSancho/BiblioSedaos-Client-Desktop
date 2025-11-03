package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.model.Exemplar;
import com.bibliosedaos.desktop.model.Llibre;
import com.bibliosedaos.desktop.service.ExemplarService;
import com.bibliosedaos.desktop.service.LlibreService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per a la navegacio i consulta de llibres per a usuaris.
 * Gestiona la visualitzacio de llibres i exemplars disponibles amb paginacio,
 * filtres de cerca i intercanvi entre vistes de llibres i exemplars.
 *
 * Assistencia d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author bibliosedaos
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class BooksBrowseController {

    private static final Logger LOGGER = Logger.getLogger(BooksBrowseController.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String BOOK_FORM_VIEW_PATH = "/com/bibliosedaos/desktop/book-form-view.fxml";
    private static final String ERROR_TITLE = "Error";
    private static final String ERROR_DESCONEGUT = "Error desconegut";
    private static final String NO_DETALL_MSG = "No sha pogut obrir el detall: dades incompletes.";

    private static final String TOTS = "Tots";
    private static final String TITOL = "Titol";
    private static final String EDITORIAL = "Editorial";
    private static final String AUTOR = "Autor";
    private static final String ISBN = "ISBN";

    private static final String VIEW_BUTTON_SVG = "M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z";
    private static final String VIEW_BUTTON_STYLE_CLASS = "view-btn";
    private static final String VIEW_BUTTON_TOOLTIP = "Veure detalls";
    private static final String VIEW_BUTTON_TEXT = "Veure";

    private static final String RESERVAT_LLIURE = "lliure";
    private static final String DISPONIBLE = "Disponible";

    @FXML private TableView<Object> browseTable;
    @FXML private TableColumn<Object, Long> idColumn;
    @FXML private TableColumn<Object, String> titolColumn;
    @FXML private TableColumn<Object, String> autorColumn;
    @FXML private TableColumn<Object, String> llocColumn;
    @FXML private TableColumn<Object, String> reservatColumn;
    @FXML private TableColumn<Object, Void> actionsColumn;
    @FXML private ScrollPane mainScrollPane;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchFieldCombo;

    @FXML private ComboBox<String> availableSearchCombo;
    @FXML private TextField availableSearchField;
    @FXML private Button availableSearchButton;
    @FXML private CheckBox onlyAvailableToggle;
    @FXML private CheckBox allBooksToggle;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final LlibreService llibreService;
    private final ExemplarService exemplarService;
    private final Navigator navigator;

    private final ObservableList<Llibre> llibresList = FXCollections.observableArrayList();
    private final ObservableList<Exemplar> exemplarsList = FXCollections.observableArrayList();
    private final ObservableList<Object> filteredList = FXCollections.observableArrayList();
    private final ObservableList<Object> currentPageList = FXCollections.observableArrayList();

    private int currentPage = 0;
    private int totalPages = 0;
    private boolean showingExemplars = false;

    /**
     * Constructor del controlador.
     *
     * @param llibreService Servei per a operacions amb llibres
     * @param exemplarService Servei per a operacions amb exemplars
     * @param navigator Sistema de navegacio entre vistes
     */
    public BooksBrowseController(LlibreService llibreService, ExemplarService exemplarService, Navigator navigator) {
        this.llibreService = Objects.requireNonNull(llibreService, "LlibreService no pot ser null");
        this.exemplarService = Objects.requireNonNull(exemplarService, "ExemplarService no pot ser null");
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
        initSearchCombos();
        setupListeners();
        allBooksToggle.setSelected(true);
        loadBooks();
    }

    /**
     * Aplica efectes de clic als botons de la interficie.
     */
    private void applyButtonEffects() {
        AnimationUtils.safeApplyClick(prevPageButton);
        AnimationUtils.safeApplyClick(nextPageButton);
        AnimationUtils.safeApplyClick(availableSearchButton);
    }

    /**
     * Configura la taula de navegacio amb les columnes i accions.
     */
    private void setupTable() {
        configureTableColumns();
        setupActionsColumn();
        browseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        browseTable.setItems(currentPageList);
    }

    /**
     * Configura les columnes de la taula amb les propietats dels objectes.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(cell -> {
            Object item = cell.getValue();
            Long id = null;
            if (item instanceof Llibre book) {
                id = book.getId();
            } else if (item instanceof Exemplar exemplar) {
                id = exemplar.getId();
            }
            return readOnlyLongWrapperSafe(id);
        });

        titolColumn.setCellValueFactory(cell -> {
            Object item = cell.getValue();
            if (item instanceof Llibre book) return new ReadOnlyStringWrapper(book.getTitol());
            if (item instanceof Exemplar exemplar && exemplar.getLlibre() != null) return new ReadOnlyStringWrapper(exemplar.getLlibre().getTitol());
            return new ReadOnlyStringWrapper("");
        });

        autorColumn.setCellValueFactory(cell -> {
            Object item = cell.getValue();
            if (item instanceof Llibre book && book.getAutor() != null) return new ReadOnlyStringWrapper(book.getAutor().getNom());
            if (item instanceof Exemplar exemplar && exemplar.getLlibre() != null && exemplar.getLlibre().getAutor() != null)
                return new ReadOnlyStringWrapper(exemplar.getLlibre().getAutor().getNom());
            return new ReadOnlyStringWrapper("");
        });

        llocColumn.setCellValueFactory(cell -> {
            Object item = cell.getValue();
            if (item instanceof Exemplar exemplar) return new ReadOnlyStringWrapper(exemplar.getLloc());
            return new ReadOnlyStringWrapper("");
        });

        reservatColumn.setCellValueFactory(cell -> {
            Object item = cell.getValue();
            if (item instanceof Exemplar exemplar) {
                String reservat = exemplar.getReservat();
                return new ReadOnlyStringWrapper(mapReservatToDisplay(reservat));
            }
            return new ReadOnlyStringWrapper("");
        });
    }

    /**
     * Converteix l'estat de reserva a text per mostrar.
     *
     * @param reservat Estat de reserva de lexemplar
     * @return Text per mostrar a la interficie
     */
    private String mapReservatToDisplay(String reservat) {
        if (reservat == null) return "—";
        return RESERVAT_LLIURE.equals(reservat) ? DISPONIBLE : reservat;
    }

    /**
     * Configura la columna d'accions amb botons per veure detalls.
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new ActionsTableCell());
    }

    /**
     * Cel·la d'accions: crea i gestiona el botó "Veure" per a llibres i exemplars.
     */
    private class ActionsTableCell extends TableCell<Object, Void> {
        private final Button viewBtn = createViewButton();
        private final HBox box = new HBox(6, viewBtn);

        /**
         * Constructor de la cel·la d'accions.
         */
        public ActionsTableCell() {
            viewBtn.setOnAction(e -> {
                Object currentItem = getCurrentItem();
                viewItem(currentItem);
            });
        }

        /**
         * Obte l'element actual de la taula.
         *
         * @return Element actual o null si no hi ha element
         */
        private Object getCurrentItem() {
            int rowIndex = getIndex();
            return (rowIndex >= 0 && rowIndex < getTableView().getItems().size()) ? getTableView().getItems().get(rowIndex) : null;
        }

        /**
         * Crea el boto per veure detalls.
         *
         * @return Boto configurat per veure detalls
         */
        private Button createViewButton() {
            Button button = new Button(VIEW_BUTTON_TEXT);
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(VIEW_BUTTON_SVG);
            svgPath.getStyleClass().add("action-icon");
            button.setGraphic(svgPath);
            button.getStyleClass().addAll("action-btn", VIEW_BUTTON_STYLE_CLASS);
            button.setTooltip(new Tooltip(VIEW_BUTTON_TOOLTIP));
            AnimationUtils.safeApplyClick(button);
            return button;
        }

        /**
         * Obre la vista de detalls per a un element (Llibre o Exemplar).
         * @param item Element a visualitzar
         */
        private void viewItem(Object item) {
            if (item instanceof Llibre book) {
                navigator.showMainView(BOOK_FORM_VIEW_PATH, (BookFormController c) -> c.setBookData(book, "VIEW"));
            } else if (item instanceof Exemplar exemplar && exemplar.getLlibre() != null) {
                navigator.showMainView(BOOK_FORM_VIEW_PATH, (BookFormController c) -> c.setBookData(exemplar.getLlibre(), "VIEW"));
            } else {
                showError(NO_DETALL_MSG);
            }
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
        }
    }

    /**
     * Inicialitza els combos de cerca per camps.
     */
    private void initSearchCombos() {
        searchFieldCombo.setItems(FXCollections.observableArrayList(TOTS, ISBN, TITOL, EDITORIAL, AUTOR));
        searchFieldCombo.setValue(TOTS);

        availableSearchCombo.setItems(FXCollections.observableArrayList(TITOL, AUTOR));
        availableSearchCombo.setValue(TITOL);
    }

    /**
     * Configura els listeners per als camps de cerca i filtres.
     */
    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (showingExemplars) {
                    allBooksToggle.setSelected(true);
                    onlyAvailableToggle.setSelected(false);
                    loadBooks();
                } else {
                    applyFilterAndPagination();
                }
            } else {
                applyFilterAndPagination();
            }
        });

        searchFieldCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!showingExemplars) applyFilterAndPagination();
        });

        availableSearchField.setOnAction(e -> onAvailableSearch());
        availableSearchButton.setOnAction(e -> onAvailableSearch());
    }

    /**
     * Carrega tots els llibres del sistema.
     */
    private void loadBooks() {
        Task<List<Llibre>> task = createLoadBooksTask();

        task.setOnSucceeded(e -> {
            List<Llibre> books = task.getValue() != null ? task.getValue() : List.of();
            llibresList.setAll(books);
            showingExemplars = false;
            onLoadBooksSucceeded();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant llibres", ex);
            showError("Error carregant llibres: " + (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona la carrega exitosa de llibres.
     */
    private void onLoadBooksSucceeded() {
        applyFilterAndPagination();
        llocColumn.setVisible(false);
        reservatColumn.setVisible(false);
    }

    /**
     * Crea una tasca per carregar tots els llibres.
     *
     * @return Tasca per carregar llibres
     */
    private Task<List<Llibre>> createLoadBooksTask() {
        return new Task<>() {
            @Override
            protected List<Llibre> call() throws Exception {
                return llibreService.getAllBooks();
            }
        };
    }

    /**
     * Carrega els exemplars disponibles segons els criteris de cerca.
     *
     * @param titol Titol per cercar
     * @param autor Autor per cercar
     */
    private void loadAvailableExemplars(String titol, String autor) {
        Task<List<Exemplar>> task = createLoadAvailableExemplarsTask(titol, autor);

        task.setOnSucceeded(e -> {
            List<Exemplar> exemplars = task.getValue() != null ? task.getValue() : List.of();
            exemplarsList.setAll(exemplars);
            showingExemplars = true;
            onLoadAvailableExemplarsSucceeded();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant exemplars lliures", ex);
            showError("Error carregant exemplars lliures: " + (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona la carrega exitosa d'exemplars.
     */
    private void onLoadAvailableExemplarsSucceeded() {
        applyFilterAndPagination();
        llocColumn.setVisible(true);
        reservatColumn.setVisible(true);
    }

    /**
     * Crea una tasca per carregar exemplars disponibles.
     *
     * @param titol Titol per cercar
     * @param autor Autor per cercar
     * @return Tasca per carregar exemplars
     */
    private Task<List<Exemplar>> createLoadAvailableExemplarsTask(String titol, String autor) {
        return new Task<>() {
            @Override
            protected List<Exemplar> call() throws Exception {
                if (titol != null && !titol.isBlank()) {
                    return exemplarService.findExemplarsByTitol(titol);
                } else if (autor != null && !autor.isBlank()) {
                    return exemplarService.findExemplarsByAutor(autor);
                } else {
                    return exemplarService.getExemplarsLliures();
                }
            }
        };
    }

    /**
     * Gestiona la cerca d'exemplars disponibles.
     */
    @FXML
    private void onAvailableSearch() {
        String query = availableSearchField.getText() == null ? "" : availableSearchField.getText().trim();
        String field = availableSearchCombo.getValue() == null ? TITOL : availableSearchCombo.getValue();

        allBooksToggle.setSelected(false);
        onlyAvailableToggle.setSelected(false);

        if (query.isBlank()) {
            loadAvailableExemplars(null, null);
            onlyAvailableToggle.setSelected(true);
            return;
        }
        if (AUTOR.equals(field)) {
            loadAvailableExemplars(null, query);
        } else {
            loadAvailableExemplars(query, null);
        }

        availableSearchField.clear();
    }

    /**
     * Gestiona el toggle per mostrar nomes exemplars disponibles.
     */
    @FXML
    private void onToggleOnlyAvailable() {
        if (onlyAvailableToggle.isSelected()) {
            allBooksToggle.setSelected(false);
            loadAvailableExemplars(null, null);
        } else {
            allBooksToggle.setSelected(true);
            loadBooks();
        }
    }

    /**
     * Gestiona el toggle per mostrar tots els llibres.
     */
    @FXML
    private void onAllBooksToggle() {
        if (allBooksToggle.isSelected()) {
            onlyAvailableToggle.setSelected(false);
            loadBooks();
        } else {
            onlyAvailableToggle.setSelected(true);
            loadAvailableExemplars(null, null);
        }
    }

    /**
     * Aplica els filtres i la paginacio a la llista actual.
     */
    private void applyFilterAndPagination() {
        if (!showingExemplars) {
            final String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            final String field = searchFieldCombo.getValue() == null ? TOTS : searchFieldCombo.getValue();

            if (query.isEmpty()) {
                filteredList.setAll(llibresList);
            } else {
                var filtered = llibresList.filtered(book -> {
                    if (book == null) return false;
                    return switch (field) {
                        case ISBN -> safeContains(book.getIsbn(), query);
                        case TITOL -> safeContains(book.getTitol(), query);
                        case EDITORIAL -> safeContains(book.getEditorial(), query);
                        case AUTOR -> book.getAutor() != null && safeContains(book.getAutor().getNom(), query);
                        default -> safeContains(book.getIsbn(), query) || safeContains(book.getTitol(), query)
                                || safeContains(book.getEditorial(), query) || (book.getAutor() != null && safeContains(book.getAutor().getNom(), query));
                    };
                });
                filteredList.setAll(filtered);
            }
        } else {
            filteredList.setAll(exemplarsList);
        }

        currentPage = 0;
        updatePagination();
    }

    /**
     * Comprova si una cadena conte la cerca de manera segura.
     *
     * @param value Cadena on cercar
     * @param query Text a cercar
     * @return true si la cadena conte el text
     */
    private static boolean safeContains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
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
                if (!currentPageList.isEmpty()) browseTable.scrollTo(0);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "No sha pogut fer scroll al principi de la taula", ex);
            }
        });
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
     * Actualitza lestat dels botons de paginacio.
     */
    private void updatePageButtons() {
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1 || filteredList.isEmpty());
    }

    /**
     * Mostra un dialeg d'error.
     *
     * @param message Missatge d'error
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

    /**
     * Crea un wrapper segur per a valors Long.
     *
     * @param valor Valor Long
     * @return ReadOnlyObjectWrapper amb el valor
     */
    private static ReadOnlyObjectWrapper<Long> readOnlyLongWrapperSafe(Long valor) {
        return new ReadOnlyObjectWrapper<>(valor);
    }
}
