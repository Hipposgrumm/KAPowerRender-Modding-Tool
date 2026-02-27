package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.util.types.Flags;
import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;

public class PR_VERTEXBUFFER {
    public int NumVertices;
    public int NumIndices;
    public VBufFlags Flags = new VBufFlags(0);
    public D3DFVF FVF;
    public int VertexSize;
    public UInteger.Array VertexMapping;
    public FVFVertex[] vertices;
    public int[] indices;
    public PR_MATRIXPALETTE MatrixPalette;
    public int MYSTERY1, MYSTERY2;

    public static class VBufFlags extends Flags {
        public static final BoolEntry RequiresSoftware = new BoolEntry("RequiresSoftware", 0); // Set within PowerRender; probably always going to be off with today's technology, plus the fact that KA doesn't set the data this flag is tied to.
        public static final BoolEntry TriangleStripMode = new BoolEntry("TrianleStripMode", 1);
        public static final BoolEntry Uses32BitIndices = new BoolEntry("32BitIndices", 5);

        public VBufFlags(int value) {
            super(value);
        }

        @Override
        public Entry[] getEntries() {
            return new Entry[] {TriangleStripMode};
        }
    }
}
