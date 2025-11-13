package dev.hipposgrumm.kamapreader.util.types.structs;

import javafx.scene.paint.Color;

public class FLOATCOLOR_RGB {
    public float R = 1, G = 1, B = 1;

    public FLOATCOLOR_RGB() {}
    public FLOATCOLOR_RGB(float R, float G, float B) {
        this.R = R;
        this.G = G;
        this.B = B;
    }

    public Color toJavaFXColor() {
        return Color.color(R/255f, G/255f, B/255f, 1f);
    }
}
