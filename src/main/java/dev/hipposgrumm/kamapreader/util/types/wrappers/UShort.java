package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class UShort {
    private final short s;

    public UShort(short s) {
        this.s = s;
    }

    public UShort(int s) {
        this.s = (short) (s & 0xFFFF); // Casting down will keep the sign in its existing place.
    }

    public final int get() {
        return s & 0xFFFF; // Casting up repurposes negative bit.
    }

    public final short getShort() {
        return s;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(s);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UShort other && this.s == other.s;
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }

    public static class Array {
        private final short[] vals;

        public Array(int size) {
            this.vals = new short[size];
        }

        public Array(short[] data) {
            this.vals = data;
        }

        public int get(int i) {
            return vals[i] & 0xFFFF;
        }

        public short[] array() {
            return vals;
        }
    }
}
