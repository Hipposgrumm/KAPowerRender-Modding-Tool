package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.EnumChoices;

import java.util.List;

public enum D3DBLEND implements EnumChoices {
    ZERO(1),
    ONE(2),
    SRCCOLOR(3),
    INVSRCCOLOR(4),
    SRCALPHA(5),
    INVSRCALPHA(6),
    DESTALPHA(7),
    INVDESTALPHA(8),
    DESTCOLOR(9),
    INVDESTCOLOR(10),
    SRCALPHASAT(11),
    BOTHSRCALPHA(12),
    BOTHINVSRCALPHA(13),
    BLENDFACTOR(14),
    INVBLENDFACTOR(15),
    SRCCOLOR2(16),
    INVSRCCOLOR2(17),
    __(0);

    public final int identifier;

    D3DBLEND(int identifier) {
        this.identifier = identifier;
    }

    public static D3DBLEND from(int i) {
        return switch (i) {
            case 0 -> __;
            case 1 -> ZERO;
            case 2 -> ONE;
            case 3 -> SRCCOLOR;
            case 4 -> INVSRCCOLOR;
            case 5 -> SRCALPHA;
            case 6 -> INVSRCALPHA;
            case 7 -> DESTALPHA;
            case 8 -> INVDESTALPHA;
            case 9 -> DESTCOLOR;
            case 10 -> INVDESTCOLOR;
            case 11 -> SRCALPHASAT;
            case 12 -> BOTHSRCALPHA;
            case 13 -> BOTHINVSRCALPHA;
            case 14 -> BLENDFACTOR;
            case 15 -> INVBLENDFACTOR;
            case 16 -> SRCCOLOR2;
            case 17 -> INVSRCCOLOR2;
            default -> {
                System.err.println("No type of "+i+" in enum D3DBLEND");
                yield __;
            }
        };
    }

    @Override
    public Enum<? extends EnumChoices> getSelf() {
        return this;
    }

    @Override
    public List<? extends Enum<? extends EnumChoices>> choices() {
        return List.of(__,
                ZERO, ONE, SRCCOLOR, INVSRCCOLOR, SRCALPHA, INVSRCALPHA,
                DESTALPHA, INVDESTALPHA, DESTCOLOR, INVDESTCOLOR,
                SRCALPHASAT, BOTHSRCALPHA, BOTHINVSRCALPHA,
                BLENDFACTOR, INVBLENDFACTOR, SRCCOLOR2, INVSRCCOLOR2
        );
    }
}
