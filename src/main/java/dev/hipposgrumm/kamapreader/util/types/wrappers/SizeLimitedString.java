package dev.hipposgrumm.kamapreader.util.types.wrappers;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.concurrent.atomic.AtomicReference;

public class SizeLimitedString implements DatingProfileValue {
    private final int size;
    private String string = "";

    private final String original;

    public SizeLimitedString(String string, int size) {
        this.size = size;
        setString(string);
        this.original = string;
    }

    public SizeLimitedString(int size) {
        this.size = size;
        this.original = "";
    }

    public int getSize() {
        return size;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        AtomicReference<TextField> text = new AtomicReference<>();
        text.set(DatingProfileValue.text(this.string, readonly, (observable, oldValue, newValue) -> {
            if (newValue.length() > this.size) {
                newValue = newValue.substring(0, this.size);
                text.get().setText(newValue);
            }
            this.setString(newValue);
            if (!newValue.equals(oldValue)) onChanged.run();
        }));
        return text.get();
    }

    @Override
    public boolean isModified() {
        return !string.equals(original);
    }

    @Override
    public String toString() {
        return string;
    }

    public void setString(String string) {
        if (string.length() > size) throw new IllegalArgumentException("String is longer than allowed value.");
        this.string = string;
    }
}
