package com.example.imagelab;

import com.example.imagelab.filtres.*;
import com.example.imagelab.transformations.Rotation;
import com.example.imagelab.transformations.Symetrie;
import com.example.imagelab.transformations.Transformation;
import com.example.imagelab.modele.ImageMetadata;
import com.example.imagelab.modele.MetadataManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class MainController {

    @FXML private ImageView imageView;
    @FXML private StackPane rootPane;
    @FXML private Label statusLabel;
    @FXML private Label rechercheResultat;
    @FXML private TextField tagField;
    @FXML private TextField rechercheField;
    @FXML private TitledPane panneauOutils;
    @FXML private TitledPane panneauFiltres;
    @FXML private VBox placeholder;
    @FXML private HBox boxTags;


    // Etat
    private WritableImage imageCourante;
    private WritableImage imageOriginale;
    private String cheminImageCourante;
    private boolean imageChiffree = false;
    private boolean traitementEnCours = false;
    private List<ImageMetadata> toutesMetadonnees;

    private final Deque<WritableImage> undoStack = new ArrayDeque<>();

    private static final double IMAGE_WIDTH  = 860;
    private static final double IMAGE_HEIGHT = 460;

    // pour initialisation
    @FXML
    public void initialize() {
        statusLabel.setText("");
        toutesMetadonnees = MetadataManager.chargerTout();
    }

    // pour afficher
    private void afficherImage(WritableImage image) {
        imageView.setFitWidth(IMAGE_WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setImage(image);
    }

    private void afficherPanneaux() {
        panneauOutils.setVisible(true);
        panneauOutils.setManaged(true);
        panneauFiltres.setVisible(true);
        panneauFiltres.setManaged(true);
        placeholder.setVisible(false);
        placeholder.setManaged(false);
        boxTags.setVisible(true);
        boxTags.setManaged(true);
    }

    private void setTraitementEnCours(boolean actif, String message) {
        traitementEnCours = actif;
        panneauOutils.setDisable(actif);
        panneauFiltres.setDisable(actif);
        statusLabel.setText(message);
    }



    @FXML
    protected void ouvrirImage() {
        if (traitementEnCours) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        File dossierImages = obtenirDossierImages();
        if (dossierImages != null && dossierImages.exists()) {
            fileChooser.setInitialDirectory(dossierImages);
        }

        Stage stage = (Stage) imageView.getScene().getWindow();
        File fichier = fileChooser.showOpenDialog(stage);

        if (fichier != null) {
            try {
                FileInputStream fis = new FileInputStream(fichier);
                Image image = new Image(fis);
                fis.close();
                chargerImageDepuisImage(image, fichier);
            } catch (Exception e) {
                statusLabel.setText("Erreur : " + e.getMessage());
            }
        }
    }

    private File obtenirDossierImages() {
        try {
            URL url = getClass().getResource("images");
            if (url != null) return new File(url.toURI());
        } catch (Exception ignored) {}

        File f = new File("src/main/resources/com/example/imagelab/images");
        if (f.exists()) return f;

        return new File(System.getProperty("user.home"));
    }

    private void chargerImageDepuisImage(Image image, File fichier) {
        if (image.isError()) {
            statusLabel.setText("Erreur : " + image.getException().getMessage());
            return;
        }
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        if (w <= 0 || h <= 0) { statusLabel.setText("Image invalide."); return; }

        undoStack.clear();
        imageChiffree = false;

        imageCourante  = new WritableImage(image.getPixelReader(), w, h);
        imageOriginale = new WritableImage(image.getPixelReader(), w, h);
        cheminImageCourante = fichier.getAbsolutePath();

        afficherImage(imageCourante);
        statusLabel.setText(fichier.getName());
        afficherPanneaux();
    }


    private Transformation creerTransformation(String nom) {
        return switch (nom) {
            case "Rotation 90°"  -> new Rotation();
            case "Symetrie"      -> new Symetrie();
            case "Echange RGB"   -> new FiltreRGB();
            case "Noir et Blanc" -> new FiltreNB();
            case "Sepia"         -> new FiltreSepia();
            case "Prewitt"       -> new FiltrePrewitt();
            default              -> null;
        };
    }

    private void appliquerTransformation(Transformation t) {
        if (imageCourante == null) {
            statusLabel.setText("Veuillez d'abord ouvrir une image.");
            return;
        }
        undoStack.push(imageCourante);
        imageCourante = t.appliquer(imageCourante);
        afficherImage(imageCourante);
        statusLabel.setText("Applique : " + t.getNom());

        ImageMetadata meta = recupererOuCreerMetadata();
        meta.ajouterTransformation(t.getNom());

        final WritableImage imageASauvegarder = copierImage(imageCourante);
        Task<Void> taskSauvegarde = new Task<>() {
            @Override
            protected Void call() throws Exception {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(imageASauvegarder, null), "png", baos);
                meta.setImageData(baos.toByteArray());
                MetadataManager.sauvegarderMeta(meta);
                return null;
            }
        };
        new Thread(taskSauvegarde, "thread-sauvegarde").start();
    }

    @FXML protected void appliquerRotation()      { appliquerTransformation(new Rotation()); }
    @FXML protected void appliquerSymetrie()      { appliquerTransformation(new Symetrie()); }
    @FXML protected void appliquerFiltreRGB()     { appliquerTransformation(new FiltreRGB()); }
    @FXML protected void appliquerFiltreNB()      { appliquerTransformation(new FiltreNB()); }
    @FXML protected void appliquerFiltreSepia()   { appliquerTransformation(new FiltreSepia()); }
    @FXML protected void appliquerFiltrePrewitt() { appliquerTransformation(new FiltrePrewitt()); }


    @FXML
    protected void annulerDerniereAction() {
        if (traitementEnCours || undoStack.isEmpty()) { statusLabel.setText("Rien à annuler."); return; }
        imageCourante = undoStack.pop();
        afficherImage(imageCourante);
        statusLabel.setText("Annulation effectuée.");
    }


    @FXML
    protected void reinitialiserImage() {
        if (traitementEnCours || imageOriginale == null) { statusLabel.setText("Aucune image à réinitialiser."); return; }
        undoStack.clear(); // vider l'historique au reset pour éviter de revenir à un état chiffré
        imageCourante = copierImage(imageOriginale);
        imageChiffree = false;
        afficherImage(imageCourante);
        statusLabel.setText("Image reinitialisée.");

        ImageMetadata meta = recupererOuCreerMetadata();
        meta.reinitialiserTransformations();
        meta.setHashMotDePasse(null);

        final WritableImage imageAReinitialiser = copierImage(imageCourante);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(
                        javafx.embed.swing.SwingFXUtils.fromFXImage(imageAReinitialiser, null),
                        "png", baos
                );
                meta.setImageData(baos.toByteArray());
                MetadataManager.sauvegarderMeta(meta);
                return null;
            }
        };
        new Thread(task, "thread-reset").start();
    }

    private WritableImage copierImage(WritableImage source) {
        return new WritableImage(source.getPixelReader(),
                (int) source.getWidth(), (int) source.getHeight());
    }


    @FXML
    protected void ajouterTag() {
        if (cheminImageCourante == null) { statusLabel.setText("Veuillez d'abord ouvrir une image."); return; }
        String tag = tagField.getText().trim();
        if (tag.isEmpty()) { statusLabel.setText("Entrez un tag"); return; }
        ImageMetadata meta = recupererOuCreerMetadata();
        for (ImageMetadata m : toutesMetadonnees) {
            if (!m.getCheminImage().equals(cheminImageCourante) && m.getTags().contains(tag)) {
                statusLabel.setText("Tag '" + tag + "' deja utilisé par une autre image.");
                return;
            }
        }
        meta.ajouterTag(tag);
        MetadataManager.sauvegarderMeta(meta);
        tagField.clear();
        statusLabel.setText("Tag ajouté : " + tag);
    }

    @FXML
    protected void supprimerTag() {
        String tag = tagField.getText().trim();
        if (tag.isEmpty()) { statusLabel.setText("Entrez le tag à supprimer."); return; }
        ImageMetadata meta = recupererOuCreerMetadata();
        meta.supprimerTag(tag);
        MetadataManager.sauvegarderMeta(meta);
        tagField.clear();
        statusLabel.setText("Tag supprimé : " + tag);
    }

    @FXML
    protected void rechercherTag() {
        String tag = rechercheField.getText().trim();
        if (tag.isEmpty()) return;

        List<ImageMetadata> resultats = MetadataManager.rechercherParTag(tag, toutesMetadonnees);
        if (resultats.isEmpty()) {
            rechercheResultat.setText("Aucun resultat");
            rechercheResultat.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
            return;
        }

        ImageMetadata premiere = resultats.get(0);

        try {
            byte[] data = premiere.getImageData();
            Image image;

            if (data != null && data.length > 0) {
                image = new Image(new java.io.ByteArrayInputStream(data));
            } else {
                File fichier = new File(premiere.getCheminImage());
                if (!fichier.exists()) { rechercheResultat.setText("Fichier introuvable"); return; }
                FileInputStream fis = new FileInputStream(fichier);
                image = new Image(fis);
                fis.close();
            }

            if (image.isError()) { rechercheResultat.setText("Erreur chargement image"); return; }

            int w = (int) image.getWidth();
            int h = (int) image.getHeight();
            undoStack.clear();
            imageCourante = new WritableImage(image.getPixelReader(), w, h);
            imageOriginale = new WritableImage(image.getPixelReader(), w, h);
            cheminImageCourante = premiere.getCheminImage();

            List<String> transformations = premiere.getTransformations();
            imageChiffree = !transformations.isEmpty()
                    && transformations.get(transformations.size() - 1).equals("Chiffrement");

            afficherImage(imageCourante);
            afficherPanneaux();

            rechercheResultat.setText(resultats.size() + "Trouvé");
            rechercheResultat.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf7d;");
            statusLabel.setText("Image trouvée pour le tag : " + tag);

        } catch (Exception e) {
            rechercheResultat.setText("Erreur : " + e.getMessage());
        }
    }

    // partie sécurité
    @FXML
    protected void chiffrerImage() {
        if (imageCourante == null) { statusLabel.setText("Veuillez d'abord ouvrir une image."); return; }
        if (imageChiffree) { statusLabel.setText("Image deja chiffree."); return; }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Chiffrement");
        dialog.setHeaderText("Entrez le mot de passe");
        PasswordField pf = new PasswordField();
        pf.setPromptText("Mot de passe");
        pf.setStyle("-fx-font-size: 13px; -fx-padding: 8 12;");
        dialog.getDialogPane().setContent(pf);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? pf.getText() : null);

        dialog.showAndWait().ifPresent(mdp -> {
            if (mdp.trim().isEmpty()) { statusLabel.setText("Mot de passe vide"); return; }
            FiltreCryptage filtre = new FiltreCryptage(mdp.trim());
            imageOriginale = copierImage(imageCourante);
            undoStack.push(copierImage(imageCourante));

            imageCourante = filtre.appliquer(imageCourante);
            afficherImage(imageCourante);
            imageChiffree = true;
            statusLabel.setText("Image chiffrée.");

            ImageMetadata meta = recupererOuCreerMetadata();
            meta.ajouterTransformation(filtre.getNom());
            meta.setHashMotDePasse(FiltreCryptage.hasherMotDePasse(mdp.trim()));

            final WritableImage imageASauvegarder = copierImage(imageCourante);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(imageASauvegarder, null), "png", baos);
                    meta.setImageData(baos.toByteArray());
                    MetadataManager.sauvegarderMeta(meta);
                    return null;
                }
            };
            new Thread(task, "thread-sauvegarde").start();
        });
    }

    @FXML
    protected void dechiffrerImage() {
        if (imageCourante == null) { statusLabel.setText("Veuillez d'abord ouvrir une image."); return; }
        if (!imageChiffree) { statusLabel.setText("Image non chiffrée."); return; }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Dechiffrement");
        dialog.setHeaderText("Entrez le mot de passe");
        PasswordField pf = new PasswordField();
        pf.setPromptText("Mot de passe");
        pf.setStyle("-fx-font-size: 13px; -fx-padding: 8 12;");
        dialog.getDialogPane().setContent(pf);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? pf.getText() : null);

        dialog.showAndWait().ifPresent(mdp -> {
            if (mdp.trim().isEmpty()) { statusLabel.setText("Mot de passe vide"); return; }

            ImageMetadata meta = recupererOuCreerMetadata();
            String hashStocke = meta.getHashMotDePasse();
            if (hashStocke != null && !hashStocke.equals(FiltreCryptage.hasherMotDePasse(mdp.trim()))) {
                statusLabel.setText("Mot de passe incorrect");
                return;
            }

            FiltreCryptage filtre = new FiltreCryptage(mdp.trim());
            undoStack.push(copierImage(imageCourante));

            // pour le déchiffrement sur le thread
            imageCourante = filtre.inverser(imageCourante);
            imageOriginale = copierImage(imageCourante);
            afficherImage(imageCourante);
            imageChiffree = false;
            statusLabel.setText("Image dechiffrée.");

            meta.reinitialiserTransformations();
            meta.setHashMotDePasse(null);

            final WritableImage imageASauvegarder = copierImage(imageCourante);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(imageASauvegarder, null), "png", baos);
                    meta.setImageData(baos.toByteArray());
                    MetadataManager.sauvegarderMeta(meta);
                    return null;
                }
            };
            new Thread(task, "thread-sauvegarde").start();
        });
    }

    private ImageMetadata recupererOuCreerMetadata() {
        ImageMetadata meta = MetadataManager.trouver(cheminImageCourante, toutesMetadonnees);
        if (meta == null) {
            meta = new ImageMetadata(cheminImageCourante);
            toutesMetadonnees.add(meta);
        }
        return meta;
    }

    private void sauvegarderImageBase() {
        if (imageCourante == null || cheminImageCourante == null) return;
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(imageCourante, null), "png", baos);
            recupererOuCreerMetadata().setImageData(baos.toByteArray());
        } catch (Exception e) {

        }
    }
}