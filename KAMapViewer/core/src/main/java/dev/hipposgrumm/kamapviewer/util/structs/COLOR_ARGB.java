package dev.hipposgrumm.kamapviewer.util.structs;

public class COLOR_ARGB {
    public int color;

    public COLOR_ARGB(int color) {
        this.color = color;
    }

    public int getRed() {
        return (color & 0x00FF0000) >> 16;
    }

    public int getGreen() {
        return (color & 0x0000FF00) >> 8;
    }

    public int getBlue() {
        return (color & 0x000000FF);
    }

    public int getAlpha() {
        return (color & 0xFF000000) >> 24;
    }

    public void setRed(int r) {
        color = (color & 0xFF00FFFF) | ((r & 0xFF) << 16);
    }

    public void setGreen(int g) {
        color = (color & 0xFFFF00FF) | ((g & 0xFF) << 8);
    }

    public void setBlue(int b) {
        color = (color & 0xFFFFFF00) | (b & 0xFF);
    }

    public void setAlpha(int a) {
        color = (color & 0x00FFFFFF) | ((a & 0xFF) << 24);
    }
}
