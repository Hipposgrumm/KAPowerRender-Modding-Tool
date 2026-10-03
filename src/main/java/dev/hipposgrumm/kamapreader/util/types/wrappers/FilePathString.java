package dev.hipposgrumm.kamapreader.util.types.wrappers;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class FilePathString implements DatingProfileValue {
    private String string;

    private final String original;

    public FilePathString(String string) {
        setString(string);
        this.original = string;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        AtomicReference<TextField> text = new AtomicReference<>();
        text.set(DatingProfileValue.text(this.string, readonly, (observable, oldValue, newValue) -> {
            try {
                setString(newValue);
            } catch (InvalidPathException e) {
                return; // Not a valid path, we're done here.
            }
            if (!this.string.equals(oldValue)) onChanged.run();
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

    public void setString(String string) throws InvalidPathException {
        // Throws InvalidPathException on fail.
        Paths.get(string);

        this.string = string;
    }
}
