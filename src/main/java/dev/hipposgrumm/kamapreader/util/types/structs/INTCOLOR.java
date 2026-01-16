package dev.hipposgrumm.kamapreader.util.types.structs;

public class INTCOLOR {
    private final Format format;
    public int color;

    public INTCOLOR(Format format, int color) {
        this.format = format;
        this.color = color;
    }

    public int getRed() {
        return (color >> format.SHIFT_R) & 0xFF;
    }

    public int getGreen() {
        return (color >> format.SHIFT_G) & 0xFF;
    }

    public int getBlue() {
        return (color >> format.SHIFT_B) & 0xFF;
    }

    public int getAlpha() {
        return (color >> format.SHIFT_A) & 0xFF;
    }

    public void setRed(int r) {
        color = (color & (0xFF<<format.SHIFT_R)) | ((r & 0xFF) << format.SHIFT_R);
    }

    public void setGreen(int g) {
        color = (color & (0xFF<<format.SHIFT_G)) | ((g & 0xFF) << format.SHIFT_G);
    }

    public void setBlue(int b) {
        color = (color & (0xFF<<format.SHIFT_B)) | ((b & 0xFF) << format.SHIFT_B);
    }

    public void setAlpha(int a) {
        color = (color & (0xFF<<format.SHIFT_A)) | ((a & 0xFF) << format.SHIFT_A);
    }

    public enum Format {
        RGBA(3, 2, 1, 0),
        ARGB(2, 1, 0, 3);

        final int SHIFT_R;
        final int SHIFT_G;
        final int SHIFT_B;
        final int SHIFT_A;

        Format(int R, int G, int B, int A) {
            this.SHIFT_R = R*8;
            this.SHIFT_G = G*8;
            this.SHIFT_B = B*8;
            this.SHIFT_A = A*8;
        }
    }
}
