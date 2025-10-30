package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Autor;
import com.bibliosedaos.desktop.model.Exemplar;
import com.bibliosedaos.desktop.model.Llibre;
import com.bibliosedaos.desktop.service.ExemplarService;
import com.bibliosedaos.desktop.service.LlibreService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
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
 * Controlador per a la llista de llibres.
 *
 * Gestiona la visualitzacio, cerca i navegacio entre llibres del sistema.
 * Implementa paginacio, cerca per diversos camps i gestio d'exemplars.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class BooksListController {

    private static final Logger LOGGER = Logger.getLogger(BooksListController.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String BOOK_FORM_VIEW_PATH = "/com/bibliosedaos/desktop/book-form-view.fxml";
    private static final String ERROR_TITLE = "Error";
    private static final String ERROR_DESCONEGUT = "Error desconegut";
    private static final String LLIBRE_NO_TROBAT = "Llibre no trobat";
    private static final String SUCCESS_DELETE = "Llibre eliminat correctament.";

    @FXML private TableView<Llibre> booksTable;
    @FXML private TableColumn<Llibre, Long> idColumn;
    @FXML private TableColumn<Llibre, String> titolColumn;
    @FXML private TableColumn<Llibre, String> autorColumn;
    @FXML private TableColumn<Llibre, Void> actionsColumn;
    @FXML private ScrollPane mainScrollPane;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchByIdField;
    @FXML private Button newExemplarButton;
    @FXML private Button newBookButton;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final LlibreService llibreService;
    private final ExemplarService exemplarService;
    private final Navigator navigator;

    private final ObservableList<Llibre> masterList = FXCollections.observableArrayList();
    private final ObservableList<Llibre> filteredList = FXCollections.observableArrayList();
    private final ObservableList<Llibre> currentPageList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;

    /**
     * Constructor del controlador.
     *
     * @param llibreService Servei per a operacions amb llibres
     * @param exemplarService Servei per a operacions amb exemplars
     * @param navigator Sistema de navegacio entre vistes
     */
    public BooksListController(LlibreService llibreService, ExemplarService exemplarService, Navigator navigator) {
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
        initSearchCombo();
        setupListeners();
        loadBooks();
    }

    /**
     * Aplica efectes de clic als botons de la interficie.
     */
    private void applyButtonEffects() {
        AnimationUtils.safeApplyClick(newBookButton);
        AnimationUtils.safeApplyClick(newExemplarButton);
        AnimationUtils.safeApplyClick(prevPageButton);
        AnimationUtils.safeApplyClick(nextPageButton);
    }

    /**
     * Configura la taula de llibres amb les columnes i accions.
     */
    private void setupTable() {
        configureTableColumns();
        setupActionsColumn();
        booksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        booksTable.setItems(currentPageList);
    }

    /**
     * Configura les columnes de la taula amb les propietats de l'objecte Llibre.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        titolColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("titol"));
        autorColumn.setCellValueFactory(cell -> {
            Autor a = cell.getValue().getAutor();
            return new ReadOnlyStringWrapper(a != null ? a.getNom() : "");
        });
    }

    /**
     * Configura la columna d'accions amb botons per veure, afegir exemplars i eliminar.
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> createActionsTableCell());
    }

    /**
     * Crea una cel·la de taula amb botons d'accio.
     *
     * @return TableCell configurada amb botons d'accio
     */
    private TableCell<Llibre, Void> createActionsTableCell() {
        return new TableCell<>() {
            private final Button viewBtn = createActionButton(
                    "M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z",
                    "view-btn", "Veure detalls", "Veure");

            private final Button addExBtn = createActionButton(
                    "M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm6 13h-5v5h-2v-5h-5v-2h5v-5h2v5h5v2z",
                    "exemplar-btn", "Afegir exemplar", "Exemplar");

            private final Button delBtn = createActionButton(
                    "M3 6v18h18v-18h-18zm5 14c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm4-18v2h-20v-2h5.711c.9 0 1.631-1.099 1.631-2h5.315c0 .901.73 2 1.631 2h5.712z",
                    "delete-btn", "Eliminar llibre", "Eliminar");

            private final HBox box = new HBox(6, viewBtn, addExBtn, delBtn);

            {
                viewBtn.setOnAction(e -> {
                    Llibre current = getCurrent();
                    if (current != null) viewBook(current);
                });
                addExBtn.setOnAction(e -> {
                    Llibre current = getCurrent();
                    if (current != null) {
                        navigator.showMainView(BOOK_FORM_VIEW_PATH,
                                (BookFormController c) -> c.setBookData(current, "ADD_EXEMPLAR"));
                    }
                });
                delBtn.setOnAction(e -> {
                    Llibre current = getCurrent();
                    if (current != null) deleteBook(current);
                });
            }

            private Llibre getCurrent() {
                int idx = getIndex();
                return (idx >= 0 && idx < getTableView().getItems().size()) ? getTableView().getItems().get(idx) : null;
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
     * @param svg Contingut SVG de l'icona
     * @param styleClass Classe CSS del boto
     * @param tooltipText Text del tooltip
     * @param text Text del boto
     * @return Boto configurat
     */
    private Button createActionButton(String svg, String styleClass, String tooltipText, String text) {
        Button button = new Button(text);
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svg);
        svgPath.getStyleClass().add("action-icon");
        button.setGraphic(svgPath);
        button.getStyleClass().addAll("action-btn", styleClass);
        button.setTooltip(new Tooltip(tooltipText));
        AnimationUtils.safeApplyClick(button);
        return button;
    }

    /**
     * Inicialitza el combo de cerca per camps.
     */
    private void initSearchCombo() {
        searchFieldCombo.setItems(FXCollections.observableArrayList("Tots", "ISBN", "Títol", "Editorial", "Autor"));
        searchFieldCombo.setValue("Tots");
    }

    /**
     * Configura els listeners per als camps de cerca.
     */
    private void setupListeners() {
        searchField.textProperty().addListener((o, oldV, newV) -> applyFilterAndPagination());
        searchFieldCombo.valueProperty().addListener((o, oldV, newV) -> applyFilterAndPagination());
        searchByIdField.setOnAction(e -> onSearchById());
        newExemplarButton.setOnAction(e -> onNewExemplar());
    }

    /**
     * Carrega tots els llibres del sistema.
     */
    private void loadBooks() {
        Task<List<Llibre>> task = createLoadBooksTask();

        // replaced multi-statement lambda with a single-expression lambda that delegates to a private helper
        task.setOnSucceeded(e -> onLoadBooksSucceeded(task.getValue()));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant llibres", ex);
            showError("Error carregant llibres", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    private void onLoadBooksSucceeded(List<Llibre> books) {
        masterList.setAll(books);
        applyFilterAndPagination();
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
     * Aplica els filtres i la paginacio a la llista de llibres.
     */
    private void applyFilterAndPagination() {
        final String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        final String field = searchFieldCombo.getValue() == null ? "Tots" : searchFieldCombo.getValue();

        filteredList.setAll(masterList.filtered(book -> {
            if (q.isEmpty()) return true;

            return switch (field) {
                case "ISBN" -> safeContains(book.getIsbn(), q);
                case "Títol" -> safeContains(book.getTitol(), q);
                case "Editorial" -> safeContains(book.getEditorial(), q);
                case "Autor" -> book.getAutor() != null && safeContains(book.getAutor().getNom(), q);
                default -> safeContains(book.getIsbn(), q) || safeContains(book.getTitol(), q)
                        || safeContains(book.getEditorial(), q) || (book.getAutor() != null && safeContains(book.getAutor().getNom(), q));
            };
        }));
        currentPage = 0;
        updatePagination();
    }

    /**
     * Comprova si una cadena conte la cerca de manera segura.
     *
     * @param value Cadena on cercar
     * @param q Text a cercar
     * @return true si la cadena conte el text
     */
    private static boolean safeContains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
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

        pageInfoLabel.setText(String.format("Pàgina %d de %d", currentPage + 1, totalPages));

        Platform.runLater(() -> {
            try {
                mainScrollPane.setVvalue(0.0);
                if (!currentPageList.isEmpty()) booksTable.scrollTo(0);
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
     * Actualitza l'estat dels botons de paginacio.
     */
    private void updatePageButtons() {
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1 || filteredList.isEmpty());
    }

    /**
     * Gestiona la cerca per ID.
     */
    @FXML
    private void onSearchById() {
        String raw = searchByIdField.getText();
        if (raw == null || raw.isBlank()) {
            showSearchError(ERROR_TITLE, "Introdueix un ID.");
            return;
        }
        if (!raw.matches("^\\d+$")) {
            showSearchError("Error format", "L'ID ha de ser numeric.");
            return;
        }

        long id = Long.parseLong(raw.trim());
        Task<Llibre> task = createSearchByIdTask(id);

        // delegate to helper to remove multi-statement lambda
        task.setOnSucceeded(e -> onSearchByIdTaskSucceeded(task.getValue()));

        task.setOnFailed(e -> showSearchError(ERROR_TITLE, LLIBRE_NO_TROBAT));
        ApiClient.BG_EXEC.submit(task);
    }

    private void onSearchByIdTaskSucceeded(Llibre b) {
        if (b == null) showSearchError("No trobat", LLIBRE_NO_TROBAT);
        else navigator.showMainView(BOOK_FORM_VIEW_PATH, (BookFormController c) -> c.setBookData(b, "VIEW"));
    }

    /**
     * Crea una tasca per cercar un llibre per ID.
     *
     * @param id ID del llibre a cercar
     * @return Tasca de cerca per ID
     */
    private Task<Llibre> createSearchByIdTask(long id) {
        return new Task<>() {
            @Override
            protected Llibre call() throws Exception {
                return llibreService.getBookById(id);
            }
        };
    }

    /**
     * Gestiona la creacio d'un nou llibre.
     */
    @FXML
    private void onNewBook() {
        navigator.showMainView(BOOK_FORM_VIEW_PATH, (BookFormController c) -> c.setBookData(null, "CREATE"));
    }

    /**
     * Gestiona la creacio d'un nou exemplar.
     */
    @FXML
    private void onNewExemplar() {
        String raw = searchByIdField.getText();
        if (raw == null || raw.isBlank()) {
            showSearchError(ERROR_TITLE, "Introdueix un ID.");
            return;
        }
        if (!raw.matches("^\\d+$")) {
            showSearchError("Error format", "L'ID ha de ser numeric.");
            return;
        }

        long id = Long.parseLong(raw.trim());
        Task<Llibre> task = createSearchByIdTask(id);

        task.setOnSucceeded(e -> onNewExemplarTaskSucceeded(task.getValue()));

        task.setOnFailed(e -> showSearchError(ERROR_TITLE, LLIBRE_NO_TROBAT));
        ApiClient.BG_EXEC.submit(task);
    }

    private void onNewExemplarTaskSucceeded(Llibre b) {
        if (b == null) showSearchError("No trobat", LLIBRE_NO_TROBAT);
        else navigator.showMainView(BOOK_FORM_VIEW_PATH, (BookFormController c) -> c.setBookData(b, "ADD_EXEMPLAR"));
    }

    /**
     * Navega a la vista de detalls del llibre.
     *
     * @param book Llibre a visualitzar
     */
    private void viewBook(Llibre book) {
        if (book != null) navigator.showMainView(BOOK_FORM_VIEW_PATH, (BookFormController c) -> c.setBookData(book, "VIEW"));
    }

    /**
     * Gestiona l'eliminacio d'un llibre.
     *
     * @param book Llibre a eliminar
     */
    private void deleteBook(Llibre book) {
        if (book == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminacio");
        alert.setHeaderText("Vols eliminar aquest llibre?");
        alert.setContentText(String.format("ID: %d%nTítol: %s", book.getId(), book.getTitol()));
        // single-expression lambda preserved; replaced multi-statement body by helper to remove unnecessary braces
        alert.showAndWait().ifPresent(btn -> { if (btn == ButtonType.OK) checkAndDeleteBookWithExemplarCheck(book); });
    }

    /**
     * Comprova si hi ha exemplars vinculats abans d'eliminar el llibre.
     *
     * @param book Llibre a verificar
     */
    private void checkAndDeleteBookWithExemplarCheck(Llibre book) {
        Task<List<Exemplar>> checkTask = createExemplarsCheckTask(book);

        // delegate to helper to remove multi-statement lambda
        checkTask.setOnSucceeded(e -> onCheckExemplarsSucceeded(checkTask.getValue(), book));

        checkTask.setOnFailed(e -> {
            Throwable ex = checkTask.getException();
            LOGGER.log(Level.WARNING, "Error comprovant exemplars abans d'eliminar llibre", ex);
            showError("Error comprovant exemplars: " + (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
        });

        ApiClient.BG_EXEC.submit(checkTask);
    }

    private void onCheckExemplarsSucceeded(List<Exemplar> linked, Llibre book) {
        if (linked == null || linked.isEmpty()) {
            performDeleteBook(book);
        } else {
            Platform.runLater(() -> {
                String msg = String.format(
                        "No es pot eliminar aquest llibre perque te %d exemplars associats.%n" +
                                "Primer cal eliminar-los",
                        linked.size()
                );

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Impossibilitat d'eliminar");
                info.setHeaderText("Aquest llibre te exemplars associats");
                info.setContentText(msg);
                info.showAndWait();
            });
        }
    }

    /**
     * Crea una tasca per comprovar exemplars associats a un llibre.
     *
     * @param book Llibre a verificar
     * @return Tasca de verificacio d'exemplars
     */
    private Task<List<Exemplar>> createExemplarsCheckTask(Llibre book) {
        return new Task<>() {
            @Override
            protected List<Exemplar> call() throws Exception {
                List<Exemplar> all = exemplarService.getAllExemplars();
                return all.stream()
                        .filter(x -> x.getLlibre() != null && x.getLlibre().getId() != null && x.getLlibre().getId().equals(book.getId()))
                        .toList();
            }
        };
    }

    /**
     * Executa l'eliminacio del llibre.
     *
     * @param book Llibre a eliminar
     */
    private void performDeleteBook(Llibre book) {
        Task<Void> task = createDeleteBookTask(book);

        // delegate to helper to remove multi-statement lambda
        task.setOnSucceeded(e -> onDeleteBookSucceeded(book));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error eliminant llibre", ex);

            if (ex instanceof ApiException apiEx) {
                int code = apiEx.getStatusCode();
                if (code == 409) {
                    showError("No es pot eliminar aquest llibre perque hi ha registres associats (exemplars)");
                    return;
                }
            }

            showError("Error eliminant llibre: " + (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    private void onDeleteBookSucceeded(Llibre book) {
        Platform.runLater(() -> {
            masterList.remove(book);
            applyFilterAndPagination();
            // inline the success dialog here to avoid always-passing-a-constant to the generic showInfo
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText(SUCCESS_DELETE);
            a.showAndWait();
        });
    }

    /**
     * Crea una tasca per eliminar un llibre.
     *
     * @param book Llibre a eliminar
     * @return Tasca d'eliminacio
     */
    private Task<Void> createDeleteBookTask(Llibre book) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                llibreService.deleteBook(book.getId());
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
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    /**
     * Mostra un dialeg d'error.
     *
     * @param title Titol de l'error
     * @param message Missatge de l'error
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    /**
     * Mostra un dialeg d'error amb titol per defecte.
     *
     * @param message Missatge de l'error
     */
    private void showError(String message) {
        showError(ERROR_TITLE, message);
    }
}
