package dev.hipposgrumm.kamapviewer.util;

import java.util.List;

public interface EnumChoices {
    Enum<?> getSelf();
    List<? extends Enum<? extends EnumChoices>> choices();
}
