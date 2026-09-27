package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.control.ContainingDatingProfileValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Flags implements ContainingDatingProfileValue.Containable<Flags> {
    protected int value;

    public Flags(int value) {
        this.value = value;
    }

    public final int getValue() {
        return value;
    }

    public final void setValue(int value) {
        this.value = value;
    }

    protected void onUpdate(Entry entry) {}

    public abstract Entry[] getEntries();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Flags other && this.value == other.value;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        Flags.Entry[] entries = this.getEntries();
        Runnable[] entryUpdaters = new Runnable[entries.length+1];
        AtomicReference<TextField> field = new AtomicReference<>();
        String startingval = Integer.toHexString(this.getValue());
        startingval = "0".repeat(8-startingval.length())+startingval;
        field.set(DatingProfileValue.text(startingval, readonly, (observable, oldValue, newValue) -> {
            try {
                this.setValue(Integer.parseInt(newValue, 16));
            } catch (NumberFormatException ignored) {return;}
            for (Runnable run:entryUpdaters) run.run();
        }));
        entryUpdaters[entryUpdaters.length-1] = () -> {
            String val = Integer.toHexString(this.getValue());
            field.get().setText("0".repeat(8-val.length())+val);
        };
        HBox box2 = new HBox(
                new Label("0x"), field.get()
        );
        box2.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(box2);
        for (int i=0;i<entries.length;i++) {
            Entry entry = entries[i];
            Pair<Runnable, Node> nodeData = entry.createDisplay(entry, this, () -> {
                for (Runnable runnable:entryUpdaters) runnable.run();
                onChanged.run();
            }, readonly);
            entryUpdaters[i] = nodeData.getKey();
            box.getChildren().add(nodeData.getValue());
        }
        return box;
    }

    @Override
    public ContainingDatingProfileValue.Containable<? extends Flags> makeCopy() {
        try {
            return getClass().getConstructor(int.class).newInstance(value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract sealed class Entry permits BoolEntry, ValueEntry, EnumEntry {
        public final String name;

        public Entry(String name) {
            this.name = name;
        }

        abstract Pair<Runnable, Node> createDisplay(Entry value, Flags flags, Runnable onChanged, boolean readonly);
    }

    public static final class BoolEntry extends Entry {
        private final int mask;

        public BoolEntry(String name, int shift) {
            super(name);
            this.mask = 1 << shift;
        }

        @Override
        Pair<Runnable, Node> createDisplay(Entry value, Flags flags, Runnable onChanged, boolean readonly) {
            if (!(value instanceof BoolEntry be)) throw new ClassCastException("Unexpected parameter type for createDisplay()");
            CheckBox check = DatingProfileValue.checkbox(be.from(flags), be.name, readonly, (observable, oldValue, newValue) -> {
                be.apply(flags, newValue);
                onChanged.run();
            });
            return new Pair<>(() -> {
                check.setSelected(be.from(flags));
            }, check);
        }

        public boolean from(Flags flags) {
            return (flags.value & mask) != 0;
        }

        public void apply(Flags flags, boolean value) {
            if (value) flags.value |= mask;
            else flags.value &= ~mask;
            flags.onUpdate(this);
        }
    }

    public static final class ValueEntry extends Entry {
        private final byte shift;
        public final int size;
        private final int mask;

        public ValueEntry(String name, int shift, int size) {
            super(name);
            this.shift = (byte) shift;
            this.size = size;
            int mask = 0;
            for (;size>0;size--) mask = (mask<<1)|1;
            this.mask = mask<<shift;
        }

        private ValueEntry(String name, int shift, int size, int mask) {
            super(name);
            this.shift = (byte) shift;
            this.size = size;
            this.mask = mask<<shift;
        }

        @Override
        Pair<Runnable, Node> createDisplay(Entry value, Flags flags, Runnable onChanged, boolean readonly) {
            if (!(value instanceof ValueEntry ve)) throw new ClassCastException("Unexpected parameter type for createDisplay()");
            Node node = DatingProfileValue.spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1<<ve.size, ve.from(flags)), readonly, (observable, oldValue, newValue) -> {
                ve.apply(flags, newValue);
                onChanged.run();
            });
            return new Pair<>(() -> {
                if (!(node instanceof Spinner<?> spinner)) return;
                //noinspection unchecked
                ((Spinner<Integer>)spinner).getValueFactory().setValue(ve.from(flags));
            }, new HBox(0,
                    new Label(ve.name), node
            ));
        }

        public int from(Flags flags) {
            return (flags.value & mask) >> shift;
        }

        public void apply(Flags flags, int value) {
            value <<= shift;
            if ((value & mask) != value) flags.value |= mask; // if inputted value doesn't fit in mask, go maximum
            else flags.value = (flags.value & ~mask) | value;
            flags.onUpdate(this);
        }
    }

    public static final class EnumEntry extends Entry {
        private final ValueEntry wrappedvalue;
        public final FlagsEnum defaultValue;
        public final SortedMap<Integer, FlagsEnum> values;

        public EnumEntry(FlagsEnum def, FlagsEnum[] values, int shift, int size) {
            super(null);
            this.defaultValue = def;
            SortedMap<Integer, FlagsEnum> map = new TreeMap<>();
            for (FlagsEnum val:values) map.put(val.getValue(), val);
            this.values = map;
            this.wrappedvalue = new ValueEntry(null, shift, size);
        }

        public EnumEntry(FlagsEnum def, FlagsEnum[] values, int shift, int size, int mask) {
            super(null);
            this.defaultValue = def;
            SortedMap<Integer, FlagsEnum> map = new TreeMap<>();
            for (FlagsEnum val:values) map.put(val.getValue(), val);
            this.values = map;
            this.wrappedvalue = new ValueEntry(null, shift, size, mask);
        }

        @Override
        Pair<Runnable, Node> createDisplay(Entry value, Flags flags, Runnable onChanged, boolean readonly) {
            if (!(value instanceof EnumEntry ee)) throw new ClassCastException("Unexpected parameter type for createDisplay()");
            ComboBox<Flags.FlagsEnum> dropdown = new ComboBox<>(FXCollections.observableList(new ArrayList<>(ee.values.values())));
            dropdown.setValue(ee.from(flags));
            dropdown.setCellFactory(new Callback<>() {
                @Override
                public ListCell<Flags.FlagsEnum> call(ListView<Flags.FlagsEnum> listView) {
                    return new ListCell<>() {
                        @Override
                        public void updateItem(Flags.FlagsEnum item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) setText(item.getSelf().toString());
                        }
                    };
                }
            });
            if (readonly) dropdown.setDisable(true);
            else dropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
                ee.apply(flags, newValue);
                onChanged.run();
            });
            return new Pair<>(() -> {
                dropdown.setValue(ee.from(flags));
            }, dropdown);
        }

        public FlagsEnum from(Flags flags) {
            return this.values.getOrDefault(wrappedvalue.from(flags)<<wrappedvalue.shift, defaultValue);
        }

        public void apply(Flags flags, FlagsEnum value) {
            wrappedvalue.apply(flags, value.getValue()>>wrappedvalue.shift);
        }
    }

    public interface FlagsEnum {
        int getValue();
        Enum<?> getSelf();
    }
}
