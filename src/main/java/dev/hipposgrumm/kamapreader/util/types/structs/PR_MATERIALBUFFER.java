package dev.hipposgrumm.kamapreader.util.types.structs;

public class PR_MATERIALBUFFER {
    public int MaterialIndex;
    public PR_VERTEXBUFFER[] VertexBuffers;

    public PR_MATERIALBUFFER() {}
    public PR_MATERIALBUFFER(int MaterialIndex) {
        this.MaterialIndex = MaterialIndex;
    }
}
