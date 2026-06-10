package com.example.imagelab.filtres;

import com.example.imagelab.transformations.Transformation;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

public abstract class Filtre implements Transformation {

    @Override
    public WritableImage appliquer(WritableImage src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();

        int[] pixels = new int[w * h];
        src.getPixelReader().getPixels(0, 0, w, h,
                PixelFormat.getIntArgbInstance(), pixels, 0, w);

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = appliquerPixel(pixels[i]);
        }

        WritableImage result = new WritableImage(w, h);
        result.getPixelWriter().setPixels(0, 0, w, h,
                PixelFormat.getIntArgbInstance(), pixels, 0, w);

        return result;
    }

    protected abstract int appliquerPixel(int argb);
}