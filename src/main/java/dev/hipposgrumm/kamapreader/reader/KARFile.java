package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.blocks.*;
import dev.hipposgrumm.kamapreader.util.types.Material;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KARFile extends Task<KARFile> {
    public final File file;
    public final List<Block> blocks = new ArrayList<>();
    private boolean loadTriggeredAlready = false;

    private int maxsize;

    public KARFile(File file) {
        this.file = file;
    }

    @Override
    protected KARFile call() throws Exception {
        if (loadTriggeredAlready) {
            cancel();
            return null;
        }
        loadTriggeredAlready = true;

        updateMessage("Starting Read");
        BlockReader reader = new BlockReader(file);
        ProgressUpdater progress = new ProgressUpdater(reader.getSize());
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
                case "MtFm" -> new MaterialsBlock(this);
                case "SnFm" -> new SoundsBlock(file.getName(), this);
                case "ObFm" -> new ObjectsBlock();
                case "FtFm" -> new FontsBlock();
                default -> new UnknownBlock(blockType);
            };
            block.readFull(reader.segment(blockSize), progress);
            progress.setMessage(""); // avoid blame game in case something goes wrong between this and the next one
            blocks.add(block);
        }
        progress.updateWith(reader);
        progress.setMessage("Done!");

        Map<Integer, Material> materials = new HashMap<>();
        for (Block block:blocks) {
            if (!(block instanceof MaterialsBlock matBlock)) continue;
            for (Map.Entry<Integer, MaterialsBlock.MaterialRef> entry:matBlock.data.materials.entrySet()) {
                materials.putIfAbsent(entry.getKey(), entry.getValue().material());
            }
        }
        for (Block block:blocks) {
            if (block instanceof PartsBlock bl) bl.fillMaterials(materials);
        }

        return this;
    }

    public void save(File file) throws IOException {
        BlockWriter writer = new BlockWriter();
        writer.writeRawString("CAT \0\0\0\0    ");
        for (Block block:blocks) {
            writer.setLittleEndian(block.isLittleEndian());

            BlockWriter subWriter = writer.segment();
            subWriter.writeRawString(writer.isLittleEndian() ?
                    "RIFF\0\0\0\0" :
                    "FORM\0\0\0\0"
            );
            subWriter.writeRawString(block.getBlockType());

            block.write(subWriter);

            subWriter.seek(4);
            subWriter.writeInt(subWriter.getSize()-8);
        }

        writer.setLittleEndian(false);
        writer.seek(4);
        writer.writeInt(writer.getSize()-8);

        writer.writeout(file);
    }

    public final class ProgressUpdater {
        private final int maxsize;

        ProgressUpdater(int maxsize) {
            this.maxsize = maxsize;
        }

        public void updateWith(BlockReader reader) {
            KARFile.this.updateProgress(reader.getTruePointer(), maxsize);
        }

        public void setMessage(String message) {
            KARFile.this.updateMessage(message);
        }
    }
}
