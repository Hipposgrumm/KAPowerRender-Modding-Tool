package dev.hipposgrumm.kamapviewer.util.structs;

public class PR_FACE {
    public final PR_VERTEX[] Vertices = new PR_VERTEX[3];
    public final FACE_VERTEX_DATA[] Data = new FACE_VERTEX_DATA[3];
    public PR_POINT Normal;

    public PR_FACE() {
        for (int i=0;i<3;i++) this.Vertices[i] = new PR_VERTEX();
        for (int i=0;i<3;i++) this.Data[i] = new FACE_VERTEX_DATA();
        this.Normal = new PR_POINT();
    }

    public PR_FACE(PR_VERTEX Vertex1, PR_VERTEX Vertex2, PR_VERTEX Vertex3,
                   FACE_VERTEX_DATA Data1, FACE_VERTEX_DATA Data2, FACE_VERTEX_DATA Data3,
                   PR_POINT Normal) {
        this.Vertices[0] = Vertex1;
        this.Vertices[1] = Vertex2;
        this.Vertices[2] = Vertex3;
        this.Data[0] = Data1;
        this.Data[1] = Data2;
        this.Data[2] = Data3;
        this.Normal = Normal;
    }

    public static class FACE_VERTEX_DATA {
        public float U,V;
        public COLOR_RGBA Col;

        public FACE_VERTEX_DATA() {
            this.Col = new COLOR_RGBA(-1);
        }

        public FACE_VERTEX_DATA(float U, float V, COLOR_RGBA Col) {
            this.U = U;
            this.V = V;
            this.Col = Col;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FACE_VERTEX_DATA other &&
                this.U == other.U &&
                this.V == other.V &&
                this.Col.color == other.Col.color;
        }
    }
}
