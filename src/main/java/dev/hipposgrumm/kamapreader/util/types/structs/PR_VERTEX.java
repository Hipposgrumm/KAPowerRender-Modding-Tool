package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PR_VERTEX {
    public PR_POINT Position;
    public short NX, NY, NZ;

    public PR_VERTEX() {
        this.Position = new PR_POINT();
    }

    public PR_VERTEX(BlockReader reader) {
        this.Position = new PR_POINT(reader);
        this.NX = reader.readShort();
        this.NY = reader.readShort();
        this.NZ = reader.readShort();
    }

    public void write(BlockWriter writer) {
        Position.write(writer);
        writer.writeShort(NX);
        writer.writeShort(NY);
        writer.writeShort(NZ);
    }
}
