package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.EnumChoices;

import java.util.List;

public enum TEXINDEX implements EnumChoices {
    _0(0),
    _1(1),
    _2(2),
    _3(3),
    _4(4),
    _5(5),
    _6(6),
    _7(7),
    __(-1);

    public final int identifier;

    TEXINDEX(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        if (this == __) return name();
        return name().substring(1);
    }

    public static TEXINDEX from(int i) {
        return switch (i) {
            case 0 -> _0;
            case 1 -> _1;
            case 2 -> _2;
            case 3 -> _3;
            case 4 -> _4;
            case 5 -> _5;
            case 6 -> _6;
            case 7 -> _7;
            default -> {
                System.err.println("No type of "+i+" in enum TEXINDEX");
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
        return List.of(_0,_1,_2,_3,_4,_5,_6,_7);
    }
}
