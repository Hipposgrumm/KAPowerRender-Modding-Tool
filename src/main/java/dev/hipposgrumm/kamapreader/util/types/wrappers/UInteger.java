package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class UInteger {
    private final int i;

    public UInteger(int i) {
        this.i = i;
    }

    public UInteger(long i) {
        this.i = (int) (i & 0xFFFFFFFFL); // Casting down will keep the sign in its existing place.
    }

    public final long get() {
        return i & 0xFFFFFFFFL; // Casting up repurposes negative bit.
    }

    public final int getInt() {
        return i;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(i);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UInteger other && this.i == other.i;
    }

    @Override
    public String toString() {
        return Long.toString(get());
    }

    public static class Array {
        private final int[] vals;

        public Array(int size) {
            this.vals = new int[size];
        }

        public Array(int[] data) {
            this.vals = data;
        }

        public long get(int i) {
            return vals[i] & 0xFFFFFFFFL;
        }

        public int[] array() {
            return vals;
        }
    }
}
