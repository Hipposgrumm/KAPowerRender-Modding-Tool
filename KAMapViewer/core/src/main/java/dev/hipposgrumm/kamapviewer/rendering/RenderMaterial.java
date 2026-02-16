package dev.hipposgrumm.kamapviewer.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

// This only extends Material so that it can be passed in through the material field.
public class RenderMaterial extends Material {
    public final PRMaterial Material;
    public final PRMaterial BackMaterial;
    private final boolean alphablend;

    public RenderMaterial(PRMaterial material, PRMaterial backMaterial) {
        super(
            createName(material, backMaterial),
            TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("libgdx.png")))
        );
        this.Material = material;
        this.BackMaterial = backMaterial;

        this.alphablend = PowerRenderShader.Properties.alphablend(this, PowerRenderShader.Properties.hasTexture(this));
        if (alphablend) set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    }

    private static String createName(PRMaterial material, PRMaterial backMaterial) {
        if (backMaterial != null) {
            if (material == backMaterial) return material.name;
            if (material == null) return backMaterial.name;
            return material.name+" "+backMaterial.name;
        } else if (material != null) return material.name;
        return "  empty  ";
    }

    public RenderMaterial(RenderMaterial copyFrom) {
        this(copyFrom.id, copyFrom);
    }

    public RenderMaterial(String id, RenderMaterial copyFrom) {
        super(id, copyFrom);
        this.Material = copyFrom.Material;
        this.BackMaterial = copyFrom.BackMaterial;
        this.alphablend = copyFrom.alphablend;
    }

    public boolean isBlending() {
        return alphablend;
    }

    @Override
    public Material copy() {
        return new RenderMaterial(this);
    }
}
