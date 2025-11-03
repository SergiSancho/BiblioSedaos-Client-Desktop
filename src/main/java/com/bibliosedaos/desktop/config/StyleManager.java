package com.bibliosedaos.desktop.config;

import com.bibliosedaos.desktop.ui.navigator.Navigator;

/**
 * Gestiona el registre d'estils CSS per a les diferents vistes de l'aplicacio.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public final class StyleManager {

    private static final String LOGIN_VIEW = "/com/bibliosedaos/desktop/login-view.fxml";
    private static final String DASHBOARD_VIEW = "/com/bibliosedaos/desktop/dashboard-view.fxml";
    private static final String WELCOME_VIEW = "/com/bibliosedaos/desktop/welcome-view.fxml";
    private static final String PROFILE_EDIT_VIEW = "/com/bibliosedaos/desktop/profile-edit-view.fxml";
    private static final String USERS_LIST_VIEW = "/com/bibliosedaos/desktop/users-list-view.fxml";
    private static final String USER_FORM_VIEW = "/com/bibliosedaos/desktop/user-form-view.fxml";
    private static final String BOOKS_LIST_VIEW = "/com/bibliosedaos/desktop/books-list-view.fxml";
    private static final String BOOK_FORM_VIEW = "/com/bibliosedaos/desktop/book-form-view.fxml";
    private static final String BOOKS_BROWSE_VIEW = "/com/bibliosedaos/desktop/books-browse-view.fxml";

    /**
     * Constructor privat per evitar instanciacio.
     */
    private StyleManager() {
        // Classe d'utilitat, no es pot instanciar
    }

    /**
     * Registra tots els estils CSS per a les vistes de l'aplicacio.
     *
     * @param navigator Instancia del Navigator on es registraran els estils
     */
    public static void registerStyles(Navigator navigator) {
        navigator.registerGlobalCss("/styles/app.css");
        navigator.registerViewCss(LOGIN_VIEW, "/styles/login.css");
        navigator.registerViewCss(DASHBOARD_VIEW, "/styles/dashboard.css");
        navigator.registerViewCss(WELCOME_VIEW, "/styles/welcome.css");
        navigator.registerViewCss(PROFILE_EDIT_VIEW, "/styles/profile-edit.css");
        navigator.registerViewCss(USERS_LIST_VIEW, "/styles/list.css");
        navigator.registerViewCss(USER_FORM_VIEW, "/styles/form.css");
        navigator.registerViewCss(BOOKS_LIST_VIEW, "/styles/list.css");
        navigator.registerViewCss(BOOK_FORM_VIEW, "/styles/form.css");
        navigator.registerViewCss(BOOKS_BROWSE_VIEW, "/styles/list.css");
    }
}