package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.io.IOException;
import java.util.List;

public class WorldObject_Spline extends WorldObject_Position {
    private int unused_spline;

    public WorldObject_Spline() {
        super(ObType.SPLINE);
    }

    protected WorldObject_Spline(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) throws IOException {
        super.read(reader);
        unused_spline = reader.readInt(); // seemingly unused
    }

    @Override
    public int size() {
        return super.size()+4;
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        super.write(writer);
        writer.writeInt(unused_spline);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();

        return items;
    }
}
