package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.blocks.worldobjects.*;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjectsBlock extends Block {
    private ResourceCheckBlock rsck;
    private ArCkBlock arck;

    private final List<WorldObject> objects = new ArrayList<>();

    @Override
    protected void read(BlockReader reader) throws IOException {
        rsck = ResourceCheckBlock.read(reader, "ObFm");
        arck = ArCkBlock.read(reader, "ObFm");

        while (reader.getRemaining() > 0) {
            short obj = reader.readShort();
            WorldObject object = switch(obj) {
                case 0 -> new WorldObject_Base();
                case 1 -> new WorldObject_Position();
                case 2 -> new WorldObject_Draw();
                case 3 -> new WorldObject_Anim();
                case 4 -> new WorldObject_Camera();
                case 5 -> new WorldObject_Light();
                case 6 -> new WorldObject_World();
                case 7 -> new WorldObject_Sphere();
                case 8 -> new WorldObject_Emitter();
                case 9 -> new WorldObject_Spline();
                case 10 -> new WorldObject_Group();
                default -> new WorldObject();
            };
            object.read(reader);
            object.readChildren(reader);
            objects.add(object);
        }
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        rsck.write(writer.segment());
        arck.write(writer.segment());

        for (WorldObject object:objects) {
            object.write(writer);
            object.writeChildren(writer);
        }
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return null;
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return objects;
    }

    @Override
    public String getBlockType() {
        return "ObFm";
    }

    @Override
    public String toString() {
        return "WorldObjects";
    }
}
