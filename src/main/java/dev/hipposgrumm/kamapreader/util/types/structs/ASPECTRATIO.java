package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class ASPECTRATIO {
    public float X, Y;

    public ASPECTRATIO() {}
    public ASPECTRATIO(BlockReader reader) {
        this.X = reader.readFloat();
        this.Y = reader.readFloat();
    }

    public void write(BlockWriter writer) {
        writer.writeFloat(X);
        writer.writeFloat(Y);
    }
}
