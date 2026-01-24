package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Part implements DatingBachelor {
    protected int UNKNOWN1;
    protected byte[] UNKNOWN2;
    protected Integer TYPE;

    protected byte[] BYTE_DATA;

    protected final TexturesBlock.TexturesData textures;
    protected final MaterialsBlock.MaterialsData materials;

    public Part(TexturesBlock.TexturesData textures, MaterialsBlock.MaterialsData materials) {
        this.textures = textures;
        this.materials = materials;
    }

    public static Part read(BlockReader reader) {
        int un1 = reader.readIntLittle();
        byte[] un2 = reader.readBytes(reader.readIntLittle());

        int type = reader.readIntLittle();
        byte typeFirst = (byte) (type & 0xFF);
        if (typeFirst == 0x03 || typeFirst == 0x08) { // TODO: Look for all of these cases.
            reader.move(-4);
            Part part = new PartEmpty();
            part.UNKNOWN1 = un1;
            part.UNKNOWN2 = un2;
            part.TYPE = null;
            return part;
        } else if ((type & 0xFF) != 0x01) {
            throw new IllegalStateException("Unknown type 0x"+Integer.toHexString(type & 0xFF).toUpperCase()+" at 0x"+Integer.toHexString(reader.getTruePointer()-4).toUpperCase()+" - You can ignore this exception (for now).");
        }
        reader = reader.segment(reader.readInt());
        reader.setLittleEndian(true);
        Part part = null;
        try {
            // These offsets are from the beginning of the segmented reader.
            int texturesOffset = reader.readInt();
            int materialsOffset = reader.readInt();
            int currentOffset = reader.getPointer();

            // TODO: Read textures and materials first before reading object data, it will be cleaner (and I think the game does that anyway).
            TexturesBlock.TexturesData textures;
            if (texturesOffset > 0) {
                reader.seek(texturesOffset-currentOffset);
                textures = new TexturesBlock.TexturesData(reader, false);
            } else textures = null;
            MaterialsBlock.MaterialsData materials;
            if (materialsOffset > 0) {
                reader.seek(materialsOffset-currentOffset);
                materials = new MaterialsBlock.MaterialsData(reader, (textures != null) ? Collections.singletonList(textures.textures) : Collections.emptyList());
            } else materials = null;
            reader.seek(currentOffset);

            part = switch ((type >> 8) & 0xFF) {
                case 0x10 -> new PartCharacter(textures, materials);
                case 0x20 -> new PartObject(textures, materials);
                default -> new PartUndefined(textures, materials);
            };
            part.UNKNOWN1 = un1;
            part.UNKNOWN2 = un2;
            part.TYPE = type;
            int endOffset = (texturesOffset>0 ? texturesOffset : (materialsOffset>0 ? materialsOffset : reader.getSize()));
            BlockReader subReader = reader.segment(endOffset-currentOffset-8);
            part.readData(subReader);
        } catch (Exception e) {
            e.printStackTrace();
            if (part == null) {
                part = new PartEmpty();
                part.UNKNOWN1 = un1;
                part.UNKNOWN2 = un2;
                part.TYPE = type;
            }
        }
        return part;
    }

    /// Reads main mesh data excluding texture and material data.
    protected abstract void readData(BlockReader reader);

    public final void write(BlockWriter writer) {
        writer.writeIntLittle(UNKNOWN1);
        writer.writeIntLittle(UNKNOWN2.length);
        writer.writeBytes(UNKNOWN2);

        if (TYPE == null) return;
        writer = writer.segment();
        writer.writeIntLittle(TYPE);
        writer.setLittleEndian(true);
        writer.writeRawString("\0\0\0\0\0\0\0\0\0\0\0\0"); // Block Size and Texture/Material Offsets - Get filled in later.
        writeData(writer.segment());
        int texPosition;
        if (textures != null) {
            texPosition = writer.getPointer();
            textures.write(writer.segment());
            writer.seek(writer.getSize()); // TODO: Maybe add an "independent pointer" option for writer (and reader) to remove the need for this?
        } else texPosition = 0;
        int matPosition;
        if (materials != null) {
            matPosition = writer.getPointer();
            materials.write(writer.segment());
            writer.seek(writer.getSize());
        } else matPosition = 0;
        int endPosition = writer.getPointer()-8;
        writer.seek(4);
        writer.writeInt(endPosition);
        writer.writeInt(texPosition);
        writer.writeInt(matPosition);
    }

    /// Writes main mesh data excluding texture and material data.
    protected abstract void writeData(BlockWriter writer);

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> list = new ArrayList<>();
        if (textures != null) list.add(new SubBachelorPreviewEntry("Textures",
                () -> textures.textureList
        ));
        if (materials != null) list.add(new SubBachelorPreviewEntry("Materials",
                () -> materials.materialList
        ));
        return list;
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        List<DatingBachelor> list = new ArrayList<>();
        if (textures != null) list.add(new TexturesWrapper(textures));
        if (materials != null) list.add(new MaterialsWrapper(materials));
        return list;
    }

    @Override
    public abstract String toString();

    private record TexturesWrapper(TexturesBlock.TexturesData textures) implements DatingBachelor {
        @Override
        public List<? extends DatingProfileEntry<?>> getDatingProfile() {
            return List.of(new SubBachelorPreviewEntry(
                    () -> textures.textureList
            ));
        }

        @Override
        public List<? extends DatingBachelor> getSubBachelors() {
            return textures.textureList;
        }

        @Override
        public String toString() {
            return "Textures";
        }
    }

    private record MaterialsWrapper(MaterialsBlock.MaterialsData materials) implements DatingBachelor {
        @Override
        public List<? extends DatingProfileEntry<?>> getDatingProfile() {
            return List.of(new SubBachelorPreviewEntry(
                    () -> materials.materialList
            ));
        }

        @Override
        public List<? extends DatingBachelor> getSubBachelors() {
            return materials.materialList;
        }

        @Override
        public String toString() {
            return "Materials";
        }
    }
}
