package dev.hipposgrumm.kamapreader.util.types.structs;

public class PR_BOUNDINGINFO {
    public float minX, maxX;
    public float minY, maxY;
    public float minZ, maxZ;
    public final PR_POINT[] bbox = new PR_POINT[9]; // 4 corners plus the center vertex
    public final PR_POINT[] tbox = new PR_POINT[9]; // transformed vertices
    public float radius;
}
