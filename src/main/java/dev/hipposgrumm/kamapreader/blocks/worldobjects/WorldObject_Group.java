package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.io.IOException;
import java.util.List;

public class WorldObject_Group extends WorldObject_Position {
    private int group_unused;

    public WorldObject_Group() {
        super(ObType.GROUP);
    }

    protected WorldObject_Group(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) throws IOException {
        super.read(reader);
        group_unused = reader.readInt(); // seemingly unused
    }

    @Override
    public int size() {
        return super.size()+4;
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        super.write(writer);
        writer.writeInt(group_unused);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();

        return items;
    }
}
