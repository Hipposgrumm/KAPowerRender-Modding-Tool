package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class SizeLimitedString {
    private final int size;
    private String string = "";

    public SizeLimitedString(String string, int size) {
        this(size);
        setString(string);
    }

    public SizeLimitedString(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
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
