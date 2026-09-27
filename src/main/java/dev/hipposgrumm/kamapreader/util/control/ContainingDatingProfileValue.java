package dev.hipposgrumm.kamapreader.util.control;

import dev.hipposgrumm.kamapreader.FirstThing;
import javafx.scene.Node;

import java.util.Objects;

/// Note: Classes implementing this should have an implemented equals() function.
public class ContainingDatingProfileValue<T> implements DatingProfileValue {
    private final Containable<T> mutable;
    private final Containable<? extends T> original;

    public ContainingDatingProfileValue(Containable<T> object) {
        if (object == null) {
            this.mutable = null;
            this.original = null;
            return;
        }
        this.mutable = object;
        this.original = object.makeCopy();
        if (original == null) throw new NullPointerException("makeCopy() in "+mutable.getClass()+" returns null!");
        if (mutable.getClass() != original.getClass()) throw new ClassCastException("makeCopy() in "+mutable.getClass()+" does not return same class type! (got "+original.getClass()+")");
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        return mutable.createDisplay(controller, onChanged, readonly);
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(mutable, original);
    }

    public interface Containable<T> {
        Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly);

        /// This should create a copy of the data identical to the current state of the original.
        Containable<? extends T> makeCopy();
    }
}
