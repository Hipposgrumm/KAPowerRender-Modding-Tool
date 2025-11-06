package dev.hipposgrumm.kamapreader.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

@SuppressWarnings("DataFlowIssue")
public class Icon {
    private static final Image play;
    private static final Image pause;
    private static final Image stop;
    private static final Image loop;
    private static final Image download;
    private static final Image upload;

    static {
        ClassLoader loader = Icon.class.getClassLoader();
        play = new Image(loader.getResourceAsStream("play-solid-full.png"));
        pause = new Image(loader.getResourceAsStream("pause-solid-full.png"));
        stop = new Image(loader.getResourceAsStream("stop-solid-full.png"));
        loop = new Image(loader.getResourceAsStream("repeat-solid.png"));
        download = new Image(loader.getResourceAsStream("download-solid.png"));
        upload = new Image(loader.getResourceAsStream("file-import-solid.png"));
    }

    public static ImageView play() {
        return icon(play, 25);
    }

    public static ImageView pause() {
        return icon(pause, 25);
    }

    public static ImageView stop() {
        return icon(stop, 25);
    }

    public static ImageView loop() {
        return icon(loop, 25);
    }

    public static ImageView download() {
        return icon(download, 15);
    }

    public static ImageView upload() {
        return icon(upload, 15);
    }

    private static ImageView icon(Image image, double size) {
        ImageView view = new ImageView(image);
        view.setFitWidth(size);
        view.setFitHeight(size);
        return view;
    }
}
