package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorldObject implements DatingBachelor {
    private final ObType type;
    private short un1;
    private short childCount;
    private final ArrayList<Child> children = new ArrayList<>();
    private short un2;
    private int uid;
    private int un3;

    public WorldObject() {
        this.type = ObType.UNKNOWN;
    }

    protected WorldObject(ObType type) {
        this.type = type;
    }

    public void read(BlockReader reader) throws IOException {
        // Assume type has already been read.
        un1 = reader.readShort();
        childCount = reader.readShort();
        children.ensureCapacity(childCount);
        un2 = reader.readShort();
        uid = reader.readInt();
        un3 = reader.readInt();
    }

    public final void readChildren(BlockReader reader) throws IOException {
        for (int i=0;i<childCount;i++)
            children.add(new Child(
                    reader.readBlockType(),
                    reader.readShort(),
                    reader.readShort(),
                    reader.readInt()
            ));
    }

    public int size() {
        return 16;
    }

    public void write(BlockWriter writer) throws IOException {
        writer.writeShort(type.identifier);
        writer.writeShort(un1);
        writer.writeShort((short) children.size());
        writer.writeShort(un2);
        writer.writeInt(uid);
        writer.writeInt(un3);
    }

    public final void writeChildren(BlockWriter writer) throws IOException {
        for (Child child:children) {
            writer.writeRawString(child.source);
            writer.writeShort(child.index);
            writer.writeShort(child.flags);
            writer.writeInt(child.ptr);
        }
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = new ArrayList<>();
        items.add(new DatingProfileEntry.ReadOnly<>("Type",
                () -> type
        ));
        return items;
    }

    @Override
    public String toString() {
        return type.name;
    }

    public record Child(String source, short index, short flags, int ptr) {}

    public enum ObType {
        UNKNOWN("Unknown", -1),
        BASE("Base", 0),
        POSITION("Position", 1),
        DRAW("Draw", 2),
        ANIM("Anim", 3),
        CAMERA("Camera", 4),
        LIGHT("Light", 5),
        WORLD("World",6),
        SPHERE("Sphere", 7),
        EMITTER("Emitter", 8),
        SPLINE("Spline", 9),
        GROUP("Group", 10);

        public final String name;
        public final short identifier;

        ObType(String name, int identifier) {
            this.name = name;
            this.identifier = (short) identifier;
        }
    }
}
