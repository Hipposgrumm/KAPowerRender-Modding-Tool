package dev.hipposgrumm.kamapreader.util.types;

import java.util.List;

public interface EnumChoices {
    Enum<?> getSelf();
    List<? extends Enum<? extends EnumChoices>> choices();
}
