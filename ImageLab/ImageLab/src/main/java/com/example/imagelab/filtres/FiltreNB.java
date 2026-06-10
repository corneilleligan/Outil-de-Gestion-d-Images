package com.example.imagelab.filtres;

public class FiltreNB extends Filtre {

    @Override
    protected int appliquerPixel(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;
        int moy = (r + g + b) / 3;
        return (argb & 0xFF000000) | (moy << 16) | (moy << 8) | moy;
    }

    @Override
    public String getNom() { return "Noir et Blanc"; }
}