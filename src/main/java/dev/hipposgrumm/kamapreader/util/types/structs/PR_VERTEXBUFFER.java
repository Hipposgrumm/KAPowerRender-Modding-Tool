package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.util.types.Flags;
import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

public class PR_VERTEXBUFFER {
    public int NumVertices;
    public int NumIndices;
    public VBufFlags Flags = new VBufFlags(0);
    public D3DFVF.AsFlags FVF;
    public int VertexSize;
    public UInteger.Array VertexMapping;
    public FVFVertex[] vertices;
    public UShort.Array indices;

    public static class VBufFlags extends Flags {
        public static final BoolEntry RequiresSoftware = new BoolEntry("RequiresSoftware", 0); // Set within PowerRender; probably always going to be off with today's technology, plus the fact that KA doesn't set the data this flag is tied to.
        public static final BoolEntry NegativeIndices = new BoolEntry("NegativeIndices", 1);

        public VBufFlags(int value) {
            super(value);
        }

        @Override
        public Entry[] getEntries() {
            return new Entry[] {NegativeIndices};
        }
    }
}
