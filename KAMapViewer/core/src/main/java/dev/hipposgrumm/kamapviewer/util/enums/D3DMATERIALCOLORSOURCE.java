package dev.hipposgrumm.kamapviewer.util.enums;

import dev.hipposgrumm.kamapviewer.util.EnumChoices;

import java.util.List;

public enum D3DMATERIALCOLORSOURCE implements EnumChoices {
    MATERIAL(0),
    COLOR1(1),
    COLOR2(2);

    public final int identifier;

    D3DMATERIALCOLORSOURCE(int identifier) {
        this.identifier = identifier;
    }

    public static D3DMATERIALCOLORSOURCE from(int i) {
        return switch (i) {
            case 0 -> MATERIAL;
            case 1 -> COLOR1;
            case 2 -> COLOR2;
            default -> {
                System.err.println("No type of "+i+" in enum D3DMATERIALCOLORSOURCE");
                yield MATERIAL;
            }
        };
    }

    @Override
    public Enum<? extends EnumChoices> getSelf() {
        return this;
    }

    @Override
    public List<? extends Enum<? extends EnumChoices>> choices() {
        return List.of(MATERIAL, COLOR1, COLOR2);
    }
}
