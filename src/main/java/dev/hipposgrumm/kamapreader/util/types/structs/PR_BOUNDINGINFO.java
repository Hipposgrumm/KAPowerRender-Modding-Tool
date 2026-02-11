package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PR_BOUNDINGINFO {
    public float minX, maxX;
    public float minY, maxY;
    public float minZ, maxZ;
    public final PR_POINT[] bbox = new PR_POINT[9]; // 4 corners plus the center vertex
    public final PR_POINT[] tbox = new PR_POINT[9]; // transformed vertices
    public float radius;

    public PR_BOUNDINGINFO() {}
    public PR_BOUNDINGINFO(BlockReader reader) {
        minX = reader.readFloat();
        maxX = reader.readFloat();
        minY = reader.readFloat();
        maxY = reader.readFloat();
        minZ = reader.readFloat();
        maxZ = reader.readFloat();
        for (int j=0;j<bbox.length;j++) bbox[j] = new PR_POINT(reader);
        for (int j=0;j<tbox.length;j++) tbox[j] = new PR_POINT(reader);
        radius = reader.readFloat();
    }

    public void write(BlockWriter writer) {
        writer.writeFloat(minX);
        writer.writeFloat(maxX);
        writer.writeFloat(minY);
        writer.writeFloat(maxY);
        writer.writeFloat(minZ);
        writer.writeFloat(maxZ);
        for (PR_POINT point:bbox) point.write(writer);
        for (PR_POINT point:tbox) point.write(writer);
        writer.writeFloat(radius);
    }
}
