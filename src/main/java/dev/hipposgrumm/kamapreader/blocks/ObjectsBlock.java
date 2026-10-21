package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.worldobjects.*;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;

import java.util.ArrayList;
import java.util.List;

public class ObjectsBlock extends Block.ArrayChecked {
    public List<WorldObject> objects = new ArrayList<>();
    private WorldObject[] objectsArrayBuilder;

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        objectsArrayBuilder = new WorldObject[count];
        for (int i=0;i<count;i++) objectsArrayBuilder[i] = new WorldObjectPlaceholder();
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading Object "+index+"/"+objectsArrayBuilder.length);
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
        objectsArrayBuilder[index] = object;
    }

    @Override
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {
        objects = new ArrayList<>(List.of(objectsArrayBuilder));
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        return objects.size();
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        WorldObject object = objects.get(index);
        if (object instanceof PlaceholderValue) return false;

        progress.setMessage("Writing Object "+index+"/"+objects.size());
        object.write(writer);
        object.writeChildren(writer);
        return true;
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(getMetadataDatingProfileEntry());
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

    private static final class WorldObjectPlaceholder extends WorldObject implements PlaceholderValue {}
}
