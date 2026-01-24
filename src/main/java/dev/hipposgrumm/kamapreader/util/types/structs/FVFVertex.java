package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;

// https://www.flipcode.com/archives/Flexible_Vertex_Format_Generator.shtml
public class FVFVertex {
    public PR_POINT Pos;
    public float RHW;
    public float[] Weights;
    public PR_POINT Normal;
    public float PSize;
    public final INTCOLOR Diffuse = new INTCOLOR(INTCOLOR.Format.ARGB, -1);
    public final INTCOLOR Specular = new INTCOLOR(INTCOLOR.Format.ARGB, -1);
    public TexCoords[] TextureCoords;

    public static FVFVertex create(D3DFVF FVF, BlockReader reader) {
        FVFVertex vert = new FVFVertex();
        D3DFVF.Enumerations Position = (D3DFVF.Enumerations) D3DFVF.Position.from(FVF);
        if (Position != D3DFVF.Enumerations.__) vert.Pos = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
        switch (Position) {
            case XYZ -> {}
            case XYZRHW, XYZW -> vert.RHW = reader.readFloat();
            default -> {
                float[] weights = new float[5];
                int count = 0;
                switch (Position) {
                    // This utilizes falldown into the next case.
                    case XYZB5: weights[count++] = reader.readFloat();
                    case XYZB4: weights[count++] = reader.readFloat();
                    case XYZB3: weights[count++] = reader.readFloat();
                    case XYZB2: weights[count++] = reader.readFloat();
                    case XYZB1: weights[count++] = reader.readFloat();
                }
                vert.Weights = new float[count];
                System.arraycopy(weights, 0, vert.Weights, 0, count);
            }
        }
        if (D3DFVF.NORMAL.from(FVF)) vert.Normal = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
        if (D3DFVF.PSIZE.from(FVF)) vert.PSize = reader.readFloat();
        if (D3DFVF.DIFFUSE.from(FVF)) vert.Diffuse.color = reader.readIntBig();
        if (D3DFVF.SPECULAR.from(FVF)) vert.Specular.color = reader.readIntBig();
        for (int i=0;i<D3DFVF.TEXCOORDNUMS.length;i++) {
            TexCoords coords = new TexCoords();
            vert.TextureCoords[i] = coords;
            int count = ((D3DFVF.TexCoordSize) D3DFVF.TEXCOORDNUMS[i].from(FVF)).texCount;
            coords.u = reader.readFloat();
            if (--count == 0) continue;
            coords.v = reader.readFloat();
            if (--count == 0) continue;
            coords.w = reader.readFloat();
            if (--count == 0) continue;
            coords.a = reader.readFloat();
        }
        return vert;
    }

    public FVFVertex write(D3DFVF FVF, BlockWriter writer) {
        D3DFVF.Enumerations Position = (D3DFVF.Enumerations) D3DFVF.Position.from(FVF);
        if (Position != D3DFVF.Enumerations.__) {
            writer.writeFloat(this.Pos.X);
            writer.writeFloat(this.Pos.Y);
            writer.writeFloat(this.Pos.Z);
        }
        switch (Position) {
            case XYZ -> {}
            case XYZRHW, XYZW -> writer.writeFloat(this.RHW);
            default -> {
                float[] weights = new float[5];
                System.arraycopy(this.Weights, 0, weights, 0, this.Weights.length);
                int count = 0;
                switch (Position) {
                    // This utilizes falldown into the next case.
                    case XYZB5: writer.writeFloat(weights[count++]);
                    case XYZB4: writer.writeFloat(weights[count++]);
                    case XYZB3: writer.writeFloat(weights[count++]);
                    case XYZB2: writer.writeFloat(weights[count++]);
                    case XYZB1: writer.writeFloat(weights[count]);
                }
            }
        }
        if (D3DFVF.NORMAL.from(FVF)) {
            writer.writeFloat(this.Normal.X);
            writer.writeFloat(this.Normal.Y);
            writer.writeFloat(this.Normal.Z);
        }
        if (D3DFVF.PSIZE.from(FVF)) writer.writeFloat(this.PSize);
        if (D3DFVF.DIFFUSE.from(FVF)) writer.writeIntBig(this.Diffuse.color);
        if (D3DFVF.SPECULAR.from(FVF)) writer.writeIntBig(this.Specular.color);
        for (int i=0;i<D3DFVF.TEXCOORDNUMS.length;i++) {
            TexCoords coords = this.TextureCoords[i];
            int count = ((D3DFVF.TexCoordSize) D3DFVF.TEXCOORDNUMS[i].from(FVF)).texCount;
            writer.writeFloat(coords.u);
            if (--count == 0) continue;
            writer.writeFloat(coords.v);
            if (--count == 0) continue;
            writer.writeFloat(coords.w);
            if (--count == 0) continue;
            writer.writeFloat(coords.a);
        }
        return this;
    }

    public static final class TexCoords {
        public float u,v,w,a;
    }
}
