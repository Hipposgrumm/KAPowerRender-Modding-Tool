package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

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
            VBox box;
            if (pv instanceof PlaceholderValue) {
                Label label = new Label("PLACE\nHOLDER");
                label.setTextAlignment(TextAlignment.CENTER);
                box = new VBox(label);
            } else {
                Node graphic = pv.getPreviewGraphic();
                Label label = new Label(pv.getPreviewName());
                label.setTextAlignment(TextAlignment.CENTER);
                if (graphic != null) {
                    box = new VBox(25, graphic, label);
                } else box = new VBox(label);
            }
            box.setAlignment(Pos.CENTER);
            Button btn = new Button("", box);
            btn.setMinWidth(75);
            btn.setMinHeight(100);
            if (pv instanceof DatingBachelor dpv) {
                TreeItem<DatingBachelor> found = DatingProfileValue.findInTree(controller.tree.getRoot(), dpv);
                if (found != null) btn.setOnAction(event -> {
                    MultipleSelectionModel<TreeItem<DatingBachelor>> selection = controller.tree.getSelectionModel();
                    selection.clearSelection();
                    selection.select(found);
                });
                else btn.setDisable(true);
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
