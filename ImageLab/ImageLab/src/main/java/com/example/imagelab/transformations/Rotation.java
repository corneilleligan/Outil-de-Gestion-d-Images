package com.example.imagelab.transformations;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

public class Rotation extends TransformationGeometrique {

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
                dst[(h - 1 - y) + x * h] = pixels[y * w + x];

        WritableImage result = new WritableImage(h, w);
        result.getPixelWriter().setPixels(0, 0, h, w,
                PixelFormat.getIntArgbInstance(), dst, 0, h);

        return result;
    }

    @Override
    public String getNom() { return "Rotation 90°"; }
}