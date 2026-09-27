package dev.hipposgrumm.kamapreader.util.control;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.types.EnumChoices;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record DatingProfileEntry(String name, boolean readonly, DatingProfileValue value) {
    /// For the cases where the DatingProfileValue type is read-only.
    public DatingProfileEntry(String name, DatingProfileValue value) {
        this(name, true, value);
    }

    public DatingProfileEntry(String name, boolean readonly, ContainingDatingProfileValue.Containable<?> containable) {
        this(name, readonly, new ContainingDatingProfileValue<>(containable));
    }

    /// DatingProfileValue that can be read and modified.
    public static <T> DatingProfileEntry simple(String name, Supplier<T> getter, Consumer<T> setter) {
        return new DatingProfileEntry(name, false, new Simple<>(getter, setter));
    }

    /// Read-only DatingProfileValue.
    public static <T> DatingProfileEntry simple(String name, Supplier<T> getter) {
        return new DatingProfileEntry(name, true, new Simple<>(getter));
    }

    /// Read-only DatingProfileValue with static value.
    public static <T> DatingProfileEntry simple(String name, T value) {
        return simple(name, () -> value);
    }

    private static final class Simple<T> implements DatingProfileValue {
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private final T originalValue;

        /// DatingProfileValue that can be read and modified.
        public Simple(Supplier<T> getter, Consumer<T> setter) {
            if (getter == null) throw new IllegalArgumentException("Getter cannot be null!");
            this.getter = getter;
            this.setter = setter;
            this.originalValue = getter.get();
        }

        /// Read-only DatingProfileValue.
        public Simple(Supplier<T> getter) {
            this(getter, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
            if (!readonly && setter == null) throw new IllegalStateException("No setter for writable displayobject.");

            T value = getter.get();
            return switch (value) {
                case null -> DatingProfileValue.NULL_DISPLAY;
                case String s -> DatingProfileValue.text(s, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case Boolean b -> DatingProfileValue.checkbox(b, "", readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case Byte b -> DatingProfileValue.byteSpinner(b, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case UByte b -> DatingProfileValue.ubyteSpinner(b, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case Short s -> DatingProfileValue.shortSpinner(s, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case UShort s -> DatingProfileValue.ushortSpinner(s, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case Integer i -> DatingProfileValue.intSpinner(i, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case UInteger i -> DatingProfileValue.uintSpinner(i, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case Float f -> DatingProfileValue.floatSpinner(f, readonly, (observable, oldValue, newValue) -> {
                    setter.accept((T) newValue);
                    onChanged.run();
                });
                case EnumChoices e -> {
                    ComboBox<? extends Enum<?>> dropdown = new ComboBox<>(FXCollections.observableList(e.choices()));
                    ((ComboBox<Enum<?>>) dropdown).setValue(e.getSelf());
                    if (readonly) dropdown.setDisable(true);
                    else dropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
                        setter.accept((T) newValue);
                        onChanged.run();
                    });
                    yield dropdown;
                }
                default -> {
                    System.out.println("WARNING: Unexpected writable type in simpledatingprofileview: " + value.getClass());
                    yield new Label(value.toString());
                }
            };
        }

        @Override
        public boolean isModified() {
            return !getter.get().equals(originalValue);
        }
    }
}
