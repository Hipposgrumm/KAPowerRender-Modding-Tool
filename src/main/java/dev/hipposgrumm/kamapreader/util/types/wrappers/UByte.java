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

    public static class Array {
        private final byte[] vals;

        public Array(int size) {
            this.vals = new byte[size];
        }

        public Array(byte[] data) {
            this.vals = data;
        }

        public short get(int i) {
            return (short) (vals[i] & 0xFF);
        }

        public byte[] array() {
            return vals;
        }
    }
}
