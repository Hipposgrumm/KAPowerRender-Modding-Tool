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
    private static final Image export;
    private static final Image redirect;
    private static final Image hashtag;
    private static final Image text;

    static {
        ClassLoader loader = Icon.class.getClassLoader();
        play = new Image(loader.getResourceAsStream("play-solid-full.png"));
        pause = new Image(loader.getResourceAsStream("pause-solid-full.png"));
        stop = new Image(loader.getResourceAsStream("stop-solid-full.png"));
        loop = new Image(loader.getResourceAsStream("repeat-solid.png"));
        download = new Image(loader.getResourceAsStream("download-solid.png"));
        upload = new Image(loader.getResourceAsStream("file-import-solid.png"));
        export = new Image(loader.getResourceAsStream("file-export-solid.png"));
        redirect = new Image(loader.getResourceAsStream("arrow-up-right-from-square-solid.png"));
        hashtag = new Image(loader.getResourceAsStream("hashtag-solid.png"));
        text = new Image(loader.getResourceAsStream("text-height-solid.png"));
    }

    public static ImageView play(double size) {
        return icon(play, size);
    }

    public static ImageView pause(double size) {
        return icon(pause, size);
    }

    public static ImageView stop(double size) {
        return icon(stop, size);
    }

    public static ImageView loop(double size) {
        return icon(loop, size);
    }

    public static ImageView download(double size) {
        return icon(download, size);
    }

    public static ImageView upload(double size) {
        return icon(upload, size);
    }

    public static ImageView export(double size) {
        return icon(export, size);
    }

    public static ImageView redirect(double size) {
        return icon(redirect, size);
    }

    public static ImageView hashtag(double size) {
        return icon(hashtag, size);
    }

    public static ImageView text(double size) {
        return icon(text, size);
    }

    private static ImageView icon(Image image, double size) {
        ImageView view = new ImageView(image);
        view.setFitWidth(size);
        view.setFitHeight(size);
        return view;
    }
}
