package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;
import dev.hipposgrumm.kamapreader.util.types.structs.*;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PROReader {
    /**
     * Read PRO data.
     * @return Object data
     * @throws IllegalStateException If there was an issue reading the data.
     */
    public static PROData readPRO(BlockReader reader) throws IOException {
        if (reader.readShortLittle() != PROCHUNK_SIGNATURE) throw new IllegalStateException("PRO file is not valid.");
        PROData data = new PROData();
        BlockReader fullReader = reader;
        reader = reader.segment(reader.readIntLittle()-6);
        reader.setLittleEndian(true);

        while (reader.getRemaining() > 10) { // size of starting
            short tag = reader.readShort();
            int size = reader.readInt()-6; // size includes the 2 byte tag and 4 byte size value
            int start = reader.getPointer();
            if (reader.getRemaining() < size) throw new IllegalStateException("PRO file ends prematurely."+reader.getRemaining()+" < "+size);
            data.Blocks.add(tag);
            switch (tag) {
                case PROCHUNK_VERSION -> data.Version = reader.readFloat();
                case PROCHUNK_OBJECTNAME -> data.Name = reader.readString();
                case PROCHUNK_SEGMENTS -> {
                    BlockReader segReader = reader.segment(size);
                    data.Segments = new PROData.Segment[segReader.readInt()];
                    PROData.Segment seg = null;
                    for (int i=0;i<data.Segments.length;) { // not incrementing here allows us to increment on our own terms
                        if (segReader.getRemaining() < 6) break;
                        short segTag = segReader.readShort();
                        int segSize = segReader.readInt()-6;
                        int segStart = segReader.getPointer();
                        if (segReader.getRemaining() < segSize) throw new IllegalStateException("Segment in PRO ends prematurely; "+segReader.getRemaining()+" < "+segSize+" "+Integer.toHexString(segReader.getTruePointer()));
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
                                    bbox.minX = reader.readFloat();
                                    bbox.maxX = reader.readFloat();
                                    bbox.minY = reader.readFloat();
                                    bbox.maxY = reader.readFloat();
                                    bbox.minZ = reader.readFloat();
                                    bbox.maxZ = reader.readFloat();
                                    for (int j=0;j<bbox.bbox.length;j++) bbox.bbox[j] = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
                                    for (int j=0;j<bbox.tbox.length;j++) bbox.tbox[j] = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
                                    bbox.radius = reader.readFloat();
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
                                                new INTCOLOR(INTCOLOR.Format.RGBA, Integer.reverseBytes(segReader.readInt())), new INTCOLOR(INTCOLOR.Format.RGBA, Integer.reverseBytes(segReader.readInt())), new INTCOLOR(INTCOLOR.Format.RGBA, Integer.reverseBytes(segReader.readInt())),
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
                                                BlockReader vertreader = segReader.segment(vbuf.VertexSize);
                                                vbuf.vertices[l] = FVFVertex.create(vbuf.FVF, vertreader);
                                                vertreader.move(vertreader.getRemaining());
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
                                    seg.UnknownBlocks.add(reader.readBytes(segSize));
                                }
                            }
                        }
                        segReader.seek(segStart+segSize);
                    }
                }
                default -> {
                    switch (tag) {
                        case PROCHUNK_OBJECTFLAGS -> {} // Not read by the game.
                        default -> System.out.println("Unhandled PRO tag: 0x"+Integer.toHexString(tag).toUpperCase());
                    }
                    data.UnknownBlocks.add(reader.readBytes(size));
                }
            }
            reader.seek(start+size);
        }

        fullReader.move(fullReader.getRemaining());
        return data;
    }

    public static void writePRO(BlockWriter writer, PROData data) throws IOException {
        writer = writer.segment();
        writer.setLittleEndian(true);
        writer.writeShort(PROCHUNK_SIGNATURE);
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
                    int segUnknownID = 0;
                    for (int i=0;i<data.Segments.length;i++) {
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
                                    for (PR_VERTEX vert:seg.Vertices) {
                                        vertWriter.writeFloat(vert.Position.X);
                                        vertWriter.writeFloat(vert.Position.Y);
                                        vertWriter.writeFloat(vert.Position.Z);
                                        vertWriter.writeShort(vert.NX);
                                        vertWriter.writeShort(vert.NY);
                                        vertWriter.writeShort(vert.NZ);
                                    }
                                    vertWriter.seek(0);
                                    vertWriter.writeInt(vertWriter.getSize());
                                    vertWriter.seek(vertWriter.getSize());
                                }
                                case PROCHUNK_FACES -> {
                                    BlockWriter faceWriter = segWriter.segment();
                                    faceWriter.writeInt(0);
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
                                        faceWriter.writeInt(Integer.reverseBytes(face.Col1.color));
                                        faceWriter.writeInt(Integer.reverseBytes(face.Col2.color));
                                        faceWriter.writeInt(Integer.reverseBytes(face.Col3.color));
                                        faceWriter.writeBytes(face.UNKNOWN2);
                                    }
                                    faceWriter.seek(0);
                                    faceWriter.writeInt(faceWriter.getSize());
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
                                }
                                default -> writer.writeBytes(seg.UnknownBlocks.get(segUnknownID++));
                            }
                        }
                    }
                    segWriter.seek(0);
                    segWriter.writeInt(segWriter.getSize()+2);
                    segWriter.seek(segWriter.getSize());
                }
                default -> writer.writeBytes(data.UnknownBlocks.get(unknownID++));
            }
        }
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
