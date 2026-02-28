package dev.hipposgrumm.kamapviewer.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;

// HACK: This is an empty model that replaces the grid so that rendering order doesn't break. But there has to be a better way...
public class EmptyModel implements Disposable {
    private final Model model;
    private final ModelInstance modelInstance;

    public EmptyModel(long attributes) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();

        builder.part("_", GL20.GL_POINTS, attributes, new Material("grid", ColorAttribute.createDiffuse(Color.WHITE)));

        model = builder.end();
        modelInstance = new ModelInstance(model);
    }

    public ModelInstance renderable() {
        return modelInstance;
    }

    @Override
    public void dispose() {
        model.dispose();
    }
}
