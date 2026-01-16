package dev.hipposgrumm.kamapreader.util.types.structs;

public class PR_VERTEX {
    public PR_POINT Position;
    public short NX, NY, NZ;

    public PR_VERTEX() {
        this.Position = new PR_POINT();
    }

    public PR_VERTEX(PR_POINT Position, short NX, short NY, short NZ) {
        this.Position = Position;
        this.NX = NX;
        this.NY = NY;
        this.NZ = NZ;
    }
}
