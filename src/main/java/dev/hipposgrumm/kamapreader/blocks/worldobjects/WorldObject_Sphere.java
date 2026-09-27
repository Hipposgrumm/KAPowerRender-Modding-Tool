package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;

import java.util.List;

public class WorldObject_Sphere extends WorldObject_Draw {
    private float radius;
    private int spherematerial;

    public WorldObject_Sphere() {
        super(ObType.SPHERE);
    }

    protected WorldObject_Sphere(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) {
        super.read(reader);
        radius = reader.readFloat();
        spherematerial = reader.readInt();
    }

    @Override
    public int size() {
        return super.size()+8;
    }

    @Override
    public void write(BlockWriter writer) {
        super.write(writer);
        writer.writeFloat(radius);
        writer.writeInt(spherematerial);
    }

    @Override
    public List<DatingProfileEntry> getDatingProfile() {
        List<DatingProfileEntry> items = super.getDatingProfile();
        items.add(DatingProfileEntry.simple("Radius",
                () -> radius,
                r -> radius = r
        ));
        items.add(DatingProfileEntry.simple("Sphere Material",
                () -> spherematerial,
                sm -> spherematerial = sm
        ));
        return items;
    }
}
