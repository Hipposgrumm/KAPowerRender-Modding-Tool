package dev.hipposgrumm.kamapviewer.util;

import com.badlogic.gdx.Gdx;
import dev.hipposgrumm.kamapviewer.Main;
import dev.hipposgrumm.kamapviewer.models.PROModelBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReaderAppConnection {
    public static Main instance;

    public static List<Runnable> actionQueue = new ArrayList<>();
    private static final List<byte[]> queue = new ArrayList<>();

    private static Thread socketThread = null;

    /// Connect to the parent app.
    public static void connect(int port) {
        socketThread = new Thread(() -> {
            try (Socket connection = new Socket((String)null, port)) {
                InputStream input = connection.getInputStream();
                OutputStream output = connection.getOutputStream();
                int messageID = 0;
                byte[] messageData = null; int messageDataIndex = 0;
                while (!socketThread.isInterrupted() && connection.isConnected()) {
                    if (messageData == null && input.available() >= 8) {
                        int message = (input.read() << 24) |
                            (input.read() << 16) |
                            (input.read() << 8) |
                            input.read();
                        int size = (input.read() << 24) |
                            (input.read() << 16) |
                            (input.read() << 8) |
                            input.read();
                        messageID = message;
                        messageDataIndex = 0;
                        messageData = new byte[size];
                    }
                    if (messageData != null) {
                        int available = input.available();
                        int needed = messageData.length-messageDataIndex;
                        byte[] data = input.readNBytes(Math.min(available, needed));
                        System.arraycopy(data, 0, messageData, messageDataIndex, data.length);
                        messageDataIndex += data.length;
                        if (messageDataIndex >= messageData.length) {
                            int lambdaSafeMessageID = messageID;
                            byte[] lambdaSafeMessageData = messageData;
                            actionQueue.add(() -> handleMessage(lambdaSafeMessageID, lambdaSafeMessageData));
                            messageData = null;
                        }
                    }
                    if (!queue.isEmpty()) {
                        synchronized (queue) {
                            for (byte[] message:queue)
                                output.write(message);
                            queue.clear();
                        }
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
        synchronized (queue) {
            queue.add(bytes);
        }
    }

    /// Inform the parent app that this app is being closed.
    public static void terminateConnection() {
        if (socketThread == null) return;
        sendMessage(Messages.TERMINATE, new byte[0]);
        socketThread.interrupt();
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
            case Messages.LOAD_TEXTURES -> Main.loadTextures(bytes);
            case Messages.LOAD_MATERIALS -> Main.loadMaterials(bytes);
        }
    }
}
