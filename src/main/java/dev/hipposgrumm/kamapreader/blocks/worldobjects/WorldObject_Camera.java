package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.structs.ASPECTRATIO;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_VIEWPORT;

import java.io.IOException;
import java.util.List;

public class WorldObject_Camera extends WorldObject_Position {
    private int cam_un1;
    private PR_VIEWPORT viewport;
    private float nearclip;
    private float farclip;
    private ASPECTRATIO fov; // Second value is used for aspect ratio calculation (X/Y).
    private int cam_un2;
    private Number[] cam_un3;
    private float[] cam_un4;

    public WorldObject_Camera() {
        super(ObType.CAMERA);
    }

    protected WorldObject_Camera(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) throws IOException {
        super.read(reader);
        cam_un1 = reader.readInt();
        viewport = new PR_VIEWPORT(reader.readInt(), reader.readInt(), reader.readInt(), reader.readInt());
        nearclip = reader.readFloat();
        farclip = reader.readFloat();
        fov = new ASPECTRATIO(reader.readFloat(), reader.readFloat());
        cam_un2 = reader.readInt();

        cam_un3 = new Number[] {
                reader.readFloat(),
                reader.readFloat(),
                reader.readFloat(),
                reader.readFloat(),
                reader.readInt(),
                reader.readInt()
        };

        cam_un4 = new float[] {
                reader.readFloat(),
                reader.readFloat(),
                reader.readFloat(),
                reader.readFloat()
        };
    }

    @Override
    public int size() {
        return super.size()+72;
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        super.write(writer);
        writer.writeInt(cam_un1);
        writer.writeInt(viewport.X);
        writer.writeInt(viewport.Y);
        writer.writeInt(viewport.WIDTH);
        writer.writeInt(viewport.HEIGHT);
        writer.writeFloat(nearclip);
        writer.writeFloat(farclip);
        writer.writeFloat(fov.X);
        writer.writeFloat(fov.Y);
        writer.writeInt(cam_un2);
        writer.writeFloat((Float) cam_un3[0]);
        writer.writeFloat((Float) cam_un3[1]);
        writer.writeFloat((Float) cam_un3[2]);
        writer.writeFloat((Float) cam_un3[3]);
        writer.writeInt((Integer) cam_un3[4]);
        writer.writeInt((Integer) cam_un3[5]);
        writer.writeFloat(cam_un4[0]);
        writer.writeFloat(cam_un4[1]);
        writer.writeFloat(cam_un4[2]);
        writer.writeFloat(cam_un4[3]);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();
        items.add(new DatingProfileEntry<>("Viewport",
                () -> viewport,
                v -> viewport = v
        ));
        items.add(new DatingProfileEntry<>("Nearclip",
                () -> nearclip,
                c -> nearclip = c
        ));
        items.add(new DatingProfileEntry<>("Farclip",
                () -> farclip,
                c -> farclip = c
        ));
        items.add(new DatingProfileEntry<>("FOV",
                () -> fov,
                f -> fov = f
        ));
        return items;
    }
}
