package dev.hipposgrumm.kamapreader.util;

import java.util.List;

public interface DatingBachelor {
    List<? extends DatingProfileEntry<?>> getDatingProfile();

    default List<? extends DatingBachelor> getSubBachelors() {
        return null;
    }
}
