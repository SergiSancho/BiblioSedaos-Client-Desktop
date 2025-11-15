
/**
 * Mòdul principal de l'aplicació BiblioSedaos Desktop.
 * Conté els paquets del client d'escriptori.
 * @author Sergio
 * @since 2025
 */
module com.bibliosedaos.bibliodesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;


    // PAQUETES PRINCIPALES
    opens com.bibliosedaos.desktop to javafx.fxml;
    exports com.bibliosedaos.desktop;

    // CONTROLLERS
    exports com.bibliosedaos.desktop.controller;
    opens com.bibliosedaos.desktop.controller to javafx.fxml;

    // SERVICIOS
    exports com.bibliosedaos.desktop.service;
    opens com.bibliosedaos.desktop.service to javafx.fxml;

    // API
    exports com.bibliosedaos.desktop.api;
    opens com.bibliosedaos.desktop.api to javafx.fxml;

    // UI - NAVIGATOR
    exports com.bibliosedaos.desktop.ui.navigator;
    opens com.bibliosedaos.desktop.ui.navigator to javafx.fxml;

    // UI - UTIL
    exports com.bibliosedaos.desktop.ui.util;
    opens com.bibliosedaos.desktop.ui.util to javafx.fxml;

    // DTOs
    exports com.bibliosedaos.desktop.model.dto;
    opens com.bibliosedaos.desktop.model.dto to com.fasterxml.jackson.databind, com.fasterxml.jackson.datatype.jsr310;

    // SECURITY
    exports com.bibliosedaos.desktop.security;
    opens com.bibliosedaos.desktop.security to javafx.fxml;
    exports com.bibliosedaos.desktop.model;
    opens com.bibliosedaos.desktop.model to com.fasterxml.jackson.databind, com.fasterxml.jackson.datatype.jsr310;

    // CONFIG
    exports com.bibliosedaos.desktop.config;
    opens com.bibliosedaos.desktop.config to javafx.fxml;
}