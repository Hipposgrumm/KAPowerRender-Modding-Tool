package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.EnumChoices;

import java.util.List;

public enum D3DCMPFUNC implements EnumChoices {
    NEVER(1),
    LESS(2),
    EQUAL(3),
    LESSEQUAL(4),
    GREATER(5),
    NOTEQUAL(6),
    GREATEREQUAL(7),
    ALWAYS(8),
    __(0);

    public final int identifier;

    D3DCMPFUNC(int identifier) {
        this.identifier = identifier;
    }

    public static D3DCMPFUNC from(int i) {
        return switch (i) {
            case 1 -> NEVER;
            case 2 -> LESS;
            case 3 -> EQUAL;
            case 4 -> LESSEQUAL;
            case 5 -> GREATER;
            case 6 -> NOTEQUAL;
            case 7 -> GREATEREQUAL;
            case 8 -> ALWAYS;
            default -> __;
        };
    }

    @Override
    public Enum<? extends EnumChoices> getSelf() {
        return this;
    }

    @Override
    public List<? extends Enum<? extends EnumChoices>> choices() {
        return List.of(__,
                NEVER, LESS, EQUAL, LESSEQUAL,
                GREATER, NOTEQUAL, GREATEREQUAL, ALWAYS
        );
    }
}
