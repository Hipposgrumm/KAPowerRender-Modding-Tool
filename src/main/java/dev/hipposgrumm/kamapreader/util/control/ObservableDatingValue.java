package dev.hipposgrumm.kamapreader.util.control;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ObservableDatingValue extends ObservableValueBase<Node> {
    public final SimpleStringProperty nameProperty;
    private final DatingProfileValue profileValue;
    private final Node result;

    public ObservableDatingValue(FirstThing controller, TreeItem<DatingBachelor> item, DatingProfileEntry entry) {
        this.nameProperty = new BachelorStringProperty(entry);
        this.profileValue = entry.value();

        Node result;
        try {
            result = profileValue.createDisplay(controller, () -> {
                boolean modified = profileValue.isModified();
                TreeItem<DatingBachelor> iter = item;
                while (iter != null) {
                    if (iter == item) {
                        ((BachelorStringProperty) nameProperty).update(modified);
                    } else if (!modified) {
                        boolean hasModifiedChildren = false;
                        for (TreeItem<DatingBachelor> childItem:iter.getChildren()) {
                            if (!(childItem instanceof FirstThing.BachelorTreeItem bachelorChild)) continue;
                            if (bachelorChild.isModified) {
                                hasModifiedChildren = true;
                                break;
                            }
                        }
                        if (hasModifiedChildren) break;
                    }
                    if (iter instanceof FirstThing.BachelorTreeItem bachelorItem) {
                        bachelorItem.isModified = modified;
                        bachelorItem.doUpdate();
                    }
                    iter = iter.getParent();
                }
            }, entry.readonly());
        } catch (Exception e) {
            StringWriter string = new StringWriter();
            e.printStackTrace(new PrintWriter(string));
            result = new Label(string.toString());
            e.printStackTrace();
        }
        this.result = result;
    }

    @Override
    public Node getValue() {
        return result;
    }

    public void decommission() {
        this.profileValue.onDestroyDisplay();
    }

    private static class BachelorStringProperty extends SimpleStringProperty {
        private final DatingProfileEntry entry;

        public BachelorStringProperty(DatingProfileEntry entry) {
            super(entry.name());
            this.entry = entry;
        }

        /// @param isModified This could be easily gotten from entry, but this is more efficient for some cases.
        public void update(boolean isModified) {
            set(isModified
                    ? "* "+entry.name()
                    : entry.name()
            );
        }
    }
}
