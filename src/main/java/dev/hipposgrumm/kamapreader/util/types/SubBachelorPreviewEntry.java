package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.util.List;
import java.util.function.Supplier;

public class SubBachelorPreviewEntry extends DatingProfileEntry.ReadOnly<List<? extends Previewable>> {
    public SubBachelorPreviewEntry(Supplier<List<? extends Previewable>> getter) {
        super("Contents", getter);
    }

    public SubBachelorPreviewEntry(String name, Supplier<List<? extends Previewable>> getter) {
        super(name, getter);
    }
}
