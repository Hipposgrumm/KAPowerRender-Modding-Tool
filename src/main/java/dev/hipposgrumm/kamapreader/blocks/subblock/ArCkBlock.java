package dev.hipposgrumm.kamapreader.blocks.subblock;

import dev.hipposgrumm.kamapreader.blocks.UnknownBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;

public class ArCkBlock extends UnknownBlock {
    private final int size;

    private ArCkBlock(int size) {
        super("ArCk");
        this.size = size;
    }

    public static ArCkBlock read(BlockReader reader, String from) {
        if (!"ArCk".equals(reader.readBlockType())) throw new IllegalStateException(from+" block is malformed: No ArCk found!");
        ArCkBlock block = new ArCkBlock(reader.readInt());
        block.readFull(reader.segment(block.size), null);
        return block;
    }

    @Override
    public void write(BlockWriter writer, KARFile.ProgressUpdater progress) {
        writer.writeRawString("ArCk");
        writer.writeInt(size);
        super.write(writer, progress);
    }
}
