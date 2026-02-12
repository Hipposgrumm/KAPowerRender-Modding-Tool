package dev.hipposgrumm.kamapreader.util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

public class ViewerAppHandle {
    private static final Map<Integer, Consumer<byte[]>> handlers = new HashMap<>();
    private static final List<byte[]> queue = new ArrayList<>();
    private static Thread processAliveThread = null;
    private static Thread processOutThread = null;
    private static Thread processErrThread = null;
    private static Thread socketThread = null;

    static {
        enactMessageHandler(Messages.TERMINATE, bytes -> {
            if (processAliveThread == null) return;
            disconnectProgram();
        });
        enactMessageHandler(Messages.TEST_ECHO, bytes -> {
            System.out.println(new String(bytes));
        });
    }

    /// Starts the [[3D Viewer App]].
    public static void startProgram() throws IOException {
        if (processAliveThread != null) terminateProgram();
        socketThread = new Thread(() -> {
            try (ServerSocket socket = new ServerSocket(0)) {
                Process proc = createInstance(socket.getLocalPort());
                processAliveThread = new Thread(() -> {
                    while (proc.isAlive()) Thread.onSpinWait();
                    if (processAliveThread != null) disconnectProgram();
                }, "KAMapViewer Alive Thread");
                processOutThread = new Thread(() -> {
                    watchAppOutput(proc.getInputStream(), processOutThread, System.out);
                }, "KAMapViewer Output Thread");
                processErrThread = new Thread(() -> {
                    watchAppOutput(proc.getErrorStream(), processErrThread, System.err);
                }, "KAMapViewer Error Thread");
                processAliveThread.setDaemon(true);
                processOutThread.setDaemon(true);
                processErrThread.setDaemon(true);
                processAliveThread.start();
                processOutThread.start();
                processErrThread.start();

                try (Socket connection = socket.accept()) {
                    InputStream input = connection.getInputStream();
                    OutputStream output = connection.getOutputStream();
                    int messageID = 0;
                    byte[] messageData = null; int messageDataIndex = 0;
                    while (socketThread != null && !socketThread.isInterrupted() && connection.isConnected()) {
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
                                Consumer<byte[]> handler = handlers.get(messageID);
                                if (handler != null) handler.accept(messageData);
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
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "KAMapViewer Server Thread");
        socketThread.setDaemon(true);
        socketThread.start();
    }

    private static void watchAppOutput(InputStream stream, Thread myThread, PrintStream out) {
        do {
            Scanner reader = new Scanner(stream);
            boolean hasNext = reader.hasNextLine();
            while (hasNext) {
                out.println("KAMapViewer: " + reader.nextLine());
                hasNext = reader.hasNextLine();
            }
        } while (!myThread.isInterrupted());
    }

    private static Process createInstance(int port) throws IOException {
        String jarpath;
        if (System.getProperty("user.dir").endsWith("bin")) {
            // Assume built jar.
            jarpath = "../KAMapViewer.jar";
        } else {
            jarpath = "KAMapViewer/KAMapViewer.jar";
        }
        if (!new File(jarpath).exists()) throw new IOException("KAMapViewer.jar is not present!");

        // https://stackoverflow.com/a/1320609/20170780
        return new ProcessBuilder("java", "-jar", jarpath, "port"+port).start();
    }

    /// Send the [[3D Viewer App]] a termination request.
    public static void terminateProgram() {
        if (processAliveThread == null) return;
        sendMessage(Messages.TERMINATE, new byte[0]);
        disconnectProgram();
    }

    private static void disconnectProgram() {
        processAliveThread.interrupt();
        processAliveThread = null;
        processOutThread.interrupt();
        processOutThread = null;
        processErrThread.interrupt();
        processErrThread = null;
        socketThread.interrupt();
        socketThread = null;
    }

    /// Send a message to the [[3D Viewer App]].
    public static void sendMessage(int message, byte[] data) {
        if (socketThread == null) {
            try {
                startProgram();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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

    public static void enactMessageHandler(int message, Consumer<byte[]> handler) {
        if (handler == null) throw new IllegalArgumentException("Use decommissionHandler to remove handlers.");
        handlers.put(message, handler);
    }

    public static void decommissionHandler(int message) {
        handlers.remove(message);
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
}
