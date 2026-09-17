package dev.hipposgrumm.kamapreader.util;

import dev.hipposgrumm.kamapreader.FirstThing;

import java.util.List;
import java.util.function.BiConsumer;

public interface DatingBachelor {
    List<? extends DatingProfileEntry<?>> getDatingProfile();

    default List<? extends DatingBachelor> getSubBachelors() {
        return null;
    }

    default ContextMenuOption[] getContextMenu() {
        return new ContextMenuOption[0];
    }

    record ContextMenuOption(String name, BiConsumer<FirstThing, DatingBachelor[]> function) {}
}
