package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.control.ContainingDatingProfileValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class FLOATCOLOR_RGB implements ContainingDatingProfileValue.Containable<FLOATCOLOR_RGB> {
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

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        Control R = DatingProfileValue.floatSpinner(this.R, readonly, (observable, oldValue, newValue) -> {
            this.R = newValue.floatValue();
            onChanged.run();
        });
        Control G = DatingProfileValue.floatSpinner(this.G, readonly, (observable, oldValue, newValue) -> {
            this.G = newValue.floatValue();
            onChanged.run();
        });
        Control B = DatingProfileValue.floatSpinner(this.B, readonly, (observable, oldValue, newValue) -> {
            this.B = newValue.floatValue();
            onChanged.run();
        });
        double width = 90;
        HBox box = new HBox(0,
                new Label("R:"), R,
                new Label(" G:"), G,
                new Label(" B:"), B
        );
        if (this instanceof FLOATCOLOR_RGBA thisA) { // TODO: These should probably just be separate classes.
            Control A = DatingProfileValue.floatSpinner(thisA.A, readonly, (observable, oldValue, newValue) -> {
                thisA.A = newValue.floatValue();
                onChanged.run();
            });
            width = 63;
            A.setMaxWidth(width);
            box.getChildren().addAll(new Label(" A:"), A);
        }
        R.setMaxWidth(width);
        G.setMaxWidth(width);
        B.setMaxWidth(width);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    @Override
    public ContainingDatingProfileValue.Containable<? extends FLOATCOLOR_RGB> makeCopy() {
        FLOATCOLOR_RGB color = new FLOATCOLOR_RGB();
        color.R = R; color.G = G; color.B = B;
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FLOATCOLOR_RGB other) || (other instanceof FLOATCOLOR_RGBA)) return false;
        return  other.R == R &&
                other.G == G &&
                other.B == B;
    }
}
