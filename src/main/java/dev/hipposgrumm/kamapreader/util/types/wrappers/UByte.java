package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class UByte {
    private final short b;

    public UByte(byte b) {
        this.b = (short) (b & 0xFF); // Casting repurposed negative bit.
    }

    public UByte(short b) {
        this.b = (short) (b & 0xFF);
    }

    public final short get() {
        return b;
    }

    public final byte getByte() {
        return (byte) b; // Casting will keep the sign in its existing place.
    }
}
