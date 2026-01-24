package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;
import dev.hipposgrumm.kamapreader.util.types.structs.*;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

import java.util.ArrayList;
import java.util.List;

public class PROReader {
    /**
     * Read PRO data.
     * @return Object data
     * @throws IllegalStateException If there was an issue reading the data.
     */
    public static PROData readPRO(BlockReader reader) {
        if (reader.readShortLittle() != PROCHUNK_SIGNATURE) throw new IllegalStateException("PRO file is not valid.");
        PROData data = new PROData();
        reader = reader.segment(reader.readIntLittle()-6);
        reader.setLittleEndian(true);

        while (reader.getRemaining() > 10) { // size of starting
            short tag = reader.readShort();
            int size = reader.readInt()-6; // size includes the 2 byte tag and 4 byte size value
            if (reader.getRemaining() < size) throw new IllegalStateException("PRO file ends prematurely "+reader.getRemaining()+" < "+size+" "+Integer.toHexString(reader.getTruePointer()));
            BlockReader chunkReader = reader.segment(size);
            data.Blocks.add(tag);
            switch (tag) {
                case PROCHUNK_VERSION -> data.Version = chunkReader.readFloat();
                case PROCHUNK_OBJECTNAME -> data.Name = chunkReader.readString();
                case PROCHUNK_SEGMENTS -> {
                    data.Segments = new PROData.Segment[chunkReader.readInt()];
                    PROData.Segment seg = null;
                    for (int i=0;i<data.Segments.length;) { // not incrementing here allows us to increment on our own terms
                        if (chunkReader.getRemaining() < 6) break;
                        short segTag = chunkReader.readShort();
                        int segSize = chunkReader.readInt()-6;
                        if (chunkReader.getRemaining() < segSize) throw new IllegalStateException("Segment in PRO ends prematurely; "+chunkReader.getRemaining()+" < "+segSize+" "+Integer.toHexString(chunkReader.getTruePointer()));
                        BlockReader segReader = chunkReader.segment(segSize);
                        if (segTag == PROCHUNK_SEGMENTNAME) {
                            if (seg != null) i++;
                            seg = new PROData.Segment();
                            seg.Name = segReader.readString();
                            data.Segments[i] = seg;
                        } else if (seg == null) {
                            throw new IllegalStateException("Segments in PRO is not valid.");
                        } else {
                            seg.Blocks.add(segTag);
                            switch (segTag) {
                                case PROCHUNK_SEGMENTFLAGS -> seg.Flags = segReader.readInt();
                                case PROCHUNK_SEGMENTBBOX -> {
                                    PR_BOUNDINGINFO bbox = new PR_BOUNDINGINFO();
                                    bbox.minX = segReader.readFloat();
                                    bbox.maxX = segReader.readFloat();
                                    bbox.minY = segReader.readFloat();
                                    bbox.maxY = segReader.readFloat();
                                    bbox.minZ = segReader.readFloat();
                                    bbox.maxZ = segReader.readFloat();
                                    for (int j=0;j<bbox.bbox.length;j++) bbox.bbox[j] = new PR_POINT(segReader.readFloat(), segReader.readFloat(), segReader.readFloat());
                                    for (int j=0;j<bbox.tbox.length;j++) bbox.tbox[j] = new PR_POINT(segReader.readFloat(), segReader.readFloat(), segReader.readFloat());
                                    bbox.radius = segReader.readFloat();
                                    seg.BBox = bbox;
                                }
                                case PROCHUNK_VERTICES -> {
                                    seg.Vertices = new PR_VERTEX[segReader.readInt()];
                                    for (int j=0;j<seg.Vertices.length;j++) {
                                        seg.Vertices[j] = new PR_VERTEX(
                                                new PR_POINT(segReader.readFloat(), segReader.readFloat(), segReader.readFloat()),
                                                segReader.readShort(), segReader.readShort(), segReader.readShort()
                                        );
                                    }
                                }
                                case PROCHUNK_FACES -> {
                                    seg.Faces = new PR_FACE[segReader.readInt()];
                                    for (int j=0;j<seg.Faces.length;j++) {
                                        seg.Faces[j] = new PR_FACE(segReader.readUShort(), segReader.readUShort(),
                                                segReader.readInt(), segReader.readInt(), segReader.readInt(),
                                                segReader.readFloat(), segReader.readFloat(), segReader.readFloat(), segReader.readFloat(), segReader.readFloat(), segReader.readFloat(),
                                                segReader.readBytes(144), // Unknown, possibly padding
                                                new INTCOLOR(INTCOLOR.Format.RGBA, segReader.readIntBig()), new INTCOLOR(INTCOLOR.Format.RGBA, segReader.readIntBig()), new INTCOLOR(INTCOLOR.Format.RGBA, segReader.readIntBig()),
                                                segReader.readBytes(7) // probably normals and flags but can't confirm it
                                        );
                                    }
                                }
                                case PROCHUNK_SEGBUF -> {
                                    if (data.SegBuffers == null) data.SegBuffers = new PR_SEGMENTBUFFER[data.Segments.length];
                                    PR_SEGMENTBUFFER segbuf = new PR_SEGMENTBUFFER();
                                    int lodcount = segReader.readInt(); // LOD count - unused/unhandled (in Master the Basics)
                                    if (lodcount != 0) throw new UnsupportedOperationException("LOD data found; was not expecting that!"); // TODO: Check this in 3DVW
                                    segbuf.MaterialBuffers = new PR_MATERIALBUFFER[segReader.readInt()];
                                    for (int j=0;j<segbuf.MaterialBuffers.length;j++) {
                                        PR_MATERIALBUFFER matbuf = new PR_MATERIALBUFFER(segReader.readInt());
                                        matbuf.VertexBuffers = new PR_VERTEXBUFFER[segReader.readInt()];
                                        for (int k=0;k<matbuf.VertexBuffers.length;k++) {
                                            PR_VERTEXBUFFER vbuf = new PR_VERTEXBUFFER();
                                            vbuf.NumVertices = segReader.readInt();
                                            vbuf.NumIndices = segReader.readInt();
                                            if (vbuf.NumIndices < 0) {
                                                vbuf.NumIndices = -vbuf.NumIndices;
                                                PR_VERTEXBUFFER.VBufFlags.NegativeIndices.apply(vbuf.Flags, true);
                                            }
                                            vbuf.FVF = new D3DFVF(segReader.readInt());
                                            vbuf.VertexSize = segReader.readInt();
                                            vbuf.VertexMapping = new UInteger.Array(vbuf.NumVertices);
                                            segbuf.TotalVertices += vbuf.NumVertices;
                                            segbuf.TotalFaces += vbuf.NumIndices / 3;
                                            vbuf.vertices = new FVFVertex[vbuf.NumVertices];
                                            for (int l=0;l<vbuf.NumVertices;l++) {
                                                vbuf.vertices[l] = FVFVertex.create(vbuf.FVF, segReader.segment(vbuf.VertexSize));
                                            }
                                            vbuf.indices = new UShort.Array(vbuf.NumVertices);
                                            for (int l=0;l<vbuf.NumIndices;l++) {
                                                vbuf.indices.array()[l] = segReader.readShort();
                                            }
                                            matbuf.VertexBuffers[k] = vbuf;
                                        }
                                        segbuf.MaterialBuffers[j] = matbuf;
                                    }
                                    data.SegBuffers[i] = segbuf;
                                }
                                default -> {
                                    switch (segTag) {
                                        default -> System.out.println("Unhandled seg tag: 0x"+Integer.toHexString(segTag).toUpperCase());
                                    }
                                    seg.UnknownBlocks.add(segReader.readBytes(segSize));
                                }
                            }
                        }
                    }
                }
                default -> {
                    switch (tag) {
                        case PROCHUNK_OBJECTFLAGS -> {} // Not read by the game.
                        default -> System.out.println("Unhandled PRO tag: 0x"+Integer.toHexString(tag).toUpperCase());
                    }
                    data.UnknownBlocks.add(chunkReader.readBytes(size));
                }
            }
        }
        return data;
    }

