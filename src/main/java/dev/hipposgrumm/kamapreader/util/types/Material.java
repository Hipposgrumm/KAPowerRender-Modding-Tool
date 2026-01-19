package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.enums.*;
import dev.hipposgrumm.kamapreader.util.types.structs.INTCOLOR;
import dev.hipposgrumm.kamapreader.util.types.structs.FLOATCOLOR_RGBA;
import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UniqueIdentifier;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Material implements DatingBachelor, Previewable {
    public final Texture[] textures = new Texture[7];
    private final SizeLimitedString name;
    private int unknown1;
    private final int[] unknown2 = new int[8];
    private UByte unknown3;
    private int unknown4;
    private final int[] unknown5 = new int[8];
    private final UByte.Array unknown6 = new UByte.Array(8);
    public FLOATCOLOR_RGBA color;
    private UByte unknown7;
    public float Bump00;
    public float Bump01;
    public float Bump10;
    public float Bump11;
    public float BumpScale;
    public FLOATCOLOR_RGBA specular;
    private byte[] unknown8;
    public float specular_power;
    public boolean doublesided;
    private byte[] internal1;
    public final RenderStyle renderstyle;
    private short internal2;
    private short internal3;
    private UniqueIdentifier uid;
    private UniqueIdentifier internal4;

    // TODO: Is this always read in little endian? I couldn't find any big endian examples.
    public Material(List<Map<Integer, Texture>> textureMaps, BlockReader reader) throws IOException {
        for (int i=0;i<7;i++) {
            int texId = reader.readInt();
            Texture texture = null;
            if (texId != 0) for (Map<Integer, Texture> map:textureMaps) {
                texture = map.get(texId);
                if (texture != null) break; // Is this accurate to ingame function?
            }
            textures[i] = texture;
        }
        name = reader.readStringFixed(0x80);

        // While not exactly unknown what this probably is, there's not enough context to figure it out.
        unknown1 = reader.readInt();
        for (int i=0;i<8;i++) unknown2[i] = reader.readInt();
        unknown3 = reader.readUByte();
        reader.move(3);
        unknown4 = reader.readInt();
        for (int i=0;i<8;i++) unknown5[i] = reader.readInt();
        byte[] ubyteArray_unknown6 = unknown6.array();
        for (int i=0;i<8;i++) ubyteArray_unknown6[i] = reader.readByte();

        color = new FLOATCOLOR_RGBA(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());

        unknown7 = reader.readUByte();
        reader.move(3);

        Bump00 = reader.readFloat();
        Bump01 = reader.readFloat();
        Bump10 = reader.readFloat();
        Bump11 = reader.readFloat();
        BumpScale = reader.readFloat();
        specular = new FLOATCOLOR_RGBA(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());

        unknown8 = reader.readBytes(16);

        specular_power = reader.readFloat();
        doublesided = reader.readByte() != 0;
        reader.move(3);

        // Unset space used by the engine
        internal1 = reader.readBytes(204);

        renderstyle = new RenderStyle(reader.segment(0x214));

        internal2 = reader.readShort(); // RefCount; managed by the engine
        internal3 = reader.readShort(); // Some thread-based number, not sure what this is really for yet.

        uid = new UniqueIdentifier(reader.readInt());
        internal4 = new UniqueIdentifier(reader.readInt()); // for "next pass"; might not actually be internal (possibly just unused)
    }

    public void write(BlockWriter writer) throws IOException {
        for (Texture texture:textures) {
            if (texture != null) {
                writer.writeInt(texture.getUid().get());
            } else {
                writer.writeInt(0);
            }
        }
        writer.writeTerminatedStringFixed(name);

        writer.writeInt(unknown1);
        for (int i=0;i<8;i++) writer.writeInt(unknown2[i]);
        writer.writeUByte(unknown3);
        writer.writeBytes(new byte[] {0,0,0});
        writer.writeInt(unknown4);
        for (int i=0;i<8;i++) writer.writeInt(unknown5[i]);
        writer.writeBytes(unknown6.array());

        writer.writeFloat(color.R);
        writer.writeFloat(color.G);
        writer.writeFloat(color.B);
        writer.writeFloat(color.A);

        writer.writeUByte(unknown7);
        writer.writeBytes(new byte[] {0,0,0});

        writer.writeFloat(Bump00);
        writer.writeFloat(Bump01);
        writer.writeFloat(Bump10);
        writer.writeFloat(Bump11);
        writer.writeFloat(BumpScale);
        writer.writeFloat(specular.R);
        writer.writeFloat(specular.G);
        writer.writeFloat(specular.B);
        writer.writeFloat(specular.A);

        writer.writeBytes(unknown8);

        writer.writeFloat(specular_power);
        writer.writeByte((byte) (doublesided ? 1 : 0));
        writer.writeBytes(new byte[] {0,0,0});

        writer.writeBytes(internal1);

        renderstyle.write(writer.segment());

        writer.writeShort(internal2);
        writer.writeShort(internal3);

        writer.writeInt(uid.get());

        writer.writeInt(internal4.get());
    }

    public UniqueIdentifier getUid() {
        return uid;
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return List.of(new DatingProfileEntry.ReadOnly<>("UID",
                () -> uid
        ), new DatingProfileEntry.ReadOnly<>("Preview",
                () -> this
        ), new DatingProfileEntry<>("Name",
                () -> name
        ), new DatingProfileEntry<>("Color",
                () -> color
        ), new DatingProfileEntry<>("Textures",
                () -> textures
        ), new DatingProfileEntry<>("Bump00",
                () -> Bump00,
                b -> Bump00 = b
        ), new DatingProfileEntry<>("Bump01",
                () -> Bump01,
                b -> Bump01 = b
        ), new DatingProfileEntry<>("Bump10",
                () -> Bump10,
                b -> Bump10 = b
        ), new DatingProfileEntry<>("Bump11",
                () -> Bump11,
                b -> Bump11 = b
        ), new DatingProfileEntry<>("BumpScale",
                () -> BumpScale,
                b -> BumpScale = b
        ), new DatingProfileEntry<>("Specular Color",
                () -> specular
        ), new DatingProfileEntry<>("Specular Power",
                () -> specular_power,
                p -> specular_power = p
        ), new DatingProfileEntry<>("Double-Sided",
                () -> doublesided,
                p -> doublesided = p
        ));
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return List.of(renderstyle);
    }

    @Override
    public Node getPreviewGraphic() {
        if (textures[0] != null) return textures[0].getPreviewGraphic();
        return new Rectangle(50, 50, color.toJavaFXColor());
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public static class RenderStyle implements DatingBachelor {
        // TODO: Maybe log something when the enum values are different?
        SizeLimitedString name;
        public D3DZBUFFERTYPE enableZ;
        public boolean enableWriteZ;
        public boolean lighting;
        public boolean vertexColors;
        public boolean enableAlphaBlend;
        public boolean enableAlphaTest;
        public boolean enableSpecular;
        public int alphaRef;
        public int textureFactor;
        public int blendFactor;
        public D3DSHADEMODE shadeMode;
        public D3DBLEND srcBlend;
        public D3DBLEND destBlend;
        public D3DCMPFUNC zFunc;
        public D3DCMPFUNC alphaFunc;
        public D3DMATERIALCOLORSOURCE diffuseMaterialSource;
        public D3DMATERIALCOLORSOURCE specularMaterialSource;
        public D3DMATERIALCOLORSOURCE ambientMaterialSource;
        public D3DMATERIALCOLORSOURCE emissiveMaterialSource;
        public ColorWriteFlags colorWriteFlags;
        public D3DBLENDOP blendop;
        public final TextureStage[] textureStages = new TextureStage[8];
        public final SamplerStage[] samplerStages = new SamplerStage[8];

        RenderStyle(BlockReader reader) throws IOException {
            name = reader.readStringFixed(0x40);
            enableZ = D3DZBUFFERTYPE.from(reader.readInt());
            enableWriteZ = reader.readInt() != 0;
            lighting = reader.readInt() != 0;
            vertexColors = reader.readInt() != 0;
            enableAlphaBlend = reader.readInt() != 0;
            enableAlphaTest = reader.readInt() != 0;
            enableSpecular = reader.readInt() != 0;
            alphaRef = reader.readInt();
            textureFactor = reader.readInt();
            blendFactor = reader.readInt();
            shadeMode = D3DSHADEMODE.from(reader.readInt());
            srcBlend = D3DBLEND.from(reader.readInt());
            destBlend = D3DBLEND.from(reader.readInt());
            zFunc = D3DCMPFUNC.from(reader.readInt());
            alphaFunc = D3DCMPFUNC.from(reader.readInt());
            diffuseMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            specularMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            ambientMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            emissiveMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            colorWriteFlags = new ColorWriteFlags(reader.readInt());
            blendop = D3DBLENDOP.from(reader.readInt());
            for (int i=0;i<textureStages.length;i++)
                textureStages[i] = new TextureStage(i, reader.segment(0x20));
            for (int i=0;i<samplerStages.length;i++)
                samplerStages[i] = new SamplerStage(i, reader.segment(0x10));
        }

        void write(BlockWriter writer) throws IOException {
            writer.writeTerminatedStringFixed(name);
            writer.writeInt(enableZ.identifier);
            writer.writeInt(enableWriteZ ? 1 : 0);
            writer.writeInt(lighting ? 1 : 0);
            writer.writeInt(vertexColors ? 1 : 0);
            writer.writeInt(enableAlphaBlend ? 1 : 0);
            writer.writeInt(enableAlphaTest ? 1 : 0);
            writer.writeInt(enableSpecular ? 1 : 0);
            writer.writeInt(alphaRef);
            writer.writeInt(textureFactor);
            writer.writeInt(blendFactor);
            writer.writeInt(shadeMode.identifier);
            writer.writeInt(srcBlend.identifier);
            writer.writeInt(destBlend.identifier);
            writer.writeInt(zFunc.identifier);
            writer.writeInt(alphaFunc.identifier);
            writer.writeInt(diffuseMaterialSource.identifier);
            writer.writeInt(specularMaterialSource.identifier);
            writer.writeInt(ambientMaterialSource.identifier);
            writer.writeInt(emissiveMaterialSource.identifier);
            writer.writeInt(colorWriteFlags.getValue());
            writer.writeInt(blendop.identifier);
            for (TextureStage stage:textureStages)
                stage.write(writer.segment());
            for (SamplerStage stage:samplerStages)
                stage.write(writer.segment());
        }

        @Override
        public List<? extends DatingProfileEntry<?>> getDatingProfile() {
            return List.of(new DatingProfileEntry<>("Name",
                    () -> name
            ), new DatingProfileEntry<>("Z-Enabled",
                    () -> enableZ,
                    b -> enableZ = b
            ), new DatingProfileEntry<>("Z-Write",
                    () -> enableWriteZ,
                    b -> enableWriteZ = b
            ), new DatingProfileEntry<>("Lighting",
                    () -> lighting,
                    b -> lighting = b
            ), new DatingProfileEntry<>("VertexColors",
                    () -> vertexColors,
                    b -> vertexColors = b
            ), new DatingProfileEntry<>("AlphaBlend",
                    () -> enableAlphaBlend,
                    b -> enableAlphaBlend = b
            ), new DatingProfileEntry<>("AlphaTest",
                    () -> enableAlphaTest,
                    b -> enableAlphaTest = b
            ), new DatingProfileEntry<>("Specular",
                    () -> enableSpecular,
                    b -> enableSpecular = b
            ), new DatingProfileEntry<>("AlphaRef",
                    () -> alphaRef,
                    i -> alphaRef = i
            ), new DatingProfileEntry<>("TextureFactor",
                    () -> textureFactor,
                    i -> textureFactor = i
            ), new DatingProfileEntry<>("BlendFactor",
                    () -> blendFactor,
                    i -> blendFactor = i
            ), new DatingProfileEntry<>("Shade Mode",
                    () -> shadeMode,
                    e -> shadeMode = e
            ), new DatingProfileEntry<>("Source Blend",
                    () -> srcBlend,
                    e -> srcBlend = e
            ), new DatingProfileEntry<>("Destination Blend",
                    () -> destBlend,
                    e -> destBlend = e
            ), new DatingProfileEntry<>("Z-Function",
                    () -> zFunc,
                    e -> zFunc = e
            ), new DatingProfileEntry<>("Alpha-Function",
                    () -> alphaFunc,
                    e -> alphaFunc = e
            ), new DatingProfileEntry<>("MatSrc: Diffuse",
                    () -> diffuseMaterialSource,
                    s -> diffuseMaterialSource = s
            ), new DatingProfileEntry<>("MatSrc: Specular",
                    () -> specularMaterialSource,
                    s -> specularMaterialSource = s
            ), new DatingProfileEntry<>("MatSrc: Ambient",
                    () -> ambientMaterialSource,
                    s -> ambientMaterialSource = s
            ), new DatingProfileEntry<>("MatSrc: Emissive",
                    () -> emissiveMaterialSource,
                    s -> emissiveMaterialSource = s
            ), new DatingProfileEntry<>("Color Write Flags",
                    () -> colorWriteFlags
            ), new DatingProfileEntry<>("BlendOp",
                    () -> blendop,
                    o -> blendop = o
            ));
        }

        @Override
        public List<? extends DatingBachelor> getSubBachelors() {
            List<DatingBachelor> items = new ArrayList<>(textureStages.length + samplerStages.length);
            items.addAll(Arrays.asList(textureStages));
            items.addAll(Arrays.asList(samplerStages));
            return items;
        }

        @Override
        public String toString() {
            if (name.toString().isEmpty()) return "RenderStyle";
            return "RenderStyle: "+name;
        }

        public static class ColorWriteFlags extends Flags {
            public static final BoolEntry Red = new BoolEntry("Red", 0);
            public static final BoolEntry Green = new BoolEntry("Green", 1);
            public static final BoolEntry Blue = new BoolEntry("Blue", 2);
            public static final BoolEntry Alpha = new BoolEntry("Alpha", 3);

            public ColorWriteFlags(int value) {
                super(value);
            }

            @Override
            public Entry[] getEntries() {
                return new Entry[] {Red, Green, Blue, Alpha};
            }
        }

        public static class TextureStage implements DatingBachelor {
            private final int i;
            public D3DTEXTUREOP colorop;
            public D3DTEXTUREOP alphaop;
            public TEXARGS colorarg1;
            public TEXARGS colorarg2;
            public TEXARGS alphaarg1;
            public TEXARGS alphaarg2;
            public TEXINDEX texcoordindex;
            public D3DTEXTURETRANSFORMFLAGS transformflags;

            TextureStage(int i, BlockReader reader) throws IOException {
                this.i = i;
                colorop = D3DTEXTUREOP.from(reader.readInt());
                alphaop = D3DTEXTUREOP.from(reader.readInt());
                colorarg1 = new TEXARGS(reader.readInt());
                colorarg2 = new TEXARGS(reader.readInt());
                alphaarg1 = new TEXARGS(reader.readInt());
                alphaarg2 = new TEXARGS(reader.readInt());
                texcoordindex = TEXINDEX.from(reader.readInt());
                transformflags = new D3DTEXTURETRANSFORMFLAGS(reader.readInt());
            }

            void write(BlockWriter writer) throws IOException {
                writer.writeInt(colorop.identifier);
                writer.writeInt(alphaop.identifier);
                writer.writeInt(colorarg1.getValue());
                writer.writeInt(colorarg2.getValue());
                writer.writeInt(alphaarg1.getValue());
                writer.writeInt(alphaarg2.getValue());
                writer.writeInt(texcoordindex.identifier);
                writer.writeInt(transformflags.getValue());
            }

            @Override
            public List<? extends DatingProfileEntry<?>> getDatingProfile() {
                return List.of(new DatingProfileEntry<>("ColorOp",
                        () -> colorop,
                        o -> colorop = o
                ), new DatingProfileEntry<>("AlphaOp",
                        () -> alphaop,
                        o -> alphaop = o
                ), new DatingProfileEntry<>("ColorArg1",
                        () -> colorarg1,
                        a -> colorarg1 = a
                ), new DatingProfileEntry<>("ColorArg2",
                        () -> colorarg2,
                        a -> colorarg2 = a
                ), new DatingProfileEntry<>("AlphaArg1",
                        () -> alphaarg1,
                        a -> alphaarg1 = a
                ), new DatingProfileEntry<>("AlphaArg2",
                        () -> alphaarg2,
                        a -> alphaarg2 = a
                ), new DatingProfileEntry<>("TexCoord Index",
                        () -> texcoordindex,
                        i -> texcoordindex = i
                ), new DatingProfileEntry<>("Texture Transform Flags",
                        () -> transformflags,
                        f -> transformflags = f
                ));
            }

            @Override
            public String toString() {
                return "TextureStage: "+i;
            }
        }

        public static class SamplerStage implements DatingBachelor {
            private final int i;
            public D3DTEXTUREADDRESS addressU;
            public D3DTEXTUREADDRESS addressV;
            public D3DTEXTUREADDRESS addressW;
            public final INTCOLOR bordercolor = new INTCOLOR(INTCOLOR.Format.RGBA, -1);

            SamplerStage(int i, BlockReader reader) throws IOException {
                this.i = i;
                addressU = D3DTEXTUREADDRESS.from(reader.readInt());
                addressV = D3DTEXTUREADDRESS.from(reader.readInt());
                addressW = D3DTEXTUREADDRESS.from(reader.readInt());
                bordercolor.color = reader.readInt();
            }

            void write(BlockWriter writer) throws IOException {
                writer.writeInt(addressU.identifier);
                writer.writeInt(addressV.identifier);
                writer.writeInt(addressW.identifier);
                writer.writeInt(bordercolor.color);
            }

            @Override
            public List<? extends DatingProfileEntry<?>> getDatingProfile() {
                return List.of(new DatingProfileEntry<>("U Address",
                        () -> addressU,
                        a -> addressU = a
                ), new DatingProfileEntry<>("V Address",
                        () -> addressV,
                        a -> addressV = a
                ), new DatingProfileEntry<>("W Address",
                        () -> addressW,
                        a -> addressW = a
                ), new DatingProfileEntry<>("Border Color",
                        () -> bordercolor
                ));
            }

            @Override
            public String toString() {
                return "SampleStage: "+i;
            }
        }
    }
}
