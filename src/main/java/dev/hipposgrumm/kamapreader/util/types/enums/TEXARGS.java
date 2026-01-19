package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.Flags;

public class TEXARGS extends Flags {
    public static final EnumEntry Argument = new EnumEntry(Enumerations.CONSTANT, new FlagsEnum[] {
            Enumerations.DIFFUSE, Enumerations.CURRENT, Enumerations.TEXTURE, Enumerations.TFACTOR,
            Enumerations.SPECULAR, Enumerations.TEMP, Enumerations.CONSTANT
    }, 0, 4);
    public static final EnumEntry Modifier = new EnumEntry(Enumerations.__, new FlagsEnum[] {
            Enumerations.__, Enumerations.COMPLEMENT, Enumerations.ALPHAREPLICATE
    }, 4, 4);

    public TEXARGS(int value) {
        super(value);
    }

    @Override
    public Entry[] getEntries() {
        return new Entry[] {Argument, Modifier};
    }

    public enum Enumerations implements Flags.FlagsEnum {
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

        Enumerations(int identifier) {
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
    }


}
