package dev.hipposgrumm.kamapreader.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DatingProfileEntry<T> {
    private final String name;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

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
        setter.accept(value);
    }

    public boolean readOnly() {
        return setter == null;
    }
}
