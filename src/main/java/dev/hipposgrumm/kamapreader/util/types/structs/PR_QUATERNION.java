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

public class PR_QUATERNION implements ContainingDatingProfileValue.Containable<PR_QUATERNION> {
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
        Control W = DatingProfileValue.floatSpinner(this.W, readonly, (observable, oldValue, newValue) -> {
            this.W = newValue.floatValue();
            onChanged.run();
        });
        X.setMaxWidth(63);
        Y.setMaxWidth(63);
        Z.setMaxWidth(63);
        W.setMaxWidth(63);
        HBox box = new HBox(0,
                new Label("X:"), X,
                new Label(" Y:"), Y,
                new Label(" Z:"), Z,
                new Label(" W:"), W
        );
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    @Override
    public ContainingDatingProfileValue.Containable<PR_QUATERNION> makeCopy() {
        PR_QUATERNION quat = new PR_QUATERNION();
        quat.W = W; quat.X = X; quat.Y = Y; quat.Z = Z;
        return quat;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PR_QUATERNION other)) return false;
        return  other.W == W &&
                other.X == X &&
                other.Y == Y &&
                other.Z == Z;
    }
}