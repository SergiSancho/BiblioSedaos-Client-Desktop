package com.bibliosedaos.desktop.controller;

import com.bibliosedaos.desktop.security.SessionStore;
import com.bibliosedaos.desktop.service.AuthService;
import com.bibliosedaos.desktop.ui.navigator.Navigator;
import com.bibliosedaos.desktop.ui.util.AnimationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador principal del dashboard de l'aplicació.
 * Gestiona la navegació entre diferents vistes dins d'un panell central dinàmic,
 * la configuració basada en el rol de l'usuari i les operacions de sessió.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    // Elements FXML
    @FXML private Label userNameLabel;
    @FXML private Label userIdLabel;
    @FXML private Button editProfileButton;
    @FXML private Button logoutButton;
    @FXML private VBox adminGroup;
    @FXML private VBox userGroup;
    @FXML private StackPane mainContentStack;
    @FXML private StackPane badgeStack;
    @FXML private Label badgeText;
    @FXML private VBox sidebar;

    private final AuthService authService;
    private final Navigator navigator;

    /**
     * Constructor amb injecció de dependències.
     *
     * @param authService servei d'autenticació
     * @param navigator gestor de navegació
     * @throws NullPointerException si alguna dependència és null
     */
    public DashboardController(AuthService authService, Navigator navigator) {
        this.authService = Objects.requireNonNull(authService, "AuthService no pot ser null");
        this.navigator = Objects.requireNonNull(navigator, "Navigator no pot ser null");
    }

    /**
     * Inicialitza el controlador després de carregar el FXML.
     * Configura efectes visuals, navegació i dades de sessió.
     */
    @FXML
    private void initialize() {
        initializeButtonEffects();
        setupNavigation();
        setupBySessionStore();
        setupWindowCloseHandler();
    }

    /**
     * Aplica efectes de click als botons de la interfície.
     */
    private void initializeButtonEffects() {
        safeApplyClick(editProfileButton);
        safeApplyClick(logoutButton);

        Platform.runLater(() -> {
            if (sidebar != null) {
                sidebar.lookupAll(".sidebar-btn").forEach(AnimationUtils::applyClickEffect);
            } else {
                LOGGER.fine("sidebar no injectat - revisa fx:id en FXML");
            }
        });
    }

    /**
     * Configura l'àrea de contingut principal per a la navegació dinàmica.
     */
    private void setupNavigation() {
        try {
            navigator.setMainContentArea(mainContentStack);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No s'ha pogut configurar l'àrea principal de navegació: {0}", e.getMessage());
        }
    }

    /**
     * Aplica efecte de click a un botó de forma segura.
     *
     * @param b botó al qual aplicar l'efecte
     */
    private void safeApplyClick(Button b) {
        if (b != null) AnimationUtils.applyClickEffect(b);
    }

    /**
     * Configura la interfície basant-se en les dades de sessió.
     */
    private void setupBySessionStore() {
        SessionStore store = SessionStore.getInstance();
        if (store == null) {
            handleMissingSessionStore();
            return;
        }

        configureUserInfo(store);
        configureRoleBasedUI(store.getRol());
        loadInitialView();
    }

    /**
     * Gestiona la falta de dades de sessió.
     */
    private void handleMissingSessionStore() {
        LOGGER.warning("SessionStore no disponible. UI inicialitzada amb valors per defecte.");
        setDisplayName("Usuari/a desconegut");
        showAdmin(false);
    }

    /**
     * Configura la informació de l'usuari a la interfície.
     *
     * @param store SessionStore amb les dades de l'usuari
     */
    private void configureUserInfo(SessionStore store) {
        setDisplayName(buildDisplayName(store));
        String uid = safeTrim(store.getUserId());
        if (userIdLabel != null) userIdLabel.setText(uid.isEmpty() ? "" : "ID: " + uid);
    }

    /**
     * Configura la interfície segons el rol de l'usuari.
     *
     * @param rol Rol de l'usuari (0 = admin)
     */
    private void configureRoleBasedUI(int rol) {
        boolean isAdmin = (rol == 0);
        showAdmin(isAdmin);
    }

    /**
     * Carrega la vista inicial al panell central.
     */
    private void loadInitialView() {
        Platform.runLater(() -> {
            try {
                navigator.showMainView("/com/bibliosedaos/desktop/welcome-view.fxml");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No s'ha pogut carregar la vista d'inici: {0}", e.getMessage());
            }
        });
    }

    /**
     * Construeix el nom complet per mostrar.
     *
     * @param store SessionStore amb les dades de l'usuari
     * @return nom complet formatat
     */
    String buildDisplayName(SessionStore store) {
        String nom = safeTrim(store.getNom());
        String cognom1 = safeTrim(store.getCognom1());
        String cognom2 = safeTrim(store.getCognom2());
        String uid = safeTrim(store.getUserId());

        if (nom.isEmpty()) {
            return (uid.isEmpty() ? "Usuari/a desconegut" : uid);
        }

        StringBuilder sb = new StringBuilder(nom);
        if (!cognom1.isEmpty()) sb.append(" ").append(cognom1);
        if (!cognom2.isEmpty()) sb.append(" ").append(cognom2);
        return sb.toString();
    }

    /**
     * Retorna una cadena neta de valors null.
     *
     * @param s cadena a netejar
     * @return cadena neta o cadena buida
     */
    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Estableix el nom a mostrar a la interfície.
     *
     * @param name nom a mostrar
     */
    private void setDisplayName(String name) {
        if (userNameLabel != null) userNameLabel.setText(name);
    }

    /**
     * Mostra o amaga els elements d'administració.
     *
     * @param isAdmin true si l'usuari és administrador
     */
    private void showAdmin(boolean isAdmin) {
        Runnable uiUpdate = () -> {
            setVisibility(adminGroup, isAdmin);
            setVisibility(userGroup, !isAdmin);
            setVisibility(badgeText, isAdmin);
            setVisibility(badgeStack, isAdmin);
        };

        if (Platform.isFxApplicationThread()) {
            uiUpdate.run();
        } else {
            Platform.runLater(uiUpdate);
        }
    }

    /**
     * Estableix la visibilitat d'un node de forma segura.
     *
     * @param node node a configurar
     * @param visible true per mostrar, false per amagar
     */
    private void setVisibility(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    /**
     * Executa el tancament de sessió
     */
    void performLogout() {
        try {
            authService.logout();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Error durante logout: {0}", e.getMessage());
        } finally {
            try {
                Platform.runLater(this::navigateToLoginAfterLogout);
            } catch (IllegalStateException e) {
                navigateToLoginAfterLogout();
            }
        }
    }

    /**
     * Navega a la vista de login després del tancament de sessió.
     */
    void navigateToLoginAfterLogout() {
        try {
            navigator.goTo("/com/bibliosedaos/desktop/login-view.fxml",
                    "BiblioSedaos - Login", 1000.0, 600.0, false, null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No sha pogut navegar al login després de logout: {0}", e.getMessage());
        }
    }

    /**
     * Gestiona el tancament de sessió (mètode lligat al botó FXML).
     */
    @FXML
    private void onLogout() {
        performLogout();
    }

    /**
     * Configura el tancament de finestra per a que realitzi un logout complet i navegi a login
     */
    private void setupWindowCloseHandler() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    event.consume();
                    performLogout();
                });
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No ha pogut configurar tancament de finestra: {0}", e.getMessage());
            }
        });
    }

    /**
     * Navega a una vista específica al panell central.
     *
     * @param fxmlPath ruta de la vista FXML
     */
    private void navigateTo(String fxmlPath) {
        try {
            navigator.showMainView(fxmlPath);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al navegar a {0}: {1}", new Object[]{fxmlPath, e.getMessage()});
        }
    }

    /**
     * Gestiona la navegació a l'edició de perfil.
     */
    @FXML
    private void onEditProfile() {
        navigateTo("/com/bibliosedaos/desktop/profile-edit-view.fxml");
    }

    /**
     * Navega a la vista d'inici.
     */
    @FXML
    private void onShowHome() {
        navigateTo("/com/bibliosedaos/desktop/welcome-view.fxml");
    }

    /**
     * Navega a la gestió d'usuaris (admin).
     */
    @FXML
    private void onManageUsers() {
        navigateTo("/com/bibliosedaos/desktop/users-list-view.fxml");
    }

    /**
     * Navega a la gestió de llibres (admin).
     */
    @FXML
    private void onManageBooks() {
        navigateTo("/com/bibliosedaos/desktop/books-list-view.fxml");
    }

    /**
     * Navega a la gestió de préstecs (admin).
     */
    @FXML
    private void onManageLoans() {
        navigateTo("/com/bibliosedaos/desktop/loans-list-view.fxml");
    }

    /**
     * Navega als meus préstecs (usuari).
     */
    @FXML
    private void onViewMyLoans() {
        navigateTo("/com/bibliosedaos/desktop/my-loans-view.fxml");
    }

    /**
     * Navega a la cerca de llibres (usuari).
     */
    @FXML
    private void onBrowseBooks() {
        navigateTo("/com/bibliosedaos/desktop/books-browse-view.fxml");
    }
}