    public static void writePRO(BlockWriter writer, PROData data) {
        writer.writeShortLittle(PROCHUNK_SIGNATURE);
        writer = writer.segment();
        writer.setLittleEndian(true);
        writer.writeInt(0); // gets filled in later
        int unknownID = 0;
        for (short tag:data.Blocks) {
            writer.writeShort(tag);
            switch (tag) {
                case PROCHUNK_VERSION -> {
                    writer.writeInt(10);
                    writer.writeFloat(data.Version);
                }
                case PROCHUNK_OBJECTNAME -> {
                    writer.writeInt(data.Name.length()+7); // tag and size plus terminating char
                    writer.writeTerminatedString(data.Name);
                }
                case PROCHUNK_SEGMENTS -> {
                    BlockWriter segWriter = writer.segment();
                    segWriter.writeInt(0); // fill in after
                    segWriter.writeInt(data.Segments.length);
                    for (int i=0;i<data.Segments.length;i++) {
                        int segUnknownID = 0;
                        PROData.Segment seg = data.Segments[i];
                        segWriter.writeShort(PROCHUNK_SEGMENTNAME);
                        segWriter.writeInt(seg.Name.length()+7);
                        segWriter.writeTerminatedString(seg.Name);
                        for (Short segTag:seg.Blocks) {
                            segWriter.writeShort(segTag);
                            switch (segTag) {
                                case PROCHUNK_SEGMENTFLAGS -> {
                                    segWriter.writeInt(10);
                                    segWriter.writeInt(seg.Flags);
                                }
                                case PROCHUNK_SEGMENTBBOX -> {
                                    segWriter.writeInt(0xF4+6);
                                    segWriter.writeFloat(seg.BBox.minX);
                                    segWriter.writeFloat(seg.BBox.maxX);
                                    segWriter.writeFloat(seg.BBox.minY);
                                    segWriter.writeFloat(seg.BBox.maxY);
                                    segWriter.writeFloat(seg.BBox.minZ);
                                    segWriter.writeFloat(seg.BBox.maxZ);
                                    for (PR_POINT point:seg.BBox.bbox) {
                                        segWriter.writeFloat(point.X);
                                        segWriter.writeFloat(point.Y);
                                        segWriter.writeFloat(point.Z);
                                    }
                                    for (PR_POINT point:seg.BBox.tbox) {
                                        segWriter.writeFloat(point.X);
                                        segWriter.writeFloat(point.Y);
                                        segWriter.writeFloat(point.Z);
                                    }
                                    segWriter.writeFloat(seg.BBox.radius);
                                }
                                case PROCHUNK_VERTICES -> {
                                    BlockWriter vertWriter = segWriter.segment();
                                    vertWriter.writeInt(0);
                                    vertWriter.writeInt(seg.Vertices.length);
                                    for (PR_VERTEX vert:seg.Vertices) {
                                        vertWriter.writeFloat(vert.Position.X);
                                        vertWriter.writeFloat(vert.Position.Y);
                                        vertWriter.writeFloat(vert.Position.Z);
                                        vertWriter.writeShort(vert.NX);
                                        vertWriter.writeShort(vert.NY);
                                        vertWriter.writeShort(vert.NZ);
                                    }
                                    vertWriter.seek(0);
                                    vertWriter.writeInt(vertWriter.getSize()+2);
                                    vertWriter.seek(vertWriter.getSize());
                                }
                                case PROCHUNK_FACES -> {
                                    BlockWriter faceWriter = segWriter.segment();
                                    faceWriter.writeInt(0);
                                    faceWriter.writeInt(seg.Faces.length);
                                    for (PR_FACE face:seg.Faces) {
                                        faceWriter.writeUShort(face.Material);
                                        faceWriter.writeUShort(face.BackMaterial);
                                        faceWriter.writeInt(face.Vertex1);
                                        faceWriter.writeInt(face.Vertex2);
                                        faceWriter.writeInt(face.Vertex3);
                                        faceWriter.writeFloat(face.U1);
                                        faceWriter.writeFloat(face.V1);
                                        faceWriter.writeFloat(face.U2);
                                        faceWriter.writeFloat(face.V2);
                                        faceWriter.writeFloat(face.U3);
                                        faceWriter.writeFloat(face.V3);
                                        faceWriter.writeBytes(face.UNKNOWN1);
                                        faceWriter.writeIntBig(face.Col1.color);
                                        faceWriter.writeIntBig(face.Col2.color);
                                        faceWriter.writeIntBig(face.Col3.color);
                                        faceWriter.writeBytes(face.UNKNOWN2);
                                    }
                                    faceWriter.seek(0);
                                    faceWriter.writeInt(faceWriter.getSize()+2);
                                    faceWriter.seek(faceWriter.getSize());
                                }
                                case PROCHUNK_SEGBUF -> {
                                    if (data.SegBuffers == null) continue;
                                    PR_SEGMENTBUFFER segbuf = data.SegBuffers[i];
                                    if (segbuf == null) continue;
                                    BlockWriter segbufWriter = segWriter.segment();
                                    segbufWriter.writeInt(0);
                                    segbufWriter.writeInt(0); // LOD count - unused/unhandled (in Master the Basics)
                                    segbufWriter.writeInt(segbuf.MaterialBuffers.length);
                                    for (PR_MATERIALBUFFER matbuf:segbuf.MaterialBuffers) {
                                        segbufWriter.writeInt(matbuf.MaterialIndex);
                                        segbufWriter.writeInt(matbuf.VertexBuffers.length);
                                        for (PR_VERTEXBUFFER vbuf:matbuf.VertexBuffers) {
                                            segbufWriter.writeInt(vbuf.NumVertices);
                                            if (PR_VERTEXBUFFER.VBufFlags.NegativeIndices.from(vbuf.Flags)) {
                                                segbufWriter.writeInt(-vbuf.NumIndices);
                                            } else {
                                                segbufWriter.writeInt(vbuf.NumIndices);
                                            }
                                            segbufWriter.writeInt(vbuf.FVF.getValue());
                                            segbufWriter.writeInt(vbuf.VertexSize);
                                            for (FVFVertex vert:vbuf.vertices) {
                                                BlockWriter vertwriter = segbufWriter.segment();
                                                vert.write(vbuf.FVF, vertwriter);
                                                vertwriter.seek(segWriter.getSize());
                                            }
                                            for (short ind:vbuf.indices.array()) {
                                                segWriter.writeShort(ind);
                                            }
                                        }
                                    }
                                    segbufWriter.seek(0);
                                    segbufWriter.writeInt(segbufWriter.getSize()+2);
                                }
                                default -> {
                                    byte[] block = seg.UnknownBlocks.get(segUnknownID++);
                                    writer.writeInt(block.length+6);
                                    writer.writeBytes(block);
                                }
                            }
                        }
                    }
                    segWriter.seek(0);
                    segWriter.writeInt(segWriter.getSize()+2);
                }
                default -> {
                    byte[] block = data.UnknownBlocks.get(unknownID++);
                    writer.writeInt(block.length+6);
                    writer.writeBytes(block);
                }
            }
        }
        writer.seek(0);
        writer.writeInt(writer.getSize()+2);
    }

