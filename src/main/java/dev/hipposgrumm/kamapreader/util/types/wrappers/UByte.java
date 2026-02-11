package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class UByte {
    private final byte b;

    public UByte(byte b) {
        this.b = b;
    }

    public UByte(short b) {
        this.b = (byte) (b & 0xFF); // Casting down will keep the sign in its existing place.
    }

    public final short get() {
        return (short) (b & 0xFF); // Casting up repurposes negative bit.
    }

    public final byte getByte() {
        return b;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(b);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UByte other && this.b == other.b;
    }

    @Override
    public String toString() {
        return Short.toString(get());
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
