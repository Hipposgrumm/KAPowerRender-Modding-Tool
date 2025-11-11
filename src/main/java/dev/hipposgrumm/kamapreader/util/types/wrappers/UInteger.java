package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class UInteger {
    private final long i;

    public UInteger(int i) {
        this.i = i & 0xFFFFFFFFL; // Casting repurposed negative bit.
    }

    public UInteger(long i) {
        this.i = i & 0xFFFFFFFFL;
    }

    public final long get() {
        return i;
    }

    public final int getInt() {
        return (int) i; // Casting will keep the sign in its existing place.
    }
}
