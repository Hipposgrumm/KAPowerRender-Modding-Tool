package dev.hipposgrumm.kamapreader.util.types;

public class Flags {
    private final String[] names = new String[32];
    private int value;

    public Flags(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean get(int shift) {
        return (value >> shift & 1) != 0;
    }

    public String getName(int shift) {
        return names[shift];
    }

    public void set(int shift, boolean val) {
        int mask = 1 << shift;
        if (val) value |= mask;
        else value &= ~mask;
    }

    public void setName(int shift, String name) {
        names[shift] = name;
    }
}
