package dev.hipposgrumm.kamapviewer.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.hipposgrumm.kamapviewer.util.enums.D3DTEXTUREADDRESS;

// https://xoppa.github.io/blog/creating-a-shader-with-libgdx/
public class PowerRenderShader implements Shader {
    private ShaderProgram shader;

    protected final boolean hasTexture;
    protected final D3DTEXTUREADDRESS wrapX;
    protected final D3DTEXTUREADDRESS wrapY;

    public PowerRenderShader(RenderMaterial material) {
        this.hasTexture = material.Material.textures[0] != null;
        if (hasTexture) {
            this.wrapX = material.Material.renderStages[0].addressU;
            this.wrapY = material.Material.renderStages[0].addressV;
        } else {
            this.wrapX = D3DTEXTUREADDRESS.__;
            this.wrapY = D3DTEXTUREADDRESS.__;
        }
    }

    @Override
    public boolean canRender(Renderable instance) {
        if (!(instance.material instanceof RenderMaterial instmat)) return false;
        boolean otherHasTexture = instmat.Material.textures[0] != null;
        if (hasTexture) {
            if (!otherHasTexture) return false;
            if (
                (wrapX != instmat.Material.renderStages[0].addressU) ||
                (wrapY != instmat.Material.renderStages[0].addressV)
            ) return false;
        } else if (otherHasTexture) return false;
        return true;
    }

    @Override
    public void init() {
        String vertSettings = "";
        String fragSettings = "";
        if (hasTexture) {
            fragSettings += "#define HAS_TEXTURE\n";
            fragSettings += String.format("#define WRAP_X %s\n", wrapX.identifier);
            fragSettings += String.format("#define WRAP_Y %s\n", wrapY.identifier);
        }
        shader = new ShaderProgram(
            vertSettings+Gdx.files.internal("shader/pr.vert").readString(),
            fragSettings+Gdx.files.internal("shader/pr.frag").readString()
        );
        if (!shader.isCompiled())
            throw new GdxRuntimeException(shader.getLog());
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    private RenderContext context;

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.context = context;
        shader.bind();
        shader.setUniformMatrix("u_projViewTrans", camera.combined);
    }

    @Override
    public void render(Renderable renderable) {
        RenderMaterial material = (RenderMaterial) renderable.material;
        shader.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        if (material.Material.textures[0] != null) {
            material.Material.textures[0].bind(0);
            shader.setUniformi("u_texture", 0);
            if (wrapX == D3DTEXTUREADDRESS.BORDER || wrapY == D3DTEXTUREADDRESS.BORDER) {
                shader.setUniformf("u_bordercolor", material.Material.renderStages[0].bordercolor);
            }
        }
        context.setCullFace(material.Material.twosided ? 0 : GL20.GL_BACK);
        renderable.meshPart.render(shader);
    }

    @Override
    public void end() {}

    @Override
    public void dispose() {
        shader.dispose();
    }
}
