package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.wrappers.FilePathString;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public abstract class KADataFile implements DatingBachelor {
    public FilePathString filename = new KADataFilePathString(defaultFilename());

    public static LoadFile<?> getLoadFunction(File file) {
        byte[] bytes = new byte[Math.min(4, (int) file.length())];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        } catch (IOException e) {
            return null;
        }

        if (bytes.length >= 4) {
            if (
                    bytes[0] == 'C' &&
                    bytes[1] == 'A' &&
                    bytes[2] == 'T' &&
                    bytes[3] == ' '
            ) return new KARFile.LoadFile(file);
            if (
                    bytes[0] == 0x31 &&
                    bytes[1] == (byte)0xCC &&
                    bytes[2] == (byte)0xCC &&
                    bytes[3] == (byte)0xCC
            ) return new KASFile.LoadFile(file);
        }

        int extPos = file.getName().lastIndexOf('.');
        if (extPos >= 0) {
            return switch (file.getName().substring(extPos+1).toUpperCase()) {
                case "KAR" -> new KARFile.LoadFile(file);
                case "KAS" -> new KASFile.LoadFile(file);
                default -> null;
            };
        }
        return null;
    }

    public abstract SaveFile<?> getSaveFunction(File file);

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(new DatingProfileEntry("Filename", false, filename));
    }

    protected abstract String defaultFilename();
    public abstract String fileDescriptor();
    public abstract String fileChooserExt();

    public static abstract non-sealed class LoadFile<T extends KADataFile> extends AccessibleTask<KADataFile> {
        public final File file;

        public LoadFile(File file) {
            this.file = file;
        }

        protected abstract T execute() throws Exception;

        @Override
        protected final KADataFile call() throws Exception {
            return execute();
        }
    }

    public static abstract non-sealed class SaveFile<T extends KADataFile> extends AccessibleTask<Void> {
        protected final T data;
        protected final File file;

        public SaveFile(T data, File file) {
            this.data = data;
            this.file = file;
        }

        protected abstract void execute() throws Exception;

        @Override
        protected final Void call() throws Exception {
            execute();
            return null;
        }
    }

    @Override
    public String toString() {
        return filename.toString();
    }

    /// Allow these two methods to be accessible for {@link KARFile.ProgressUpdater}
    protected static abstract sealed class AccessibleTask<T> extends Task<T> permits LoadFile, SaveFile {
        @Override public void updateMessage(String message) {
            super.updateMessage(message);
        }
        @Override public void updateProgress(long workDone, long max) {
            super.updateProgress(workDone, max);
        }
    }

    protected static class KADataFilePathString extends FilePathString {
        public KADataFilePathString(String string) {
            super(string);
        }

        @Override
        public boolean isModified() {
            return false; // Don't flag the user if the output filename is modified. They probably know what they're doing and it doesn't matter too much.
        }
    }
}
