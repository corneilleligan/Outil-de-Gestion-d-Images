package com.example.imagelab.filtres;

import com.example.imagelab.transformations.Transformation;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

public class FiltrePrewitt implements Transformation {

    @Override
    public WritableImage appliquer(WritableImage src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();

        int[] pixels = new int[w * h];
        src.getPixelReader().getPixels(0, 0, w, h,
                PixelFormat.getIntArgbInstance(), pixels, 0, w);

        double[] lum = new double[w * h];
        for (int i = 0; i < pixels.length; i++) {
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >>  8) & 0xFF;
            int b =  pixels[i]        & 0xFF;
            lum[i] = (r + g + b) / 765.0;
        }

        int[] dst = new int[w * h];
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                double tl = lum[(y-1)*w+(x-1)], tc = lum[(y-1)*w+x], tr = lum[(y-1)*w+(x+1)];
                double ml = lum[y*w+(x-1)],                            mr = lum[y*w+(x+1)];
                double bl = lum[(y+1)*w+(x-1)], bc = lum[(y+1)*w+x], br = lum[(y+1)*w+(x+1)];

                double gx = -tl + tr - ml + mr - bl + br;
                double gy = -tl - tc - tr + bl + bc + br;

                int mag = (int)(Math.min(Math.sqrt(gx*gx + gy*gy), 1.0) * 255);
                dst[y*w+x] = 0xFF000000 | (mag << 16) | (mag << 8) | mag;
            }
        }

        WritableImage result = new WritableImage(w, h);
        result.getPixelWriter().setPixels(0, 0, w, h,
                PixelFormat.getIntArgbInstance(), dst, 0, w);

        return result;
    }

    @Override
    public String getNom() { return "Prewitt"; }
}