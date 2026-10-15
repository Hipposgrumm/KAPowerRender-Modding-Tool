package dev.hipposgrumm.kamapreader.util.types;

public record Version(int major, int minor, int patch) {
    @Override
    public String toString() {
        return String.format("v%s.%s.%s", major, minor, patch);
    }
}