    public static class PROData {
        public float Version;
        public String Name;
        public Segment[] Segments;
        public PR_SEGMENTBUFFER[] SegBuffers;
        private final List<Short> Blocks = new ArrayList<>();
        private final List<byte[]> UnknownBlocks = new ArrayList<>();

        public static class Segment {
            public String Name;
            public int Flags; // Maybe replace this with a Flags object, if we ever find out what these are.
            public PR_BOUNDINGINFO BBox;
            public PR_VERTEX[] Vertices;
            public PR_FACE[] Faces;
            private final List<Short> Blocks = new ArrayList<>();
            private final List<byte[]> UnknownBlocks = new ArrayList<>();
        }
    }

    public static final short PROCHUNK_SIGNATURE          = 0x0303;
    public static final short PROCHUNK_VERSION            = 0x0000;
    public static final short PROCHUNK_OBJECTNAME         = 0x0100;
    public static final short PROCHUNK_OBJECTFLAGS        = 0x0101;
    public static final short PROCHUNK_SEGMENTS           = 0x1000;
    public static final short PROCHUNK_SEGMENTNAME        = 0x1010;
    public static final short PROCHUNK_SEGMENTFLAGS       = 0x1020;
    public static final short PROCHUNK_SEGMENTBBOX        = 0x1022;
    public static final short PROCHUNK_VERTICES           = 0x1030;
    public static final short PROCHUNK_FACES              = 0x1040;
    public static final short PROCHUNK_SEGBUF             = 0x1050;
    public static final short PROCHUNK_SEGBUF2            = 0x1051;
    public static final short PROCHUNK_SEGBUF3            = 0x1052;
    public static final short PROCHUNK_LODINFO            = 0x1060;

