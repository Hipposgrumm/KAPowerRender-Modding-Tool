package dev.hipposgrumm.kamapreader.util;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SavableDatingProfileEntry<T> extends DatingProfileEntry<T> {
    private final IOUnsafeConsumer<File> saving;

    public SavableDatingProfileEntry(String name, Supplier<T> getter, Consumer<T> setter, IOUnsafeConsumer<File> saving) {
        super(name, getter, setter);
        this.saving = saving;
    }

    public void save(File file) throws IOException {
        saving.accept(file);
    }

    public interface IOUnsafeConsumer<T> {
        void accept(T t) throws IOException;
    }
}
