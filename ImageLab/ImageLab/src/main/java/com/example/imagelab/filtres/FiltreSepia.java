package com.example.imagelab.filtres;

public class FiltreSepia extends Filtre {

    @Override
    protected int appliquerPixel(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;

        int nr = Math.min((int)(0.393*r + 0.769*g + 0.189*b), 255);
        int ng = Math.min((int)(0.349*r + 0.686*g + 0.168*b), 255);
        int nb = Math.min((int)(0.272*r + 0.534*g + 0.131*b), 255);

        return (a << 24) | (nr << 16) | (ng << 8) | nb;
    }

    @Override
    public String getNom() { return "Sépia"; }
}