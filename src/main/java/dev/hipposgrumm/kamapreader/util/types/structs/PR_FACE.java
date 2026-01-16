package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

public class PR_FACE {
    public UShort Material, BackMaterial;
    public int Vertex1, Vertex2, Vertex3;
    public float U1,V1, U2,V2, U3,V3;
    public final byte[] UNKNOWN1;
    public INTCOLOR Col1, Col2, Col3;
    public final byte[] UNKNOWN2;

    public PR_FACE() {
        this.Material = new UShort(0);
        this.BackMaterial = new UShort(0);
        this.UNKNOWN1 = new byte[144];
        this.Col1 = new INTCOLOR(INTCOLOR.Format.RGBA, -1);
        this.Col2 = new INTCOLOR(INTCOLOR.Format.RGBA, -1);
        this.Col3 = new INTCOLOR(INTCOLOR.Format.RGBA, -1);
        this.UNKNOWN2 = new byte[7];
    }

    public PR_FACE(UShort Material, UShort BackMaterial,
                   int vert1, int vert2, int vert3,
                   float u1, float v1, float u2, float v2, float u3, float v3,
                   byte[] UNKNOWN1,
                   INTCOLOR col1, INTCOLOR col2, INTCOLOR col3,
                   byte[] UNKNOWN2) {
        this.Material = Material;
        this.BackMaterial = BackMaterial;
        this.Vertex1 = vert1;
        this.Vertex2 = vert2;
        this.Vertex3 = vert3;
        this.U1 = u1;
        this.V1 = v1;
        this.U2 = u2;
        this.V2 = v2;
        this.U3 = u3;
        this.V3 = v3;
        this.UNKNOWN1 = UNKNOWN1;
        this.Col1 = col1;
        this.Col2 = col2;
        this.Col3 = col3;
        this.UNKNOWN2 = UNKNOWN2;
    }
}
