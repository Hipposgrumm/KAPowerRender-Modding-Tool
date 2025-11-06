package dev.hipposgrumm.kamapreader.blocks.subblock;

import dev.hipposgrumm.kamapreader.blocks.UnknownBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

import java.io.IOException;

public class ArCkBlock extends UnknownBlock {
    private final int size;

    private ArCkBlock(int size) {
        super("ArCk");
        this.size = size;
    }

    public static ArCkBlock read(BlockReader reader, String from) throws IOException {
        if (!"ArCk".equals(reader.readBlockType())) throw new IllegalStateException(from+" block is malformed: No ArCk found!");
        ArCkBlock block = new ArCkBlock(reader.readInt());
        block.readFull(reader.segment(block.size));
        return block;
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        writer.writeRawString("ArCk");
        writer.writeInt(size);
        super.write(writer);
    }
}
