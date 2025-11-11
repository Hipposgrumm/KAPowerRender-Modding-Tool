package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.structs.COLOR_RGBA;
import dev.hipposgrumm.kamapreader.util.types.structs.FLOATCOLOR_RGBA;
import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UniqueIdentifier;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Material implements DatingBachelor, Previewable {
    private final Texture[] textures = new Texture[7];
    private final SizeLimitedString name;
    private int unknown1;
    private final int[] unknown2 = new int[8];
    private UByte unknown3;
    private int unknown4;
    private final int[] unknown5 = new int[8];
    private final UByte[] unknown6 = new UByte[8];
    private FLOATCOLOR_RGBA color;
    private UByte unknown7;
    private float Bump00;
    private float Bump01;
    private float Bump10;
    private float Bump11;
    private float BumpScale;
    private FLOATCOLOR_RGBA spectacular;
    private byte[] unknown8;
    private float spectacular_power;
    private boolean doublesided;
    private byte[] internal1;
    private final RenderStyle renderstyle;
    private short internal2;
    private short internal3;
    private UniqueIdentifier uid;
    private UniqueIdentifier internal4;

    // TODO: Is this always read in little endian? I couldn't find any big endian examples.
    public Material(KARFile file, BlockReader reader) throws IOException {
        List<TexturesBlock> textureBlocks = file.blocks.stream()
                .filter(b -> b instanceof TexturesBlock)
                .map(b -> (TexturesBlock) b)
                .toList();
        for (int i=0;i<7;i++) {
            int texId = reader.readInt();
            Texture texture = null;
            if (texId != 0) for (TexturesBlock block:textureBlocks) {
                texture = block.textures.get(texId);
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
        for (int i=0;i<8;i++) unknown6[i] = reader.readUByte();

        color = new FLOATCOLOR_RGBA(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());

        unknown7 = reader.readUByte();
        reader.move(3);

        Bump00 = reader.readFloat();
        Bump01 = reader.readFloat();
        Bump10 = reader.readFloat();
        Bump11 = reader.readFloat();
        BumpScale = reader.readFloat();
        spectacular = new FLOATCOLOR_RGBA(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());

        unknown8 = reader.readBytes(16);

        spectacular_power = reader.readFloat();
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
        for (int i=0;i<8;i++) writer.writeUByte(unknown6[i]);

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
        writer.writeFloat(spectacular.R);
        writer.writeFloat(spectacular.G);
        writer.writeFloat(spectacular.B);
        writer.writeFloat(spectacular.A);

        writer.writeBytes(unknown8);

        writer.writeFloat(spectacular_power);
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
        return List.of(new DatingProfileEntry<>("UID",
                () -> uid,
                null
        ), new DatingProfileEntry<>("Name",
                () -> name,
                null
        ), new DatingProfileEntry<>("Color",
                () -> color,
                c -> color = c
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
        ), new DatingProfileEntry<>("Spectacular Color",
                () -> spectacular,
                c -> spectacular = c
        ), new DatingProfileEntry<>("Spectacular Power",
                () -> spectacular_power,
                p -> spectacular_power = p
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
        return new Rectangle(50, 50, Color.color(color.R/255f, color.G/255f, color.B/255f, color.A/255f));
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public static class RenderStyle implements DatingBachelor {
        SizeLimitedString name;
        D3DZBUFFERTYPE enableZ;
        boolean enableWriteZ;
        boolean lighting;
        boolean vertexColors;
        boolean enableAlphaBlend;
        boolean enableAlphaTest;
        boolean enableSpectacular;
        int alphaRef;
        int textureFactor;
        int blendFactor;
        D3DSHADEMODE shadeMode;
        D3DBLEND srcBlend;
        D3DBLEND destBlend;
        D3DCMPFUNC zFunc;
        D3DCMPFUNC alphaFunc;
        D3DMATERIALCOLORSOURCE diffuseMaterialSource;
        D3DMATERIALCOLORSOURCE spectacularMaterialSource;
        D3DMATERIALCOLORSOURCE ambientMaterialSource;
        D3DMATERIALCOLORSOURCE emissiveMaterialSource;
        Flags colorWriteFlags;
        D3DBLENDOP blendop;
        final TextureStage[] textureStages = new TextureStage[8];
        final SamplerStage[] samplerStages = new SamplerStage[8];

        RenderStyle(BlockReader reader) throws IOException {
            name = reader.readStringFixed(0x40);
            enableZ = D3DZBUFFERTYPE.from(reader.readInt());
            enableWriteZ = reader.readInt() != 0;
            lighting = reader.readInt() != 0;
            vertexColors = reader.readInt() != 0;
            enableAlphaBlend = reader.readInt() != 0;
            enableAlphaTest = reader.readInt() != 0;
            enableSpectacular = reader.readInt() != 0;
            alphaRef = reader.readInt();
            textureFactor = reader.readInt();
            blendFactor = reader.readInt();
            shadeMode = D3DSHADEMODE.from(reader.readInt());
            srcBlend = D3DBLEND.from(reader.readInt());
            destBlend = D3DBLEND.from(reader.readInt());
            zFunc = D3DCMPFUNC.from(reader.readInt());
            alphaFunc = D3DCMPFUNC.from(reader.readInt());
            diffuseMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            spectacularMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            ambientMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            emissiveMaterialSource = D3DMATERIALCOLORSOURCE.from(reader.readInt());
            colorWriteFlags = new Flags(reader.readInt());
            colorWriteFlags.setName(0, "Red");
            colorWriteFlags.setName(1, "Green");
            colorWriteFlags.setName(2, "Blue");
            colorWriteFlags.setName(3, "Alpha");
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
            writer.writeInt(enableSpectacular ? 1 : 0);
            writer.writeInt(alphaRef);
            writer.writeInt(textureFactor);
            writer.writeInt(blendFactor);
            writer.writeInt(shadeMode.identifier);
            writer.writeInt(srcBlend.identifier);
            writer.writeInt(destBlend.identifier);
            writer.writeInt(zFunc.identifier);
            writer.writeInt(alphaFunc.identifier);
            writer.writeInt(diffuseMaterialSource.identifier);
            writer.writeInt(spectacularMaterialSource.identifier);
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
                    () -> name,
                    n -> name = n
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
            ), new DatingProfileEntry<>("Spectacular",
                    () -> enableSpectacular,
                    b -> enableSpectacular = b
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
            ), new DatingProfileEntry<>("Material Source: Diffuse",
                    () -> diffuseMaterialSource,
                    s -> diffuseMaterialSource = s
            ), new DatingProfileEntry<>("Material Source: Spectacular",
                    () -> spectacularMaterialSource,
                    s -> spectacularMaterialSource = s
            ), new DatingProfileEntry<>("Material Source: Ambient",
                    () -> ambientMaterialSource,
                    s -> ambientMaterialSource = s
            ), new DatingProfileEntry<>("Material Source: Emissive",
                    () -> emissiveMaterialSource,
                    s -> emissiveMaterialSource = s
            ), new DatingProfileEntry<>("Color Write Flags",
                    () -> colorWriteFlags,
                    f -> colorWriteFlags = f
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

        private enum D3DZBUFFERTYPE implements EnumChoices {
            FALSE(0),
            TRUE(1),
            USEW(2);

            public final int identifier;

            D3DZBUFFERTYPE(int identifier) {
                this.identifier = identifier;
            }

            static D3DZBUFFERTYPE from(int i) {
                return switch (i) {
                    case 1 -> TRUE;
                    case 2 -> USEW;
                    default -> FALSE;
                };
            }

            @Override
            public Enum<? extends EnumChoices> getSelf() {
                return this;
            }

            @Override
            public List<? extends Enum<? extends EnumChoices>> choices() {
                return List.of(FALSE, TRUE, USEW);
            }
        }

        private enum D3DSHADEMODE implements EnumChoices {
            FLAT(1),
            GOURAUD(2),
            PHONG(3);

            public final int identifier;

            D3DSHADEMODE(int identifier) {
                this.identifier = identifier;
            }

            static D3DSHADEMODE from(int i) {
                return switch (i) {
                    case 1 -> FLAT;
                    case 3 -> PHONG;
                    default -> GOURAUD;
                };
            }

            @Override
            public Enum<? extends EnumChoices> getSelf() {
                return this;
            }

            @Override
            public List<? extends Enum<? extends EnumChoices>> choices() {
                return List.of(FLAT, GOURAUD, PHONG);
            }
        }

        private enum D3DBLEND implements EnumChoices {
            ZERO(1),
            ONE(2),
            SRCCOLOR(3),
            INVSRCCOLOR(4),
            SRCALPHA(5),
            INVSRCALPHA(6),
            DESTALPHA(7),
            INVDESTALPHA(8),
            DESTCOLOR(9),
            INVDESTCOLOR(10),
            SRCALPHASAT(11),
            BOTHSRCALPHA(12),
            BOTHINVSRCALPHA(13),
            BLENDFACTOR(14),
            INVBLENDFACTOR(15),
            SRCCOLOR2(16),
            INVSRCCOLOR2(17),
            __(18);

            public final int identifier;

            D3DBLEND(int identifier) {
                this.identifier = identifier;
            }

            static D3DBLEND from(int i) {
                return switch (i) {
                    case 1 -> ZERO;
                    case 2 -> ONE;
                    case 3 -> SRCCOLOR;
                    case 4 -> INVSRCCOLOR;
                    case 5 -> SRCALPHA;
                    case 6 -> INVSRCALPHA;
                    case 7 -> DESTALPHA;
                    case 8 -> INVDESTALPHA;
                    case 9 -> DESTCOLOR;
                    case 10 -> INVDESTCOLOR;
                    case 11 -> SRCALPHASAT;
                    case 12 -> BOTHSRCALPHA;
                    case 13 -> BOTHINVSRCALPHA;
                    case 14 -> BLENDFACTOR;
                    case 15 -> INVBLENDFACTOR;
                    case 16 -> SRCCOLOR2;
                    case 17 -> INVSRCCOLOR2;
                    default -> __;
                };
            }

            @Override
            public Enum<? extends EnumChoices> getSelf() {
                return this;
            }

            @Override
            public List<? extends Enum<? extends EnumChoices>> choices() {
                return List.of(
                        ZERO, ONE, SRCCOLOR, INVSRCCOLOR, SRCALPHA, INVSRCALPHA,
                        DESTALPHA, INVDESTALPHA, DESTCOLOR, INVDESTCOLOR,
                        SRCALPHASAT, BOTHSRCALPHA, BOTHINVSRCALPHA,
                        BLENDFACTOR, INVBLENDFACTOR, SRCCOLOR2, INVSRCCOLOR2
                );
            }
        }

        private enum D3DCMPFUNC implements EnumChoices {
            NEVER(1),
            LESS(2),
            EQUAL(3),
            LESSEQUAL(4),
            GREATER(5),
            NOTEQUAL(6),
            GREATEREQUAL(7),
            ALWAYS(8),
            __(9);

            public final int identifier;

            D3DCMPFUNC(int identifier) {
                this.identifier = identifier;
            }

            static D3DCMPFUNC from(int i) {
                return switch (i) {
                    case 1 -> NEVER;
                    case 2 -> LESS;
                    case 3 -> EQUAL;
                    case 4 -> LESSEQUAL;
                    case 5 -> GREATER;
                    case 6 -> NOTEQUAL;
                    case 7 -> GREATEREQUAL;
                    case 8 -> ALWAYS;
                    default -> __;
                };
            }

            @Override
            public Enum<? extends EnumChoices> getSelf() {
                return this;
            }

            @Override
            public List<? extends Enum<? extends EnumChoices>> choices() {
                return List.of(
                        NEVER, LESS, EQUAL, LESSEQUAL,
                        GREATER, NOTEQUAL, GREATEREQUAL, ALWAYS
                );
            }
        }

        private enum D3DMATERIALCOLORSOURCE implements EnumChoices {
            MATERIAL(0),
            COLOR1(1),
            COLOR2(2),
            __(3);

            public final int identifier;

            D3DMATERIALCOLORSOURCE(int identifier) {
                this.identifier = identifier;
            }

            static D3DMATERIALCOLORSOURCE from(int i) {
                return switch (i) {
                    case 0 -> MATERIAL;
                    case 1 -> COLOR1;
                    case 2 -> COLOR2;
                    default -> __;
                };
            }

            @Override
            public Enum<? extends EnumChoices> getSelf() {
                return this;
            }

            @Override
            public List<? extends Enum<? extends EnumChoices>> choices() {
                return List.of(MATERIAL, COLOR1, COLOR2);
            }
        }

        private enum D3DBLENDOP implements EnumChoices {
            ADD(1),
            SUBTRACT(2),
            REVSUBTRACT(3),
            MIN(4),
            MAX(5),
            __(6);

            public final int identifier;

            D3DBLENDOP(int identifier) {
                this.identifier = identifier;
            }

            static D3DBLENDOP from(int i) {
                return switch (i) {
                    case 1 -> ADD;
                    case 2 -> SUBTRACT;
                    case 3 -> REVSUBTRACT;
                    case 4 -> MIN;
                    case 5 -> MAX;
                    default -> __;
                };
            }

            @Override
            public Enum<? extends EnumChoices> getSelf() {
                return this;
            }

            @Override
            public List<? extends Enum<? extends EnumChoices>> choices() {
                return List.of(ADD, SUBTRACT, REVSUBTRACT, MIN, MAX);
            }
        }

        public static class TextureStage implements DatingBachelor {
            private final int i;
            private D3DTEXTUREOP colorop;
            private D3DTEXTUREOP alphaop;
            private TEXARGS colorarg1;
            private TEXARGS colorarg2;
            private TEXARGS alphaarg1;
            private TEXARGS alphaarg2;
            private TEXINDEX texcoordindex;
            private D3DTEXTURETRANSFORMFLAGS transformflags;

            TextureStage(int i, BlockReader reader) throws IOException {
                this.i = i;
                colorop = D3DTEXTUREOP.from(reader.readInt());
                alphaop = D3DTEXTUREOP.from(reader.readInt());
                colorarg1 = TEXARGS.from(reader.readInt());
                colorarg2 = TEXARGS.from(reader.readInt());
                alphaarg1 = TEXARGS.from(reader.readInt());
                alphaarg2 = TEXARGS.from(reader.readInt());
                texcoordindex = TEXINDEX.from(reader.readInt());
                transformflags = D3DTEXTURETRANSFORMFLAGS.from(reader.readInt());
            }

            void write(BlockWriter writer) throws IOException {
                writer.writeInt(colorop.identifier);
                writer.writeInt(alphaop.identifier);
                writer.writeInt(colorarg1.identifier);
                writer.writeInt(colorarg2.identifier);
                writer.writeInt(alphaarg1.identifier);
                writer.writeInt(alphaarg2.identifier);
                writer.writeInt(texcoordindex.identifier);
                writer.writeInt(transformflags.identifier);
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
                ), new DatingProfileEntry<>("Texture Coordinate Index",
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

            private enum D3DTEXTUREOP implements EnumChoices {
                DISABLE(1),
                SELECTARG1(2),
                SELECTARG2(3),
                MODULATE(4),
                MODULATE2X(5),
                MODULATE4X(6),
                ADD(7),
                ADDSIGNED(8),
                ADDSIGNED2X(9),
                SUBTRACT(10),
                ADDSMOOTH(11),
                BLENDDIFFUSEALPHA(12),
                BLENDTEXTUREALPHA(13),
                BLENDFACTORALPHA(14),
                BLENDTEXTUREALPHAPM(15),
                BLENDCURRENTALPHA(16),
                PREMODULATE(17),
                MODULATEALPHA_ADDCOLOR(18),
                MODULATECOLOR_ADDALPHA(19),
                MODULATEINVALPHA_ADDCOLOR(20),
                MODULATEINVCOLOR_ADDALPHA(21),
                BUMPENVMAP(22),
                BUMPENVMAPLUMINANCE(23),
                DOTPRODUCT3(24),
                MULTIPLYADD(25),
                LERP(26),
                __(27);

                public final int identifier;

                D3DTEXTUREOP(int identifier) {
                    this.identifier = identifier;
                }

                static D3DTEXTUREOP from(int i) {
                    return switch (i) {
                        case 1 -> DISABLE;
                        case 2 -> SELECTARG1;
                        case 3 -> SELECTARG2;
                        case 4 -> MODULATE;
                        case 5 -> MODULATE2X;
                        case 6 -> MODULATE4X;
                        case 7 -> ADD;
                        case 8 -> ADDSIGNED;
                        case 9 -> ADDSIGNED2X;
                        case 10 -> SUBTRACT;
                        case 11 -> ADDSMOOTH;
                        case 12 -> BLENDDIFFUSEALPHA;
                        case 13 -> BLENDTEXTUREALPHA;
                        case 14 -> BLENDFACTORALPHA;
                        case 15 -> BLENDTEXTUREALPHAPM;
                        case 16 -> BLENDCURRENTALPHA;
                        case 17 -> PREMODULATE;
                        case 18 -> MODULATEALPHA_ADDCOLOR;
                        case 19 -> MODULATECOLOR_ADDALPHA;
                        case 20 -> MODULATEINVALPHA_ADDCOLOR;
                        case 21 -> MODULATEINVCOLOR_ADDALPHA;
                        case 22 -> BUMPENVMAP;
                        case 23 -> BUMPENVMAPLUMINANCE;
                        case 24 -> DOTPRODUCT3;
                        case 25 -> MULTIPLYADD;
                        case 26 -> LERP;
                        default -> __;
                    };
                }

                @Override
                public Enum<? extends EnumChoices> getSelf() {
                    return this;
                }

                @Override
                public List<? extends Enum<? extends EnumChoices>> choices() {
                    return List.of(
                            DISABLE, SELECTARG1, SELECTARG2,
                            MODULATE, MODULATE2X, MODULATE4X,
                            ADD, ADDSIGNED, ADDSIGNED2X, SUBTRACT, ADDSMOOTH,
                            BLENDDIFFUSEALPHA, BLENDTEXTUREALPHA, BLENDFACTORALPHA, BLENDTEXTUREALPHAPM, BLENDCURRENTALPHA,
                            PREMODULATE, MODULATEALPHA_ADDCOLOR, MODULATECOLOR_ADDALPHA, MODULATEINVALPHA_ADDCOLOR, MODULATEINVCOLOR_ADDALPHA,
                            BUMPENVMAP, BUMPENVMAPLUMINANCE,
                            DOTPRODUCT3, MULTIPLYADD, LERP
                    );
                }
            }

            private enum TEXARGS implements EnumChoices {
                DIFFUSE("DIFFUSE", 0x00),
                CURRENT("CURRENT", 0x01),
                TEXTURE("TEXTURE", 0x02),
                TFACTOR("TFACTOR", 0x03),
                SPECTACULAR("SPECTACULAR", 0x04),
                TEMP("TEMP", 0x05),
                CONSTANT("CONSTANT", 0x06),
                DIFFUSE_COMPLEMENT("DIFFUSE|COMPLEMENT", 0x10),
                CURRENT_COMPLEMENT("CURRENT|COMPLEMENT", 0x11),
                TEXTURE_COMPLEMENT("TEXTURE|COMPLEMENT", 0x12),
                TFACTOR_COMPLEMENT("TFACTOR|COMPLEMENT", 0x13),
                SPECTACULAR_COMPLEMENT("SPECTACULAR|COMPLEMENT", 0x14),
                TEMP_COMPLEMENT("TEMP|COMPLEMENT", 0x15),
                CONSTANT_COMPLEMENT("CONSTANT|COMPLEMENT", 0x16),
                DIFFUSE_ALPHAREPLICATE("DIFFUSE|ALPHAREPLICATE", 0x20),
                CURRENT_ALPHAREPLICATE("CURRENT|ALPHAREPLICATE", 0x21),
                TEXTURE_ALPHAREPLICATE("TEXTURE|ALPHAREPLICATE", 0x22),
                TFACTOR_ALPHAREPLICATE("TFACTOR|ALPHAREPLICATE", 0x23),
                SPECTACULAR_ALPHAREPLICATE("SPECTACULAR|ALPHAREPLICATE", 0x24),
                TEMP_ALPHAREPLICATE("TEMP|ALPHAREPLICATE", 0x25),
                CONSTANT_ALPHAREPLICATE("CONSTANT|ALPHAREPLICATE", 0x26);

                private final String name;
                public final int identifier;

                TEXARGS(String name, int identifier) {
                    this.name = name;
                    this.identifier = identifier;
                }

                @Override
                public String toString() {
                    return name;
                }

                static TEXARGS from(int i) {
                    return switch (i) {
                        case 0x00 -> DIFFUSE;
                        case 0x01 -> CURRENT;
                        case 0x02 -> TEXTURE;
                        case 0x03 -> TFACTOR;
                        case 0x04 -> SPECTACULAR;
                        case 0x05 -> TEMP;
                        case 0x10 -> DIFFUSE_COMPLEMENT;
                        case 0x11 -> CURRENT_COMPLEMENT;
                        case 0x12 -> TEXTURE_COMPLEMENT;
                        case 0x13 -> TFACTOR_COMPLEMENT;
                        case 0x14 -> SPECTACULAR_COMPLEMENT;
                        case 0x15 -> TEMP_COMPLEMENT;
                        case 0x16 -> CONSTANT_COMPLEMENT;
                        case 0x20 -> DIFFUSE_ALPHAREPLICATE;
                        case 0x21 -> CURRENT_ALPHAREPLICATE;
                        case 0x22 -> TEXTURE_ALPHAREPLICATE;
                        case 0x23 -> TFACTOR_ALPHAREPLICATE;
                        case 0x24 -> SPECTACULAR_ALPHAREPLICATE;
                        case 0x25 -> TEMP_ALPHAREPLICATE;
                        case 0x26 -> CONSTANT_ALPHAREPLICATE;
                        default -> CONSTANT;
                    };
                }

                @Override
                public Enum<? extends EnumChoices> getSelf() {
                    return this;
                }

                @Override
                public List<? extends Enum<? extends EnumChoices>> choices() {
                    return List.of(
                            DIFFUSE, CURRENT, TEXTURE, TFACTOR,
                            SPECTACULAR, TEMP, CONSTANT,
                            DIFFUSE_COMPLEMENT, CURRENT_COMPLEMENT, TEXTURE_COMPLEMENT, TFACTOR_COMPLEMENT,
                            SPECTACULAR_COMPLEMENT, TEMP_COMPLEMENT, CONSTANT_COMPLEMENT,
                            DIFFUSE_ALPHAREPLICATE, CURRENT_ALPHAREPLICATE, TEXTURE_ALPHAREPLICATE, TFACTOR_ALPHAREPLICATE,
                            SPECTACULAR_ALPHAREPLICATE, TEMP_ALPHAREPLICATE, CONSTANT_ALPHAREPLICATE
                    );
                }
            }

            private enum TEXINDEX implements EnumChoices {
                _0(0),
                _1(1),
                _2(2),
                _3(3),
                _4(4),
                _5(5),
                _6(6),
                _7(7),
                __(8);

                public final int identifier;

                TEXINDEX(int identifier) {
                    this.identifier = identifier;
                }

                @Override
                public String toString() {
                    return name().substring(1);
                }

                static TEXINDEX from(int i) {
                    return switch (i) {
                        case 0 -> _0;
                        case 1 -> _1;
                        case 2 -> _2;
                        case 3 -> _3;
                        case 4 -> _4;
                        case 5 -> _5;
                        case 6 -> _6;
                        case 7 -> _7;
                        default -> __;
                    };
                }

                @Override
                public Enum<? extends EnumChoices> getSelf() {
                    return this;
                }

                @Override
                public List<? extends Enum<? extends EnumChoices>> choices() {
                    return List.of(_0,_1,_2,_3,_4,_5,_6,_7);
                }
            }

            private enum D3DTEXTURETRANSFORMFLAGS implements EnumChoices {
                DISABLE("DISABLE",0x000),
                COUNT1("COUNT1", 0x001),
                COUNT2("COUNT2", 0x002),
                COUNT3("COUNT3", 0x003),
                COUNT4("COUNT4", 0x004),
                COUNT1_PROJECTED("COUNT1|PROJECTED", 0x101),
                COUNT2_PROJECTED("COUNT2|PROJECTED", 0x102),
                COUNT3_PROJECTED("COUNT3|PROJECTED", 0x103),
                COUNT4_PROJECTED("COUNT4|PROJECTED", 0x104);

                private final String name;
                public final int identifier;

                D3DTEXTURETRANSFORMFLAGS(String name, int identifier) {
                    this.name = name;
                    this.identifier = identifier;
                }

                @Override
                public String toString() {
                    return name;
                }

                static D3DTEXTURETRANSFORMFLAGS from(int i) {
                    return switch (i) {
                        case 0x001 -> COUNT1;
                        case 0x002 -> COUNT2;
                        case 0x003 -> COUNT3;
                        case 0x004 -> COUNT4;
                        case 0x101 -> COUNT1_PROJECTED;
                        case 0x102 -> COUNT2_PROJECTED;
                        case 0x103 -> COUNT3_PROJECTED;
                        case 0x104 -> COUNT4_PROJECTED;
                        default -> DISABLE;
                    };
                }

                @Override
                public Enum<? extends EnumChoices> getSelf() {
                    return this;
                }

                @Override
                public List<? extends Enum<? extends EnumChoices>> choices() {
                    return List.of(
                            DISABLE, COUNT1, COUNT2, COUNT3, COUNT4,
                            COUNT1_PROJECTED, COUNT2_PROJECTED, COUNT3_PROJECTED, COUNT4_PROJECTED
                    );
                }
            }
        }

        public static class SamplerStage implements DatingBachelor {
            private final int i;
            private D3DTEXTUREADDRESS addressU;
            private D3DTEXTUREADDRESS addressV;
            private D3DTEXTUREADDRESS addressW;
            private COLOR_RGBA bordercolor;

            SamplerStage(int i, BlockReader reader) throws IOException {
                this.i = i;
                addressU = D3DTEXTUREADDRESS.from(reader.readInt());
                addressV = D3DTEXTUREADDRESS.from(reader.readInt());
                addressW = D3DTEXTUREADDRESS.from(reader.readInt());
                bordercolor = new COLOR_RGBA(reader.readInt());
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
                        () -> bordercolor,
                        c -> bordercolor = c
                ));
            }

            @Override
            public String toString() {
                return "SampleStage: "+i;
            }

            private enum D3DTEXTUREADDRESS implements EnumChoices {
                WRAP(1),
                MIRROR(2),
                CLAMP(3),
                BORDER(4),
                MIRROR_ONCE(5),
                __(6);

                public final int identifier;

                D3DTEXTUREADDRESS(int identifier) {
                    this.identifier = identifier;
                }

                static D3DTEXTUREADDRESS from(int i) {
                    return switch (i) {
                        case 1 -> WRAP;
                        case 2 -> MIRROR;
                        case 3 -> CLAMP;
                        case 4 -> BORDER;
                        case 5 -> MIRROR_ONCE;
                        default -> __;
                    };
                }

                @Override
                public Enum<? extends EnumChoices> getSelf() {
                    return this;
                }

                @Override
                public List<? extends Enum<? extends EnumChoices>> choices() {
                    return List.of(WRAP, MIRROR, CLAMP, BORDER, MIRROR_ONCE);
                }
            }
        }
    }
}
