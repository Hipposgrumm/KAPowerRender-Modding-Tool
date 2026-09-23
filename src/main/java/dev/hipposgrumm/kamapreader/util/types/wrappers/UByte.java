package dev.hipposgrumm.kamapreader.util.types.wrappers;

public record UByte(byte innerVal) {
    public UByte(short b) {
        this((byte) (b & 0xFF)); // Casting down will keep the sign in its existing place.
    }

    public short get() {
        return (short) (innerVal & 0xFF); // Casting up repurposes negative bit.
    }

    @Override
    public String toString() {
        return Short.toString(get());
    }

    public record Array(byte[] array) {
        public Array(int size) {
            this(new byte[size]);
        }

        public short get(int i) {
            return (short) (array[i] & 0xFF);
        }
    }
}
