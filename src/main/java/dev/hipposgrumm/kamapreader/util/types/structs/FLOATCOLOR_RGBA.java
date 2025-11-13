package dev.hipposgrumm.kamapreader.util.types.structs;

import javafx.scene.paint.Color;

public class FLOATCOLOR_RGBA extends FLOATCOLOR_RGB {
    public float A = 1;

    public FLOATCOLOR_RGBA() {}
    public FLOATCOLOR_RGBA(float R, float G, float B, float A) {
        super(R, G, B);
        this.A = A;
    }

    @Override
    public Color toJavaFXColor() {
        return Color.color(R/255f, G/255f, B/255f, A/255f);
    }
}
