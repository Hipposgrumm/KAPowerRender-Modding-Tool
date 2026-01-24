package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PartEmpty extends Part {
    public PartEmpty() {
        super(null, null);
    }

    @Override
    protected void readData(BlockReader reader) {}

    @Override
    protected void writeData(BlockWriter writer) {}

    @Override
    public String toString() {
        return "empty";
    }
}
