package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Autor;
import com.bibliosedaos.desktop.model.Exemplar;
import com.bibliosedaos.desktop.model.Llibre;
import com.bibliosedaos.desktop.security.SessionStore;
import com.bibliosedaos.desktop.service.AutorService;
import com.bibliosedaos.desktop.service.ExemplarService;
import com.bibliosedaos.desktop.service.LlibreService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
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
 * Controlador per al formulari de gestio de llibres.
 * Gestiona la creacio, visualitzacio i gestio d'exemplars de llibres en diferents modes (CREATE, VIEW, ADD_EXEMPLAR).
 *
 * Assistencia d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class BookFormController {

    private static final Logger LOGGER = Logger.getLogger(BookFormController.class.getName());
    private static final String BOOKS_LIST_PATH = "/com/bibliosedaos/desktop/books-list-view.fxml";

    private static final String MODE_CREATE = "CREATE";
    private static final String MODE_VIEW = "VIEW";
    private static final String MODE_EDIT = "EDIT";
    private static final String MODE_ADD_EXEMPLAR = "ADD_EXEMPLAR";
    private static final String RESERVAT_LLIURE = "lliure";
    private static final String DISPONIBLE = "Disponible";
    private static final String ICON_PLUS = "icon-plus";
    private static final String DELETE_ICON = "M3 6v18h18v-18h-18zm5 14c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm4-18v2h-20v-2h5.711c.9 0 1.631-1.099 1.631-2h5.315c0 .901.73 2 1.631 2h5.712z";
    private static final String ERROR_UNKNOWN = "Error desconegut";

    @FXML private Label titleLabel;
    @FXML private Label bookIdLabel;
    @FXML private TextField isbnField;
    @FXML private TextField titolField;
    @FXML private TextField paginesField;
    @FXML private TextField editorialField;
    @FXML private ComboBox<Autor> autorCombo;
    @FXML private Label autorLabel;
    @FXML private Button addAutorButton;

    @FXML private TableView<Exemplar> exemplarsTable;
    @FXML private TableColumn<Exemplar, Long> exemplarIdColumn;
    @FXML private TableColumn<Exemplar, String> exemplarLlocColumn;
    @FXML private TableColumn<Exemplar, String> exemplarReservatColumn;
    @FXML private TableColumn<Exemplar, Void> exemplarActionsColumn;

    @FXML private TextField exemplarLlocField;
    @FXML private Label llocLabel;
    @FXML private Label estatLabel;
    @FXML private Label exemplarEstatLabel;
    @FXML private Button exemplarButton;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    @FXML private SVGPath formIcon;

    private final LlibreService llibreService;
    private final AutorService autorService;
    private final ExemplarService exemplarService;
    private final Navigator navigator;

    private Llibre currentBook;
    private String mode;
    private boolean initialized = false;

    /**
     * Constructor amb injeccio de dependencies.
     *
     * @param llibreService servei de gestio de llibres
     * @param autorService servei de gestio d'autors
     * @param exemplarService servei de gestio d'exemplars
     * @param navigator gestor de navegacio
     * @throws NullPointerException si alguna dependencia es null
     */
    public BookFormController(LlibreService llibreService,
                              AutorService autorService,
                              ExemplarService exemplarService,
                              Navigator navigator) {
        this.llibreService = Objects.requireNonNull(llibreService, "LlibreService no pot ser null");
        this.autorService = Objects.requireNonNull(autorService, "AutorService no pot ser null");
        this.exemplarService = Objects.requireNonNull(exemplarService, "ExemplarService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador despres de carregar el FXML.
     * Configura efectes visuals, taula d'exemplars i carrega dades inicials.
     */
    @FXML
    private void initialize() {
        AnimationUtils.safeApplyClick(saveButton);
        AnimationUtils.safeApplyClick(addAutorButton);
        AnimationUtils.safeApplyClick(exemplarButton);
        AnimationUtils.safeApplyClick(backButton);

        exemplarsTable.getStyleClass().add("data-table");
        exemplarsTable.setFixedCellSize(36);
        exemplarsTable.setPrefHeight(140);
        exemplarIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        exemplarLlocColumn.setCellValueFactory(new PropertyValueFactory<>("lloc"));
        exemplarReservatColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(mapReservatToDisplay(cell.getValue().getReservat()))
        );

        exemplarsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupExemplarActionsColumn();

        exemplarsTable.setItems(FXCollections.observableArrayList());

        autorCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Autor item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "" : item.getNom());
            }
        });
        autorCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Autor item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "" : item.getNom());
            }
        });

        loadAuthors();
        initialized = true;

        if (mode != null) populateForm();
    }

    /**
     * Converteix l'estat de reserva a text per mostrar.
     *
     * @param reservat estat de reserva de l'exemplar
     * @return text representatiu de l'estat
     */
    private String mapReservatToDisplay(String reservat) {
        if (reservat == null) return "—";
        return RESERVAT_LLIURE.equals(reservat) ? DISPONIBLE : reservat;
    }

    /**
     * Estableix les dades del llibre i el mode d'operacio.
     *
     * @param book llibre a mostrar/editar
     * @param mode mode d'operacio (CREATE, VIEW, ADD_EXEMPLAR)
     */
    public void setBookData(Llibre book, String mode) {
        this.currentBook = book;
        this.mode = mode != null ? mode : MODE_VIEW;
        if (initialized) populateForm();
    }

    /**
     * Omple el formulari amb les dades del llibre actual.
     * Configura la interfac segons el mode d'operacio.
     */
    private void populateForm() {

        if (currentBook != null) {
            bookIdLabel.setText(currentBook.getId() != null ? currentBook.getId().toString() : "");
            isbnField.setText(currentBook.getIsbn());
            titolField.setText(currentBook.getTitol());
            paginesField.setText(String.valueOf(currentBook.getPagines()));
            editorialField.setText(currentBook.getEditorial());
            if (currentBook.getAutor() != null) {
                autorCombo.setValue(currentBook.getAutor());
                autorLabel.setText(currentBook.getAutor().getNom());
            } else {
                autorCombo.getSelectionModel().clearSelection();
                autorLabel.setText("");
            }
        } else {
            clearForm();
        }

        exemplarEstatLabel.setText(DISPONIBLE);

        switch (mode) {
            case MODE_CREATE:
                applyIconAndTitle(MODE_CREATE);
                setFieldsEditable(true);
                autorCombo.setVisible(true);
                autorCombo.setManaged(true);
                autorLabel.setVisible(false);
                autorLabel.setManaged(false);
                addAutorButton.setVisible(true);
                addAutorButton.setManaged(true);

                saveButton.setVisible(true);
                saveButton.setManaged(true);

                exemplarButton.setDisable(true);
                exemplarButton.setVisible(true);
                exemplarButton.setManaged(true);

                backButton.setText("Cancel·lar");

                llocLabel.setVisible(false);
                llocLabel.setManaged(false);
                estatLabel.setVisible(false);
                estatLabel.setManaged(false);
                exemplarLlocField.setVisible(false);
                exemplarLlocField.setManaged(false);
                exemplarEstatLabel.setVisible(false);
                exemplarEstatLabel.setManaged(false);
                setExemplarActionsColumnVisible(false);

                exemplarsTable.getItems().clear();
                break;

            case MODE_EDIT:
                applyIconAndTitle(MODE_EDIT);
                setFieldsEditable(true);
                autorCombo.setVisible(true);
                autorCombo.setManaged(true);
                autorLabel.setVisible(false);
                autorLabel.setManaged(false);
                addAutorButton.setVisible(true);
                addAutorButton.setManaged(true);

                saveButton.setVisible(true);
                saveButton.setManaged(true);
                saveButton.setText("Actualitzar Llibre");

                exemplarButton.setDisable(false);
                exemplarButton.setVisible(true);
                exemplarButton.setManaged(true);
                exemplarButton.setText("Afegir Exemplar");

                backButton.setText("Cancel·lar");

                llocLabel.setVisible(true);
                llocLabel.setManaged(true);
                estatLabel.setVisible(true);
                estatLabel.setManaged(true);
                exemplarLlocField.setVisible(true);
                exemplarLlocField.setManaged(true);
                exemplarEstatLabel.setVisible(true);
                exemplarEstatLabel.setManaged(true);
                setExemplarActionsColumnVisible(true);

                if (currentBook != null && currentBook.getId() != null) loadExemplarsForBook(currentBook.getId());
                break;

            case MODE_ADD_EXEMPLAR:
                applyIconAndTitle(MODE_ADD_EXEMPLAR);
                setFieldsEditable(false);
                autorCombo.setVisible(false);
                autorCombo.setManaged(false);
                autorLabel.setVisible(true);
                autorLabel.setManaged(true);
                addAutorButton.setVisible(false);
                addAutorButton.setManaged(false);

                saveButton.setVisible(false);
                saveButton.setManaged(false);

                exemplarButton.setDisable(false);
                exemplarButton.setVisible(true);
                exemplarButton.setManaged(true);

                backButton.setText("Cancel·lar");

                llocLabel.setVisible(true);
                llocLabel.setManaged(true);
                estatLabel.setVisible(true);
                estatLabel.setManaged(true);
                exemplarLlocField.setVisible(true);
                exemplarLlocField.setManaged(true);
                exemplarEstatLabel.setVisible(true);
                exemplarEstatLabel.setManaged(true);
                setExemplarActionsColumnVisible(true);

                if (currentBook != null && currentBook.getId() != null) loadExemplarsForBook(currentBook.getId());
                break;

            default: // MODE_VIEW
                applyIconAndTitle(MODE_VIEW);
                setFieldsEditable(false);
                autorCombo.setVisible(false);
                autorCombo.setManaged(false);
                autorLabel.setVisible(true);
                autorLabel.setManaged(true);
                addAutorButton.setVisible(false);
                addAutorButton.setManaged(false);

                saveButton.setVisible(false);
                saveButton.setManaged(false);

                exemplarButton.setText("Crear Exemplar");
                exemplarButton.setDisable(true);
                exemplarButton.setVisible(false);
                exemplarButton.setManaged(false);

                backButton.setText("Tornar");

                llocLabel.setVisible(false);
                llocLabel.setManaged(false);
                estatLabel.setVisible(false);
                estatLabel.setManaged(false);
                exemplarLlocField.setVisible(false);
                exemplarLlocField.setManaged(false);
                exemplarEstatLabel.setVisible(false);
                exemplarEstatLabel.setManaged(false);

                setExemplarActionsColumnVisible(false);

                if (currentBook != null && currentBook.getId() != null) loadExemplarsForBook(currentBook.getId());
                break;
        }
    }

    /**
     * Neteja tots els camps del formulari.
     */
    private void clearForm() {
        isbnField.setText("");
        titolField.setText("");
        paginesField.setText("");
        editorialField.setText("");
        autorCombo.getSelectionModel().clearSelection();
        autorLabel.setText("");
        exemplarLlocField.setText("");
        exemplarEstatLabel.setText(DISPONIBLE);
        exemplarsTable.getItems().clear();
    }

    /**
     * Estableix l'edicio dels camps del formulari.
     *
     * @param editable true per permetre edicio, false per bloquejar
     */
    private void setFieldsEditable(boolean editable) {
        isbnField.setEditable(editable);
        titolField.setEditable(editable);
        paginesField.setEditable(editable);
        editorialField.setEditable(editable);

        boolean disable = !editable;
        autorCombo.setDisable(disable);
        addAutorButton.setDisable(disable);
        saveButton.setDisable(disable);
    }

    /**
     * Aplica la icona i titol segons el mode d'operacio.
     *
     * @param currentMode mode d'operacio actual
     */
    private void applyIconAndTitle(String currentMode) {
        final String iconEye = "M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z";
        final String iconPlus = "M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm6 13h-5v5h-2v-5h-5v-2h5v-5h2v5h5v2z";
        final String iconPencil = "M14.078 4.232l-12.64 12.639-1.438 7.129 7.127-1.438 12.641-12.64-5.69-5.69zm-10.369 14.893l-.85-.85 11.141-11.125.849.849-11.14 11.126zm2.008 2.008l-.85-.85 11.141-11.125.85.850-11.141 11.125zm18.283-15.444l-2.816 2.818-5.691-5.691 2.816-2.816 5.691 5.689z";

        switch (currentMode) {
            case MODE_VIEW:
                formIconSet(iconEye, "icon-eye");
                titleLabel.setText("Veure Llibre");
                break;
            case MODE_CREATE:
                formIconSet(iconPlus, ICON_PLUS);
                titleLabel.setText("Crear Nou Llibre");
                break;
            case MODE_EDIT:
                formIconSet(iconPencil, "icon-pencil");
                titleLabel.setText("Editar Llibre");
                break;
            case MODE_ADD_EXEMPLAR:
                formIconSet(iconPlus, ICON_PLUS);
                titleLabel.setText("Afegir Exemplar");
                break;
            default:
                formIconSet(iconPlus, ICON_PLUS);
                titleLabel.setText("Crear Nou Llibre");
        }
    }

    /**
     * Configura la icona del formulari.
     *
     * @param content contingut SVG de la icona
     * @param styleClass classe CSS per a l'estil
     */
    private void formIconSet(String content, String styleClass) {
        formIcon.getStyleClass().removeAll("icon-eye", ICON_PLUS, "icon-pencil");
        formIcon.setContent(content);
        formIcon.getStyleClass().add(styleClass);
        formIcon.setVisible(true);
        formIcon.setManaged(true);
    }

    /**
     * Carrega la llista d'autors des del servei.
     */
    private void loadAuthors() {
        Task<List<Autor>> task = new Task<>() {
            @Override protected List<Autor> call() throws Exception { return autorService.getAllAutors(); }
        };
        task.setOnSucceeded(e -> {
            List<Autor> items = task.getValue() != null ? task.getValue() : List.of();
            autorCombo.setItems(FXCollections.observableArrayList(items));
        });
        task.setOnFailed(e -> LOGGER.log(Level.WARNING, "Error carregant autors", task.getException()));
        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Carrega els exemplars associats a un llibre.
     *
     * @param llibreId identificador del llibre
     */
    private void loadExemplarsForBook(Long llibreId) {
        if (llibreId == null) {
            exemplarsTable.getItems().clear();
            return;
        }

        Task<List<Exemplar>> task = new Task<>() {
            @Override protected List<Exemplar> call() throws Exception {
                List<Exemplar> all = exemplarService.getAllExemplars();
                return all.stream()
                        .filter(x -> x.getLlibre() != null && x.getLlibre().getId() != null && x.getLlibre().getId().equals(llibreId))
                        .toList();
            }
        };

        task.setOnSucceeded(e -> exemplarsTable.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> {
            LOGGER.log(Level.WARNING, "Error carregant exemplars", task.getException());
            exemplarsTable.getItems().clear();
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Configura la columna d'accions per a la taula d'exemplars.
     */
    private void setupExemplarActionsColumn() {
        if (exemplarActionsColumn == null) return;

        exemplarActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = createDeleteExemplarButton();

            {
                AnimationUtils.safeApplyClick(delBtn);

                delBtn.setOnAction(e -> {
                    Exemplar current = getCurrent();
                    if (current == null) return;

                    boolean isLliure = current.getReservat() != null && RESERVAT_LLIURE.equalsIgnoreCase(current.getReservat());
                    if (!isLliure) {
                        Platform.runLater(() -> {
                            Alert a = new Alert(Alert.AlertType.WARNING);
                            a.setHeaderText(null);
                            a.setContentText("No es pot eliminar aquest exemplar fins que estigui disponible.");
                            a.showAndWait();
                        });
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmar eliminació");
                    confirm.setHeaderText("Vols eliminar aquest exemplar?");
                    confirm.setContentText(String.format("ID: %d%nLloc: %s", current.getId(), current.getLloc()));
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.OK) deleteExemplarById(current.getId());
                    });
                });
            }

            /**
             * Obté l'exemplar actual de la fila.
             *
             * @return exemplar actual o null si no n'hi ha
             */
            private Exemplar getCurrent() {
                int idx = getIndex();
                return (idx >= 0 && idx < getTableView().getItems().size()) ? getTableView().getItems().get(idx) : null;
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                setGraphic(new HBox(6, delBtn));
            }
        });
    }

    /**
     * Crea un boto per eliminar exemplars amb icona i tooltip.
     *
     * @return boto configurat per eliminar exemplars
     */
    private Button createDeleteExemplarButton() {
        Button button = new Button();
        SVGPath path = new SVGPath();
        path.setContent(DELETE_ICON);
        path.getStyleClass().add("action-icon");
        button.setGraphic(path);
        button.getStyleClass().addAll("action-btn", "delete-exemplar-btn");
        button.setTooltip(new Tooltip("Eliminar exemplar"));
        AnimationUtils.safeApplyClick(button);
        return button;
    }

    /**
     * Controla la visibilitat de la columna d'accions.
     *
     * @param visible true per mostrar, false per amagar
     */
    private void setExemplarActionsColumnVisible(boolean visible) {
        if (exemplarActionsColumn == null) return;
        exemplarActionsColumn.setVisible(visible);
        if (!visible) {
            exemplarActionsColumn.setMinWidth(0);
            exemplarActionsColumn.setPrefWidth(0);
            exemplarActionsColumn.setMaxWidth(0);
        } else {
            exemplarActionsColumn.setMinWidth(50);
        }
    }

    /**
     * Elimina un exemplar pel seu identificador.
     *
     * @param id identificador de l'exemplar a eliminar
     */
    private void deleteExemplarById(Long id) {
        if (id == null) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                exemplarService.deleteExemplar(id);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showInfo("Exemplar eliminat correctament.");
            if (currentBook != null && currentBook.getId() != null) loadExemplarsForBook(currentBook.getId());
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error eliminant exemplar", ex);
            if (ex instanceof ApiException apiEx) {
                showError("Error eliminant exemplar: " + apiEx.getMessage());
            } else {
                showError("Error eliminant exemplar: " + (ex != null ? ex.getMessage() : ERROR_UNKNOWN));
            }
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona l'accio d'afegir un nou autor.
     */
    @FXML
    private void onAddAutor() {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Afegir Autor");
        inputDialog.setHeaderText("Crea un autor nou");
        inputDialog.setContentText("Nom:");
        inputDialog.showAndWait().ifPresent(name -> {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) return;
            createAutor(trimmedName);
        });
    }

    /**
     * Crea un nou autor al sistema.
     *
     * @param name nom del nou autor
     */
    private void createAutor(String name) {
        Task<Autor> task = new Task<>() {
            @Override protected Autor call() throws Exception { return autorService.createAutor(new Autor(null, name)); }
        };
        task.setOnSucceeded(e -> {
            Autor created = task.getValue();
            autorCombo.getItems().add(created);
            autorCombo.setValue(created);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error creant autor", ex);
            showError("Error creant autor: " + (ex != null ? ex.getMessage() : ERROR_UNKNOWN));
        });
        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona l'accio d'afegir un nou exemplar.
     */
    @FXML
    private void onAddExemplar() {
        if (currentBook == null || currentBook.getId() == null) {
            showError("Primer desa o selecciona el llibre per poder afegir un exemplar.");
            return;
        }
        String lloc = exemplarLlocField.getText();
        if (lloc == null || lloc.isBlank()) {
            showError("El camp 'Lloc' és obligatori.");
            return;
        }

        Exemplar exemplar = new Exemplar();
        exemplar.setLloc(lloc);
        exemplar.setReservat(RESERVAT_LLIURE);
        Llibre ref = new Llibre();
        ref.setId(currentBook.getId());
        exemplar.setLlibre(ref);

        Task<Exemplar> task = new Task<>() {
            @Override protected Exemplar call() throws Exception { return exemplarService.createExemplar(exemplar); }
        };

        task.setOnSucceeded(e -> {
            showInfo("Exemplar creat correctament.");
            exemplarLlocField.clear();
            loadExemplarsForBook(currentBook.getId());
        });

        task.setOnFailed(e -> {
            Throwable exx = task.getException();
            LOGGER.log(Level.WARNING, "Error creant exemplar", exx);
            showError("Error creant exemplar: " + (exx != null ? exx.getMessage() : ERROR_UNKNOWN));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona l'accio de guardar el formulari.
     */
    @FXML
    private void onSave() {
        if (!validateFields()) return;

        Llibre book = createBookFromForm();

        if (MODE_CREATE.equals(mode)) {
            Task<Llibre> task = new Task<>() {
                @Override protected Llibre call() throws Exception { return llibreService.createBook(book); }
            };
            task.setOnSucceeded(e -> {
                Llibre created = task.getValue();
                currentBook = created;
                bookIdLabel.setText(created.getId().toString());
                exemplarButton.setDisable(false);
                showInfo("Llibre creat correctament.");
                setBookData(created, MODE_ADD_EXEMPLAR);
            });
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                LOGGER.log(Level.WARNING, "Error creant llibre", ex);
                showError("Error creant llibre: " + (ex != null ? ex.getMessage() : ERROR_UNKNOWN));
            });
            ApiClient.BG_EXEC.submit(task);
        } else if (MODE_EDIT.equals(mode)) {
            Task<Llibre> task = new Task<>() {
                @Override protected Llibre call() throws Exception {
                    return llibreService.updateBook(currentBook.getId(), book);
                }
            };

            task.setOnSucceeded(e -> {
                Llibre updated = task.getValue();
                currentBook = updated;
                showInfo("Llibre actualitzat correctament.");
                setBookData(updated, MODE_VIEW);
            });

            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                LOGGER.log(Level.WARNING, "Error actualitzant llibre", ex);
                showError("Error actualitzant llibre: " + (ex != null ? ex.getMessage() : ERROR_UNKNOWN));
            });

            ApiClient.BG_EXEC.submit(task);
        } else {
            showError("Operació no disponible.");
        }
    }

    /**
     * Valida els camps del formulari de llibre.
     *
     * @return true si tots els camps son valids, false altrament
     */
    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();
        if (isbnField.getText().trim().isEmpty()) errorMessage.append("El ISBN és obligatori\n");
        if (titolField.getText().trim().isEmpty()) errorMessage.append("El títol és obligatori\n");
        try {
            int paginas = Integer.parseInt(paginesField.getText().trim());
            if (paginas < 1) errorMessage.append("Les pàgines han de ser >= 1\n");
        } catch (NumberFormatException e) { errorMessage.append("Les pàgines han de ser numèriques\n"); }
        if (editorialField.getText().trim().isEmpty()) errorMessage.append("L'editorial és obligatòria\n");
        if (autorCombo.getValue() == null) errorMessage.append("Selecciona un autor o crea-ne un nou\n");

        if (!errorMessage.isEmpty()) { showError(errorMessage.toString().trim()); return false; }
        return true;
    }

    /**
     * Crea un objecte Llibre amb les dades del formulari.
     *
     * @return objecte Llibre amb les dades del formulari
     */
    private Llibre createBookFromForm() {
        Llibre book = new Llibre();
        book.setIsbn(isbnField.getText().trim());
        book.setTitol(titolField.getText().trim());
        book.setPagines(Integer.parseInt(paginesField.getText().trim()));
        book.setEditorial(editorialField.getText().trim());
        book.setAutor(autorCombo.getValue());
        return book;
    }

    /**
     * Gestiona l'accio de tornar a la llista de llibres.
     */
    @FXML
    private void onBack() {
        try {
            int rol = SessionStore.getInstance().getRol();
            if (rol == 2) {
                navigator.showMainView(BOOKS_LIST_PATH);
            } else {
                navigator.showMainView("/com/bibliosedaos/desktop/books-browse-view.fxml");
            }
        } catch (Exception ex) {
            navigator.showMainView("/com/bibliosedaos/desktop/welcome-view.fxml");
        }
    }

    /**
     * Mostra un missatge d'error.
     *
     * @param message missatge d'error a mostrar
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
        });
    }

    /**
     * Mostra un missatge d'informacio.
     *
     * @param message missatge d'informacio a mostrar
     */
    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
        });
    }
}