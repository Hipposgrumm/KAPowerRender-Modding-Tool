package dev.hipposgrumm.kamapreader.util.control;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.types.EnumChoices;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface DatingProfileValue {
    Node NULL_DISPLAY = new Label("null");

    Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly);
    boolean isModified();
    default void onDestroyDisplay() {}

    static TreeItem<DatingBachelor> findInTree(TreeItem<DatingBachelor> root, DatingBachelor target) {
        if (root.getValue() == target) return root;
        for (TreeItem<DatingBachelor> item:root.getChildren()) {
            TreeItem<DatingBachelor> found = findInTree(item, target);
            if (found != null) return found;
        }
        return null;
    }

    static TextField text(String s, boolean readonly, ChangeListener<String> listener) {
        TextField text = new TextField(s);
        if (readonly) text.setEditable(false);
        else text.textProperty().addListener(listener);
        return text;
    }

    static CheckBox checkbox(boolean b, String name, boolean readonly, ChangeListener<Boolean> listener) {
        CheckBox check = new CheckBox(name);
        check.setSelected(b);
        if (readonly) check.setDisable(true);
        else check.selectedProperty().addListener(listener);
        return check;
    }

    static Control byteSpinner(byte b, boolean readonly, ChangeListener<Integer> listener) {
        return spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(Byte.MIN_VALUE, Byte.MAX_VALUE, b), readonly, listener);
    }

    static Control ubyteSpinner(UByte b, boolean readonly, ChangeListener<Integer> listener) {
        return spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0xFF, b.get()), readonly, listener);
    }

    static Control shortSpinner(short s, boolean readonly, ChangeListener<Integer> listener) {
        return spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(Short.MIN_VALUE, Short.MAX_VALUE, s), readonly, listener);
    }

    static Control ushortSpinner(UShort s, boolean readonly, ChangeListener<Integer> listener) {
        return spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0xFFFF, s.get()), readonly, listener);
    }

    static Control intSpinner(int i, boolean readonly, ChangeListener<Integer> listener) {
        return spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, i), readonly, listener);
    }

    static Control uintSpinner(UInteger i, boolean readonly, ChangeListener<Long> listener) {
        return spinner(new LongSpinnerValueFactory(0L, 0xFFFFFFFFL, i.get()), readonly, listener);
    }

    static Control floatSpinner(float f, boolean readonly, ChangeListener<Double> listener) {
        return spinner(new SpinnerValueFactory.DoubleSpinnerValueFactory(Float.MIN_VALUE, Float.MAX_VALUE, f), readonly, listener);
    }

    static <T> Control spinner(SpinnerValueFactory<T> factory, boolean readonly, ChangeListener<T> listener) {
        if (readonly) {
            return text(factory.getValue().toString(), true, null);
        } else {
            Spinner<T> spinner = new Spinner<>();
            spinner.setValueFactory(factory);
            spinner.setEditable(true);
            spinner.valueProperty().addListener(listener);
            return spinner;
        }
    }
}
