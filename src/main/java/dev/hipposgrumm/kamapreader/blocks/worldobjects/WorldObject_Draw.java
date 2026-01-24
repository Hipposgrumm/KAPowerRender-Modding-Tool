package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.util.List;

public class WorldObject_Draw extends WorldObject_Position {
    private int draw_un1;
    private int rendermethod;

    public WorldObject_Draw() {
        super(ObType.DRAW);
    }

    protected WorldObject_Draw(ObType type) {
        super(type);
    }

    @Override
    public int size() {
        return super.size()+8;
    }

    @Override
    public void read(BlockReader reader) {
        super.read(reader);
        draw_un1 = reader.readInt();
        rendermethod = reader.readInt();
    }

    @Override
    public void write(BlockWriter writer) {
        super.write(writer);
        writer.writeInt(draw_un1);
        writer.writeInt(rendermethod);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();
        items.add(new DatingProfileEntry<>("RenderMethod",
                () -> rendermethod,
                rm -> rendermethod = rm
        ));
        return items;
    }
}
