package dev.hipposgrumm.kamapreader.util.types;

import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Flags {
    protected int value;

    public Flags(int value) {
        this.value = value;
    }

    public final int getValue() {
        return value;
    }

    public final void setValue(int value) {
        this.value = value;
    }

    public void onUpdate(Entry entry) {}

    public abstract Entry[] getEntries();

    public static abstract sealed class Entry permits BoolEntry, ValueEntry, EnumEntry {
        public final String name;

        public Entry(String name) {
            this.name = name;
        }
    }

    public static final class BoolEntry extends Entry {
        private final int mask;

        public BoolEntry(String name, int shift) {
            super(name);
            this.mask = 1 << shift;
        }

        public boolean from(Flags flags) {
            return (flags.value & mask) != 0;
        }

        public void apply(Flags flags, boolean value) {
            if (value) flags.value |= mask;
            else flags.value &= ~mask;
            flags.onUpdate(this);
        }
    }

    public static final class ValueEntry extends Entry {
        private final byte shift;
        public final int size;
        private final int mask;

        public ValueEntry(String name, int shift, int size) {
            super(name);
            this.shift = (byte) shift;
            this.size = size;
            int mask = 0;
            for (;size>0;size--) mask = (mask<<1)|1;
            this.mask = mask<<shift;
        }

        private ValueEntry(String name, int shift, int size, int mask) {
            super(name);
            this.shift = (byte) shift;
            this.size = size;
            this.mask = mask<<shift;
        }

        public int from(Flags flags) {
            return (flags.value & mask) >> shift;
        }

        public void apply(Flags flags, int value) {
            value <<= shift;
            if ((value & mask) != value) flags.value |= mask; // if inputted value doesn't fit in mask, go maximum
            else flags.value = (flags.value & ~mask) | value;
            flags.onUpdate(this);
        }
    }

    public static final class EnumEntry extends Entry {
        private final ValueEntry wrappedvalue;
        public final FlagsEnum defaultValue;
        public final SortedMap<Integer, FlagsEnum> values;

        public EnumEntry(FlagsEnum def, FlagsEnum[] values, int shift, int size) {
            super(null);
            this.defaultValue = def;
            SortedMap<Integer, FlagsEnum> map = new TreeMap<>();
            for (FlagsEnum val:values) map.put(val.getValue(), val);
            this.values = map;
            this.wrappedvalue = new ValueEntry(null, shift, size);
        }

        public EnumEntry(FlagsEnum def, FlagsEnum[] values, int shift, int size, int mask) {
            super(null);
            this.defaultValue = def;
            SortedMap<Integer, FlagsEnum> map = new TreeMap<>();
            for (FlagsEnum val:values) map.put(val.getValue(), val);
            this.values = map;
            this.wrappedvalue = new ValueEntry(null, shift, size, mask);
        }

        public FlagsEnum from(Flags flags) {
            return this.values.getOrDefault(wrappedvalue.from(flags)<<wrappedvalue.shift, defaultValue);
        }

        public void apply(Flags flags, FlagsEnum value) {
            wrappedvalue.apply(flags, value.getValue()>>wrappedvalue.shift);
        }
    }

    public interface FlagsEnum {
        int getValue();
        Enum<?> getSelf();
    }
}
