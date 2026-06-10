package com.example.imagelab.modele;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MetadataManager {

    public static List<ImageMetadata> chargerTout() {
        List<ImageMetadata> liste = new ArrayList<>();
        String sql = "SELECT id, chemin, image_data, hash_mdp FROM images";

        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int imageId = rs.getInt("id");
                String chemin = rs.getString("chemin");
                ImageMetadata meta = new ImageMetadata(chemin);

                byte[] data = rs.getBytes("image_data");
                if (data != null) meta.setImageData(data);

                meta.setHashMotDePasse(rs.getString("hash_mdp"));

                try (PreparedStatement pst = conn.prepareStatement(
                        "SELECT tag FROM tags WHERE image_id = ?")) {
                    pst.setInt(1, imageId);
                    ResultSet rsTags = pst.executeQuery();
                    while (rsTags.next()) meta.ajouterTag(rsTags.getString("tag"));
                }

                try (PreparedStatement pst = conn.prepareStatement(
                        "SELECT nom FROM transformations WHERE image_id = ? ORDER BY ordre")) {
                    pst.setInt(1, imageId);
                    ResultSet rsTrans = pst.executeQuery();
                    while (rsTrans.next()) meta.ajouterTransformation(rsTrans.getString("nom"));
                }

                liste.add(meta);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement : " + e.getMessage());
        }
        return liste;
    }

    public static void sauvegarderTout(List<ImageMetadata> liste) {
        if (liste == null || liste.isEmpty()) return;
        ImageMetadata meta;
        synchronized (liste) {
            meta = liste.get(liste.size() - 1);
        }
        sauvegarder(meta);
    }

    public static void sauvegarderMeta(ImageMetadata meta) {
        sauvegarder(meta);
    }

    public static void sauvegarder(ImageMetadata meta) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            int imageId = obtenirOuCreerImageId(conn, meta.getCheminImage());

            try (PreparedStatement pst = conn.prepareStatement(
                    "UPDATE images SET image_data = ?, hash_mdp = ? WHERE id = ?")) {
                pst.setBytes(1, meta.getImageData());
                pst.setString(2, meta.getHashMotDePasse());
                pst.setInt(3, imageId);
                pst.executeUpdate();
            }

            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM tags WHERE image_id = ?")) {
                pst.setInt(1, imageId);
                pst.executeUpdate();
            }
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM transformations WHERE image_id = ?")) {
                pst.setInt(1, imageId);
                pst.executeUpdate();
            }

            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO tags (image_id, tag) VALUES (?, ?)")) {
                for (String tag : meta.getTags()) {
                    pst.setInt(1, imageId);
                    pst.setString(2, tag);
                    pst.addBatch();
                }
                pst.executeBatch();
            }

            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO transformations (image_id, nom, ordre) VALUES (?, ?, ?)")) {
                int ordre = 0;
                for (String transfo : meta.getTransformations()) {
                    pst.setInt(1, imageId);
                    pst.setString(2, transfo);
                    pst.setInt(3, ordre++);
                    pst.addBatch();
                }
                pst.executeBatch();
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static int obtenirOuCreerImageId(Connection conn, String chemin) throws Exception {
        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT id FROM images WHERE chemin = ?")) {
            pst.setString(1, chemin);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }

        try (PreparedStatement pst = conn.prepareStatement(
                "MERGE INTO images (chemin) KEY(chemin) VALUES (?)")) {
            pst.setString(1, chemin);
            pst.executeUpdate();
        }

        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT id FROM images WHERE chemin = ?")) {
            pst.setString(1, chemin);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }

        throw new Exception("Impossible de créer l'image");
    }

    public static ImageMetadata trouver(String chemin, List<ImageMetadata> liste) {
        synchronized (liste) {
            for (ImageMetadata m : liste) {
                if (m.getCheminImage().equals(chemin)) return m;
            }
        }
        return null;
    }

    public static List<ImageMetadata> rechercherParTag(String tag, List<ImageMetadata> liste) {
        List<ImageMetadata> resultats = new ArrayList<>();
        synchronized (liste) {
            for (ImageMetadata m : liste) {
                if (m.getTags().contains(tag)) resultats.add(m);
            }
        }
        return resultats;
    }
}