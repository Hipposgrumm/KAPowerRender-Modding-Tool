package dev.hipposgrumm.kamapreader.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DatingProfileEntry<T> {
    private final String name;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public DatingProfileEntry(String name, Supplier<T> getter) {
        this.name = name;
        this.getter = getter;
        this.setter = null;
    }

    public DatingProfileEntry(String name, Supplier<T> getter, Consumer<T> setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return getter.get();
    }

    public void set(T value) {
        if (setter == null) throw new IllegalCallerException("The type "+(value != null ? value.getClass() : "")+" does not use a separate setter.");
        setter.accept(value);
    }

    public boolean readOnly() {
        return false;
    }

    public static class ReadOnly<T> extends DatingProfileEntry<T> {
        public ReadOnly(String name, Supplier<T> getter) {
            super(name, getter, null);
        }

        @Override
        public void set(T value) {
            throw new IllegalStateException("Trying to set a read-only value.");
        }

        public boolean readOnly() {
            return true;
        }
    }
}
