package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.util.types.Flags;
import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;
import dev.hipposgrumm.kamapreader.util.types.structs.*;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;

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
                case PROCHUNK_VERSION -> readVersion(chunkReader, data);
                case PROCHUNK_OBJECTNAME -> readObjectName(chunkReader, data);
                case PROCHUNK_SEGMENTS -> readSegments(chunkReader, data);
                case PROCHUNK_MATERIALLIST -> readMaterials(chunkReader, data);
                case PROCHUNK_OBJECTBBOX -> readObjectBBox(chunkReader, data);
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
                case PROCHUNK_VERSION -> writeVersion(writer, data);
                case PROCHUNK_OBJECTNAME -> writeObjectName(writer, data);
                case PROCHUNK_SEGMENTS -> writeSegments(writer, data);
                case PROCHUNK_MATERIALLIST -> writeMaterials(writer, data);
                case PROCHUNK_OBJECTBBOX -> writeObjectBBox(writer, data);
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

    private static void readVersion(BlockReader reader, PROData data) {
        data.Version = reader.readFloat();
    }

    private static void writeVersion(BlockWriter writer, PROData data) {
        writer.writeInt(10); // full block size
        writer.writeFloat(data.Version);
    }

    private static void readObjectName(BlockReader reader, PROData data) {
        data.Name = reader.readString();
    }

    private static void writeObjectName(BlockWriter writer, PROData data) {
        writer.writeInt(data.Name.length()+7); // tag and size plus terminating char
        writer.writeTerminatedString(data.Name);
    }

    private static void readObjectBBox(BlockReader reader, PROData data) {
        data.BBox = new PR_BOUNDINGINFO(reader);
    }

    private static void writeObjectBBox(BlockWriter writer, PROData data) {
        writer.writeInt(0xF4+6);
        data.BBox.write(writer);
    }

    private static void readSegments(BlockReader reader, PROData data) {
        data.Segments = new PROData.Segment[reader.readInt()];
        PROData.Segment seg = null;
        for (int i=0;i<data.Segments.length;) { // not incrementing here allows us to increment on our own terms
            if (reader.getRemaining() < 6) break;
            short segTag = reader.readShort();
            int segSize = reader.readInt()-6;
            if (reader.getRemaining() < segSize) throw new IllegalStateException("Segment in PRO ends prematurely; "+reader.getRemaining()+" < "+segSize+" "+Integer.toHexString(reader.getTruePointer()));
            BlockReader segReader = reader.segment(segSize);
            if (segTag == PROCHUNK_SEGMENTNAME) {
                if (seg != null) i++;
                seg = new PROData.Segment();
                seg.Name = segReader.readString();
                seg.i = i;
                data.Segments[i] = seg;
                continue;
            } else if (seg == null) {
                throw new IllegalStateException("Segments in PRO is not valid.");
            }
            seg.Blocks.add(segTag);
            switch (segTag) {
                case PROCHUNK_SEGMENTFLAGS -> readSegmentFlags(segReader, seg);
                case PROCHUNK_SEGMENTBBOX -> readSegmentBBox(segReader, seg);
                case PROCHUNK_VERTICES -> readSegmentVertices(segReader, seg);
                case PROCHUNK_FACES -> readSegmentFaces(segReader, seg);
                case PROCHUNK_SEGBUF, PROCHUNK_SEGBUF3 ->
                        readSegmentBuffer(segReader, data, i, segTag & 0b11);
                case PROCHUNK_KEYFRAME_PIVOT -> readSegmentPivot(segReader, seg);
                case PROCHUNK_KEYFRAME_LINKS -> readSegmentLinks(segReader, seg);
                default -> {
                    switch (segTag) {
                        case PROCHUNK_SEGBUF2 -> System.err.println(
                                "SegmentBuffer2 data is currently not readable because the corresponding code is too mangled and there are no usages that I could find.\n"+
                                        "If you see this message, please make a github ticket with a way to access the file you opened."
                        );
                        case PROCHUNK_TEX_COORDS,
                             PROCHUNK_KEYFRAME_MATRIX,
                             PROCHUNK_KEYFRAME_ROTKEYS,
                             PROCHUNK_KEYFRAME_POSKEYS,
                             PROCHUNK_KEYFRAME_SCLKEYS -> {} // I could find no examples of this being used or not empty.
                        default -> System.out.println("Unhandled seg tag: 0x"+Integer.toHexString(segTag).toUpperCase());
                    }
                    seg.UnknownBlocks.add(segReader.readBytes(segSize));
                }
            }
        }
        finalizeSegmentLinks(data);
    }

    private static void writeSegments(BlockWriter writer, PROData data) {
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
                    case PROCHUNK_SEGMENTFLAGS -> writeSegmentFlags(segWriter, seg);
                    case PROCHUNK_SEGMENTBBOX -> writeSegmentBBox(segWriter, seg);
                    case PROCHUNK_VERTICES -> writeSegmentVertices(segWriter, seg);
                    case PROCHUNK_FACES -> writeSegmentFaces(segWriter, seg);
                    case PROCHUNK_SEGBUF, PROCHUNK_SEGBUF3 -> {
                        writeSegmentBuffer(segWriter, data, i, segTag & 0b11);
                    }
                    case PROCHUNK_KEYFRAME_PIVOT -> writeSegmentPivot(segWriter, seg);
                    case PROCHUNK_KEYFRAME_LINKS -> writeSegmentLinks(segWriter, seg);
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

    private static void readSegmentFlags(BlockReader reader, PROData.Segment seg) {
        seg.Flags = new PROData.Segment.SegmentFlags(reader.readInt());
    }

    private static void writeSegmentFlags(BlockWriter writer, PROData.Segment seg) {
        writer.writeInt(10);
        writer.writeInt(seg.Flags.getValue());
    }

    private static void readSegmentBBox(BlockReader reader, PROData.Segment seg) {
        seg.BBox = new PR_BOUNDINGINFO(reader);
    }

    private static void writeSegmentBBox(BlockWriter writer, PROData.Segment seg) {
        writer.writeInt(0xF4+6);
        seg.BBox.write(writer);
    }

    private static void readSegmentVertices(BlockReader reader, PROData.Segment seg) {
        seg.Vertices = new PR_VERTEX[reader.readInt()];
        for (int j=0;j<seg.Vertices.length;j++) {
            seg.Vertices[j] = new PR_VERTEX(reader);
        }
    }

    private static void writeSegmentVertices(BlockWriter writer, PROData.Segment seg) {
        BlockWriter vertWriter = writer.segment();
        vertWriter.writeInt(0);
        vertWriter.writeInt(seg.Vertices.length);
        for (PR_VERTEX vert:seg.Vertices) vert.write(vertWriter);
        vertWriter.seek(0);
        vertWriter.writeInt(vertWriter.getSize()+2);
    }

    private static void readSegmentFaces(BlockReader reader, PROData.Segment seg) {
        seg.Faces = new PR_FACE[reader.readInt()];
        for (int j=0;j<seg.Faces.length;j++) {
            seg.Faces[j] = new PR_FACE(reader);
        }
    }

    private static void writeSegmentFaces(BlockWriter writer, PROData.Segment seg) {
        BlockWriter faceWriter = writer.segment();
        faceWriter.writeInt(0);
        faceWriter.writeInt(seg.Faces.length);
        for (PR_FACE face:seg.Faces) face.write(faceWriter);
        faceWriter.seek(0);
        faceWriter.writeInt(faceWriter.getSize()+2);
    }

    private static void readSegmentBuffer(BlockReader reader, PROData data, int i, int variant) {
        if (variant == 1) throw new IllegalArgumentException("SegmentBuffer2 Data is currently not readable (corresponding code is too mangled)");
        // From now on variant is always 0 or 2; this should make thing easier
        boolean secondVariant = variant == 2; // convenience variable; replace if SegBuf2 becomes readable

        if (data.SegBuffers == null) return;
        PR_SEGMENTBUFFER segbuf = new PR_SEGMENTBUFFER();
        int lodcount = reader.readInt(); // LOD count - unused/unhandled (in Master the Basics)
        if (lodcount != 0) throw new UnsupportedOperationException("LOD data found; was not expecting that!"); // TODO: Check this in 3DVW
        segbuf.ShareVertexData = secondVariant && reader.readInt() != 0;
        segbuf.MaterialBuffers = new PR_MATERIALBUFFER[reader.readInt()];
        for (int j=0;j<segbuf.MaterialBuffers.length;j++) {
            PR_MATERIALBUFFER matbuf = new PR_MATERIALBUFFER(reader.readInt());
            matbuf.VertexBuffers = new PR_VERTEXBUFFER[reader.readInt()];
            for (int k=0;k<matbuf.VertexBuffers.length;k++) {
                PR_VERTEXBUFFER vbuf = new PR_VERTEXBUFFER();
                vbuf.NumVertices = reader.readInt();
                if (secondVariant && vbuf.NumVertices > 0xFFFF) {
                    PR_VERTEXBUFFER.VBufFlags.Uses32BitIndices.apply(vbuf.Flags, true);
                }
                vbuf.NumIndices = reader.readInt();
                if (vbuf.NumIndices < 0) {
                    vbuf.NumIndices = -vbuf.NumIndices;
                    PR_VERTEXBUFFER.VBufFlags.NegativeIndices.apply(vbuf.Flags, true);
                }
                vbuf.FVF = new D3DFVF(reader.readInt());
                vbuf.VertexSize = reader.readInt();
                segbuf.TotalVertices += vbuf.NumVertices;
                segbuf.TotalFaces += vbuf.NumIndices / 3;
                if (!segbuf.ShareVertexData || i==0) {
                    if (secondVariant) {
                        vbuf.VertexMapping = new UInteger.Array(vbuf.NumVertices);
                        for (int l = 0; l < vbuf.NumVertices; l++) {
                            vbuf.VertexMapping.array()[l] = reader.readInt();
                        }
                    }
                    vbuf.vertices = new FVFVertex[vbuf.NumVertices];
                    for (int l = 0; l < vbuf.NumVertices; l++) {
                        vbuf.vertices[l] = FVFVertex.create(vbuf.FVF, reader.segment(vbuf.VertexSize));
                    }
                } else {
                    vbuf.VertexMapping = segbuf.MaterialBuffers[0].VertexBuffers[0].VertexMapping;
                }
                vbuf.indices = new int[vbuf.NumIndices];
                if (secondVariant && vbuf.NumVertices > 0xFFFF) {
                    for (int l=0;l<vbuf.NumIndices;l++)
                        vbuf.indices[l] = reader.readInt();
                } else {
                    for (int l=0;l<vbuf.NumIndices;l++)
                        vbuf.indices[l] = reader.readUShortDirect();
                }
                if (secondVariant) {
                    int matrices = reader.readInt();
                    if (matrices > 0) {
                        vbuf.MatrixPalette = new PR_MATRIXPALETTE();
                        vbuf.MatrixPalette.MatrixNumbers = new int[matrices];
                        vbuf.MatrixPalette.FirstConstant = reader.readInt();
                        for (int l=0;l<matrices;l++) {
                            vbuf.MatrixPalette.MatrixNumbers[l] = reader.readInt();
                        }
                    }
                    vbuf.MYSTERY1 = reader.readInt();
                    vbuf.MYSTERY2 = reader.readInt();
                }
                matbuf.VertexBuffers[k] = vbuf;
            }
            segbuf.MaterialBuffers[j] = matbuf;
        }
        data.SegBuffers[i] = segbuf;
    }

    private static void writeSegmentBuffer(BlockWriter writer, PROData data, int i, int variant) {
        if (variant == 1) throw new IllegalArgumentException("SegmentBuffer2 Data is currently not writable (corresponding code is too mangled)");
        // From now on variant is always 0 or 2; this should make thing easier
        boolean secondVariant = variant == 2; // convenience variable; replace if SegBuf2 becomes readable

        if (data.SegBuffers == null) return;
        PR_SEGMENTBUFFER segbuf = data.SegBuffers[i];
        if (segbuf == null) return;

        BlockWriter segbufWriter = writer.segment();
        segbufWriter.writeInt(0);
        segbufWriter.writeInt(0); // LOD count - unused/unhandled (in Master the Basics)
        if (secondVariant) segbufWriter.writeInt(segbuf.ShareVertexData ? 1 : 0);
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
                if (!segbuf.ShareVertexData || i==0) {
                    if (secondVariant) {
                        for (int mapping:vbuf.VertexMapping.array()) {
                            segbufWriter.writeInt(mapping);
                        }
                    }
                    for (FVFVertex vert:vbuf.vertices) {
                        BlockWriter vertwriter = segbufWriter.segment();
                        vert.write(vbuf.FVF, vertwriter);
                    }
                }
                for (int ind:vbuf.indices) {
                    if (secondVariant && vbuf.NumVertices > 0xFFFF)
                        segbufWriter.writeInt(ind);
                    else segbufWriter.writeUShortDirect(ind);
                }
                if (secondVariant) {
                    if (vbuf.MatrixPalette != null) {
                        segbufWriter.writeInt(vbuf.MatrixPalette.MatrixNumbers.length);
                        segbufWriter.writeInt(vbuf.MatrixPalette.FirstConstant);
                        for (int num:vbuf.MatrixPalette.MatrixNumbers) {
                            segbufWriter.writeInt(num);
                        }
                    } else segbufWriter.writeInt(0);
                    segbufWriter.writeInt(vbuf.MYSTERY1);
                    segbufWriter.writeInt(vbuf.MYSTERY2);
                }
            }
        }
        segbufWriter.seek(0);
        segbufWriter.writeInt(segbufWriter.getSize()+2);
    }

    private static void readSegmentPivot(BlockReader reader, PROData.Segment seg) {
        seg.Pivot = new PR_POINT(reader);
    }

    private static void writeSegmentPivot(BlockWriter writer, PROData.Segment seg) {
        writer.writeInt(12);
        seg.Pivot.write(writer);
    }

    private static void readSegmentLinks(BlockReader reader, PROData.Segment seg) {
        seg.children = reader.readInt();
        seg.next = reader.readInt();
        seg.parent = reader.readInt();
    }

    private static void finalizeSegmentLinks(PROData data) {
        for (PROData.Segment seg:data.Segments) {
            if (seg.children != -1) {
                PROData.Segment child = data.Segments[seg.children];
                int s=1; for (PROData.Segment c=child;
                     c.next!=-1;
                     c=data.Segments[c.next]
                ) s++;
                seg.Children = new PROData.Segment[s];
                s=0; for (PROData.Segment c=child;
                     c.next!=-1;
                     c=data.Segments[c.next]
                ) seg.Children[s++] = c;
            }
            if (seg.next != -1) seg.Next = data.Segments[seg.next];
            if (seg.parent != -1) seg.Parent = data.Segments[seg.parent];
        }
    }

    private static void writeSegmentLinks(BlockWriter writer, PROData.Segment seg) {
        writer.writeInt(12);
        if (seg.Children.length == 0) writer.writeInt(-1);
        else writer.writeInt(seg.Children[0].i);
        if (seg.Next != null) writer.writeInt(seg.Next.i);
        if (seg.Parent != null) writer.writeInt(seg.Parent.i);
    }

    private static void readMaterials(BlockReader reader, PROData data) {
        PROData.MaterialDat matdat = null;
        while (reader.getRemaining() > 0) {
            short segTag = reader.readShort();
            int segSize = reader.readInt()-6;
            if (reader.getRemaining() < segSize) throw new IllegalStateException("Material in PRO ends prematurely; "+reader.getRemaining()+" < "+segSize+" "+Integer.toHexString(reader.getTruePointer()));
            BlockReader matReader = reader.segment(segSize);
            if (segTag == PROCHUNK_MATERIAL_NAME) {
                matdat = new PROData.MaterialDat();
                if (data.Materials == null) {
                    data.Materials = new PROData.MaterialDat[1];
                    data.Materials[0] = matdat;
                } else {
                    PROData.MaterialDat[] last = data.Materials;
                    data.Materials = new PROData.MaterialDat[last.length+1];
                    System.arraycopy(last, 0, data.Materials, 0, last.length);
                    data.Materials[last.length] = matdat;
                }
                matdat.Name = matReader.readString();
                continue;
            } else if (matdat == null) {
                throw new IllegalStateException("Materials in PRO is not valid.");
            } else if (segTag == PROCHUNK_MATERIAL_END) {
                matdat = null;
                continue;
            }
            matdat.Blocks.add(segTag);
            switch (segTag) {
                case PROCHUNK_MATERIAL_EFFECT -> // Not actually effect, but the UID of the target material.
                        readMaterialUID(matReader, matdat);
                default -> {
                    System.out.println("Unhandled mat tag: 0x"+Integer.toHexString(segTag).toUpperCase());
                    matdat.UnknownBlocks.add(matReader.readBytes(segSize));
                }
            }
        }
    }

    private static void writeMaterials(BlockWriter writer, PROData data) {
        BlockWriter matWriter = writer.segment();
        matWriter.writeInt(0); // fill in after
        for (int i=0;i<data.Materials.length;i++) {
            int segUnknownID = 0;
            PROData.MaterialDat matdat = data.Materials[i];
            matWriter.writeShort(PROCHUNK_MATERIAL_NAME);
            matWriter.writeInt(matdat.Name.length()+7);
            matWriter.writeTerminatedString(matdat.Name);
            for (Short segTag:matdat.Blocks) {
                matWriter.writeShort(segTag);
                switch (segTag) {
                    case PROCHUNK_MATERIAL_EFFECT -> writeMaterialUID(matWriter, matdat);
                    default -> {
                        byte[] block = matdat.UnknownBlocks.get(segUnknownID++);
                        writer.writeInt(block.length+6);
                        writer.writeBytes(block);
                    }
                }
            }
            matWriter.writeShort(PROCHUNK_MATERIAL_END);
            matWriter.writeInt(6);
        }
        matWriter.seek(0);
        matWriter.writeInt(matWriter.getSize()+2);
    }

    private static void readMaterialUID(BlockReader reader, PROData.MaterialDat matdat) {
        matdat.UID = reader.readInt();
    }

    private static void writeMaterialUID(BlockWriter writer, PROData.MaterialDat matdat) {
        writer.writeInt(10);
        writer.writeInt(matdat.UID);
    }

    public static class PROData {
        public float Version;
        public String Name;
        public Segment[] Segments;
        public MaterialDat[] Materials;
        public PR_SEGMENTBUFFER[] SegBuffers;
        public PR_BOUNDINGINFO BBox;

        private final List<Short> Blocks = new ArrayList<>();
        private final List<byte[]> UnknownBlocks = new ArrayList<>();

        public static class Segment {
            public String Name;
            public SegmentFlags Flags; // Maybe replace this with a Flags object, if we ever find out what these are.
            public PR_BOUNDINGINFO BBox;
            public PR_VERTEX[] Vertices;
            public PR_FACE[] Faces;
            public PR_POINT Pivot;
            public Segment[] Children = new Segment[0];
            public Segment Parent = null;
            public Segment Next = null;

            private int i = -1;
            private int children = -1;
            private int next = -1;
            private int parent = -1;

            private final List<Short> Blocks = new ArrayList<>();
            private final List<byte[]> UnknownBlocks = new ArrayList<>();

            public static class SegmentFlags extends Flags {
                public static final BoolEntry SUBSEG = new BoolEntry("SUBSEG", 1);
                public static final BoolEntry NOTRANSFORM = new BoolEntry("NOTRANSFORM", 2);
                public static final BoolEntry SELECTED = new BoolEntry("SELECTED", 3);
                public static final BoolEntry NORENDER = new BoolEntry("NORENDER", 4);
                public static final BoolEntry NORMALS = new BoolEntry("NORMALS", 5);
                public static final BoolEntry ALWAYS_VISIBLE = new BoolEntry("ALWAYS_VISIBLE", 6);
                public static final BoolEntry SPRITE = new BoolEntry("SPRITE", 7);
                public static final BoolEntry LOOP_ANIMATION = new BoolEntry("LOOP_ANIMATION", 8);
                public static final BoolEntry COMPLETELY_VISIBLE = new BoolEntry("COMPLETELY_VISIBLE", 9);
                public static final BoolEntry D3DPIPELINE = new BoolEntry("D3DPIPELINE", 10);
                public static final BoolEntry UPDATEMATRIX = new BoolEntry("UPDATEMATRIX", 11);
                public static final BoolEntry SELECTIONTEST = new BoolEntry("SELECTIONTEST", 12);

                public SegmentFlags(int value) {
                    super(value);
                }

                @Override
                public Entry[] getEntries() {
                    return new Entry[0];
                }
            }
        }

        public static class MaterialDat {
            public String Name;
            public int UID;

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
