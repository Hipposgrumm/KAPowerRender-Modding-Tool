package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class SubBachelorPreviewEntry implements DatingProfileValue {
    private final List<? extends Previewable> previewables;

    public SubBachelorPreviewEntry(List<? extends Previewable> previewables) {
        this.previewables = previewables;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        GridPane grid = new GridPane();

        int i=0;
        for (Previewable pv:previewables) {
            VBox box = new VBox(25,
                    pv.getPreviewGraphic(),
                    new Label(pv.getPreviewName())
            );
            box.setAlignment(Pos.CENTER);
            Button btn = new Button("", box);
            btn.setMinWidth(75);
            btn.setMinHeight(100);
            if (pv instanceof DatingBachelor dpv) {
                TreeItem<DatingBachelor> found = DatingProfileValue.findInTree(controller.tree.getRoot(), dpv);
                if (found != null) {
                    btn.setOnAction(event ->
                            controller.tree.getSelectionModel().select(found)
                    );
                } else btn.setDisable(true);
            } else btn.setDisable(true);
            grid.add(btn, i%4, i/4);
            i++;
        }
        return grid;
    }

    @Override
    public boolean isModified() {
        return false;
    }
}
