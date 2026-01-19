package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.Flags;

public class D3DTEXTURETRANSFORMFLAGS extends Flags {
    public static final Flags.EnumEntry COUNT = new Flags.EnumEntry(Enumerations.DISABLE, new Flags.FlagsEnum[] {
            Enumerations.DISABLE, Enumerations.COUNT1, Enumerations.COUNT2, Enumerations.COUNT3, Enumerations.COUNT4
    }, 0, 4);
    public static final Flags.BoolEntry PROJECTED = new Flags.BoolEntry("PROJECTED", 8);

    public D3DTEXTURETRANSFORMFLAGS(int value) {
        super(value);
    }

    @Override
    public Flags.Entry[] getEntries() {
        return new Flags.Entry[] {COUNT, PROJECTED};
    }

    public enum Enumerations implements Flags.FlagsEnum {
        DISABLE(0x000),
        COUNT1(0x001),
        COUNT2(0x002),
        COUNT3(0x003),
        COUNT4(0x004),

        PROJECTED(0x100);

        public final int identifier;

        Enumerations(int identifier) {
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
    }
}
