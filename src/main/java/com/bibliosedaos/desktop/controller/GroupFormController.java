package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.api.ApiClient;
import com.bibliosedaos.desktop.api.ApiException;
import com.bibliosedaos.desktop.model.Grup;
import com.bibliosedaos.desktop.model.Horari;
import com.bibliosedaos.desktop.model.User;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador per al formulari de gestió de grups.
 * Gestiona la creació i visualització de grups en diferents modes (CREATE, VIEW).
 *
 * Assistència d'IA: fragments de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class GroupFormController {

    private static final Logger LOGGER = Logger.getLogger(GroupFormController.class.getName());
    private static final String GROUPS_LIST_PATH = "/com/bibliosedaos/desktop/groups-list-view.fxml";
    private static final String MODE_CREATE = "CREATE";
    private static final String MODE_VIEW = "VIEW";
    private static final String ERROR_DESCONEGUT = "Error desconegut";
    private static final String NO_GROUP_SELECTED_MESSAGE = "No s'ha seleccionat cap grup";
    private static final String GROUP_CREATED_MESSAGE = "Grup creat correctament";
    private static final String INFO_TITLE = "Informació";
    private static final String CONFIRMAR_ELIMINACIO_TITLE = "Confirmar eliminació";

    private static final String ICON_EYE_PATH = "M12.015 7c4.751 0 8.063 3.012 9.504 4.636-1.401 1.837-4.713 5.364-9.504 5.364-4.42 0-7.93-3.536-9.478-5.407 1.493-1.647 4.817-4.593 9.478-4.593zm0-2c-7.569 0-12.015 6.551-12.015 6.551s4.835 7.449 12.015 7.449c7.733 0 11.985-7.449 11.985-7.449s-4.291-6.551-11.985-6.551zm-.015 3c-2.21 0-4 1.791-4 4s1.79 4 4 4c2.209 0 4-1.791 4-4s-1.791-4-4-4zm-.004 3.999c-.564.564-1.479.564-2.044 0s-.565-1.48 0-2.044c.564-.564 1.479-.564 2.044 0s.565 1.479 0 2.044z";
    private static final String ICON_PLUS_PATH = "M12 0c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm6 13h-5v5h-2v-5h-5v-2h5v-5h2v5h5v2z";
    private static final String DELETE_ICON = "M3 6v18h18v-18h-18zm5 14c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm5 0c0 .552-.448 1-1 1s-1-.448-1-1v-10c0-.552.448-1 1-1s1 .448 1 1v10zm4-18v2h-20v-2h5.711c.9 0 1.631-1.099 1.631-2h5.315c0 .901.73 2 1.631 2h5.712z";

    @FXML private Label titleLabel;
    @FXML private Label groupIdLabel;
    @FXML private TextField nomField;
    @FXML private TextField tematicaField;
    @FXML private ComboBox<Horari> horariCombo;
    @FXML private Label horariLabel;
    @FXML private Label adminLabel;

    @FXML private TableView<User> membresTable;
    @FXML private TableColumn<User, Long> membreIdColumn;
    @FXML private TableColumn<User, String> membreNomColumn;
    @FXML private TableColumn<User, String> membreEmailColumn;
    @FXML private TableColumn<User, Void> membreActionsColumn;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button apuntarButton;
    @FXML private Button desapuntarButton;
    @FXML private Button eliminarButton;

    @FXML private SVGPath formIcon;

    private final GrupService grupService;
    private final HorariService horariService;
    private final Navigator navigator;

    private Grup currentGroup;
    private String mode;
    private boolean initialized = false;
    private final ObservableList<User> membresList = FXCollections.observableArrayList();
    private final ObservableList<Horari> horarisList = FXCollections.observableArrayList();

    /**
     * Constructor amb injecció de dependències.
     *
     * @param grupService servei de gestió de grups
     * @param horariService servei de gestió d'horaris
     * @param navigator gestor de navegació
     * @throws NullPointerException si alguna dependència és null
     */
    public GroupFormController(GrupService grupService, HorariService horariService, Navigator navigator) {
        this.grupService = Objects.requireNonNull(grupService, "GrupService no pot ser null");
        this.horariService = Objects.requireNonNull(horariService, "HorariService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador després de carregar el FXML.
     * Configura efectes visuals, modes d'operació i dades del formulari.
     */
    @FXML
    private void initialize() {
        applyButtonEffects();
        setupTable();
        loadHoraris();
        setupForm();
        loadGroupData();
        initialized = true;
    }

    /**
     * Aplica efectes de clic als botons de la interfície.
     */
    private void applyButtonEffects() {
        AnimationUtils.safeApplyClick(saveButton);
        AnimationUtils.safeApplyClick(cancelButton);
        AnimationUtils.safeApplyClick(backButton);
        AnimationUtils.safeApplyClick(apuntarButton);
        AnimationUtils.safeApplyClick(desapuntarButton);
        AnimationUtils.safeApplyClick(eliminarButton);
    }

    /**
     * Configura la taula de membres amb les columnes i accions.
     */
    private void setupTable() {
        configureTableColumns();
        setupMembreActionsColumn();
        membresTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        membresTable.setItems(membresList);
    }

    /**
     * Configura les columnes de la taula amb les propietats de l'objecte User.
     */
    private void configureTableColumns() {
        membreIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        membreNomColumn.setCellValueFactory(cell -> {
            User user = cell.getValue();
            if (user != null) {
                String nomComplet = safeGet(user.getNom()) + " " + safeGet(user.getCognom1());
                return new ReadOnlyStringWrapper(nomComplet.trim());
            }
            return new ReadOnlyStringWrapper("");
        });
        membreEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    /**
     * Configura la columna d'accions amb botons per eliminar membres.
     */
    private void setupMembreActionsColumn() {
        membreActionsColumn.setCellFactory(col -> new MembreActionsTableCell());
    }

    /**
     * Cel·la de la taula que mostra els botons d'acció per als membres.
     */
    private class MembreActionsTableCell extends TableCell<User, Void> {
        private final Button deleteBtn;
        private final HBox box;

        /**
         * Constructor: crea botons i assigna handlers.
         */
        MembreActionsTableCell() {
            deleteBtn = createDeleteMemberButton();
            box = new HBox(6, deleteBtn);
            configureButtonActions();
        }

        /**
         * Configura les accions dels botons d'acció.
         */
        private void configureButtonActions() {
            deleteBtn.setOnAction(e -> {
                User current = getCurrentItem();
                if (current != null) deleteMembre(current);
            });
        }

        /**
         * Retorna l'usuari associat a la fila d'aquesta cel·la.
         *
         * @return User o null si no hi ha item
         */
        private User getCurrentItem() {
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

            boolean isAdminGlobal = SessionStore.getInstance().getRol() == 2;
            setGraphic(isAdminGlobal ? box : null);
        }

        /**
         * Crea un botó d'eliminació per als membres del grup.
         *
         * @return Botó configurat per eliminar membres
         */
        private Button createDeleteMemberButton() {
            Button button = new Button("Eliminar");
            SVGPath svg = new SVGPath();
            svg.setContent(DELETE_ICON);
            svg.getStyleClass().add("action-icon");
            button.setGraphic(svg);
            button.getStyleClass().addAll("action-btn", "delete-member-btn");
            button.setTooltip(new Tooltip("Eliminar membre del grup"));
            AnimationUtils.safeApplyClick(button);
            return button;
        }
    }

    /**
     * Configura la visibilitat i gestió d'un node al layout.
     *
     * @param node node a configurar
     * @param visible true per mostrar, false per amagar
     */
    private void setVisibleManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Configura el formulari segons el mode d'operació.
     */
    private void setupForm() {
        if (mode == null) mode = MODE_VIEW;

        setVisibleManaged(saveButton, false);
        setVisibleManaged(cancelButton, false);
        setVisibleManaged(backButton, false);
        setVisibleManaged(apuntarButton, false);
        setVisibleManaged(desapuntarButton, false);
        setVisibleManaged(eliminarButton, false);
        setVisibleManaged(horariCombo, false);
        setVisibleManaged(horariLabel, true);

        formIcon.getStyleClass().removeAll("icon-eye", "icon-plus");
        formIcon.setVisible(true);
        formIcon.setManaged(true);

        switch (mode) {
            case MODE_CREATE:
                configureCreateMode();
                break;
            case MODE_VIEW:
                configureViewMode();
                break;
            default:
                configureViewMode();
                break;
        }

        boolean showActions = SessionStore.getInstance().getRol() == 2 && MODE_VIEW.equals(mode);
        setMembreActionsColumnVisible(showActions);
    }

    /**
     * Configura el formulari en mode CREAR.
     */
    private void configureCreateMode() {
        titleLabel.setText("Crear Nou Grup");
        setFieldsEditable(true);
        setVisibleManaged(saveButton, true);
        setVisibleManaged(cancelButton, true);
        setVisibleManaged(groupIdLabel, false);
        setVisibleManaged(horariLabel, false);
        setVisibleManaged(horariCombo, true);
        setVisibleManaged(adminLabel, false);
        formIconSet(ICON_PLUS_PATH, "icon-plus");
    }

    /**
     * Configura el formulari en mode VISUALITZAR.
     */
    private void configureViewMode() {
        titleLabel.setText("Veure Grup");
        setFieldsEditable(false);
        setVisibleManaged(backButton, true);
        setVisibleManaged(apuntarButton, true);
        setVisibleManaged(desapuntarButton, true);
        setVisibleManaged(eliminarButton, true);
        setVisibleManaged(horariLabel, true);
        setVisibleManaged(horariCombo, false);
        formIconSet(ICON_EYE_PATH, "icon-eye");
    }

    /**
     * Configura la icona del formulari.
     */
    private void formIconSet(String content, String styleClass) {
        formIcon.setContent(content);
        formIcon.getStyleClass().add(styleClass);
    }

    /**
     * Controla la visibilitat de la columna d'accions.
     */
    private void setMembreActionsColumnVisible(boolean visible) {
        if (membreActionsColumn == null) return;
        membreActionsColumn.setVisible(visible);
        if (!visible) {
            membreActionsColumn.setMinWidth(0);
            membreActionsColumn.setPrefWidth(0);
            membreActionsColumn.setMaxWidth(0);
        } else {
            membreActionsColumn.setMinWidth(90);
        }
    }

    /**
     * Estableix l'edició dels camps del formulari.
     *
     * @param editable true per permetre edició, false per bloquejar
     */
    private void setFieldsEditable(boolean editable) {
        nomField.setEditable(editable);
        tematicaField.setEditable(editable);

        boolean disable = !editable;
        nomField.setDisable(disable);
        tematicaField.setDisable(disable);
        horariCombo.setDisable(disable);
        saveButton.setDisable(disable);
    }

    /**
     * Carrega la llista d'horaris des del servei.
     */
    private void loadHoraris() {
        Task<List<Horari>> task = new Task<>() {
            @Override
            protected List<Horari> call() throws Exception {
                return horariService.getAllHoraris();
            }
        };

        task.setOnSucceeded(e -> {
            List<Horari> items = task.getValue() != null ? task.getValue() : List.of();
            List<Horari> horarisLliures = items.stream()
                    .filter(horari -> "lliure".equals(horari.getEstat()))
                    .toList();
            horarisList.setAll(items);
            horariCombo.setItems(FXCollections.observableArrayList(horarisLliures));
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant horaris", ex);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Carrega les dades del grup al formulari.
     */
    private void loadGroupData() {
        if (MODE_CREATE.equals(mode)) {
            clearForm();
            return;
        }

        if (currentGroup != null) {
            populateFormWithGroupData(currentGroup);
            loadMembres(currentGroup.getId());
        } else {
            showError(NO_GROUP_SELECTED_MESSAGE);
        }
    }

    /**
     * Omple el formulari amb les dades del grup.
     *
     * @param grup grup amb les dades a mostrar
     */
    private void populateFormWithGroupData(Grup grup) {
        groupIdLabel.setText(grup.getId() != null ? grup.getId().toString() : "");
        nomField.setText(safeGet(grup.getNom()));
        tematicaField.setText(safeGet(grup.getTematica()));

        if (grup.getHorari() != null) {
            horariLabel.setText(formatHorariDisplay(grup.getHorari()));
            horariCombo.setValue(grup.getHorari());
        } else {
            horariLabel.setText("");
        }

        if (grup.getAdministrador() != null) {
            String adminInfo = String.format("%d - %s %s (%s)",
                    grup.getAdministrador().getId(),
                    safeGet(grup.getAdministrador().getNom()),
                    safeGet(grup.getAdministrador().getCognom1()),
                    safeGet(grup.getAdministrador().getEmail()));
            adminLabel.setText(adminInfo);
        } else {
            adminLabel.setText("");
        }
    }

    /**
     * Formata l'horari per a mostrar.
     */
    String formatHorariDisplay(Horari horari) {
        if (horari == null) return "";
        return String.format("%s - %s %s",
                safeGet(horari.getSala()),
                safeGet(horari.getDia()),
                safeGet(horari.getHora()));
    }

    /**
     * Neteja tots els camps del formulari.
     */
    private void clearForm() {
        nomField.setText("");
        tematicaField.setText("");
        horariCombo.getSelectionModel().clearSelection();
        horariLabel.setText("");
        adminLabel.setText("");
        membresList.clear();
    }

    /**
     * Carrega els membres del grup.
     *
     * @param grupId ID del grup
     */
    private void loadMembres(Long grupId) {
        if (grupId == null) {
            membresList.clear();
            return;
        }

        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return grupService.getMembres(grupId);
            }
        };

        task.setOnSucceeded(e -> {
            List<User> membres = task.getValue();
            membresList.setAll(membres != null ? membres : List.of());
            updateActionButtons();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error carregant membres del grup", ex);
            membresList.clear();
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Actualitza els botons d'acció segons l'estat de l'usuari actual.
     */
    private void updateActionButtons() {
        if (currentGroup == null || !MODE_VIEW.equals(mode)) return;

        Long currentUserId = SessionStore.getInstance().getUserId();
        if (currentUserId == null) {
            setVisibleManaged(apuntarButton, false);
            setVisibleManaged(desapuntarButton, false);
            setVisibleManaged(eliminarButton, false);
            return;
        }

        boolean isMember = membresList.stream()
                .anyMatch(user -> user.getId() != null && user.getId().equals(currentUserId));
        boolean isAdmin = currentGroup.getAdministrador() != null &&
                currentGroup.getAdministrador().getId() != null &&
                currentGroup.getAdministrador().getId().equals(currentUserId);
        boolean isAdminGlobal = SessionStore.getInstance().getRol() == 2;
        boolean shouldShowJoinLeaveButtons = !isAdmin;

        setVisibleManaged(apuntarButton, shouldShowJoinLeaveButtons && !isMember);
        setVisibleManaged(desapuntarButton, shouldShowJoinLeaveButtons && isMember);
        setVisibleManaged(eliminarButton, isAdminGlobal || isAdmin);
    }

    /**
     * Estableix les dades del grup i el mode d'operació.
     *
     * @param grup grup a mostrar/editar
     * @param mode mode d'operació (CREATE, VIEW)
     */
    public void setGroupData(Grup grup, String mode) {
        this.currentGroup = grup;
        this.mode = mode != null ? mode : MODE_VIEW;

        if (initialized) {
            setupForm();
            loadGroupData();
        }
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
     * Gestiona l'acció de guardar el formulari.
     */
    @FXML
    private void onSave() {
        if (!validateFields()) return;

        Grup grup = createGrupFromForm();
        createGrup(grup);
    }

    /**
     * Gestiona l'acció de cancel·lar i tornar a la llista de grups.
     */
    @FXML
    private void onCancel() {
        navigator.showMainView(GROUPS_LIST_PATH);
    }

    /**
     * Gestiona l'acció de tornar enrere a la llista de grups.
     */
    @FXML
    private void onBack() {
        navigator.showMainView(GROUPS_LIST_PATH);
    }

    /**
     * Gestiona l'acció d'apuntar-se al grup.
     */
    @FXML
    private void onApuntar() {
        if (currentGroup == null || currentGroup.getId() == null) {
            showError(NO_GROUP_SELECTED_MESSAGE);
            return;
        }

        Long userId = SessionStore.getInstance().getUserId();
        if (userId == null) {
            showError("No s'ha pogut identificar l'usuari");
            return;
        }

        performJoinGroup(currentGroup.getId(), userId);
    }

    /**
     * Gestiona l'acció de desapuntar-se del grup.
     */
    @FXML
    private void onDesapuntar() {
        if (currentGroup == null || currentGroup.getId() == null) {
            showError(NO_GROUP_SELECTED_MESSAGE);
            return;
        }

        Long userId = SessionStore.getInstance().getUserId();
        if (userId == null) {
            showError("No s'ha pogut identificar l'usuari");
            return;
        }

        performLeaveGroup(currentGroup.getId(), userId);
    }

    /**
     * Gestiona l'acció d'eliminar el grup.
     */
    @FXML
    private void onEliminar() {
        if (currentGroup == null) {
            showError(NO_GROUP_SELECTED_MESSAGE);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(CONFIRMAR_ELIMINACIO_TITLE);
        alert.setHeaderText("Vols eliminar aquest grup?");
        alert.setContentText(String.format("ID: %d%nNom: %s",
                currentGroup.getId(), currentGroup.getNom()));

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) performDelete(currentGroup);
        });
    }

    /**
     * Valida tots els camps del formulari.
     *
     * @return true si tots els camps són vàlids, false altrament
     */
    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (nomField.getText().trim().isEmpty()) {
            errorMessage.append("El nom és obligatori\n");
        }

        if (tematicaField.getText().trim().isEmpty()) {
            errorMessage.append("La temàtica és obligatòria\n");
        }

        if (horariCombo.getValue() == null) {
            errorMessage.append("Has de seleccionar un horari\n");
        }

        if (!errorMessage.isEmpty()) {
            showError("Error de validació", errorMessage.toString().trim());
            return false;
        }

        return true;
    }

    /**
     * Crea un objecte Grup amb les dades del formulari.
     *
     * @return objecte Grup amb les dades del formulari
     */
    private Grup createGrupFromForm() {
        Grup grup = new Grup();
        grup.setNom(nomField.getText().trim());
        grup.setTematica(tematicaField.getText().trim());
        grup.setHorari(horariCombo.getValue());

        User admin = new User();
        admin.setId(SessionStore.getInstance().getUserId());
        grup.setAdministrador(admin);

        return grup;
    }

    /**
     * Crea un nou grup al sistema.
     *
     * @param grup grup a crear
     */
    private void createGrup(Grup grup) {
        saveButton.setDisable(true);
        Task<Grup> task = new Task<>() {
            @Override
            protected Grup call() throws Exception {
                return grupService.createGrup(grup);
            }
        };

        task.setOnSucceeded(e -> {
            showSuccessAndNavigate();
            saveButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            String errorMsg = "Error al crear grup: " +
                    (exception instanceof ApiException ? exception.getMessage() : "Error de connexió");
            showError(errorMsg);
            LOGGER.log(Level.WARNING, "Error al crear grup", exception);
            Platform.runLater(() -> saveButton.setDisable(false));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Executa l'apuntar-se a un grup.
     *
     * @param grupId ID del grup
     * @param userId ID de l'usuari
     */
    private void performJoinGroup(Long grupId, Long userId) {
        Task<Grup> task = new Task<>() {
            @Override
            protected Grup call() throws Exception {
                return grupService.joinGrup(grupId, userId);
            }
        };

        task.setOnSucceeded(e -> {
            showInfo("T'has apuntat al grup correctament");
            loadMembres(grupId);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error apuntant-se al grup", ex);
            showError("Error apuntant-se al grup", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Executa el desapuntar-se d'un grup.
     *
     * @param grupId ID del grup
     * @param userId ID de l'usuari
     */
    private void performLeaveGroup(Long grupId, Long userId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                grupService.sortirDelGrup(grupId, userId);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showInfo("T'has desapuntat del grup correctament");
            loadMembres(grupId);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error desapuntant-se del grup", ex);
            showError("Error desapuntant-se del grup", ex != null ? ex.getMessage() : ERROR_DESCONEGUT);
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Executa l'eliminació d'un membre del grup.
     *
     * @param member membre a eliminar
     */
    private void deleteMembre(User member) {
        if (currentGroup == null || currentGroup.getId() == null) {
            showError(NO_GROUP_SELECTED_MESSAGE);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(CONFIRMAR_ELIMINACIO_TITLE);
        confirm.setHeaderText("Vols eliminar aquest membre del grup?");
        confirm.setContentText(String.format("ID: %d%nNom: %s",
                member.getId(), safeGet(member.getNom()) + " " + safeGet(member.getCognom1())));

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) performRemoveMember(member.getId());
        });
    }

    /**
     * Executa l'eliminació d'un membre del grup.
     *
     * @param memberId ID del membre a eliminar
     */
    private void performRemoveMember(Long memberId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                grupService.sortirDelGrup(currentGroup.getId(), memberId);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showInfo("Membre eliminat correctament del grup.");
            loadMembres(currentGroup.getId());
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error eliminant membre del grup", ex);
            showError("Error eliminant membre del grup: " + (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Executa l'eliminació d'un grup.
     *
     * @param grup grup a eliminar
     */
    private void performDelete(Grup grup) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                grupService.deleteGrup(grup.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> navigator.showMainView(GROUPS_LIST_PATH));

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.WARNING, "Error eliminant grup", ex);
            showError("Error eliminant grup: " + (ex != null ? ex.getMessage() : ERROR_DESCONEGUT));
        });

        ApiClient.BG_EXEC.submit(task);
    }

    /**
     * Mostra un missatge d'èxit i navega a la llista de grups.
     */
    private void showSuccessAndNavigate() {
        navigator.showMainView(GROUPS_LIST_PATH);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Èxit");
            alert.setHeaderText(null);
            alert.setContentText(GROUP_CREATED_MESSAGE);
            alert.showAndWait();
        });
    }

    /**
     * Mostra un missatge d'error.
     *
     * @param message missatge d'error a mostrar
     */
    private void showError(String message) {
        showError("Error", message);
    }

    /**
     * Mostra un missatge d'error amb títol.
     *
     * @param title títol de l'error
     * @param message missatge de l'error
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
     * Mostra un missatge d'informació.
     *
     * @param message missatge d'informació a mostrar
     */
    private void showInfo(String message) {
        if (Boolean.getBoolean("tests.noDialog")) return;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(INFO_TITLE);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}