module org.example.lalala {
    // Dependencias
    requires javafx.controls;
    requires javafx.fxml;
    requires com.almasb.fxgl.all;

    // Abrimos paquetes para FXML
    opens app to javafx.fxml;
    opens controller to javafx.fxml;

    // Exportamos si planeamos usar clases fuera del módulo
    exports app;
    exports controller;
}
