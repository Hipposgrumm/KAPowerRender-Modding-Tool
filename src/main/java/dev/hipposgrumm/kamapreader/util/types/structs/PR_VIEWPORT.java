package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PR_VIEWPORT {
    public int X, Y;
    public int WIDTH, HEIGHT;

    public PR_VIEWPORT() {}
    public PR_VIEWPORT(BlockReader reader) {
        this.X = reader.readInt();
        this.Y = reader.readInt();
        this.WIDTH = reader.readInt();
        this.HEIGHT = reader.readInt();
    }

    public void write(BlockWriter writer) {
        writer.writeInt(X);
        writer.writeInt(Y);
        writer.writeInt(WIDTH);
        writer.writeInt(HEIGHT);
    }
}