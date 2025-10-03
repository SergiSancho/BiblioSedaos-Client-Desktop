module com.bibliosedaos.bibliodesktop {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;

    // HTTP client (Java 11+)
    requires java.net.http;

    // Jackson JSON (si lo pones en module-path).
    // Si prefieres evitar problemas con módulos, añade jackson al classpath (no al module-path)
    requires com.fasterxml.jackson.databind;


    opens com.bibliosedaos.desktop to javafx.fxml;
    exports com.bibliosedaos.desktop;
    exports com.bibliosedaos.desktop.controller;
    opens com.bibliosedaos.desktop.controller to javafx.fxml;

    // Abrir los DTOs a Jackson para permitir serialización/deserialización por reflexión
    opens com.bibliosedaos.desktop.model.dto to com.fasterxml.jackson.databind;
}