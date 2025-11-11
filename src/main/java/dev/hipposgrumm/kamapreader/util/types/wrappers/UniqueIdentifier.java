package dev.hipposgrumm.kamapreader.util.types.wrappers;

public class UniqueIdentifier {
    private int uid;
    private String str;

    public UniqueIdentifier(int uid) {
        this.uid = uid;
        this.str = Integer.toHexString(uid).toUpperCase();
    }

    public int get() {
        return uid;
    }

    public void set(int uid) {
        this.uid = uid;
        this.str = Integer.toHexString(uid).toUpperCase();
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public String toString() {
        return str;
    }
}