    public static final short PROCHUNK_KEYFRAME_PIVOT     = 0x1F00;
    public static final short PROCHUNK_KEYFRAME_MATRIX    = 0x1F10;
    public static final short PROCHUNK_KEYFRAME_ROTKEYS   = 0x1F20;
    public static final short PROCHUNK_KEYFRAME_POSKEYS   = 0x1F30;
    public static final short PROCHUNK_KEYFRAME_SCLKEYS   = 0x1F40;
    public static final short PROCHUNK_KEYFRAME_LINKS     = 0x1F50;
    public static final short PROCHUNK_SHADETABLELIST     = 0x2000;
    public static final short PROCHUNK_TEXTURELIST        = 0x2010;
    public static final short PROCHUNK_TEXTUREALPHA       = 0x2011;
    public static final short PROCHUNK_TEXTURESTAGE       = 0x2012;
    public static final short PROCHUNK_TEXTUREFORMAT      = 0x2013;
    public static final short PROCHUNK_TEXTUREMULTI       = 0x2014;
    public static final short PROCHUNK_MATERIALLIST       = 0x2100;
    public static final short PROCHUNK_MATERIAL_NAME      = 0x2101;
    public static final short PROCHUNK_MATERIAL_METHOD    = 0x2102;
    public static final short PROCHUNK_MATERIAL_TEXNUM    = 0x2103;
    public static final short PROCHUNK_MATERIAL_BASECOLOR = 0x2104;
    public static final short PROCHUNK_MATERIAL_SHADES    = 0x2105;
    public static final short PROCHUNK_MATERIAL_TABLE     = 0x2106;
    public static final short PROCHUNK_MATERIAL_ENVMAP    = 0x2107;
    public static final short PROCHUNK_MATERIAL_MIPMAP    = 0x2108;
    public static final short PROCHUNK_MATERIAL_COLOR     = 0x2109;
    public static final short PROCHUNK_MATERIAL_NUMSTAGES = 0x2110;
    public static final short PROCHUNK_MATERIAL_TEXNUM2   = 0x2111;
    public static final short PROCHUNK_MATERIAL_ENVMAP2   = 0x2112;
    public static final short PROCHUNK_MATERIAL_BUMP      = 0x2113;
    public static final short PROCHUNK_MATERIAL_SPECULAR  = 0x2114;
    public static final short PROCHUNK_MATERIAL_TWOSIDED  = 0x2115;
    public static final short PROCHUNK_MATERIAL_VERTEXSHADER_NAME  = 0x2116;
    public static final short PROCHUNK_MATERIAL_PIXELSHADER_NAME   = 0x2117;
    public static final short PROCHUNK_MATERIAL_NEXT_NAME = 0x2118;
    public static final short PROCHUNK_MATERIAL_EFFECT    = 0x2119;

    public static final short PROCHUNK_MATERIAL_END       = 0x2199;
    public static final short PROCHUNK_CAMERA             = 0x3000;
    public static final short PROCHUNK_OBJECTBBOX         = 0x3010;

    public static final short PROCHUNK_VERTEXSHADERLIST   = 0x3100;
    public static final short PROCHUNK_VERTEXSHADERLIST2  = 0x3101;
    public static final short PROCHUNK_VERTEXSHADERLIST3  = 0x3102;
    public static final short PROCHUNK_PIXELSHADERLIST    = 0x3200;
    public static final short PROCHUNK_PIXELSHADERLIST3   = 0x3202;
    public static final short PROCHUNK_PIXELSHADERLIST4   = 0x3203;

    public static final short PROCHUNK_TEX_COORDS         = 0x4000;

    public static final short PROCHUNK_RS_NAME            = 0x5000;
    public static final short PROCHUNK_RS_BOOLS           = 0x5100;
    public static final short PROCHUNK_RS_VALS            = 0x5200;
    public static final short PROCHUNK_RS_ENUMS           = 0x5300;
    public static final short PROCHUNK_RS_TEX             = 0x5400;
    public static final short PROCHUNK_RS_SAMP            = 0x5500;
}
