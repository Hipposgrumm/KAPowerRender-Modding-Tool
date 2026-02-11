package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PR_QUATERNION {
    public float X, Y, Z;
    public float W = 1;

    public PR_QUATERNION() {}
    public PR_QUATERNION(BlockReader reader) {
        this.X = reader.readFloat();
        this.Y = reader.readFloat();
        this.Z = reader.readFloat();
        this.W = reader.readFloat();
    }

    public void write(BlockWriter writer) {
        writer.writeFloat(X);
        writer.writeFloat(Y);
        writer.writeFloat(Z);
        writer.writeFloat(W);
    }
}