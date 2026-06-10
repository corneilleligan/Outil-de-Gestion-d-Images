package com.example.imagelab.modele;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:h2:" + System.getProperty("user.dir") + "/gestionimage_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static Connection connection;

    public static Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void initialiserBase() {
        try (Statement st = getConnection().createStatement()) {

            // Création des tables, si elles n'existent pas
            st.execute("""
                CREATE TABLE IF NOT EXISTS images (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    chemin VARCHAR(500) UNIQUE NOT NULL,
                    image_data BLOB,
                    hash_mdp VARCHAR(64)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS tags (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    image_id INT NOT NULL,
                    tag VARCHAR(200) NOT NULL UNIQUE,
                    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS transformations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    image_id INT NOT NULL,
                    nom VARCHAR(200) NOT NULL,
                    ordre INT NOT NULL,
                    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE
                )
            """);

            // migrations pour bases existantes dans l'ordre et séparément
            executerMigration(st, "ALTER TABLE images ADD COLUMN IF NOT EXISTS image_data BLOB");
            executerMigration(st, "ALTER TABLE images ADD COLUMN IF NOT EXISTS hash_mdp VARCHAR(64)");

            // ajouter UNIQUE sur tag si pas déjà présent
            executerMigration(st, "ALTER TABLE tags ADD CONSTRAINT IF NOT EXISTS uq_tag UNIQUE (tag)");

            System.out.println("Base de donnees initialisee.");
        } catch (Exception e) {
            System.out.println("Erreur d'initialisation de la base");
        }
    }

    private static void executerMigration(Statement st, String sql) {
        try {
            st.execute(sql);
        } catch (Exception e) {
            // On ignore
        }
    }
}