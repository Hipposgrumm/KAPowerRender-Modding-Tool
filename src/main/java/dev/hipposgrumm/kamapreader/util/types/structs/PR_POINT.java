package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PR_POINT {
    public float X, Y, Z;

    public PR_POINT() {}
    public PR_POINT(BlockReader reader) {
        this.X = reader.readFloat();
        this.Y = reader.readFloat();
        this.Z = reader.readFloat();
    }

    public void write(BlockWriter writer) {
        writer.writeFloat(X);
        writer.writeFloat(Y);
        writer.writeFloat(Z);
    }
}