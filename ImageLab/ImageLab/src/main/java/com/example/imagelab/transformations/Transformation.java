package com.example.imagelab.transformations;

import javafx.scene.image.WritableImage;

public interface Transformation {
    WritableImage appliquer(WritableImage src);
    String getNom();
}