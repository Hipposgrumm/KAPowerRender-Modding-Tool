package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.Flags;

public enum D3DTEXTURETRANSFORMFLAGS implements Flags.FlagsEnum {
    DISABLE(0x000),
    COUNT1(0x001),
    COUNT2(0x002),
    COUNT3(0x003),
    COUNT4(0x004),

    PROJECTED(0x100);

    public final int identifier;

    D3DTEXTURETRANSFORMFLAGS(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public Enum<? extends Flags.FlagsEnum> getSelf() {
        return this;
    }

    @Override
    public int getValue() {
        return identifier;
    }

    public static class AsFlags extends Flags {
        public static final EnumEntry COUNT = new EnumEntry(DISABLE, new FlagsEnum[] {
                DISABLE, COUNT1, COUNT2, COUNT3, COUNT4
        }, 0, 4);
        public static final BoolEntry PROJECTED = new BoolEntry("PROJECTED", 8);

        public AsFlags(int value) {
            super(value);
        }

        @Override
        public Entry[] getEntries() {
            return new Entry[] {COUNT, PROJECTED};
        }
    }
}
