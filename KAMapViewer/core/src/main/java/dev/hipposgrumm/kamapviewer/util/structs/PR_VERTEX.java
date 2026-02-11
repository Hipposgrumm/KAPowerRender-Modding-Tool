package dev.hipposgrumm.kamapviewer.util.structs;

public class PR_VERTEX {
    public PR_POINT Position;
    public PR_POINT Normal;

    public PR_VERTEX() {
        this(new PR_POINT(), new PR_POINT());
    }

    public PR_VERTEX(PR_POINT Position, PR_POINT Normal) {
        this.Position = Position;
        this.Normal = Normal;
    }
}
