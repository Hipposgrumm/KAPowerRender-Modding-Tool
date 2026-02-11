package dev.hipposgrumm.kamapviewer.util.enums;

import dev.hipposgrumm.kamapviewer.util.EnumChoices;

import java.util.List;

public enum D3DBLENDOP implements EnumChoices {
    ADD(1),
    SUBTRACT(2),
    REVSUBTRACT(3),
    MIN(4),
    MAX(5),
    UNKNOWN13(13),
    __(0);

    public final int identifier;

    D3DBLENDOP(int identifier) {
        this.identifier = identifier;
    }

    public static D3DBLENDOP from(int i) {
        return switch (i) {
            case 0 -> __;
            case 1 -> ADD;
            case 2 -> SUBTRACT;
            case 3 -> REVSUBTRACT;
            case 4 -> MIN;
            case 5 -> MAX;
            case 13 -> UNKNOWN13;
            default -> {
                System.err.println("No type of "+i+" in enum D3DBLENDOP");
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
        return List.of(__, ADD, SUBTRACT, REVSUBTRACT, MIN, MAX);
    }
}
