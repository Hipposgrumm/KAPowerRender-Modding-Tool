package dev.hipposgrumm.kamapreader.util.control.display;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BachelorProfileValueList<T extends DatingProfileValue> implements DatingProfileValue {
    private final Supplier<T> addFunction;
    private final List<T> values;
    private DatingProfileValue[] originalValues;

    private final Map<T, Node> displayCache = new HashMap<>();

    public BachelorProfileValueList(List<T> values) {
        this(values, null);
    }

    public BachelorProfileValueList(List<T> values, Supplier<T> addFunction) {
        this.values = values;
        this.originalValues = values.toArray(new DatingProfileValue[0]);
        this.addFunction = addFunction;
    }

    private void populateElementList(VBox elements, FirstThing controller, Runnable onChanged, boolean readonly) {
        for (T v:values) elements.getChildren().addAll(
                displayCache.computeIfAbsent(v, k -> k.createDisplay(controller, onChanged, readonly)),
                new Separator()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        VBox elements = new VBox();
        populateElementList(elements, controller, onChanged, readonly);

        Button addButton = new Button("Add Entry");
        Button removeButton = new Button("Remove Entry");
        if (!readonly && addFunction != null) {
            addButton.setOnAction(event -> {
                if (values.size() < originalValues.length) { // recycle from old values
                    values.add((T) originalValues[values.size()]);
                } else values.add(addFunction.get());

                elements.getChildren().clear();
                populateElementList(elements, controller, onChanged, readonly);
                elements.getChildren().addAll(addButton, removeButton);
            });
            removeButton.setOnAction(event -> {
                values.removeLast();

                elements.getChildren().clear();
                populateElementList(elements, controller, onChanged, readonly);
                elements.getChildren().addAll(addButton, removeButton);
            });
        } else {
            addButton.setDisable(true);
            removeButton.setDisable(true);
        }
        elements.getChildren().addAll(addButton, removeButton);
        return elements;
    }

    @Override
    public boolean isModified() {
        if (values.size() != originalValues.length) return true;
        for (int i=0;i<originalValues.length;i++) {
            T val = values.get(i);
            if (val != originalValues[i] || val.isModified()) return true;
        }
        return false;
    }

    @Override
    public void onDestroyDisplay() {
        for (DatingProfileValue v:displayCache.keySet()) {
            v.onDestroyDisplay();
        }
    }
}
