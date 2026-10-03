package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.blocks.*;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.Material;
import dev.hipposgrumm.kamapreader.util.types.wrappers.FilePathString;
import javafx.concurrent.Task;

import java.io.File;
import java.util.*;

public class KARFile implements DatingBachelor {
    public FilePathString filename = new KARFilePathString("KARFile.kar");
    public final List<Block> blocks = new ArrayList<>();

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(new DatingProfileEntry("Filename", false, filename));
    }

    public static class LoadFile extends AccessibleTask<KARFile> {
        public final File file;
        public LoadFile(File file) {
            this.file = file;
        }

        @Override
        protected KARFile call() throws Exception {
            KARFile kar = new KARFile();
            kar.filename = new KARFilePathString(file.getName());

            updateMessage("Starting Read");
            BlockReader reader = new BlockReader(file);
            ProgressUpdater progress = new ProgressUpdater(this, reader.getSize());
            // Very start of the KAR file
            if (!"CAT ".equals(reader.readBlockType())) throw new IllegalArgumentException("File is not a KAResource file.");
            int size = reader.readIntBig();
            reader.move(4); // unread padding
            reader = reader.segment(size-4);

            while (reader.getRemaining() > 0) {
                progress.updateWith(reader);
                String blockFormat = reader.readBlockType();
                boolean littleEndian = switch (blockFormat) {
                    case "RIFF" -> true;
                    case "FORM" -> false;
                    case "CAT " -> throw new IllegalStateException("Nested category tags are not supported.");
                    default -> throw new IllegalStateException("Unknown block format: " + blockFormat);
                };
                reader.setLittleEndian(littleEndian);

                int blockSize = reader.readInt()-4;
                String blockType = reader.readBlockType();
                progress.setMessage("Reading "+blockType);
                Block block = switch (blockType) {
                    case "PtFm" -> new PartsBlock();
                    case "TxFm" -> new TextureArrayBlock();
                    case "TrFm" -> new TextureSingleBlock();
                    case "MtFm" -> new MaterialsBlock(kar);
                    case "SnFm" -> new SoundsBlock(file.getName(), kar);
                    case "ObFm" -> new ObjectsBlock();
                    case "FtFm" -> new FontsBlock();
                    default -> new UnknownBlock(blockType);
                };
                block.readFull(reader.segment(blockSize), progress);
                progress.setMessage(""); // avoid blame game in case something goes wrong between this and the next one
                kar.blocks.add(block);
            }
            progress.updateWith(reader);
            progress.setMessage("Done!");

            Map<Integer, Material> materials = new HashMap<>();
            for (Block block:kar.blocks) {
                if (!(block instanceof MaterialsBlock matBlock)) continue;
                for (Map.Entry<Integer, MaterialsBlock.MaterialRef> entry:matBlock.data.materials.entrySet()) {
                    materials.putIfAbsent(entry.getKey(), entry.getValue().material());
                }
            }
            for (Block block:kar.blocks) {
                if (block instanceof PartsBlock bl) bl.fillMaterials(materials);
            }

            return kar;
        }
    }

    public static class SaveFile extends AccessibleTask<Void> {
        private final KARFile kar;
        private final File file;

        public SaveFile(KARFile kar, File file) {
            this.kar = kar;
            this.file = file;
        }

        @Override
        protected Void call() throws Exception {
            updateMessage("Starting Write");
            BlockWriter writer = new BlockWriter();
            ProgressUpdater progress = new ProgressUpdater(this, 0);
            writer.writeRawString("CAT \0\0\0\0    ");
            for (Block block:kar.blocks) {
                progress.setMessage("Writing "+block.getBlockType());

                writer.setLittleEndian(block.isLittleEndian());

                BlockWriter subWriter = writer.segment();
                subWriter.writeRawString(writer.isLittleEndian() ?
                        "RIFF\0\0\0\0" :
                        "FORM\0\0\0\0"
                );
                subWriter.writeRawString(block.getBlockType());

                block.write(subWriter, progress);
                progress.setMessage(""); // avoid blame game in case something goes wrong between this and the next one

                subWriter.seek(4);
                subWriter.writeInt(subWriter.getSize()-8);
            }
            updateMessage("Done!");

            writer.setLittleEndian(false);
            writer.seek(4);
            writer.writeInt(writer.getSize()-8);

            writer.writeout(file);
            return null;
        }
    }

    @Override
    public String toString() {
        return filename.toString();
    }

    /// Allow these two methods to be accessible for {@link ProgressUpdater}
    private abstract static class AccessibleTask<T> extends Task<T> {
        @Override public void updateMessage(String message) {
            super.updateMessage(message);
        }
        @Override public void updateProgress(long workDone, long max) {
            super.updateProgress(workDone, max);
        }
    }

    public static final class ProgressUpdater {
        private final AccessibleTask<?> task;
        private final int maxsize;

        ProgressUpdater(AccessibleTask<?> task, int maxsize) {
            this.task = task;
            this.maxsize = maxsize;
        }

        public void updateWith(BlockReader reader) {
            task.updateProgress(reader.getTruePointer(), maxsize);
        }

        public void setMessage(String message) {
            task.updateMessage(message);
        }
    }

    private static class KARFilePathString extends FilePathString {
        public KARFilePathString(String string) {
            super(string);
        }

        @Override
        public boolean isModified() {
            return false; // Don't flag the user if the output filename is modified. They probably know what they're doing and it doesn't matter too much.
        }
    }
}
