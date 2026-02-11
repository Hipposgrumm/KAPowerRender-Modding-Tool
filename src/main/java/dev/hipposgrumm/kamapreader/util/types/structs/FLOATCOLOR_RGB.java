package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import javafx.scene.paint.Color;

public class FLOATCOLOR_RGB {
    public float R = 1, G = 1, B = 1;

    public FLOATCOLOR_RGB() {}
    public FLOATCOLOR_RGB(BlockReader reader) {
        this.R = reader.readFloat();
        this.G = reader.readFloat();
        this.B = reader.readFloat();
    }

    public void write(BlockWriter writer) {
        writer.writeFloat(R);
        writer.writeFloat(G);
        writer.writeFloat(B);
    }

    public Color toJavaFXColor() {
        return Color.color(R/255f, G/255f, B/255f, 1f);
    }
}
