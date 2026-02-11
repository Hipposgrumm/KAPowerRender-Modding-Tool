package dev.hipposgrumm.kamapviewer.util.enums;

import dev.hipposgrumm.kamapviewer.util.Flags;

public class D3DFVF extends Flags {
    public static final EnumEntry Position = new EnumEntry(Enumerations.__, new FlagsEnum[] {
            Enumerations.XYZ, Enumerations.XYZRHW,
            Enumerations.XYZB1, Enumerations.XYZB2, Enumerations.XYZB3, Enumerations.XYZB4, Enumerations.XYZB5,
            Enumerations.XYZW
    }, 0, 16, 0x400E);
    public static final BoolEntry DIFFUSE = new BoolEntry("DIFFUSE", 6);
    public static final BoolEntry NORMAL = new BoolEntry("NORMAL", 4);
    public static final BoolEntry PSIZE = new BoolEntry("PSIZE", 5);
    public static final BoolEntry SPECULAR = new BoolEntry("SPECULAR", 7);
    public static final EnumEntry TexCoord = new EnumEntry(Enumerations.TEX0, new FlagsEnum[] {
            Enumerations.TEX0, Enumerations.TEX1, Enumerations.TEX2, Enumerations.TEX3, Enumerations.TEX4, Enumerations.TEX5, Enumerations.TEX6, Enumerations.TEX7, Enumerations.TEX8
    }, 0, 3);
    public static final EnumEntry LASTBETA = new EnumEntry(Enumerations.__, new FlagsEnum[] {
            Enumerations.LASTBETA_UBYTE4, Enumerations.LASTBETA_D3DCOLOR
    }, 12, 4);
    public static final EnumEntry[] TEXCOORDNUMS = {
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 16, 2),
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 18, 2),
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 20, 2),
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 22, 2),
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 24, 2),
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 26, 2),
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 28, 2),
            new EnumEntry(TexCoordSize._2, TexCoordSize.values(), 30, 2)
    };

    public D3DFVF(int value) {
        super(value);
    }

    @Override
    public Entry[] getEntries() {
        return new Entry[] {
                Position, DIFFUSE, NORMAL, PSIZE, SPECULAR,
                TexCoord, LASTBETA,
                TEXCOORDNUMS[0], TEXCOORDNUMS[1], TEXCOORDNUMS[2], TEXCOORDNUMS[3],
                TEXCOORDNUMS[4], TEXCOORDNUMS[5], TEXCOORDNUMS[6], TEXCOORDNUMS[7]
        };
    }

    @Override
    public void onUpdate(Entry entry) {
        if (Position.from(this) == Enumerations.XYZRHW && NORMAL.from(this)) {
            if (entry == Position) {
                NORMAL.apply(this, false);
            } else Position.apply(this, Enumerations.XYZB1);
        }
    }

    public enum Enumerations implements Flags.FlagsEnum {
        __(0),

        RESERVED0(0b1),
        XYZ(0b1<<1),
        XYZRHW(0b10<<1),
        XYZB1(0b011<<1),
        XYZB2(0b100<<1),
        XYZB3(0b101<<1),
        XYZB4(0b110<<1),
        XYZB5(0b111<<1),
        XYZW(0x4000|(0b1<<1)),

        NORMAL  (1<<4),
        PSIZE   (1<<5),
        DIFFUSE (1<<6),
        SPECULAR(1<<7),

        TEX0(0<<8),
        TEX1(1<<8),
        TEX2(2<<8),
        TEX3(3<<8),
        TEX4(4<<8),
        TEX5(5<<8),
        TEX6(6<<8),
        TEX7(7<<8),
        TEX8(8<<8),

        LASTBETA_UBYTE4   (1<<12),
        LASTBETA_RESERVED1(1<<13),
        LASTBETA_RESERVED2(1<<14),
        LASTBETA_D3DCOLOR (1<<15);

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

    public enum TexCoordSize implements Flags.FlagsEnum {
        _1(3,1),
        _2(0,2),
        _3(1,3),
        _4(2,4);

        public final int identifier;
        public final int texCount;

        TexCoordSize(int identifier, int count) {
            this.identifier = identifier;
            this.texCount = count;
        }

        @Override
        public String toString() {
            return name().substring(1);
        }

        @Override
        public Enum<?> getSelf() {
            return null;
        }

        @Override
        public int getValue() {
            return identifier;
        }
    }
}
