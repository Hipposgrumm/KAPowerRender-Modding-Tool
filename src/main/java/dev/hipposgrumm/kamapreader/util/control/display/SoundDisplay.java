package dev.hipposgrumm.kamapreader.util.control.display;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.Icon;
import dev.hipposgrumm.kamapreader.util.types.SnSound;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoundDisplay {
    private static boolean loopingMode = false;

    private Player player;
    private SoundDisplay(Player player) {
        this.player = player;
    }

    public static Node create(FirstThing controller, DatingProfileEntry<SnSound> entry) {
        Node soundInterface;
        SoundDisplay changableDisplay = null;
        Slider changableProgressBar = null;
        try {
            SoundDisplay display = new SoundDisplay(new Player(entry.get()));
            Slider progress = new Slider(0, display.player.length(), 0);
            changableDisplay = display; // Lambda moment
            changableProgressBar = progress;

            Button playbtn = new Button("", Icon.play());
            Button pausebtn = new Button("", Icon.pause());
            Button stopbtn = new Button("", Icon.stop());
            ToggleButton loopbtn = new ToggleButton("", Icon.loop());

            loopbtn.setSelected(loopingMode);
            display.player.setLooping(loopingMode);
            playbtn.setOnAction(event -> display.player.resume());
            pausebtn.setOnAction(event -> display.player.pause());
            stopbtn.setOnAction(event -> display.player.stop());
            loopbtn.setOnAction(event -> {
                loopingMode = loopbtn.isSelected();
                display.player.setLooping(loopingMode);
            });
            controller.setKeyEventListener(event -> {
                KeyCode key = event.getCode();
                if (key == KeyCode.PLAY || key == KeyCode.SPACE) {
                    if (display.player.isPlaying()) {
                        display.player.pause();
                    } else {
                        display.player.resume();
                    }
                }
            });

            AtomicBoolean modifyingState = new AtomicBoolean(false);
            AtomicBoolean playingState = new AtomicBoolean();
            progress.setOnMousePressed(event -> {
                modifyingState.set(true);
                playingState.set(display.player.isPlaying());
                display.player.pause();
            });
            progress.setOnMouseReleased(event -> {
                display.player.playFrom((int) Math.round(progress.getValue()));
                if (!playingState.get()) display.player.pause();
                modifyingState.set(false);
            });

            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (progress.getScene() == null) {
                        stop();
                        display.player.close();
                        controller.setKeyEventListener(null);
                        return;
                    }

                    if (!modifyingState.get()) progress.setValue(display.player.getPosition());
                }
            }.start();

            soundInterface = new VBox(10, progress, new HBox(5,
                    playbtn, pausebtn, stopbtn, loopbtn
            ));
        } catch (Exception e) {
            e.printStackTrace();
            soundInterface = new Label("Preview could not be loaded.");
        }

        Button saveButton = new Button("Save Sound", Icon.download());
        Button changeButton = new Button("Replace Sound", Icon.upload());
        saveButton.setOnAction(event -> {
            try {
                File file = controller.popupSaveFile("Export File", "sound.ogg", "OGG", ".ogg");
                if (file == null) return;
                if (!file.getName().endsWith(".ogg")) file = new File(file.getPath()+".ogg");
                if ((!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?"))) return;

                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(entry.get().getData());
                }
            } catch (Exception e) {
                controller.popupError("Error Saving", "An exception was thrown when exporting.", e);
            }
        });
        SoundDisplay finalDisplay = changableDisplay;
        Slider finalProgressBar = changableProgressBar;
        changeButton.setOnAction(event -> {
            try {
                File file = controller.popupOpenFile("Choose a File", null, "OGG", ".ogg");
                if (file == null) return;
                try (InputStream input = new FileInputStream(file)) {
                    entry.set(new SnSound(input.readAllBytes()));
                    if (finalDisplay != null) {
                        finalDisplay.player.close();
                        finalDisplay.player = new Player(entry.get());
                        finalProgressBar.setMax(finalDisplay.player.length());
                    }
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
            synchronized (lock) {
                playing = true;
                line.start();
                lock.notifyAll();
            }
        }

        public void pause() {
            playing = false;
            line.stop();
        }

        public void stop() {
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
