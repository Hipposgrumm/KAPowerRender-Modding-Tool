package dev.hipposgrumm.kamapviewer.util.enums;

import dev.hipposgrumm.kamapviewer.util.EnumChoices;

import java.util.List;

public enum D3DTEXTUREOP implements EnumChoices {
    DISABLE(1),
    SELECTARG1(2),
    SELECTARG2(3),
    MODULATE(4),
    MODULATE2X(5),
    MODULATE4X(6),
    ADD(7),
    ADDSIGNED(8),
    ADDSIGNED2X(9),
    SUBTRACT(10),
    ADDSMOOTH(11),
    BLENDDIFFUSEALPHA(12),
    BLENDTEXTUREALPHA(13),
    BLENDFACTORALPHA(14),
    BLENDTEXTUREALPHAPM(15),
    BLENDCURRENTALPHA(16),
    PREMODULATE(17),
    MODULATEALPHA_ADDCOLOR(18),
    MODULATECOLOR_ADDALPHA(19),
    MODULATEINVALPHA_ADDCOLOR(20),
    MODULATEINVCOLOR_ADDALPHA(21),
    BUMPENVMAP(22),
    BUMPENVMAPLUMINANCE(23),
    DOTPRODUCT3(24),
    MULTIPLYADD(25),
    LERP(26),
    __(0);

    public final int identifier;

    D3DTEXTUREOP(int identifier) {
        this.identifier = identifier;
    }

    public static D3DTEXTUREOP from(int i) {
        return switch (i) {
            case 0 -> __;
            case 1 -> DISABLE;
            case 2 -> SELECTARG1;
            case 3 -> SELECTARG2;
            case 4 -> MODULATE;
            case 5 -> MODULATE2X;
            case 6 -> MODULATE4X;
            case 7 -> ADD;
            case 8 -> ADDSIGNED;
            case 9 -> ADDSIGNED2X;
            case 10 -> SUBTRACT;
            case 11 -> ADDSMOOTH;
            case 12 -> BLENDDIFFUSEALPHA;
            case 13 -> BLENDTEXTUREALPHA;
            case 14 -> BLENDFACTORALPHA;
            case 15 -> BLENDTEXTUREALPHAPM;
            case 16 -> BLENDCURRENTALPHA;
            case 17 -> PREMODULATE;
            case 18 -> MODULATEALPHA_ADDCOLOR;
            case 19 -> MODULATECOLOR_ADDALPHA;
            case 20 -> MODULATEINVALPHA_ADDCOLOR;
            case 21 -> MODULATEINVCOLOR_ADDALPHA;
            case 22 -> BUMPENVMAP;
            case 23 -> BUMPENVMAPLUMINANCE;
            case 24 -> DOTPRODUCT3;
            case 25 -> MULTIPLYADD;
            case 26 -> LERP;
            default -> {
                System.err.println("No type of "+i+" in enum D3DTEXTUREOP");
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
                DISABLE, SELECTARG1, SELECTARG2,
                MODULATE, MODULATE2X, MODULATE4X,
                ADD, ADDSIGNED, ADDSIGNED2X, SUBTRACT, ADDSMOOTH,
                BLENDDIFFUSEALPHA, BLENDTEXTUREALPHA, BLENDFACTORALPHA, BLENDTEXTUREALPHAPM, BLENDCURRENTALPHA,
                PREMODULATE, MODULATEALPHA_ADDCOLOR, MODULATECOLOR_ADDALPHA, MODULATEINVALPHA_ADDCOLOR, MODULATEINVCOLOR_ADDALPHA,
                BUMPENVMAP, BUMPENVMAPLUMINANCE,
                DOTPRODUCT3, MULTIPLYADD, LERP
        );
    }
}
