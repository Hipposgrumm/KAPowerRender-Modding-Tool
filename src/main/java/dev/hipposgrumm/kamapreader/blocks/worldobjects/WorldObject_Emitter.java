package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.util.List;

public class WorldObject_Emitter extends WorldObject_Position {
    private int emitter_unused;

    public WorldObject_Emitter() {
        super(ObType.EMITTER);
    }

    protected WorldObject_Emitter(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) {
        super.read(reader);
        emitter_unused = reader.readInt(); // seemingly unused
    }

    @Override
    public int size() {
        return super.size()+4;
    }

    @Override
    public void write(BlockWriter writer) {
        super.write(writer);
        writer.writeInt(emitter_unused);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();

        return items;
    }
}
