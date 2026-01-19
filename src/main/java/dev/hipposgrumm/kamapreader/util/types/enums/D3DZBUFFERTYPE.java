package dev.hipposgrumm.kamapreader.util.types.enums;

import dev.hipposgrumm.kamapreader.util.types.EnumChoices;

import java.util.List;

public enum D3DZBUFFERTYPE implements EnumChoices {
    FALSE(0),
    TRUE(1),
    USEW(2);

    public final int identifier;

    D3DZBUFFERTYPE(int identifier) {
        this.identifier = identifier;
    }

    public static D3DZBUFFERTYPE from(int i) {
        return switch (i) {
            case 0 -> FALSE;
            case 1 -> TRUE;
            case 2 -> USEW;
            default -> {
                System.err.println("No type of "+i+" in enum D3DZBUFFERTYPE; defaulting to FALSE");
                yield FALSE;
            }
        };
    }

    @Override
    public Enum<? extends EnumChoices> getSelf() {
        return this;
    }

    @Override
    public List<? extends Enum<? extends EnumChoices>> choices() {
        return List.of(FALSE, TRUE, USEW);
    }
}
