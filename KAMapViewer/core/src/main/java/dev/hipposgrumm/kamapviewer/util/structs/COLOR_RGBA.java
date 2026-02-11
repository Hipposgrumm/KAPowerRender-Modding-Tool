package dev.hipposgrumm.kamapviewer.util.structs;

public class COLOR_RGBA {
    public int color;

    public COLOR_RGBA(int color) {
        this.color = color;
    }

    public int getRed() {
        return (color & 0xFF000000) >> 24;
    }

    public int getGreen() {
        return (color & 0x00FF0000) >> 16;
    }

    public int getBlue() {
        return (color & 0x0000FF00) >> 8;
    }

    public int getAlpha() {
        return (color & 0x000000FF);
    }

    public void setRed(int r) {
        color = (color & 0x00FFFFFF) | ((r & 0xFF) << 24);
    }

    public void setGreen(int g) {
        color = (color & 0xFF00FFFF) | ((g & 0xFF) << 16);
    }

    public void setBlue(int b) {
        color = (color & 0xFFFF00FF) | ((b & 0xFF) << 8);
    }

    public void setAlpha(int a) {
        color = (color & 0xFFFFFF00) | (a & 0xFF);
    }
}
