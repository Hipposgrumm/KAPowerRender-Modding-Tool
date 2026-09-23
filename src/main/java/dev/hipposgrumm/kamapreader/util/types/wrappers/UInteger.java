package dev.hipposgrumm.kamapreader.util.types.wrappers;

public record UInteger(int innerVal) {
    public UInteger(long innerVal) {
        this((int) (innerVal & 0xFFFFFFFFL)); // Casting down will keep the sign in its existing place.
    }

    public long get() {
        return innerVal & 0xFFFFFFFFL; // Casting up repurposes negative bit.
    }

    @Override
    public String toString() {
        return Long.toString(get());
    }

    public record Array(int[] array) {
        public Array(int size) {
            this(new int[size]);
        }

        public long get(int i) {
            return array[i] & 0xFFFFFFFFL;
        }
    }
}
