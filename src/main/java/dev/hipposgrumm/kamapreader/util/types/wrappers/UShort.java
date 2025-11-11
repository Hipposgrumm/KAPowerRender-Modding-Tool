package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class UShort {
    private final int s;

    public UShort(short s) {
        this.s = s & 0xFFFF; // Casting repurposed negative bit.
    }

    public UShort(int s) {
        this.s = s & 0xFFFF;
    }

    public final int get() {
        return s;
    }

    public final short getShort() {
        return (short) s; // Casting will keep the sign in its existing place.
    }
}
