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
import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;
import dev.hipposgrumm.kamapreader.util.types.structs.*;

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
        } else if (typeFirst != 0x01) {
            System.err.println("Unknown type 0x"+Integer.toHexString(typeFirst).toUpperCase()+" at 0x"+Integer.toHexString(reader.getTruePointer()-4).toUpperCase()+" - You can ignore this exception (for now).");
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
            int endOffset = (texturesOffset>0 ? texturesOffset : (materialsOffset>0 ? materialsOffset : (reader.getSize()+8)));
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
        boolean wasLittleEndian = writer.isLittleEndian();
        writer.setLittleEndian(true);
        writer.writeRawString("\0\0\0\0\0\0\0\0\0\0\0\0"); // Block Size and Texture/Material Offsets - Get filled in later.
        writeData(writer.segment());
        int texPosition;
        if (hasTextures()) {
            texPosition = writer.getPointer();
            textures.write(writer.segment());
        } else texPosition = 0;
        int matPosition;
        if (hasMaterial()) {
            matPosition = writer.getPointer();
            materials.write(writer.segment());
        } else matPosition = 0;
        int endPosition = writer.getPointer()-8;
        writer.setLittleEndian(wasLittleEndian);
        writer.seek(4);
        writer.writeInt(endPosition);
        writer.writeIntLittle(texPosition);
        writer.writeIntLittle(matPosition);
    }

    /// Writes main mesh data excluding texture and material data.
    protected abstract void writeData(BlockWriter writer);

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        List<DatingProfileEntry<?>> list = new ArrayList<>();
        if (hasTextures()) list.add(new SubBachelorPreviewEntry("Textures",
                () -> textures.textureList
        ));
        if (hasMaterial()) list.add(new SubBachelorPreviewEntry("Materials",
                () -> materials.materialList
        ));
        return list;
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        List<DatingBachelor> list = new ArrayList<>();
        if (hasTextures()) list.add(new TexturesWrapper(textures));
        if (hasMaterial()) list.add(new MaterialsWrapper(materials));
        return list;
    }

    protected final boolean hasTextures() {
        return textures != null;
    }

    protected final boolean hasMaterial() {
        return materials != null && !(materials instanceof FillableMaterials);
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
            for (int i=0;i<mesh_data.Segments.length;i++) {
                PROReader.PROData.Segment seg = mesh_data.Segments[i];
                PR_MATERIALBUFFER[] matbufs = mesh_data.SegBuffers[i].MaterialBuffers;
                for (int j=0;j<matbufs.length;j++) {
                    PR_VERTEXBUFFER[] vertbufs = matbufs[j].VertexBuffers;
                    for (int k=0;k<vertbufs.length;k++) {
                        PR_VERTEXBUFFER vertbuf = vertbufs[k];
                        boolean stripTriangles = PR_VERTEXBUFFER.VBufFlags.TriangleStripMode.from(vertbuf.Flags);
                        int faceCount = stripTriangles
                                ? (vertbuf.NumIndices-2)
                                : (vertbuf.NumIndices/3);

                        String matBufInd = Integer.toString(j);
                        String vertBufInd = Integer.toString(k);
                        ByteBuffer buffer = ByteBuffer.allocate(
                                11 + matBufInd.length() + vertBufInd.length() +
                                        (seg.Name.length()) +
                                        (vertbuf.vertices.length * 24) +
                                        (faceCount * 62)
                        );
                        buffer.put(String.format("%s_%s_%s", seg.Name, matBufInd, vertBufInd).getBytes(StandardCharsets.US_ASCII));
                        buffer.put((byte) 0);

                        buffer.putInt(vertbuf.vertices.length);
                        for (FVFVertex vert : vertbuf.vertices) {
                            buffer.putFloat(vert.Pos.X);
                            buffer.putFloat(vert.Pos.Y);
                            buffer.putFloat(vert.Pos.Z);
                            buffer.putFloat(vert.Normal.X);
                            buffer.putFloat(vert.Normal.Y);
                            buffer.putFloat(vert.Normal.Z);
                        }

                        buffer.putInt(faceCount);
                        int l = stripTriangles ? 2 : 0;
                        boolean stripFlip = false;
                        while (l<vertbuf.NumIndices) {
                            int ind1, ind2, ind3;

                            if (stripTriangles) {
                                ind1 = vertbuf.indices[l-2];
                                int in2 = vertbuf.indices[l-1];
                                int in3 = vertbuf.indices[l-0];
                                if (stripFlip) {
                                    // "Notice that the vertices of the second and fourth triangles are out of order; this is required to make sure that all the triangles are drawn in a clockwise orientation."
                                    // - D3D Documentation
                                    ind2 = in3;
                                    ind3 = in2;
                                } else {
                                    ind2 = in2;
                                    ind3 = in3;
                                }
                                stripFlip = !stripFlip;
                            } else {
                                ind1 = vertbuf.indices[l+0];
                                ind2 = vertbuf.indices[l+1];
                                ind3 = vertbuf.indices[l+2];
                            }

                            FVFVertex vert1 = vertbuf.vertices[ind1];
                            FVFVertex vert2 = vertbuf.vertices[ind2];
                            FVFVertex vert3 = vertbuf.vertices[ind3];
                            buffer.putInt(materials.materialList.get(matbufs[j].MaterialIndex).getUid().get());
                            buffer.putInt(0);
                            //buffer.putInt(materials.materialList.get(face.Material.get()).getUid().get());
                            //buffer.putInt(materials.materialList.get(face.BackMaterial.get()).getUid().get());
                            buffer.putInt(ind1);
                            buffer.putInt(ind2);
                            buffer.putInt(ind3);
                            boolean hasTexCoord = (D3DFVF.TexCoord.from(vertbuf.FVF).getValue()>>8) > 0;
                            if (hasTexCoord) {
                                buffer.putFloat(vert1.TextureCoords[0].u);
                                buffer.putFloat(vert1.TextureCoords[0].v);
                            } else buffer.putLong(0L); // Same length in bytes.
                            buffer.putInt(vert1.Diffuse.color);
                            if (hasTexCoord) {
                                buffer.putFloat(vert2.TextureCoords[0].u);
                                buffer.putFloat(vert2.TextureCoords[0].v);
                            } else buffer.putLong(0L);
                            buffer.putInt(vert2.Diffuse.color);
                            if (hasTexCoord) {
                                buffer.putFloat(vert3.TextureCoords[0].u);
                                buffer.putFloat(vert3.TextureCoords[0].v);
                            } else buffer.putLong(0L);
                            buffer.putInt(vert3.Diffuse.color);
                            buffer.put(new byte[6]); // normals

                            if (stripTriangles) l++;
                            else l += 3;
                        }

                        ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.MODEL_PART, buffer.array());
                    }
                }
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
