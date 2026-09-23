package dev.hipposgrumm.kamapreader.util.types.wrappers;

public record UShort(short innerVal) {
    public UShort(int innerVal) {
        this((short) (innerVal & 0xFFFF)); // Casting down will keep the sign in its existing place.
    }

    public int get() {
        return innerVal & 0xFFFF; // Casting up repurposes negative bit.
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }

    public record Array(short[] array) {
        public Array(int size) {
            this(new short[size]);
        }

        public int get(int i) {
            return array[i] & 0xFFFF;
        }
    }
}
