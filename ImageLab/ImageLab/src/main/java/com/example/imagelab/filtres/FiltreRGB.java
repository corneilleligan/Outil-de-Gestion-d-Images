package com.example.imagelab.filtres;

public class FiltreRGB extends Filtre {

    @Override
    protected int appliquerPixel(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;
        return (a << 24) | (b << 16) | (r << 8) | g;
    }

    @Override
    public String getNom() { return "Échange RGB"; }
}
