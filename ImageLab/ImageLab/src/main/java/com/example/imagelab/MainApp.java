package com.example.imagelab;

import com.example.imagelab.modele.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.InputStream;
import org.h2.tools.Server;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Initialisation de la base de données
        DatabaseManager.initialiserBase();

        // Lancement de la console H2
        try {
            Server webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8083");
            webServer.start();
            System.out.println("Console H2 sur : http://localhost:8083");
            System.out.println("JDBC URL : jdbc:h2:" + System.getProperty("user.dir") + "/gestionimage_db");
        } catch (Exception e) {

        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));
        StackPane root = fxmlLoader.load();
        Scene scene = new Scene(root, 1100, 700);

        stage.setMinWidth(900);
        stage.setMinHeight(650);

        InputStream logoStream = getClass().getResourceAsStream("/com/example/imagelab/icons/app_icon.png");        if (logoStream != null) {
            stage.getIcons().add(new Image(logoStream));
        }

        stage.setTitle("ImageLab");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
