package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.Horari;
import com.bibliosedaos.desktop.security.SessionStore;
import com.bibliosedaos.desktop.service.GrupService;
import com.bibliosedaos.desktop.service.HorariService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per a la llista de grups.
 * Gestiona la visualització, cerca i navegació entre grups del sistema.
 * Implementa paginació i gestió d'horaris.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class GroupsListController {

    private static final Logger LOGGER = Logger.getLogger(GroupsListController.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String GROUP_FORM_VIEW_PATH = "/com/bibliosedaos/desktop/group-form-view.fxml";
    private static final String ERROR_DESCONEGUT = "Error desconegut";
    private static final String ERROR_TITLE = "Error";

    @FXML private TableView<Grup> groupsTable;
    @FXML private TableColumn<Grup, Long> idColumn;
    @FXML private TableColumn<Grup, String> nomColumn;
    @FXML private TableColumn<Grup, String> tematicaColumn;
    @FXML private TableColumn<Grup, String> horariColumn;
    @FXML private TableColumn<Grup, Void> actionsColumn;
    @FXML private ScrollPane mainScrollPane;

    @FXML private Button veureHorarisButton;
    @FXML private Button afegirHorariButton;
    @FXML private Button meusGrupsButton;
    @FXML private Button crearGrupButton;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final GrupService grupService;
    private final HorariService horariService;
    private final Navigator navigator;

    private final ObservableList<Grup> masterList = FXCollections.observableArrayList();
    private final ObservableList<Grup> filteredList = FXCollections.observableArrayList();
    private final ObservableList<Grup> currentPageList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;
    private boolean showingMyGroups = false;

    /**
     * Constructor del controlador.
     *
     * @param grupService Servei per a operacions amb grups
     * @param horariService Servei per a operacions amb horaris
     * @param navigator Sistema de navegació entre vistes
     */
    public GroupsListController(GrupService grupService, HorariService horariService, Navigator navigator) {
        this.grupService = Objects.requireNonNull(grupService, "GrupService no pot ser null");
        this.horariService = Objects.requireNonNull(horariService, "HorariService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador després de carregar el FXML.
     * Configura la taula i carrega les dades inicials.
     */
    @FXML
    private void initialize() {
        applyButtonEffects();
        setupTable();
        loadGroups();
        setupHorariButtonVisibility();
    }

    /**
     * Aplica efectes de clic als botons de la interfície.
     */
    private void applyButtonEffects() {
        AnimationUtils.safeApplyClick(veureHorarisButton);
        AnimationUtils.safeApplyClick(afegirHorariButton);
        AnimationUtils.safeApplyClick(meusGrupsButton);
        AnimationUtils.safeApplyClick(crearGrupButton);
        AnimationUtils.safeApplyClick(prevPageButton);
        AnimationUtils.safeApplyClick(nextPageButton);
    }

    /**
     * Configura la visibilitat del botó d'afegir horari segons els permisos d'administrador.
     */
    private void setupHorariButtonVisibility() {
        boolean isAdmin = isAdmin();
        afegirHorariButton.setVisible(isAdmin);
        afegirHorariButton.setManaged(isAdmin);
    }

    /**
     * Configura la taula de grups amb les columnes i accions.
     */
    private void setupTable() {
        configureTableColumns();
        setupActionsColumn();
        groupsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        groupsTable.setItems(currentPageList);
    }

    /**
     * Configura les columnes de la taula amb les propietats de l'objecte Grup.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        tematicaColumn.setCellValueFactory(new PropertyValueFactory<>("tematica"));
        horariColumn.setCellValueFactory(cell -> {
            Grup grup = cell.getValue();
            if (grup != null && grup.getHorari() != null) {
                Horari horari = grup.getHorari();
                String horariText = safeGet(horari.getDia()) + " " + safeGet(horari.getHora());
                return new ReadOnlyStringWrapper(horariText.trim());
            }
            return new ReadOnlyStringWrapper("");
        });
    }

    /**
     * Configura la columna d'accions amb botons per veure i apuntar-se/desapuntar-se.
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> createActionsTableCell());
    }

    /**
     * Crea una cel·la de taula amb botons d'acció.
     *
     * @return TableCell configurada amb botons d'acció
     */
    private TableCell<Grup, Void> createActionsTableCell() {
        return new ActionsTableCell();
    }

    /**
     * Crea un botó d'acció amb icona.
     *
     * @param svgContent Contingut SVG de la icona
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
        AnimationUtils.safeApplyClick(button);

        return button;
    }

    /**
     * Cel·la de la taula que mostra els botons d'acció per a la fila actual.
     */
    private class ActionsTableCell extends TableCell<Grup, Void> {
        private final Button viewBtn;
        private final Button joinBtn;
        private final Button leaveBtn;
        private final HBox box;

        /**
         * Constructor: crea botons, contenidor i assigna handlers.
         */
        ActionsTableCell() {
            viewBtn = createActionButton(
                    "M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z",
                    "view-btn", "Veure detalls del grup", "Veure"
            );

            joinBtn = createActionButton(
                    "M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm6 13h-5v5h-2v-5h-5v-2h5v-5h2v5h5v2z",
                    "join-btn", "Apuntar-se al grup", "Apuntar-se"
            );

            leaveBtn = createActionButton(
                    "M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm6 13h-12v-2h12v2z",
                    "leave-btn", "Desapuntar-se del grup", "Desapuntar-se"
            );

            box = new HBox(6);
            box.getChildren().addAll(viewBtn, joinBtn, leaveBtn);
            configureButtonActions();
        }

        /**
         * Configura les accions dels botons d'acció.
         */
        private void configureButtonActions() {
            viewBtn.setOnAction(e -> {
                Grup currentItem = getCurrentItem();
                if (currentItem != null) viewGroup(currentItem);
            });

            joinBtn.setOnAction(e -> {
                Grup currentItem = getCurrentItem();
                if (currentItem != null) joinGroup(currentItem);
            });

            leaveBtn.setOnAction(e -> {
                Grup currentItem = getCurrentItem();
                if (currentItem != null) leaveGroup(currentItem);
            });
        }

        /**
         * Retorna el grup associat a la fila d'aquesta cel·la.
         *
         * @return Grup o null si no hi ha ítem
         */
        private Grup getCurrentItem() {
            int rowIndex = getIndex();
            return (rowIndex >= 0 && rowIndex < getTableView().getItems().size()) ?
                    getTableView().getItems().get(rowIndex) : null;
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }

            Grup grup = getCurrentItem();
            if (grup == null) {
                setGraphic(null);
                return;
            }
            Long currentUserId = SessionStore.getInstance().getUserId();
            if (currentUserId == null) {
                setGraphic(null);
                return;
            }
            boolean isAdminGroup = isUserAdminOfGroup(grup);
            boolean isMember = isUserMemberOfGroup(grup);

            if (isAdminGroup) {
                joinBtn.setVisible(false);
                joinBtn.setManaged(false);
                leaveBtn.setVisible(false);
                leaveBtn.setManaged(false);
            } else {
                joinBtn.setVisible(!isMember);
                joinBtn.setManaged(!isMember);
                leaveBtn.setVisible(isMember);
                leaveBtn.setManaged(isMember);
            }

            setGraphic(box);
        }
    }

    /**
     * Gestiona la navegació a la pàgina anterior.
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
     * Gestiona la navegació a la pàgina següent.
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
     * Gestiona la visualització dels horaris.
     */
    @FXML
    private void onVeureHoraris() {
        Task<List<Horari>> task = createLoadHorarisTask();

        task.setOnSucceeded(e -> {
            List<Horari> horaris = task.getValue();
            showHorarisDialog(horaris);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant horaris", ex);
            showError("Error carregant horaris", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona l'addició d'un nou horari.
     */
    @FXML
    private void onAfegirHorari() {
        showAddHorariDialog();
    }

    /**
     * Gestiona el filtre pels grups de l'usuari actual.
     */
    @FXML
    private void onMeusGrups() {
        showingMyGroups = !showingMyGroups;
        meusGrupsButton.getStyleClass().removeAll("primary-btn", "secondary-btn");

        if (showingMyGroups) {
            meusGrupsButton.getStyleClass().add("primary-btn");
            meusGrupsButton.setText("Tots els Grups");
        } else {
            meusGrupsButton.getStyleClass().add("secondary-btn");
            meusGrupsButton.setText("Els Meus Grups");
        }

        applyFilterAndPagination();
    }

    /**
     * Gestiona la creació d'un nou grup.
     */
    @FXML
    private void onCrearGrup() {
        navigator.showMainView(GROUP_FORM_VIEW_PATH,
                (GroupFormController controller) -> controller.setGroupData(null, "CREATE"));
    }

    /**
     * Carrega tots els grups del sistema.
     */
    private void loadGroups() {
        Task<List<Grup>> task = createLoadGroupsTask();

        task.setOnSucceeded(e -> onLoadGroupsSucceeded(task.getValue()));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant grups", ex);
            showError("Error carregant grups", ex.getMessage());
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Crea una tasca per carregar tots els grups.
     *
     * @return Tasca per carregar grups
     */
    private Task<List<Grup>> createLoadGroupsTask() {
        return new Task<>() {
            @Override
            protected List<Grup> call() throws Exception {
                return grupService.getAllGrups();
            }
        };
    }

    /**
     * Maneja l'èxit de la càrrega de grups.
     *
     * @param grups Llista de grups carregats
     */
    private void onLoadGroupsSucceeded(List<Grup> grups) {
        masterList.setAll(grups);
        applyFilterAndPagination();
    }

    /**
     * Crea una tasca per carregar tots els horaris.
     *
     * @return Tasca per carregar horaris
     */
    private Task<List<Horari>> createLoadHorarisTask() {
        return new Task<>() {
            @Override
            protected List<Horari> call() throws Exception {
                return horariService.getAllHoraris();
            }
        };
    }

    /**
     * Aplica els filtres i la paginació a la llista de grups.
     */
    private void applyFilterAndPagination() {
        if (showingMyGroups) {
            Long currentUserId = SessionStore.getInstance().getUserId();
            filteredList.setAll(masterList.filtered(grup -> {
                if (currentUserId == null) return false;

                return isUserAdminOfGroup(grup) || isUserMemberOfGroup(grup);
            }));
        } else {
            filteredList.setAll(masterList);
        }

        currentPage = 0;
        updatePagination();
    }

    /**
     * Actualitza la informació de paginació.
     */
    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredList.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        updateCurrentPage();
        updatePageButtons();
    }

    /**
     * Actualitza la pàgina actual.
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
                if (!currentPageList.isEmpty()) {
                    groupsTable.scrollTo(0);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "No s'ha pogut fer scroll al principi de la taula", ex);
            }
        });
    }

    /**
     * Actualitza l'estat dels botons de paginació.
     */
    private void updatePageButtons() {
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1 || filteredList.isEmpty());
    }

    /**
     * Mostra un diàleg amb tots els horaris.
     *
     * @param horaris Llista d'horaris a mostrar
     */
    private void showHorarisDialog(List<Horari> horaris) {
        Platform.runLater(() -> {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Horaris de Sales");
            dialog.setHeaderText("Llista completa d'horaris");

            TableView<Horari> table = new TableView<>();
            table.setItems(FXCollections.observableArrayList(horaris));

            TableColumn<Horari, Long> idHorariColumn = new TableColumn<>("ID");
            idHorariColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<Horari, String> salaHorariColumn = new TableColumn<>("Sala");
            salaHorariColumn.setCellValueFactory(new PropertyValueFactory<>("sala"));

            TableColumn<Horari, String> diaHorariColumn = new TableColumn<>("Dia");
            diaHorariColumn.setCellValueFactory(new PropertyValueFactory<>("dia"));

            TableColumn<Horari, String> horaHorariColumn = new TableColumn<>("Hora");
            horaHorariColumn.setCellValueFactory(new PropertyValueFactory<>("hora"));

            TableColumn<Horari, String> estatHorariColumn = new TableColumn<>("Estat");
            estatHorariColumn.setCellValueFactory(new PropertyValueFactory<>("estat"));

            table.getColumns().add(idHorariColumn);
            table.getColumns().add(salaHorariColumn);
            table.getColumns().add(diaHorariColumn);
            table.getColumns().add(horaHorariColumn);
            table.getColumns().add(estatHorariColumn);

            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefSize(600, 400);

            dialog.getDialogPane().setContent(table);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        });
    }

    /**
     * Mostra el diàleg per afegir un nou horari.
     */
    private void showAddHorariDialog() {
        Platform.runLater(() -> {
            Dialog<Horari> dialog = new Dialog<>();
            dialog.setTitle("Afegir Nou Horari");
            dialog.setHeaderText("Introdueix les dades del nou horari");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField salaField = new TextField();
            salaField.setPromptText("Ex: Sala A");
            TextField diaField = new TextField();
            diaField.setPromptText("Ex: Dilluns");
            TextField horaField = new TextField();
            horaField.setPromptText("Ex: 18:00");

            grid.add(new Label("Sala:"), 0, 0);
            grid.add(salaField, 1, 0);
            grid.add(new Label("Dia:"), 0, 1);
            grid.add(diaField, 1, 1);
            grid.add(new Label("Hora:"), 0, 2);
            grid.add(horaField, 1, 2);

            VBox content = new VBox(10);
            content.getChildren().add(grid);
            content.setPadding(new Insets(10));

            dialog.getDialogPane().setContent(content);

            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    if (!validateHorariFields(salaField.getText(), diaField.getText(), horaField.getText())) {
                        return null;
                    }

                    Horari horari = new Horari();
                    horari.setSala(salaField.getText().trim());
                    horari.setDia(diaField.getText().trim());
                    horari.setHora(horaField.getText().trim());
                    horari.setEstat("lliure");
                    return horari;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(this::createHorari);
        });
    }

    /**
     * Valida els camps del formulari d'horari.
     *
     * @param sala Sala a validar
     * @param dia Dia a validar
     * @param hora Hora a validar
     * @return true si tots els camps són vàlids
     */
    private boolean validateHorariFields(String sala, String dia, String hora) {
        if (sala.trim().isEmpty() || dia.trim().isEmpty() || hora.trim().isEmpty()) {
            showError(ERROR_TITLE, "Tots els camps són obligatoris");
            return false;
        }
        return true;
    }

    /**
     * Crea un nou horari al sistema.
     *
     * @param horari Horari a crear
     */
    private void createHorari(Horari horari) {
        Task<Horari> task = new Task<>() {
            @Override
            protected Horari call() throws Exception {
                return horariService.createHorari(horari);
            }
        };

        task.setOnSucceeded(e -> showInfo("Horari creat correctament"));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error creant horari", ex);
            showError("Error creant horari", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Navega a la vista de detalls del grup.
     *
     * @param grup Grup a visualitzar
     */
    private void viewGroup(Grup grup) {
        navigator.showMainView(GROUP_FORM_VIEW_PATH,
                (GroupFormController controller) -> controller.setGroupData(grup, "VIEW"));
    }

    /**
     * Gestiona l'apuntar-se a un grup.
     *
     * @param grup Grup al qual apuntar-se
     */
    private void joinGroup(Grup grup) {
        Long userId = SessionStore.getInstance().getUserId();
        if (userId == null) {
            showError(ERROR_TITLE, "No s'ha pogut identificar l'usuari");
            return;
        }

        Task<Grup> task = new Task<>() {
            @Override
            protected Grup call() throws Exception {
                return grupService.joinGrup(grup.getId(), userId);
            }
        };

        task.setOnSucceeded(e -> {
            showInfo("T'has apuntat al grup correctament");
            loadGroups();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error apuntant-se al grup", ex);
            showError("Error apuntant-se al grup", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Gestiona el desapuntar-se d'un grup.
     *
     * @param grup Grup del qual desapuntar-se
     */
    private void leaveGroup(Grup grup) {
        Long userId = SessionStore.getInstance().getUserId();
        if (userId == null) {
            showError(ERROR_TITLE, "No s'ha pogut identificar l'usuari");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                grupService.sortirDelGrup(grup.getId(), userId);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showInfo("T'has desapuntat del grup correctament");
            loadGroups();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error desapuntant-se del grup", ex);
            showError("Error desapuntant-se del grup", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Retorna una cadena segura sense valors null.
     *
     * @param value cadena a verificar
     * @return cadena original o cadena buida si és null
     */
    String safeGet(String value) {
        return value != null ? value : "";
    }

    /**
     * Comprova si l'usuari actual és administrador.
     *
     * @return true si l'usuari és administrador
     */
    boolean isAdmin() {
        try {
            return SessionStore.getInstance().getRol() == 2;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si l'usuari actual és administrador del grup.
     *
     * @param grup Grup a verificar
     * @return true si l'usuari és administrador del grup
     */
    boolean isUserAdminOfGroup(Grup grup) {
        Long currentUserId = SessionStore.getInstance().getUserId();
        if (currentUserId == null) return false;

        return grup.getAdministrador() != null &&
                grup.getAdministrador().getId() != null &&
                grup.getAdministrador().getId().equals(currentUserId);
    }

    /**
     * Verifica si l'usuari actual és membre del grup.
     *
     * @param grup Grup a verificar
     * @return true si l'usuari és membre del grup
     */
    boolean isUserMemberOfGroup(Grup grup) {
        Long currentUserId = SessionStore.getInstance().getUserId();
        if (currentUserId == null) return false;

        return grup.getMembres() != null &&
                grup.getMembres().stream()
                        .anyMatch(user -> user.getId() != null && user.getId().equals(currentUserId));
    }

    /**
     * Mostra un diàleg d'error.
     *
     * @param title Títol de l'error
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

    /**
     * Mostra un diàleg d'informació.
     *
     * @param message Missatge d'informació
     */
    private void showInfo(String message) {
        if (Boolean.getBoolean("tests.noDialog")) return;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informació");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}