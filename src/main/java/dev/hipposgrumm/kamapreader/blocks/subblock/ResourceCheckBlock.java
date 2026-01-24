package dev.hipposgrumm.kamapreader.blocks.subblock;

import dev.hipposgrumm.kamapreader.blocks.UnknownBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class ResourceCheckBlock extends UnknownBlock {
    private final int size;

    private ResourceCheckBlock(int size) {
        super("RsCk");
        this.size = size;
    }

    public static ResourceCheckBlock read(BlockReader reader, String from) {
        if (!"RsCk".equals(reader.readBlockType())) throw new IllegalStateException(from+" block is malformed: No RsCk found!");
        ResourceCheckBlock block = new ResourceCheckBlock(reader.readInt());
        block.readFull(reader.segment(block.size));
        return block;
    }

    @Override
    public void write(BlockWriter writer) {
        writer.writeRawString("RsCk");
        writer.writeInt(size);
        super.write(writer);
    }
}
