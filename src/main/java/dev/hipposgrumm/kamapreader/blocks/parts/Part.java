package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.PROReader;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.ViewerAppHandle;
import dev.hipposgrumm.kamapreader.util.types.Material;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;
import dev.hipposgrumm.kamapreader.util.types.structs.BITMAP_TEXTURE;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_FACE;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_VERTEX;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

        if (reader.getRemaining() == 0) {
            Part part = new PartEmpty();
            part.UNKNOWN1 = un1;
            part.UNKNOWN2 = un2;
            part.TYPE = null;
            return part;
        }
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
            System.err.println("Unknown type 0x"+Integer.toHexString(type & 0xFF).toUpperCase()+" at 0x"+Integer.toHexString(reader.getTruePointer()-4).toUpperCase()+" - You can ignore this exception (for now).");
            return null;
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
            } else materials = new FillableMaterials();
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
        } else texPosition = 0;
        int matPosition;
        if (materials != null) {
            matPosition = writer.getPointer();
            materials.write(writer.segment());
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
        if (materials != null && !(materials instanceof FillableMaterials)) list.add(new SubBachelorPreviewEntry("Materials",
                () -> materials.materialList
        ));
        return list;
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        List<DatingBachelor> list = new ArrayList<>();
        if (textures != null) list.add(new TexturesWrapper(textures));
        if (materials != null && !(materials instanceof FillableMaterials))
            list.add(new MaterialsWrapper(materials));
        return list;
    }

    public void fillMaterials(Map<Integer, Material> materials) {}

    @Override
    public abstract String toString();

    protected void sendMaterials() {
        Map<Integer, Texture> materialTextures = new HashMap<>();
        ByteBuffer[] materialdatas = new ByteBuffer[materials.materials.size()];
        int matsize = 0;
        int i=0;for (Map.Entry<Integer, MaterialsBlock.MaterialRef> entry:materials.materials.entrySet()) {
            Material mat = entry.getValue().material();
            byte[] matname = mat.toString().getBytes(StandardCharsets.US_ASCII);
            int cap = matname.length+246;
            ByteBuffer data = ByteBuffer.allocate(cap);
            data.putInt(entry.getKey());    // + 4
            for (byte b:matname) data.put(b);
            data.put((byte)'\00');          // + 1
            for (int j=0;j<7;j++) {         // +28 (4*7)
                if (mat.textures[j] != null) {
                    int uid = mat.textures[j].getUid().get();
                    materialTextures.put(uid, mat.textures[j]);
                    data.putInt(uid);
                } else data.putInt(0);
            }

            data.putFloat(mat.color.R); data.putFloat(mat.color.G); data.putFloat(mat.color.B); data.putFloat(mat.color.A);                                                 // +16
            data.putFloat(mat.Bump00); data.putFloat(mat.Bump01); data.putFloat(mat.Bump10); data.putFloat(mat.Bump11); data.putFloat(mat.BumpScale);                       // +20
            data.putFloat(mat.specular.R); data.putFloat(mat.specular.G); data.putFloat(mat.specular.B); data.putFloat(mat.specular.A); data.putFloat(mat.specular_power);  // +20
            data.put((byte) (mat.doublesided ? 1 : 0));                                                                                                                     // + 1

            data.put((byte) mat.renderstyle.enableZ.identifier);        // # +9
            data.put((byte)(mat.renderstyle.enableWriteZ ? 1 : 0));     // |
            data.put((byte)(mat.renderstyle.lighting ? 1 : 0));         // |
            data.put((byte)(mat.renderstyle.vertexColors ? 1 : 0));     // |
            data.put((byte)(mat.renderstyle.enableAlphaBlend ? 1 : 0)); // |
            data.put((byte)(mat.renderstyle.enableAlphaTest ? 1 : 0));  // |
            data.put((byte)(mat.renderstyle.enableSpecular ? 1 : 0));   // v
            data.putShort((short) mat.renderstyle.alphaRef);            // _
            data.putInt(mat.renderstyle.textureFactor.color);           // +4
            data.putInt(mat.renderstyle.blendFactor.color);             // +4

            data.put((byte) mat.renderstyle.shadeMode.identifier);              // # +11
            data.put((byte) mat.renderstyle.srcBlend.identifier);               // |
            data.put((byte) mat.renderstyle.destBlend.identifier);              // |
            data.put((byte) mat.renderstyle.zFunc.identifier);                  // |
            data.put((byte) mat.renderstyle.alphaFunc.identifier);              // |
            data.put((byte) mat.renderstyle.diffuseMaterialSource.identifier);  // |
            data.put((byte) mat.renderstyle.specularMaterialSource.identifier); // |
            data.put((byte) mat.renderstyle.ambientMaterialSource.identifier);  // |
            data.put((byte) mat.renderstyle.emissiveMaterialSource.identifier); // |
            data.put((byte) mat.renderstyle.colorWriteFlags.getValue());        // v
            data.put((byte) mat.renderstyle.blendop.identifier);                // _

            for (int j=0;j<8;j++) {                                                                 // +128 (16*8)
                data.put((byte) mat.renderstyle.textureStages[j].colorop.identifier);               // # +9
                data.put((byte) mat.renderstyle.textureStages[j].alphaop.identifier);               // |
                data.put((byte) mat.renderstyle.textureStages[j].colorarg1.getValue());             // |
                data.put((byte) mat.renderstyle.textureStages[j].colorarg2.getValue());             // |
                data.put((byte) mat.renderstyle.textureStages[j].alphaarg1.getValue());             // |
                data.put((byte) mat.renderstyle.textureStages[j].alphaarg2.getValue());             // |
                data.put((byte) mat.renderstyle.textureStages[j].texcoordindex.identifier);         // v
                data.putShort((short) mat.renderstyle.textureStages[j].transformflags.getValue());  // _

                data.put((byte) mat.renderstyle.samplerStages[j].addressU.identifier);              // +1
                data.put((byte) mat.renderstyle.samplerStages[j].addressV.identifier);              // +1
                data.put((byte) mat.renderstyle.samplerStages[j].addressW.identifier);              // +1
                data.putInt((short) mat.renderstyle.samplerStages[j].bordercolor.color);            // +4
            }
            data.position(0);

            matsize += cap;
            materialdatas[i++] = data;
        }

        ByteBuffer[] texturedatas = new ByteBuffer[materialTextures.size()];
        int texsize = 0;
        i=0;for (Map.Entry<Integer, Texture> entry:materialTextures.entrySet()) {
            BITMAP_TEXTURE texture = entry.getValue().getViewable();
            int cap = (texture.getImage().length*4)+12;
            ByteBuffer data = ByteBuffer.allocate(cap);
            data.putInt(entry.getKey());
            data.putInt(texture.WIDTH);
            data.putInt(texture.HEIGHT);
            for (int pixel:texture.getImage()) data.putInt(pixel);
            data.position(0);
            texsize += cap;
            texturedatas[i++] = data;
        }
        ByteBuffer buffer1 = ByteBuffer.allocate(texsize);
        for (ByteBuffer data:texturedatas) buffer1.put(data);
        ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.LOAD_TEXTURES, buffer1.array());

        ByteBuffer buffer2 = ByteBuffer.allocate(matsize);
        for (ByteBuffer data:materialdatas) buffer2.put(data);
        ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.LOAD_MATERIALS, buffer2.array());
    }

    protected void sendMeshData(String name, PROReader.PROData mesh_data) {
        sendMaterials();
        ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.MODEL_START, (name+"\0").getBytes(StandardCharsets.US_ASCII));
        try {
            for (PROReader.PROData.Segment seg:mesh_data.Segments) {
                ByteBuffer buffer = ByteBuffer.allocate(
                        9 +
                                (seg.Name.length()) +
                                (seg.Vertices.length * 24) +
                                (seg.Faces.length * 62)
                );
                buffer.put(seg.Name.getBytes(StandardCharsets.US_ASCII));
                buffer.put((byte) 0);
                buffer.putInt(seg.Vertices.length);
                for (PR_VERTEX vert : seg.Vertices) {
                    buffer.putFloat(vert.Position.X);
                    buffer.putFloat(vert.Position.Y);
                    buffer.putFloat(vert.Position.Z);
                    buffer.putFloat(vert.NX);
                    buffer.putFloat(vert.NY);
                    buffer.putFloat(vert.NZ);
                }
                buffer.putInt(seg.Faces.length);
                for (PR_FACE face : seg.Faces) {
                    buffer.putInt(materials.materialList.get(face.Material.get()).getUid().get());
                    buffer.putInt(materials.materialList.get(face.BackMaterial.get()).getUid().get());
                    buffer.putInt(face.Vertex1);
                    buffer.putInt(face.Vertex2);
                    buffer.putInt(face.Vertex3);
                    buffer.putFloat(face.U1);
                    buffer.putFloat(face.V1);
                    buffer.putInt(face.Col1.color);
                    buffer.putFloat(face.U2);
                    buffer.putFloat(face.V2);
                    buffer.putInt(face.Col2.color);
                    buffer.putFloat(face.U3);
                    buffer.putFloat(face.V3);
                    buffer.putInt(face.Col3.color);
                    buffer.put(new byte[6]); // normals
                }

                ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.MODEL_PART, buffer.array());
            }
        } finally {
            ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.MODEL_END, new byte[0]);
        }
    }

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

    public static class FillableMaterials extends MaterialsBlock.MaterialsData {
        public FillableMaterials() {}

        @Override
        public void write(BlockWriter writer) {}
    }
}
