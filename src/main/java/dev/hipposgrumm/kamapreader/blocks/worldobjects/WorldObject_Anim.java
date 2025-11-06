package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.io.IOException;
import java.util.List;

public class WorldObject_Anim extends WorldObject_Draw {
    private int[] anim_un1;
    private int[] anim_un2;

    public WorldObject_Anim() {
        super(ObType.ANIM);
    }

    protected WorldObject_Anim(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) throws IOException {
        super.read(reader);
        anim_un1 = new int[] {
                reader.readInt(),
                reader.readInt(),
                reader.readInt(),
                reader.readInt()
        };
        anim_un2 = new int[] {
                reader.readInt(),
                reader.readInt(),
                reader.readInt(),
                reader.readInt()
        };
    }

    @Override
    public int size() {
        return super.size()+32;
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        super.write(writer);
        writer.writeInt(anim_un1[0]);
        writer.writeInt(anim_un1[1]);
        writer.writeInt(anim_un1[2]);
        writer.writeInt(anim_un1[3]);
        writer.writeInt(anim_un2[0]);
        writer.writeInt(anim_un2[1]);
        writer.writeInt(anim_un2[2]);
        writer.writeInt(anim_un2[3]);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();

        return items;
    }
}
