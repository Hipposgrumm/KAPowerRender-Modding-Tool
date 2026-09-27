package dev.hipposgrumm.kamapreader.util.types.wrappers;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.concurrent.atomic.AtomicReference;

public class UniqueIdentifier implements DatingProfileValue {
    private int uid;
    private String str;

    private final int original;

    public UniqueIdentifier(int uid) {
        this.uid = uid;
        this.str = Integer.toHexString(uid).toUpperCase();
        this.original = this.uid;
    }

    public int get() {
        return uid;
    }

    public void set(int uid) {
        this.uid = uid;
        this.str = Integer.toHexString(uid).toUpperCase();
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        AtomicReference<TextField> text = new AtomicReference<>();
        text.set(DatingProfileValue.text(this.str, readonly, (observable, oldValue, newValue) -> {
            String val = oldValue;
            try {
                this.set(Integer.parseInt(newValue, 16));
                val = newValue.toUpperCase();
            } catch (NumberFormatException ignored) {}
            text.get().setText(val);
            onChanged.run();
        }));
        return text.get();
    }

    @Override
    public boolean isModified() {
        return uid != original;
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public String toString() {
        return str;
    }
}
