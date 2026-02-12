package dev.hipposgrumm.kamapviewer.models;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.hipposgrumm.kamapviewer.Main;
import dev.hipposgrumm.kamapviewer.rendering.PRMaterial;
import dev.hipposgrumm.kamapviewer.rendering.RenderMaterial;
import dev.hipposgrumm.kamapviewer.util.structs.COLOR_RGBA;
import dev.hipposgrumm.kamapviewer.util.structs.PR_FACE;
import dev.hipposgrumm.kamapviewer.util.structs.PR_POINT;
import dev.hipposgrumm.kamapviewer.util.structs.PR_VERTEX;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

// TODO: This can change when the system gets more complex (eg mesh editing, maybe exporting too).
public class PROModelBuilder {
    private static final VertexAttributes VERTEXATTR = new VertexAttributes(
        new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
        new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"),
        new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
    );

    private final String name;
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private PR_VERTEX[] vertices;
    private PR_FACE[] faces;

    public PROModelBuilder(String name) {
        System.out.println();
        System.out.println("Mesh name "+name);
        this.name = name;
        modelBuilder.begin();
    }

    public void addPartFromData(byte[] bytes) {
        ByteBuffer data = ByteBuffer.wrap(bytes);
        StringBuilder name = new StringBuilder();
        for (byte c=data.get();c!='\00';c=data.get())
            name.append((char)c);
        System.out.println("Segment mesh "+name);
        vertices = new PR_VERTEX[data.getInt()];
        for (int i=0;i<vertices.length;i++) {
            float pX = -data.getFloat(), pY = data.getFloat(), pZ = data.getFloat();
            float nX = -data.getFloat(), nY = data.getFloat(), nZ = data.getFloat();
            vertices[i] = new PR_VERTEX(
                new PR_POINT(pX, pY, pZ),
                new PR_POINT(nX, nY, nZ)
            );
        }
        AtomicInteger matind = new AtomicInteger(0);
        Function<Integer, Integer> matindIncFunc = uid -> matind.getAndIncrement();
        Map<Integer, Integer> matindMap = new HashMap<>();
        Map<Integer, Submesh> submeshes = new HashMap<>();
        DefinedVertex[][] vertexDefines = new DefinedVertex[vertices.length][];
        faces = new PR_FACE[data.getInt()];
        for (int i=0;i<faces.length;i++) {
            int materialUID = data.getInt();
            int materialBackUID = data.getInt(); // Probably not even used.
            int ve1 = data.getInt(), ve2 = data.getInt(), ve3 = data.getInt();
            float u1 = data.getFloat(), v1 = data.getFloat(); COLOR_RGBA col1 = new COLOR_RGBA(data.getInt());
            float u2 = data.getFloat(), v2 = data.getFloat(); COLOR_RGBA col2 = new COLOR_RGBA(data.getInt());
            float u3 = data.getFloat(), v3 = data.getFloat(); COLOR_RGBA col3 = new COLOR_RGBA(data.getInt());
            short nX = data.getShort(), nY = data.getShort(), nZ = data.getShort();
            PR_FACE.FACE_VERTEX_DATA Data1 = new PR_FACE.FACE_VERTEX_DATA(u1, v1, col1),
                Data2 = new PR_FACE.FACE_VERTEX_DATA(u2, v2, col2),
                Data3 = new PR_FACE.FACE_VERTEX_DATA(u3, v3, col3);
            faces[i] = new PR_FACE(
                vertices[ve1],
                vertices[ve2],
                vertices[ve3],
                Data1, Data2, Data3,
                new PR_POINT(nX, nY, nZ)
            );
            DefinedVertex defVert1 = new DefinedVertex(vertices[ve1], Data1);
            DefinedVertex defVert2 = new DefinedVertex(vertices[ve2], Data2);
            DefinedVertex defVert3 = new DefinedVertex(vertices[ve3], Data3);
            addVertexDefineToList(vertexDefines, ve1, defVert1);
            addVertexDefineToList(vertexDefines, ve2, defVert2);
            addVertexDefineToList(vertexDefines, ve3, defVert3);
            Submesh submesh = submeshes.computeIfAbsent(Objects.hash(
                    matindMap.computeIfAbsent(materialBackUID, matindIncFunc),
                    matindMap.computeIfAbsent(materialUID, matindIncFunc)
            ), hash -> new Submesh(hash,
                materialUID!=0 ? Main.materials.get(materialUID) : null,
                materialBackUID!=0 ? Main.materials.get(materialBackUID) : null
            ));
            submesh.Vertices.add(defVert1);
            submesh.Vertices.add(defVert2);
            submesh.Vertices.add(defVert3);
            submesh.Faces.add(new DefinedFace(defVert1, defVert2, defVert3));
        }
        PR_FACE.FACE_VERTEX_DATA FaceDataBlank = new PR_FACE.FACE_VERTEX_DATA();
        for (int i=0;i<vertexDefines.length;i++) {
            if (vertexDefines[i] == null) vertexDefines[i] = new DefinedVertex[]{new DefinedVertex(vertices[i],FaceDataBlank)};
        }
        modelBuilder.node().id = new String(name.toString().getBytes(), StandardCharsets.US_ASCII);
        for (Submesh submesh:submeshes.values()) {
            LinkedHashMap<DefinedVertex, IndexedVertex> submeshVertices = LinkedHashMap.newLinkedHashMap(submesh.Vertices.size());
            int i=0;for (DefinedVertex vert:submesh.Vertices)
                submeshVertices.put(vert, new IndexedVertex(i++, vert));
            Mesh mesh = new Mesh(true, submesh.Vertices.size(), submesh.Faces.size() * 3, VERTEXATTR);
            float[] packedVertices = new float[submeshVertices.size() * (VERTEXATTR.vertexSize/4)];
            i=0;for (DefinedVertex vert:submeshVertices.keySet()) {
                packedVertices[i+0] = vert.Vert.Position.X;
                packedVertices[i+1] = vert.Vert.Position.Y;
                packedVertices[i+2] = vert.Vert.Position.Z;
                packedVertices[i+3] = vert.Vert.Normal.X;
                packedVertices[i+4] = vert.Vert.Normal.Y;
                packedVertices[i+5] = vert.Vert.Normal.Z;
                packedVertices[i+6] = vert.Data.U;
                packedVertices[i+7] = vert.Data.V;
                packedVertices[i+8] = Float.intBitsToFloat(vert.Data.Col.color);
                i+=VERTEXATTR.vertexSize/4;
            }
            mesh.setVertices(packedVertices);
            short[] packedIndices = new short[submesh.Faces.size()*3];
            i=0;for (DefinedFace face:submesh.Faces) {
                packedIndices[i+0] = (short) (submeshVertices.get(face.ve3).index & 0xFFFF);
                packedIndices[i+1] = (short) (submeshVertices.get(face.ve2).index & 0xFFFF);
                packedIndices[i+2] = (short) (submeshVertices.get(face.ve1).index & 0xFFFF);
                i+=3;
            }
            mesh.setIndices(packedIndices);
            modelBuilder.part(
                Integer.toString(submesh.Materialhash),
                mesh, GL20.GL_TRIANGLES, new RenderMaterial(submesh.Material, submesh.MaterialBack)
            );
        }
    }

