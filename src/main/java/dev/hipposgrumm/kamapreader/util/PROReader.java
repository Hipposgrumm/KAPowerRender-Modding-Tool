package dev.hipposgrumm.kamapreader.util;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;
import dev.hipposgrumm.kamapreader.util.types.structs.*;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

import java.io.IOException;

public class PROReader {
    /**
     * Read PRO data.
     * @return Object data
     * @throws IllegalStateException If there was an issue reading the data.
     */
    public static PROData readPRO(BlockReader reader) throws IOException {
        if (reader.readShort() != PROCHUNK_SIGNATURE) throw new IllegalStateException("PRO file is not valid.");
        PROData data = new PROData();
        BlockReader fullReader = reader;
        reader = reader.segment(reader.readInt()-6);

        while (reader.getRemaining() > 10) { // size of starting
            short tag = reader.readShort();
            int size = reader.readInt()-6; // size includes the 2 byte tag and 4 byte size value
            int start = reader.getPointer();
            if (reader.getRemaining() < size) throw new IllegalStateException("PRO file ends prematurely.");
            switch (tag) {
                case PROCHUNK_VERSION -> data.Version = reader.readFloat();
                case PROCHUNK_OBJECTNAME -> data.Name = reader.readString();
                case PROCHUNK_OBJECTFLAGS -> {} // Not read by the game.
                case PROCHUNK_SEGMENTS -> {
                    BlockReader segReader = reader.segment(size);
                    data.Segments = new PROData.Segment[segReader.readInt()];
                    for (int i=0;i<data.Segments.length;) {
                        int segTag = reader.readShort();
                        int segSize = reader.readInt()-6;
                        int segStart = reader.getPointer();
                        if (segReader.getRemaining() < segSize) throw new IllegalStateException("Segment in PRO ends prematurely.");
                        if (segTag == PROCHUNK_SEGMENTNAME) {
                            if (data.Segments[i] != null) i++;
                            data.Segments[i] = new PROData.Segment();
                            data.Segments[i].Name = segReader.readString(segSize);
                        } else if (data.Segments[i] == null) {
                            throw new IllegalStateException("Segments in PRO is not valid.");
                        } else switch (segTag) {
                            case PROCHUNK_SEGMENTFLAGS -> data.Segments[i].Flags = segReader.readInt();
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
                                data.Segments[i].BBox = bbox;
                            }
                            case PROCHUNK_VERTICES -> {
                                data.Segments[i].Vertices = new PR_VERTEX[segReader.readInt()];
                                for (int j=0;j<data.Segments[i].Vertices.length;j++) {
                                    data.Segments[i].Vertices[j] = new PR_VERTEX(
                                            new PR_POINT(segReader.readFloat(), segReader.readFloat(), segReader.readFloat()),
                                            segReader.readShort(), segReader.readShort(), segReader.readShort()
                                    );
                                }
                            }
                            case PROCHUNK_FACES -> {
                                data.Segments[i].Faces = new PR_FACE[segReader.readInt()];
                                for (int j=0;j<data.Segments[i].Faces.length;j++) {
                                    data.Segments[i].Faces[j] = new PR_FACE(segReader.readUShort(), segReader.readUShort(),
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
                                    for (int k=0;k<segbuf.MaterialBuffers[j].VertexBuffers.length;k++) {
                                        PR_VERTEXBUFFER vbuf = new PR_VERTEXBUFFER();
                                        vbuf.NumVertices = segReader.readInt();
                                        vbuf.NumIndices = segReader.readInt();
                                        if (vbuf.NumIndices < 0) {
                                            vbuf.NumIndices = -vbuf.NumIndices;
                                            PR_VERTEXBUFFER.VBufFlags.NegativeIndices.apply(vbuf.Flags, true);
                                        }
                                        vbuf.FVF = new D3DFVF.AsFlags(segReader.readInt());
                                        vbuf.VertexSize = segReader.readInt();
                                        vbuf.VertexMapping = new UInteger.Array(vbuf.NumVertices);
                                        segbuf.TotalVertices += vbuf.NumVertices;
                                        segbuf.TotalFaces += vbuf.NumIndices / 3;
                                        vbuf.vertices = new FVFVertex[vbuf.NumVertices];
                                        for (int l=0;l< vbuf.NumVertices;l++) {
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
                        }
                        segReader.seek(segStart+segSize);
                    }
                }
            }
            reader.seek(start+size);
        }

        fullReader.move(fullReader.getRemaining());
        return data;
    }

    public static class PROData {
        public float Version;
        public String Name;
        public Segment[] Segments;
        public PR_SEGMENTBUFFER[] SegBuffers;

        public static class Segment {
            public String Name;
            public int Flags; // Maybe replace this with a Flags object, if we ever find out what these are.
            public PR_BOUNDINGINFO BBox;
            public PR_VERTEX[] Vertices;
            public PR_FACE[] Faces;
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
