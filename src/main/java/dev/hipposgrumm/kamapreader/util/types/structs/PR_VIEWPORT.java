package dev.hipposgrumm.kamapreader.util.types.structs;

public class PR_VIEWPORT {
    public int X, Y;
    public int WIDTH, HEIGHT;

    public PR_VIEWPORT() {}
    public PR_VIEWPORT(int X, int Y, int WIDTH, int HEIGHT) {
        this.X = X;
        this.Y = Y;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
    }
}