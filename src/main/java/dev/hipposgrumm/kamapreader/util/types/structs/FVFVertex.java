package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.util.types.enums.D3DFVF;

import java.io.IOException;

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

    public static FVFVertex create(D3DFVF.AsFlags FVF, BlockReader reader) throws IOException {
        FVFVertex vert = new FVFVertex();
        D3DFVF Position = (D3DFVF) D3DFVF.AsFlags.Position.from(FVF);
        if (Position != D3DFVF.__) vert.Pos = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
        switch (Position) {
            case XYZ -> {}
            case XYZRHW, XYZW -> vert.RHW = reader.readFloat();
            default -> {
                float[] weights = new float[5];
                int count = 0;
                switch (Position) {
                    // This utilizes falldown into the next case.
                    case D3DFVF.XYZB5: weights[count++] = reader.readFloat();
                    case D3DFVF.XYZB4: weights[count++] = reader.readFloat();
                    case D3DFVF.XYZB3: weights[count++] = reader.readFloat();
                    case D3DFVF.XYZB2: weights[count++] = reader.readFloat();
                    case D3DFVF.XYZB1: weights[count++] = reader.readFloat();
                }
                vert.Weights = new float[count];
                System.arraycopy(weights, 0, vert.Weights, 0, count);
            }
        }
        if (D3DFVF.AsFlags.NORMAL.from(FVF)) vert.Normal = new PR_POINT(reader.readFloat(), reader.readFloat(), reader.readFloat());
        if (D3DFVF.AsFlags.PSIZE.from(FVF)) vert.PSize = reader.readFloat();
        if (D3DFVF.AsFlags.DIFFUSE.from(FVF)) vert.Diffuse.color = Integer.reverseBytes(reader.readInt());
        if (D3DFVF.AsFlags.SPECULAR.from(FVF)) vert.Specular.color = Integer.reverseBytes(reader.readInt());
        for (int i=0;i<D3DFVF.AsFlags.TEXCOORDNUMS.length;i++) {
            vert.TextureCoords[i++] = switch ((D3DFVF.TexCoordSize) D3DFVF.AsFlags.TEXCOORDNUMS[i].from(FVF)) {
                case _1 -> new TexCoords1D(reader.readFloat());
                case _2 -> new TexCoords2D(reader.readFloat(), reader.readFloat());
                case _3 -> new TexCoords3D(reader.readFloat(), reader.readFloat(), reader.readFloat());
                case _4 -> new TexCoords4D(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());
            };
        }
        return vert;
    }

    public static abstract sealed class TexCoords permits TexCoords1D, TexCoords2D, TexCoords3D, TexCoords4D {}
    public static final class TexCoords1D extends TexCoords {
        public float v;

        TexCoords1D(float v) {
            this.v = v;
        }
    }
    public static final class TexCoords2D extends TexCoords {
        public float u,v;

        TexCoords2D(float u, float v) {
            this.u = u;
            this.v = v;
        }
    }
    public static final class TexCoords3D extends TexCoords {
        public float u,v,w;

        TexCoords3D(float u, float v, float w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }
    public static final class TexCoords4D extends TexCoords {
        public float u,v,w,a;

        TexCoords4D(float u, float v, float w, float a) {
            this.u = u;
            this.v = v;
            this.w = w;
            this.a = a;
        }
    }
}
