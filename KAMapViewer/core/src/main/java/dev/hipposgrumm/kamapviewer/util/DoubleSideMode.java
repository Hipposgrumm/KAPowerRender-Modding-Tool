package dev.hipposgrumm.kamapviewer.util;

import dev.hipposgrumm.kamapviewer.rendering.PRMaterial;

public enum DoubleSideMode {
    FORCE_OFF("Force Off"),
    DEFAULT("Material Value"),
    FORCE_ON("Force On");

    public final String name;

    DoubleSideMode(String name) {
        this.name = name;
    }

    public boolean doubleSide(PRMaterial material) {
        return switch (this) {
            case FORCE_OFF -> false;
            case FORCE_ON -> true;
            case DEFAULT -> material.twosided;
        };
    }

    @Override
    public String toString() {
        return name;
    }
}
