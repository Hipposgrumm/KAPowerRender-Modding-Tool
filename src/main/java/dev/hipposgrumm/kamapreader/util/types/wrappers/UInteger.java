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
