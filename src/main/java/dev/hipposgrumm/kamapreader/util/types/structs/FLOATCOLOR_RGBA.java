package dev.hipposgrumm.kamapreader.util.types.structs;

public class FLOATCOLOR_RGBA extends FLOATCOLOR_RGB {
    public float A = 1;

    public FLOATCOLOR_RGBA() {}
    public FLOATCOLOR_RGBA(float R, float G, float B, float A) {
        super(R, G, B);
        this.A = A;
    }
}
