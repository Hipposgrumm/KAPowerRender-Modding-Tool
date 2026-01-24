package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_POINT;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_QUATERNION;

import java.util.List;

public class WorldObject_Position extends WorldObject_Base {
    private PR_QUATERNION rotation;
    private PR_POINT position;
    private PR_POINT scale;
    private int position_un1;
    private int position_un2;
    private int position_un3;

    public WorldObject_Position() {
        super(ObType.POSITION);
    }

    protected WorldObject_Position(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) {
        super.read(reader);
        rotation = new PR_QUATERNION(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());
        position = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
        scale = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
        position_un1 = reader.readInt();
        position_un2 = reader.readInt();
        position_un3 = reader.readInt();
    }

    @Override
    public int size() {
        return super.size()+52;
    }

    @Override
    public void write(BlockWriter writer) {
        super.write(writer);
        writer.writeFloat(rotation.X);
        writer.writeFloat(rotation.Y);
        writer.writeFloat(rotation.Z);
        writer.writeFloat(rotation.W);
        writer.writeFloat(position.X);
        writer.writeFloat(position.Y);
        writer.writeFloat(position.Z);
        writer.writeFloat(scale.X);
        writer.writeFloat(scale.Y);
        writer.writeFloat(scale.Z);
        writer.writeInt(position_un1);
        writer.writeInt(position_un2);
        writer.writeInt(position_un3);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();
        items.add(new DatingProfileEntry<>("Rotation",
                () -> rotation
        ));
        items.add(new DatingProfileEntry<>("Position",
                () -> position
        ));
        items.add(new DatingProfileEntry<>("Scale",
                () -> scale
        ));
        return items;
    }
}
