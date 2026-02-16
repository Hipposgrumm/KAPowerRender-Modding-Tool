package dev.hipposgrumm.kamapviewer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dev.hipposgrumm.kamapviewer.models.GridModel;
import dev.hipposgrumm.kamapviewer.rendering.PRMaterial;
import dev.hipposgrumm.kamapviewer.rendering.PowerRenderShaderProvider;
import dev.hipposgrumm.kamapviewer.ui.UIHandler;
import dev.hipposgrumm.kamapviewer.util.ReaderAppConnection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private static final int GRID_SIZE = 10;

    public static final String WINDOW_TITLE = "KAMapViewer";
    public static final String[] WINDOW_ICON = {"libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png"};
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;

    private final List<Model> models = new ArrayList<>();
    private final List<ModelInstance> modelInstances = new ArrayList<>();
    public static Map<Integer, Texture> textures = new HashMap<>();
    public static Map<Integer, PRMaterial> materials = new HashMap<>();

    private UIHandler ui;
    private GridModel grid;
    private Environment environment;
    private PerspectiveCamera cam;
    private FirstPersonCameraController camController;
    private ModelBatch world;

    public Main(String[] args) {
        for (String arg:args) {
            if (arg.startsWith("port")) {
                int port = Integer.parseInt(arg.substring(4));
                System.out.println("Listening on "+port);
                ReaderAppConnection.connect(port);
                break;
            }
        }
    }

    public void addModel(Model model) {
        models.add(model);
        modelInstances.add(new ModelInstance(model));
    }

    public void clearModels() {
        modelInstances.clear();
        for (Model model:models) model.dispose();
        models.clear();
    }

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f,10f,10f);
        cam.lookAt(0f,0f,0f);
        cam.up.set(0f, 1f, 0f);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        camController = new FirstPersonCameraController(cam);
        int flipkey = camController.upKey;
        camController.upKey = camController.downKey;
        camController.downKey = flipkey;

        world = new ModelBatch(new PowerRenderShaderProvider());
        grid = new GridModel(GRID_SIZE, VertexAttributes.Usage.Position);
        addModel(new ModelBuilder().createBox(5f, 5f, 5f,
            new Material(ColorAttribute.createDiffuse(Color.PINK)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal));

        ui = new UIHandler();

        Gdx.input.setInputProcessor(new InputMultiplexer(ui.getInput(), camController));
        ReaderAppConnection.instance = this;
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        ui.resize(width, height);
    }

    @Override
    public void render() {
        // Needs to be on libGDX thread.
        if (!ReaderAppConnection.actionQueue.isEmpty()) {
            List<Runnable> actions = ReaderAppConnection.actionQueue;
            ReaderAppConnection.actionQueue = new ArrayList<>();
            for (Runnable run : actions) run.run();
        }

        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.2f,0.5f, 0.75f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        world.begin(cam);
        world.render(grid.renderable());
        world.render(modelInstances, environment);
        world.end();

        ui.render();
    }

    @Override
    public void dispose() {
        world.dispose();
        for (Model model:models) model.dispose();
        grid.dispose();
        ui.dispose();
    }

    public static void loadTextures(byte[] bytes) {
        textures.clear();
        ByteBuffer data = ByteBuffer.wrap(bytes);
        while (data.hasRemaining()) {
            int uid = data.getInt();
            int width = data.getInt();
            int height = data.getInt();
            Pixmap image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            for (int i=0;i<(width*height);i++) {
                int color = data.getInt();
                color = ((color&0xFF) << 8) |
                    ((color&0x0000FF00)<<8) |
                    ((color<<8)&0xFF000000) |
                    ((color>>24)&0xFF);
                image.drawPixel(i%width, i/width, color);
            }
            if (textures.containsKey(uid)) System.out.println("WARN: Texture with duplicate UID 0x"+Integer.toHexString(uid).toUpperCase());
            else {
                Texture tex = new Texture(image);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textures.put(uid, tex);
            }
        }
    }

    public static void loadMaterials(byte[] bytes) {
        ByteBuffer data = ByteBuffer.wrap(bytes);
        while (data.hasRemaining()) {
            int uid = data.getInt();
            StringBuilder name = new StringBuilder();
            for (byte b=data.get();b!='\00';b=data.get()) {
                name.append((char)b);
            }
            PRMaterial material = new PRMaterial(name.toString());
            for (int i=0;i<7;i++) {
                int texid = data.getInt();
                if (texid != 0) material.textures[i] = textures.get(texid);
                else material.textures[i] = null;
            }
            material.setColor(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
            material.setBump(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
            material.setSpecColor(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
            material.setTwoSided(data.get() != 0);
            material.setRenderStyle(
                data.get(), data.get() != 0,
                data.get() != 0, data.get() != 0,
                data.get() != 0, data.get() != 0, data.get() != 0,
                data.getShort(), data.getInt(), data.getInt()
            );
            material.setRenderFunction(
                data.get(), data.get(), data.get(),
                data.get(), data.get(),
                data.get(), data.get(), data.get(), data.get(),
                data.get(),
                data.get()
            );
            for (int j=0;j<8;j++) {
                PRMaterial.RenderStageData texdat = new PRMaterial.RenderStageData();
                texdat.setTextureStage(
                    data.get(), data.get(),
                    data.get(), data.get(),
                    data.get(), data.get(),
                    data.get(), data.getShort()
                );
                texdat.setSamplerStage(
                    data.get(), data.get(), data.get(),
                    data.getInt()
                );
                material.renderStages[j] = texdat;
            }
            if (textures.containsKey(uid)) System.out.println("WARN: Material with duplicate UID 0x"+Integer.toHexString(uid).toUpperCase());
            else materials.put(uid, material);
        }
    }
}
