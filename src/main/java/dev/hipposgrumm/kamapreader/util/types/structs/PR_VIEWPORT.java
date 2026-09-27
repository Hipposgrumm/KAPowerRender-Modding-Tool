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
import javafx.scene.layout.GridPane;

public class PR_VIEWPORT implements ContainingDatingProfileValue.Containable<PR_VIEWPORT> {
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

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        Control X = DatingProfileValue.intSpinner(this.X, readonly, (observable, oldValue, newValue) -> {
            this.X = newValue;
            onChanged.run();
        });
        Control Y = DatingProfileValue.intSpinner(this.Y, readonly, (observable, oldValue, newValue) -> {
            this.Y = newValue;
            onChanged.run();
        });
        Control WIDTH = DatingProfileValue.intSpinner(this.WIDTH, readonly, (observable, oldValue, newValue) -> {
            this.WIDTH = newValue;
            onChanged.run();
        });
        Control HEIGHT = DatingProfileValue.intSpinner(this.HEIGHT, readonly, (observable, oldValue, newValue) -> {
            this.HEIGHT = newValue;
            onChanged.run();
        });
        X.setMaxWidth(67);
        Y.setMaxWidth(67);
        WIDTH.setMaxWidth(67);
        HEIGHT.setMaxWidth(67);
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.add(new Label("X:"), 0, 0);
        grid.add(X, 1, 0);
        grid.add(new Label(" Y:"), 2, 0);
        grid.add(Y, 3, 0);
        grid.add(new Label("Width:"), 0, 1);
        grid.add(WIDTH, 1, 1);
        grid.add(new Label(" Height:"), 2, 1);
        grid.add(HEIGHT, 3, 1);
        grid.setAlignment(Pos.CENTER_LEFT);
        return grid;
    }

    @Override
    public ContainingDatingProfileValue.Containable<PR_VIEWPORT> makeCopy() {
        PR_VIEWPORT view = new PR_VIEWPORT();
        view.X = X; view.Y = Y; view.WIDTH = WIDTH; view.HEIGHT = HEIGHT;
        return view;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PR_VIEWPORT other)) return false;
        return  other.X == X &&
                other.Y == Y &&
                other.WIDTH == WIDTH &&
                other.HEIGHT == HEIGHT;
    }
}