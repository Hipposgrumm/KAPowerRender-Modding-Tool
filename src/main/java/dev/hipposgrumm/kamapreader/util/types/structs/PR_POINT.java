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

public class PR_POINT implements ContainingDatingProfileValue.Containable<PR_POINT> {
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
        Control Z = DatingProfileValue.floatSpinner(this.Z, readonly, (observable, oldValue, newValue) -> {
            this.Z = newValue.floatValue();
            onChanged.run();
        });
        X.setMaxWidth(90);
        Y.setMaxWidth(90);
        Z.setMaxWidth(90);
        HBox box = new HBox(0,
                new Label("X:"), X,
                new Label(" Y:"), Y,
                new Label(" Z:"), Z
        );
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    @Override
    public ContainingDatingProfileValue.Containable<PR_POINT> makeCopy() {
        PR_POINT point = new PR_POINT();
        point.X = X; point.Y = Y; point.Z = Z;
        return point;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PR_POINT other)) return false;
        return  other.X == X &&
                other.Y == Y &&
                other.Z == Z;
    }
}