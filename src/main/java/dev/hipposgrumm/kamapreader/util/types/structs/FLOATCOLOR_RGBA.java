package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import javafx.scene.paint.Color;

public class FLOATCOLOR_RGBA extends FLOATCOLOR_RGB {
    public float A = 1;

    public FLOATCOLOR_RGBA() {}
    public FLOATCOLOR_RGBA(BlockReader reader) {
        super(reader);
        this.A = reader.readFloat();
    }

    public void write(BlockWriter writer) {
        super.write(writer);
        writer.writeFloat(A);
    }

    @Override
    public Color toJavaFXColor() {
        return Color.color(R/255f, G/255f, B/255f, A/255f);
    }
}
