package dev.hipposgrumm.kamapreader.util.control;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.Node;
import javafx.scene.control.*;

public class ObservableDatingValue extends ObservableValueBase<Node> {
    private static ObservableDatingValue lastValue = null;
    private static Node lastResult = null;

    private final FirstThing controller;
    private final TreeItem<DatingBachelor> item;
    private final DatingProfileEntry entry;

    public ObservableDatingValue(FirstThing controller, TreeItem<DatingBachelor> item, DatingProfileEntry entry) {
        this.controller = controller;
        this.item = item;
        this.entry = entry;
    }

    @Override
    public Node getValue() {
        if (this == lastValue) return lastResult;
        lastValue = this;

        try {
            DatingProfileValue value = entry.value();
            lastResult = value.createDisplay(controller, () -> {
                boolean modified = value.isModified();
                TreeItem<DatingBachelor> iter = item;
                while (iter != null) {
                    if (!(iter instanceof FirstThing.BachelorTreeItem bachelorItem)) break;
                    if (iter == item) {
                        bachelorItem.updateLabel(entry, modified);
                    } else if (!modified) {
                        boolean hasModifiedChildren = false;
                        for (TreeItem<DatingBachelor> childItem:bachelorItem.getChildren()) {
                            if (!(childItem instanceof FirstThing.BachelorTreeItem bachelorChild)) continue;
                            if (bachelorChild.isModified) {
                                hasModifiedChildren = true;
                                break;
                            }
                        }
                        if (hasModifiedChildren) break;
                    }
                    bachelorItem.isModified = modified;
                    bachelorItem.doUpdate();
                    iter = iter.getParent();
                }
            }, entry.readonly());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastResult;
    }
}
