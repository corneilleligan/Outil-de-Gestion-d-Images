package com.example.imagelab.transformations;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

public class Symetrie extends TransformationGeometrique {

    @Override
    public WritableImage appliquer(WritableImage src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();

        int[] pixels = new int[w * h];
        src.getPixelReader().getPixels(0, 0, w, h,
                PixelFormat.getIntArgbInstance(), pixels, 0, w);

        int[] dst = new int[w * h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                dst[y * w + (w - 1 - x)] = pixels[y * w + x];

        WritableImage result = new WritableImage(w, h);
        result.getPixelWriter().setPixels(0, 0, w, h,
                PixelFormat.getIntArgbInstance(), dst, 0, w);

        return result;
    }

    @Override
    public String getNom() { return "Symétrie"; }
}
