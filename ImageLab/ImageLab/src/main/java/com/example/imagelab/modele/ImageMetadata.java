package com.example.imagelab.modele;

import java.util.ArrayList;
import java.util.List;

public class ImageMetadata {
    private String cheminImage;
    private List<String> tags;
    private List<String> transformations;
    private byte[] imageData;
    private String hashMotDePasse;

    public ImageMetadata(String cheminImage) {
        this.cheminImage = cheminImage;
        this.tags = new ArrayList<>();
        this.transformations = new ArrayList<>();
        this.imageData = null;
        this.hashMotDePasse = null;
    }

    public String getCheminImage() { return cheminImage; }

    public List<String> getTags() { return tags; }

    public void ajouterTag(String tag) {
        if (!tags.contains(tag)) tags.add(tag);
    }

    public void supprimerTag(String tag) { tags.remove(tag); }

    public List<String> getTransformations() { return transformations; }

    public void ajouterTransformation(String nom) { transformations.add(nom); }

    public void reinitialiserTransformations() { transformations.clear(); }

    public byte[] getImageData() { return imageData; }

    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String getHashMotDePasse() { return hashMotDePasse; }
    public void setHashMotDePasse(String hash) { this.hashMotDePasse = hash; }
}