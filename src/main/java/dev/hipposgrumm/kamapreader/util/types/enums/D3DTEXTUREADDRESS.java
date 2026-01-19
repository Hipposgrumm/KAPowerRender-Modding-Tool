package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.EnumChoices;

import java.util.List;

public enum D3DTEXTUREADDRESS implements EnumChoices {
    WRAP(1),
    MIRROR(2),
    CLAMP(3),
    BORDER(4),
    MIRROR_ONCE(5),
    __(0);

    public final int identifier;

    D3DTEXTUREADDRESS(int identifier) {
        this.identifier = identifier;
    }

    public static D3DTEXTUREADDRESS from(int i) {
        return switch (i) {
            case 0 -> __;
            case 1 -> WRAP;
            case 2 -> MIRROR;
            case 3 -> CLAMP;
            case 4 -> BORDER;
            case 5 -> MIRROR_ONCE;
            default -> {
                System.err.println("No type of "+i+" in enum D3DTEXTUREADDRESS");
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
        return List.of(__, WRAP, MIRROR, CLAMP, BORDER, MIRROR_ONCE);
    }
}
