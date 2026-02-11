package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
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

    public PR_FACE(BlockReader reader) {
        this.Material = reader.readUShort();
        this.BackMaterial = reader.readUShort();
        this.Vertex1 = reader.readInt();
        this.Vertex2 = reader.readInt();
        this.Vertex3 = reader.readInt();
        this.U1 = reader.readFloat();
        this.V1 = reader.readFloat();
        this.U2 = reader.readFloat();
        this.V2 = reader.readFloat();
        this.U3 = reader.readFloat();
        this.V3 = reader.readFloat();
        this.UNKNOWN1 = reader.readBytes(144);
        this.Col1 = new INTCOLOR(INTCOLOR.Format.RGBA, reader.readIntBig());
        this.Col2 = new INTCOLOR(INTCOLOR.Format.RGBA, reader.readIntBig());
        this.Col3 = new INTCOLOR(INTCOLOR.Format.RGBA, reader.readIntBig());
        this.UNKNOWN2 = reader.readBytes(7); // probably normals and flags but can't confirm it
    }

    public void write(BlockWriter writer) {
        writer.writeUShort(Material);
        writer.writeUShort(BackMaterial);
        writer.writeInt(Vertex1);
        writer.writeInt(Vertex2);
        writer.writeInt(Vertex3);
        writer.writeFloat(U1);
        writer.writeFloat(V1);
        writer.writeFloat(U2);
        writer.writeFloat(V2);
        writer.writeFloat(U3);
        writer.writeFloat(V3);
        writer.writeBytes(UNKNOWN1);
        writer.writeIntBig(Col1.color);
        writer.writeIntBig(Col2.color);
        writer.writeIntBig(Col3.color);
        writer.writeBytes(UNKNOWN2);
    }
}
