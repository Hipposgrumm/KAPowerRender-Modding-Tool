package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.util.List;

public class WorldObject_World extends WorldObject_Draw {
    private float[] world_un1;
    private int world_unused;

    public WorldObject_World() {
        super(ObType.WORLD);
    }

    protected WorldObject_World(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) {
        super.read(reader);
        world_un1 = new float[]{
                reader.readFloat(),
                reader.readFloat(),
                reader.readFloat(),
                reader.readFloat()
        };
        world_unused = reader.readInt(); // seemingly unused
    }

    @Override
    public int size() {
        return super.size()+20;
    }

    @Override
    public void write(BlockWriter writer) {
        super.write(writer);
        writer.writeFloat(world_un1[0]);
        writer.writeFloat(world_un1[1]);
        writer.writeFloat(world_un1[2]);
        writer.writeFloat(world_un1[3]);
        writer.writeInt(world_unused);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();

        return items;
    }
}
