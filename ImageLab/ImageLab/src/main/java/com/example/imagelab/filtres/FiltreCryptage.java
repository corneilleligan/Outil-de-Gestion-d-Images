package com.example.imagelab.filtres;

import com.example.imagelab.transformations.Transformation;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FiltreCryptage implements Transformation {

    private final String motDePasse;

    public FiltreCryptage(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public static String hasherMotDePasse(String mdp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(mdp.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur hash : " + e.getMessage());
        }
    }

    private List<Integer> genererPermutation(int taille) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] graine = digest.digest(motDePasse.getBytes("UTF-8"));

            List<Integer> indices = new ArrayList<>(taille);
            for (int i = 0; i < taille; i++) indices.add(i);

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(graine);
            Collections.shuffle(indices, random);

            return indices;

        } catch (Exception e) {
            throw new RuntimeException("Erreur permutation : " + e.getMessage());
        }
    }

    @Override
    public WritableImage appliquer(WritableImage src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        int taille = w * h;

        int[] pixels = new int[taille];
        src.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);

        List<Integer> perm = genererPermutation(taille);
        int[] melanges = new int[taille];
        for (int i = 0; i < taille; i++)
            melanges[perm.get(i)] = pixels[i];

        WritableImage result = new WritableImage(w, h);
        result.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), melanges, 0, w);

        return result;
    }

    public WritableImage inverser(WritableImage src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        int taille = w * h;

        int[] melanges = new int[taille];
        src.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), melanges, 0, w);

        List<Integer> perm = genererPermutation(taille);
        int[] originaux = new int[taille];
        for (int i = 0; i < taille; i++)
            originaux[i] = melanges[perm.get(i)];

        WritableImage result = new WritableImage(w, h);
        result.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), originaux, 0, w);

        return result;
    }

    @Override
    public String getNom() { return "Chiffrement"; }
}