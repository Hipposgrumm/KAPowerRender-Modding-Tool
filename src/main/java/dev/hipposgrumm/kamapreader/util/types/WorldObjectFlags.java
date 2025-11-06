package dev.hipposgrumm.kamapreader.util.types;

public class WorldObjectFlags {
    private static final int FLAG_VISIBLE = 0x00000001;

    private int value;

    public WorldObjectFlags(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public boolean getVisible() {
        return (value & FLAG_VISIBLE) != 0;
    }

    public void setVisible(boolean v) {
        if (v) value |= FLAG_VISIBLE;
        else value &= ~FLAG_VISIBLE;
    }
}
