package dev.hipposgrumm.kamapviewer.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import dev.hipposgrumm.kamapviewer.Main;
import dev.hipposgrumm.kamapviewer.models.PROModelBuilder;
import dev.hipposgrumm.kamapviewer.rendering.PRMaterial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReaderAppConnection {
    public static Main instance;

    public static List<Runnable> actionQueue = new ArrayList<>();
    private static List<byte[]> queue = null;

    private static Thread socketThread = null;

    /// Connect to the parent app.
    public static void connect(int port) {
        queue = new ArrayList<>();
        socketThread = new Thread(() -> {
            try (Socket connection = new Socket((String)null, port)) {
                InputStream input = connection.getInputStream();
                OutputStream output = connection.getOutputStream();
                while (!socketThread.isInterrupted() && connection.isConnected()) {
                    if (input.available() >= 8) {
                        int message = (input.read() << 24) |
                            (input.read() << 16) |
                            (input.read() << 8) |
                            input.read();
                        int size = (input.read() << 24) |
                            (input.read() << 16) |
                            (input.read() << 8) |
                            input.read();
                        byte[] data = input.readNBytes(size);
                        actionQueue.add(() -> handleMessage(message, data));
                    }
                    if (!queue.isEmpty()) {
                        List<byte[]> messages = queue;
                        queue = new ArrayList<>();
                        for (byte[] message:messages)
                            output.write(message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "KAMapViewer Client Thread");
        socketThread.setDaemon(true);
        socketThread.start();
    }

    /// Send a message to the parent app.
    public static void sendMessage(int message, byte[] data) {
        if (socketThread == null) return;
        byte[] bytes = new byte[data.length+8];
        bytes[0] = (byte)(message>>24);
        bytes[1] = (byte)((message>>16)&0xFF);
        bytes[2] = (byte)((message>>8)&0xFF);
        bytes[3] = (byte)(message&0xFF);
        bytes[4] = (byte)(data.length>>24);
        bytes[5] = (byte)((data.length>>16)&0xFF);
        bytes[6] = (byte)((data.length>>8)&0xFF);
        bytes[7] = (byte)(data.length&0xFF);
        System.arraycopy(data, 0, bytes, 8, data.length);
        queue.add(bytes);
    }

    /// Inform the parent app that this app is being closed.
    public static void sendTermination() {
        if (socketThread == null) return;
        sendMessage(Messages.TERMINATE, new byte[0]);
    }

    public static final class Messages {
        private static final int TERMINATE = -1;
        public static final int MODEL_START = 0;
        public static final int MODEL_PART = 1;
        public static final int MODEL_END = 2;
        public static final int MODELS_CLEAR = 3;
        public static final int LOAD_TEXTURES = 4;
        public static final int LOAD_MATERIALS = 5;
        public static final int TEST_ECHO = 67;
    }

    private static PROModelBuilder buildingmesh = null;
    private static void handleMessage(int message, byte[] bytes) {
        switch (message) {
            case Messages.TERMINATE -> {
                System.out.println("Received exit signal from parent application.");
                Gdx.app.exit();
            }
            case Messages.TEST_ECHO -> {
                for (int i=0;i<bytes.length;i++)
                    bytes[i] = (byte)(bytes[i] ^ 0b00100000);
                sendMessage(Messages.TEST_ECHO, bytes);
            }
            case Messages.MODEL_START -> {
                if (buildingmesh != null) throw new RuntimeException("Tried to start new model before closing last one! This is unsupported behaviour!");
                StringBuilder name = new StringBuilder();
                for (byte b:bytes) {
                    if (b == '\00') break;
                    name.append((char)b);
                }
                buildingmesh = new PROModelBuilder(new String(name.toString().getBytes(), StandardCharsets.US_ASCII));
            }
            case Messages.MODEL_PART -> {
                buildingmesh.addPartFromData(bytes);
            }
            case Messages.MODEL_END -> {
                instance.addModel(buildingmesh.bakeModel());
                buildingmesh = null;
            }
            case Messages.MODELS_CLEAR -> instance.clearModels();
            case Messages.LOAD_TEXTURES -> {
                Main.textures.clear();
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
                    if (Main.textures.containsKey(uid)) System.out.println("WARN: Texture with duplicate UID 0x"+Integer.toHexString(uid).toUpperCase());
                    else Main.textures.put(uid, new Texture(image));
                }
            }
            case Messages.LOAD_MATERIALS -> {
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
                        if (texid != 0) material.textures[i] = Main.textures.get(texid);
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
                    if (Main.textures.containsKey(uid)) System.out.println("WARN: Material with duplicate UID 0x"+Integer.toHexString(uid).toUpperCase());
                    else Main.materials.put(uid, material);
                }
            }
        }
    }
}
