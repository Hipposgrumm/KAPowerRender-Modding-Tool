package dev.hipposgrumm.kamapreader.util.control.display;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.Icon;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import dev.hipposgrumm.kamapreader.util.types.SnSound;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoundDisplay implements DatingProfileValue {
    private static boolean autoplay = false;
    private static boolean loopingMode = false;

    private final SnSound sound;
    private boolean isModified = false;

    public SoundDisplay(SnSound sound) {
        this.sound = sound;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        Node soundInterface;
        PlayerRef playerref = new PlayerRef();
        Slider progress = new Slider(0, 0, 0);
        try {
            playerref.player = new Player(sound);
            progress.setMax(playerref.player.length());

            Button playbtn = new Button("", Icon.play());
            Button pausebtn = new Button("", Icon.pause());
            Button stopbtn = new Button("", Icon.stop());
            ToggleButton loopbtn = new ToggleButton("", Icon.loop());
            CheckBox autoplaybtn = new CheckBox("Auto-play");

            loopbtn.setSelected(loopingMode);
            autoplaybtn.setSelected(autoplay);
            playerref.player.setLooping(loopingMode);
            playbtn.setOnAction(event -> playerref.player.resume());
            pausebtn.setOnAction(event -> playerref.player.pause());
            stopbtn.setOnAction(event -> playerref.player.stop());
            loopbtn.setOnAction(event -> {
                loopingMode = loopbtn.isSelected();
                playerref.player.setLooping(loopingMode);
            });
            autoplaybtn.setOnAction(event -> {
                autoplay = autoplaybtn.isSelected();
            });

            AtomicBoolean modifyingState = new AtomicBoolean(false);
            AtomicBoolean playingState = new AtomicBoolean();
            progress.setOnMousePressed(event -> {
                modifyingState.set(true);
                playingState.set(playerref.player.isPlaying());
                playerref.player.pause();
            });
            progress.setOnMouseReleased(event -> {
                playerref.player.playFrom((int) Math.round(progress.getValue()));
                if (!playingState.get()) playerref.player.pause();
                modifyingState.set(false);
            });

            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (progress.getScene() == null) {
                        stop();
                        playerref.player.close();
                        return;
                    }

                    if (!modifyingState.get()) progress.setValue(playerref.player.getPosition());
                }
            }.start();

            soundInterface = new VBox(10, progress, autoplaybtn, new HBox(5,
                    playbtn, pausebtn, stopbtn, loopbtn
            ));

            if (autoplay) playerref.player.resume();
        } catch (Exception e) {
            e.printStackTrace();
            soundInterface = new Label("Preview could not be loaded.");
        }

        Button saveButton = new Button("Save Sound", Icon.download());
        Button changeButton = new Button("Replace Sound", Icon.upload());
        saveButton.setOnAction(event -> {
            try {
                File file = controller.popupSaveFile("Export File", sound.exportFileName+".ogg", "OGG", "*.ogg");
                if (file == null) return;
                if (!file.getName().endsWith(".ogg")) file = new File(file.getPath()+".ogg");
                if ((!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?"))) return;

                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    sound.writeExportData(outputStream);
                }
            } catch (Exception e) {
                controller.popupError("Error Saving", "An exception was thrown when exporting.", e);
            }
        });
        if (readonly) changeButton.setDisable(true);
        else changeButton.setOnAction(event -> {
            try {
                File file = controller.popupOpenFile("Choose a File", null, "OGG", "*.ogg");
                if (file == null) return;
                try (InputStream input = new FileInputStream(file)) {
                    String name = file.getName();
                    int extPos = name.lastIndexOf('.');
                    name = name.substring(0, extPos);
                    sound.copyFrom(new SnSound(name, input.readAllBytes()));
                    if (playerref.player != null) {
                        playerref.player.close();
                        playerref.player = new Player(sound);
                        progress.setMax(playerref.player.length());
                    }
                    isModified = true;
                    onChanged.run();
                }
            } catch (Exception e) {
                controller.popupError("Error", "An exception was thrown when modifying sound.", e);
            }
        });

        return new VBox(5,
                soundInterface,
                saveButton,
                changeButton
        );
    }

    @Override
    public boolean isModified() {
        return isModified;
    }

    private static class PlayerRef {
        public Player player = null;
    }

    private static class Player implements AutoCloseable {
        private final Object lock = new Object();
        private final SnSound sound;
        private final SourceDataLine line;

        private int startPosition = 0;
        private int lineOffset = 0;
        private boolean playing = false;
        private boolean loop = false;

        public Player(SnSound sound) throws LineUnavailableException {
            this.sound = sound;

            // EPresident on StackOverflow
            // https://stackoverflow.com/q/33110772/20170780
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, sound.FORMAT);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(sound.FORMAT);

            Thread thread = new Thread(() -> {
                int i = -1;
                int framesize = sound.FORMAT.getFrameSize();
                while (line.isOpen()) {
                    if (playing) {
                        if (i == -1) {
                            i = startPosition * framesize;
                            lineOffset = line.getFramePosition()-startPosition;
                        }
                        byte[] buffer = new byte[line.available()];
                        int available = sound.DATA_DECODED.length - i;
                        int read = buffer.length;
                        boolean looped = false;
                        if (available >= buffer.length) {
                            System.arraycopy(sound.DATA_DECODED, i, buffer, 0, buffer.length);
                        } else {
                            System.arraycopy(sound.DATA_DECODED, i, buffer, 0, available);
                            if (loop && sound.DATA_DECODED.length >= buffer.length) {
                                System.arraycopy(sound.DATA_DECODED, 0, buffer, available, buffer.length-available);

                                looped = true;
                            } else read = available;
                        }
                        if (loop || available > 0) {
                            if (looped) {
                                line.drain();
                                i = buffer.length - available;
                                lineOffset = line.getFramePosition()-i;
                            } else i += read;
                            line.write(buffer, 0, read);
                            continue;
                        } else {
                            line.drain();
                            stop();
                        }
                    }
                    i = -1;
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }, "AudioPlayer");
            thread.setDaemon(true);
            thread.start();
        }

        /// Play from the beginning of the data.
        public void playFromBeginning() {
            stop();
            resume();
        }

        /**
         * Play from a position.
         * @param pos Position (in seconds).
         */
        public void playFrom(float pos) {
            playFrom(pos * sound.FORMAT.getFrameRate());
        }

        /**
         * Play from a position.
         * @param frame Frame to start at.
         */
        public void playFrom(int frame) {
            stop();
            startPosition = frame;
            lineOffset = line.getFramePosition()-frame;
            resume();
        }

        public void resume() {
            if (playing) return;
            synchronized (lock) {
                playing = true;
                line.start();
                lock.notifyAll();
            }
        }

        public void pause() {
            if (!playing) return;
            playing = false;
            line.stop();
        }

        public void stop() {
            if (!playing) return;
            pause();
            line.flush();
            startPosition = 0;
            lineOffset = line.getFramePosition();
        }

        public boolean isPlaying() {
            return playing;
        }

        /// @return current frame position
        public int getPosition() {
            return line.getFramePosition()-lineOffset;
        }

        /// @return size in frames
        public int length() {
            return sound.DATA_DECODED.length / sound.FORMAT.getFrameSize();
        }

        /// Converts a frame value from either of the fields above to seconds.
        public float framesToSeconds(int frame) {
            return frame / sound.FORMAT.getFrameRate();
        }

        public boolean isLooping() {
            return loop;
        }

        public void setLooping(boolean looping) {
            this.loop = looping;
        }

        @Override
        public void close() {
            stop();
            line.close();
        }
    }
}
