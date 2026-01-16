package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.EnumChoices;
import dev.hipposgrumm.kamapreader.util.types.structs.FLOATCOLOR_RGB;

import java.io.IOException;
import java.util.List;

public class WorldObject_Light extends WorldObject_Position {
    private LightType lightmode;
    private float coneInner;
    private float coneOuter;
    private float falloff;
    private float strength;
    private FLOATCOLOR_RGB color;
    private int light_unused;

    public WorldObject_Light() {
        super(ObType.LIGHT);
    }

    protected WorldObject_Light(ObType type) {
        super(type);
    }

    @Override
    public void read(BlockReader reader) throws IOException {
        super.read(reader);
        lightmode = switch (reader.readInt()) {
            case 0 -> LightType.DIRECTIONAL;
            case 1 -> LightType.POINT;
            case 2 -> LightType.SPOT;
            case -1 -> LightType.UNSET;
            default -> LightType.UNKNOWN;
        };
        coneInner = reader.readFloat();
        coneOuter = reader.readFloat();
        falloff = reader.readFloat();
        strength = reader.readFloat();
        color = new FLOATCOLOR_RGB(reader.readFloat(), reader.readFloat(), reader.readFloat());
        light_unused = reader.readInt(); // seemingly unused
    }

    @Override
    public int size() {
        return super.size()+36;
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        super.write(writer);
        writer.writeInt(lightmode.identifier);
        writer.writeFloat(coneInner);
        writer.writeFloat(coneOuter);
        writer.writeFloat(falloff);
        writer.writeFloat(strength);
        writer.writeFloat(color.R);
        writer.writeFloat(color.G);
        writer.writeFloat(color.B);
        writer.writeInt(light_unused);
    }

    @Override
    public List<DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> items = super.getDatingProfile();
        items.add(new DatingProfileEntry<>("Lighting Mode",
                () -> lightmode,
                m -> lightmode = m
        ));
        items.add(new DatingProfileEntry<>("Inner Cone (Spotlight)",
                () -> coneInner,
                c -> coneInner = c
        ));
        items.add(new DatingProfileEntry<>("Outer Cone (Spotlight)",
                () -> coneOuter,
                c -> coneOuter = c
        ));
        items.add(new DatingProfileEntry<>("Falloff",
                () -> falloff,
                f -> falloff = f
        ));
        items.add(new DatingProfileEntry<>("Strength",
                () -> strength,
                s -> strength = s
        ));
        items.add(new DatingProfileEntry<>("Color",
                () -> color
        ));
        return items;
    }

    public enum LightType implements EnumChoices {
        UNKNOWN(-1),
        UNSET(-1),
        DIRECTIONAL(0),
        POINT(1),
        SPOT(2);

        public final int identifier;

        LightType(int identifier) {
            this.identifier = identifier;
        }

        @Override
        public Enum<? extends EnumChoices> getSelf() {
            return this;
        }

        @Override
        public List<? extends Enum<? extends EnumChoices>> choices() {
            return List.of(UNSET, DIRECTIONAL, POINT, SPOT);
        }
    }
}
