package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.util.Exportable;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.SnSound;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KASFile extends KADataFile implements Exportable {
    public static final String FILE_DESCRIPTOR = "KAStream (FMOD)";
    public static final String FILE_EXT = "*.kas";

    private byte[] seeminglyUnimportantBytes;
    public SnSound sound;

    @Override
    public KADataFile.SaveFile<?> getSaveFunction(File file) {
        return new SaveFile(this, file);
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        List<DatingProfileEntry> list = new ArrayList<>(super.getDatingProfile());
        list.addAll(sound.getDatingProfile());
        return list;
    }

    @Override
    public ContextMenuOption[] getContextMenu() {
        return sound.getContextMenu();
    }

    @Override
    public void onSelected() {
        sound.onSelected();
    }

    @Override
    public String getFileName() {
        return sound.getFileName();
    }

    @Override
    public String getFileExtension() {
        return sound.getFileExtension();
    }

    @Override
    public void writeExportData(FileOutputStream outputStream) throws IOException {
        sound.writeExportData(outputStream);
    }

    @Override
    protected String defaultFilename() {
        return "audioStream.KAS";
    }

    @Override
    public String fileDescriptor() {
        return FILE_DESCRIPTOR;
    }

    @Override
    public String fileChooserExt() {
        return FILE_EXT;
    }

    public static class LoadFile extends KADataFile.LoadFile<KASFile> {
        public LoadFile(File file) {
            super(file);
        }

        @Override
        protected KASFile execute() throws Exception {
            String filename = file.getName();
            KASFile kas = new KASFile();
            kas.filename = new KADataFilePathString(filename);

            updateMessage("Starting Read");
            BlockReader reader = new BlockReader(file);
            kas.seeminglyUnimportantBytes = reader.readBytes(4);
            kas.sound = new SnSound(filename.substring(0, filename.lastIndexOf('.')));
            kas.sound.setData(reader.readBytes(reader.getRemaining()));
            updateMessage("Done!");

            return kas;
        }
    }

    public static class SaveFile extends KADataFile.SaveFile<KASFile> {
        public SaveFile(KASFile kas, File file) {
            super(kas, file);
        }

        @Override
        protected void execute() throws Exception {
            updateMessage("Starting Write");
            BlockWriter writer = new BlockWriter();
            writer.writeBytes(data.seeminglyUnimportantBytes);
            writer.writeBytes(data.sound.getData());
            writer.writeout(file);
        }
    }
}
