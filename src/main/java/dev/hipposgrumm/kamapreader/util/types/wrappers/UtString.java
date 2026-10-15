package dev.hipposgrumm.kamapreader.util.types.wrappers;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import dev.hipposgrumm.kamapreader.util.types.EnumChoices;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UtString implements DatingProfileValue {
    public int unknownFlags; // string flags - not sure what they're used for
    private Encoding encoding;
    public UInteger gamePointer; // pointer location used internally by the game
    public UInteger uselessPointer; // placeholder pointer; is overwritten by the game
    public int maxSize;
    private String value;

    private final String original;

    public UtString(int maxSize) {
        this.value = "";
        this.original = null;
        this.encoding = Encoding.US_ASCII;
        this.maxSize = maxSize;
    }

    public UtString(String string, int maxSize) {
        this.value = string;
        this.original = string;
        this.encoding = detectEncoding(string);
        this.maxSize = maxSize;
    }

    @Override
    public String toString() {
        return value;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public static Encoding detectEncoding(String string) {
        for (char c:string.toCharArray()) {
            if ((c >> 8) != 0) return Encoding.UTF16;
        }
        return Encoding.US_ASCII;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        AtomicReference<TextField> text = new AtomicReference<>();
        text.set(DatingProfileValue.text(this.value, readonly, (observable, oldValue, newValue) -> {
            this.encoding = detectEncoding(newValue);
            int maxSize = this.maxSize / this.encoding.charsize;
            if (newValue.length() > maxSize) {
                newValue = newValue.substring(0, maxSize);
                text.get().setText(newValue);
            }
            this.value = newValue;
            if (!newValue.equals(oldValue)) onChanged.run();
        }));
        return text.get();
    }

    @Override
    public boolean isModified() {
        return original == null || !original.equals(value);
    }

    public enum Encoding implements EnumChoices {
        UNKNOWN(-1, 1, StandardCharsets.US_ASCII),
        US_ASCII(0, 1, StandardCharsets.US_ASCII),
        UTF16(3, 2, StandardCharsets.UTF_16LE);

        public final int identifier;
        public final int charsize;
        public final Charset charset;

        Encoding(int identifier, int charsize, Charset charset) {
            this.identifier = identifier;
            this.charsize = charsize;
            this.charset = charset;
        }

        @Override
        public Enum<? extends EnumChoices> getSelf() {
            return this;
        }

        @Override
        public List<? extends Enum<? extends EnumChoices>> choices() {
            return List.of(US_ASCII, UTF16);
        }
    }
}
