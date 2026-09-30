package dev.hipposgrumm.kamapviewer.util;

public enum VertexcolorMode {
    OFF("Off"),
    BLENDED("Blended", false, false),
    BLENDED_ACCURATE("Blended (Accurate) (Incomplete)", false, true),
    ISOLATED("Isolated", true, false),
    ISOLATED_ACCURATE("Isolated (Accurate) (Incomplete)", true, true);

    private final String name;
    public final boolean enabled;
    public final boolean isolated;
    public final boolean accurate;

    VertexcolorMode(String name) {
        this.name = name;
        this.enabled = false;
        this.isolated = false;
        this.accurate = false;
    }

    VertexcolorMode(String name, boolean isolated, boolean accurate) {
        this.name = name;
        this.enabled = true;
        this.isolated = isolated;
        this.accurate = accurate;
    }


    @Override
    public String toString() {
        return name;
    }
}
