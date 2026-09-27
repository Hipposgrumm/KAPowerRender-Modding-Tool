package dev.hipposgrumm.kamapreader.blocks.worldobjects;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.EnumChoices;
import dev.hipposgrumm.kamapreader.util.types.structs.FLOATCOLOR_RGB;

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
    public void read(BlockReader reader) {
        super.read(reader);
        int lighttype = reader.readInt();
        lightmode = switch (lighttype) {
            case 1 -> LightType.POINT;
            case 2 -> LightType.SPOT;
            case 3 -> LightType.DIRECTIONAL;
            case -1 -> LightType.UNSET;
            default -> {
                System.err.println("No type of "+lighttype+" in enum LightType");
                yield LightType.UNKNOWN;
            }
        };
        coneInner = reader.readFloat();
        coneOuter = reader.readFloat();
        falloff = reader.readFloat();
        strength = reader.readFloat();
        color = new FLOATCOLOR_RGB(reader);
        light_unused = reader.readInt(); // seemingly unused
    }

    @Override
    public int size() {
        return super.size()+36;
    }

    @Override
    public void write(BlockWriter writer) {
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
    public List<DatingProfileEntry> getDatingProfile() {
        List<DatingProfileEntry> items = super.getDatingProfile();
        items.add(DatingProfileEntry.simple("Lighting Mode",
                () -> lightmode,
                m -> lightmode = m
        ));
        items.add(DatingProfileEntry.simple("Inner Cone (Spotlight)",
                () -> coneInner,
                c -> coneInner = c
        ));
        items.add(DatingProfileEntry.simple("Outer Cone (Spotlight)",
                () -> coneOuter,
                c -> coneOuter = c
        ));
        items.add(DatingProfileEntry.simple("Falloff",
                () -> falloff,
                f -> falloff = f
        ));
        items.add(DatingProfileEntry.simple("Strength",
                () -> strength,
                s -> strength = s
        ));
        items.add(new DatingProfileEntry("Color", false, color));
        return items;
    }

    public enum LightType implements EnumChoices {
        UNKNOWN(-1),
        UNSET(-1),
        POINT(1),
        SPOT(2),
        DIRECTIONAL(3);

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
            return List.of(UNSET, POINT, SPOT, DIRECTIONAL);
        }
    }
}
