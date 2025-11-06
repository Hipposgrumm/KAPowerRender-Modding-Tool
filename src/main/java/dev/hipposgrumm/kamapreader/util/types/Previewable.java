package dev.hipposgrumm.kamapreader.util.types;

import javafx.scene.Node;

public interface Previewable {
    default String getPreviewName() {
        return toString();
    }
    Node getPreviewGraphic();
}
