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

public class ASPECTRATIO implements ContainingDatingProfileValue.Containable<ASPECTRATIO> {
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

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        Control X = DatingProfileValue.floatSpinner(this.X, readonly, (observable, oldValue, newValue) -> {
            this.X = newValue.floatValue();
            onChanged.run();
        });
        Control Y = DatingProfileValue.floatSpinner(this.Y, readonly, (observable, oldValue, newValue) -> {
            this.Y = newValue.floatValue();
            onChanged.run();
        });
        X.setMaxWidth(96);
        Y.setMaxWidth(96);
        HBox box = new HBox(0, X, new Label(" X "), Y);
        box.setAlignment(Pos.BOTTOM_LEFT);
        return box;
    }

    @Override
    public ContainingDatingProfileValue.Containable<ASPECTRATIO> makeCopy() {
        ASPECTRATIO ratio = new ASPECTRATIO();
        ratio.X = X; ratio.Y = Y;
        return ratio;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ASPECTRATIO other)) return false;
        return  other.X == X &&
                other.Y == Y;
    }
}
