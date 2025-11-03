package com.bibliosedaos.desktop.config;

import com.bibliosedaos.desktop.controller.*;
import com.bibliosedaos.desktop.service.*;
import com.bibliosedaos.desktop.ui.navigator.Navigator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la creacio i injeccio de dependencies dels controladors.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class ControllerRegistry {
    private static final Logger LOGGER = Logger.getLogger(ControllerRegistry.class.getName());

    private final AuthService authService;
    private final UserService userService;
    private final LlibreService llibreService;
    private final AutorService autorService;
    private final ExemplarService exemplarService;
    private final Navigator navigator;

    /**
     * Constructor que rep totes les dependencies necessaries per als controladors.
     *
     * @param authService Servei d'autenticacio
     * @param userService Servei d'usuaris
     * @param llibreService Servei de llibres
     * @param autorService Servei d'autors
     * @param exemplarService Servei d'exemplars
     * @param navigator Gestor de navegacio
     */
    public ControllerRegistry(AuthService authService,
                              UserService userService,
                              LlibreService llibreService,
                              AutorService autorService,
                              ExemplarService exemplarService,
                              Navigator navigator) {
        this.authService = authService;
        this.userService = userService;
        this.llibreService = llibreService;
        this.autorService = autorService;
        this.exemplarService = exemplarService;
        this.navigator = navigator;
    }

    /**
     * Crea una instancia del controlador sol·licitat amb les dependencies injectades.
     *
     * @param clazz Classe del controlador a crear
     * @return Instancia del controlador amb les dependencies injectades
     * @throws RuntimeException si hi ha un error creant el controlador
     */
    public Object createController(Class<?> clazz) {
        try {
            if (clazz == LoginController.class) return new LoginController(authService, navigator);
            if (clazz == DashboardController.class) return new DashboardController(authService, navigator);
            if (clazz == ProfileEditController.class) return new ProfileEditController(userService, navigator);
            if (clazz == UsersListController.class) return new UsersListController(userService, navigator);
            if (clazz == UserFormController.class) return new UserFormController(userService, navigator);
            if (clazz == BooksListController.class) return new BooksListController(llibreService, exemplarService, navigator);
            if (clazz == BookFormController.class) return new BookFormController(llibreService, autorService, exemplarService, navigator);
            if (clazz == BooksBrowseController.class) return new BooksBrowseController(llibreService, exemplarService, navigator);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creant controller {0}", clazz.getName());
            throw new RuntimeException("Error creant controller " + clazz.getName(), e);
        }
    }
}