    public Model bakeModel() {
        return modelBuilder.end();
    }

    private static void addVertexDefineToList(DefinedVertex[][] vertexDefines, int index, DefinedVertex defVert) {
        DefinedVertex[] defines = vertexDefines[index];
        if (defines == null) {
            vertexDefines[index] = new DefinedVertex[] {defVert};
        } else {
            vertexDefines[index] = new DefinedVertex[defines.length+1];
            System.arraycopy(defines, 0, vertexDefines[index], 0, defines.length);
            vertexDefines[index][defines.length] = defVert;
        }
    }

    private record DefinedVertex(PR_VERTEX Vert, PR_FACE.FACE_VERTEX_DATA Data) {}
    private record IndexedVertex(int index, DefinedVertex Def) {}
    private record DefinedFace(DefinedVertex ve1, DefinedVertex ve2, DefinedVertex ve3) {}
    private static class Submesh {
        public final List<DefinedVertex> Vertices = new ArrayList<>();
        public final List<DefinedFace> Faces = new ArrayList<>();
        public final int Materialhash;
        public final PRMaterial Material;
        public final PRMaterial MaterialBack;

        public Submesh(int hash, PRMaterial mat, PRMaterial matback) {
            this.Materialhash = hash;
            this.Material = mat;
            this.MaterialBack = matback;
        }
    }
}
