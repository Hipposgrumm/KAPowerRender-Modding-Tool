package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.Flags;

public enum TEXARGS implements Flags.FlagsEnum {
    DIFFUSE(0x00),
    CURRENT(0x01),
    TEXTURE(0x02),
    TFACTOR(0x03),
    SPECULAR(0x04),
    TEMP(0x05),
    CONSTANT(0x06),

    __(0x00),
    COMPLEMENT(0x10),
    ALPHAREPLICATE(0x20);

    public final int identifier;

    TEXARGS(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public Enum<?> getSelf() {
        return this;
    }

    @Override
    public int getValue() {
        return identifier;
    }

    public static class AsFlags extends Flags {
        public static final EnumEntry Argument = new EnumEntry(CONSTANT, new FlagsEnum[] {
                DIFFUSE, CURRENT, TEXTURE, TFACTOR,
                SPECULAR, TEMP, CONSTANT
        }, 0, 4);
        public static final EnumEntry Modifier = new EnumEntry(__, new FlagsEnum[] {
                __, COMPLEMENT, ALPHAREPLICATE
        }, 4, 4);

        public AsFlags(int value) {
            super(value);
        }

        @Override
        public Entry[] getEntries() {
            return new Entry[] {Argument, Modifier};
        }
    }
}
