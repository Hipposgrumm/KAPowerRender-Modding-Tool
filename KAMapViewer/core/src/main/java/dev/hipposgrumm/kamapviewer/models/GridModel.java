package dev.hipposgrumm.kamapviewer.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class GridModel {
    private final Model model;
    private final ModelInstance modelInstance;

    /// @see ModelBuilder#createLineGrid(int, int, float, float, Material, long)
    public GridModel(int size, long attributes) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();

        MeshPartBuilder lines = builder.part("lines", GL20.GL_LINES, attributes | VertexAttributes.Usage.ColorPacked, new Material("grid", ColorAttribute.createDiffuse(Color.WHITE)));
        for (int x=-size;x<=size;x++) {
            if (x==0) continue;
            lines.line(x, 0f, size, x, 0f, -size);
        }
        for (int z=-size;z<=size;z++) {
            if (z==0) continue;
            lines.line(size, 0f, z, -size, 0f, z);
        }

        lines.line(new Vector3(size, 0f, 0f), Color.RED, new Vector3(-size, 0f, 0f), Color.RED);
        lines.line(new Vector3(0f, size, 0f), Color.GREEN, new Vector3(0f, -size, 0f), Color.GREEN);
        lines.line(new Vector3(0f, 0f, size), Color.BLUE, new Vector3(0f, 0f, -size), Color.BLUE);

        model = builder.end();
        modelInstance = new ModelInstance(model);
    }

    public ModelInstance renderable() {
        return modelInstance;
    }

    public void dispose() {
        model.dispose();
    }
}
