package dev.hipposgrumm.kamapviewer.util.enums;

import dev.hipposgrumm.kamapviewer.util.EnumChoices;

import java.util.List;

public enum D3DSHADEMODE implements EnumChoices {
    FLAT(1),
    GOURAUD(2),
    PHONG(3);

    public final int identifier;

    D3DSHADEMODE(int identifier) {
        this.identifier = identifier;
    }

    public static D3DSHADEMODE from(int i) {
        return switch (i) {
            case 1 -> FLAT;
            case 2 -> GOURAUD;
            case 3 -> PHONG;
            default -> {
                System.err.println("No type of "+i+" in enum D3DSHADEMODE; defaulting to GOURAUD");
                yield GOURAUD;
            }
        };
    }

    @Override
    public Enum<? extends EnumChoices> getSelf() {
        return this;
    }

    @Override
    public List<? extends Enum<? extends EnumChoices>> choices() {
        return List.of(FLAT, GOURAUD, PHONG);
    }
}
