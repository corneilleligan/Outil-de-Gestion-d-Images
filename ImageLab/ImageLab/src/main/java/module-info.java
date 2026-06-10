module com.example.gestionimage {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.sql;
    requires java.desktop;
    requires com.h2database;

    opens com.example.imagelab to javafx.fxml;
    exports com.example.imagelab;
    exports com.example.imagelab.filtres;
    opens com.example.imagelab.filtres to javafx.fxml;
    exports com.example.imagelab.transformations;
    opens com.example.imagelab.transformations to javafx.fxml;
}