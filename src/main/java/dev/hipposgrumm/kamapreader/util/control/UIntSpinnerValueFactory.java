package dev.hipposgrumm.kamapreader.util.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.LongStringConverter;

public class UIntSpinnerValueFactory extends SpinnerValueFactory<Long> {
    public UIntSpinnerValueFactory(long min, long max) {
        this(min, max, min, 1);
    }

    public UIntSpinnerValueFactory(long min, long max, long initial) {
        this(min, max, initial, 1);
    }

    public UIntSpinnerValueFactory(long min, long max, long initial, int step) {
        if (min < 0) min = 0;
        if (max > 0xFFFFFFFFL) max = 0xFFFFFFFFL;
        setMin(min);
        setMax(max);
        setAmountToStepBy(step);
        setConverter(new LongStringConverter());

        valueProperty().addListener((o, oldValue, newValue) -> {
            // when the value is set, we need to react to ensure it is a
            // valid value (and if not, blow up appropriately)
            if (newValue < getMin()) {
                setValue(getMin());
            } else if (newValue > getMax()) {
                setValue(getMax());
            }
        });
        setValue(initial >= min && initial <= max ? initial : min);
    }

    private final LongProperty min = new SimpleLongProperty(this, "min") {
        @Override
        protected void invalidated() {
            Long currentValue = UIntSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            long newMin = get();
            if (newMin > getMax()) {
                setMin(getMax());
                return;
            }

            if (currentValue < newMin) {
                UIntSpinnerValueFactory.this.setValue(newMin);
            }
        }
    };

    public final void setMin(long value) {
        min.set(Math.min(Math.max(value,0L),0xFFFFFFFFL));
    }

    public final long getMin() {
        return min.get();
    }

    public final LongProperty minProperty() {
        return min;
    }

    private final LongProperty max = new SimpleLongProperty(this, "max") {
        @Override
        protected void invalidated() {
            Long currentValue = UIntSpinnerValueFactory.this.getValue();
            if (currentValue == null) {
                return;
            }

            long newMax = get();
            if (newMax < getMin()) {
                setMax(getMin());
                return;
            }

            if (currentValue > newMax) {
                UIntSpinnerValueFactory.this.setValue(newMax);
            }
        }
    };

    public final void setMax(long value) {
        max.set(Math.min(Math.max(value,0L),0xFFFFFFFFL));
    }

    public final long getMax() {
        return max.get();
    }

    public final LongProperty maxProperty() {
        return max;
    }

    private final IntegerProperty amountToStepBy = new SimpleIntegerProperty(this, "amountToStepBy");

    public final void setAmountToStepBy(int value) {
        amountToStepBy.set(value);
    }

    public final int getAmountToStepBy() {
        return amountToStepBy.get();
    }

    public final IntegerProperty amountToStepByProperty() {
        return amountToStepBy;
    }

    @Override
    public void increment(int steps) {
        final long min = getMin();
        final long max = getMax();
        final long currentValue = getValue();
        final long newIndex = currentValue + (long) steps * getAmountToStepBy();
        setValue(newIndex <= max ? newIndex : (isWrapAround() ? min : max));
    }

    @Override
    public void decrement(int steps) {
        final long min = getMin();
        final long max = getMax();
        final long newIndex = getValue() - (long) steps * getAmountToStepBy();
        setValue(newIndex >= min ? newIndex : (isWrapAround() ? max : min));
    }
}