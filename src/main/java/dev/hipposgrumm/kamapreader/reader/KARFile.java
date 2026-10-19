package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.blocks.*;
import dev.hipposgrumm.kamapreader.util.types.Material;

import java.io.File;
import java.util.*;

public class KARFile extends KADataFile {
    public static final String FILE_DESCRIPTOR = "KAResource";
    public static final String FILE_EXT = "*.kar";

    public final List<Block> blocks = new ArrayList<>();

    @Override
    public KADataFile.SaveFile<?> getSaveFunction(File file) {
        return new SaveFile(this, file);
    }

    @Override
    protected String defaultFilename() {
        return "KARFile.kar";
    }

    @Override
    public String fileDescriptor() {
        return FILE_DESCRIPTOR;
    }

    @Override
    public String fileChooserExt() {
        return FILE_EXT;
    }

    public static class LoadFile extends KADataFile.LoadFile<KARFile> {
        public LoadFile(File file) {
            super(file);
        }

        @Override
        protected KARFile execute() throws Exception {
            KARFile kar = new KARFile();
            kar.filename = new KADataFilePathString(file.getName());

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
                    case "TxFm" -> new MaterialTexturesBlock();
                    case "TrFm" -> new TextureSetBlock();
                    case "MtFm" -> new MaterialsBlock(kar);
                    case "SnFm" -> new SoundsBlock(file.getName(), kar);
                    case "ObFm" -> new ObjectsBlock();
                    case "FtFm" -> new FontsBlock();
                    case "LvFm" -> new LevelDefinesBlock();
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
                if (block instanceof PartsBlock bl) {
                    try {
                        bl.fillMaterials(materials);
                    } catch (Exception e) {
                        System.err.println("Error while filling materials:");
                        e.printStackTrace();
                    }
                }
            }

            return kar;
        }
    }

    public static class SaveFile extends KADataFile.SaveFile<KARFile> {
        public SaveFile(KARFile kar, File file) {
            super(kar, file);
        }

        @Override
        protected void execute() throws Exception {
            updateMessage("Starting Write");
            BlockWriter writer = new BlockWriter();
            ProgressUpdater progress = new ProgressUpdater(this, 0);
            writer.writeRawString("CAT \0\0\0\0    ");
            for (Block block:data.blocks) {
                progress.setMessage("Writing "+block.getBlockType());

                writer.setLittleEndian(block.isLittleEndian());

                BlockWriter subWriter = writer.segment();
                subWriter.writeRawString(writer.isLittleEndian() ?
                        "RIFF\0\0\0\0" :
                        "FORM\0\0\0\0"
                );
                subWriter.writeRawString(block.getBlockType());

                block.writeFull(subWriter, progress);
                progress.setMessage(""); // avoid blame game in case something goes wrong between this and the next one

                subWriter.seek(4);
                subWriter.writeInt(subWriter.getSize()-8);
            }
            updateMessage("Done!");

            writer.setLittleEndian(false);
            writer.seek(4);
            writer.writeInt(writer.getSize()-8);

            writer.writeout(file);
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
}
