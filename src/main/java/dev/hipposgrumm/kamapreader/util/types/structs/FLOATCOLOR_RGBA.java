package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.control.ContainingDatingProfileValue;
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

    @Override
    public ContainingDatingProfileValue.Containable<? extends FLOATCOLOR_RGB> makeCopy() {
        FLOATCOLOR_RGBA color = new FLOATCOLOR_RGBA();
        color.R = R; color.G = G; color.B = B; color.A = A;
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FLOATCOLOR_RGBA other)) return false;
        return  other.R == R &&
                other.G == G &&
                other.B == B &&
                other.A == A;
    }
}
