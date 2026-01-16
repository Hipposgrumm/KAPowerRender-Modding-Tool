package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.Flags;

import java.io.IOException;
import java.util.List;

public class WorldObject_Base extends WorldObject {
    private ObjectFlags flags;
    private int base_un1;
    private int base_un2;
    private int base_un3;
    private int script;
    private int base_un5;

    public WorldObject_Base() {
        super(ObType.BASE);
    }

    protected WorldObject_Base(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) throws IOException {
        super.read(reader);
        flags = new ObjectFlags(reader.readInt());
        base_un1 = reader.readInt();
        base_un2 = reader.readInt();
        base_un3 = reader.readInt();
        script = reader.readInt();
        base_un5 = reader.readInt();
    }

    @Override
    public int size() {
        return super.size()+24;
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        super.write(writer);
        writer.writeInt(flags.getValue());
        writer.writeInt(base_un1);
        writer.writeInt(base_un2);
        writer.writeInt(base_un3);
        writer.writeInt(script);
        writer.writeInt(base_un5);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();
        items.add(new DatingProfileEntry<>("Flags",
                () -> flags
        ));
        items.add(new DatingProfileEntry<>("Script",
                () -> script,
                s -> script = s
        ));
        return items;
    }

    public static class ObjectFlags extends Flags {
        public static final BoolEntry VISIBLE = new BoolEntry("Visible", 0);

        public ObjectFlags(int value) {
            super(value);
        }

        @Override
        public Entry[] getEntries() {
            return new Entry[] {VISIBLE};
        }
    }
}
