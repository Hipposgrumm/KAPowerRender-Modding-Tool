package dev.hipposgrumm.kamapviewer.rendering;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.utils.Array;

public class PowerRenderShaderProvider extends DefaultShaderProvider {
    protected Array<Shader> shaderCache = new Array<>();

    public PowerRenderShaderProvider() {
        super();
    }

    @Override
    public Shader getShader(Renderable renderable) {
        for (Shader shader:shaderCache) {
            if (shader.canRender(renderable)) return shader;
        }
        if (renderable.material instanceof RenderMaterial material) {
            final Shader shader = new PowerRenderShader(material);
            shader.init();
            shaderCache.add(shader);
            return shader;
        } else return super.getShader(renderable);
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Shader shader:shaderCache) shader.dispose();
        shaderCache.clear();
    }
}
