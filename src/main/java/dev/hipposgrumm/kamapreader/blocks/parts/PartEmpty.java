package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;

public class PartEmpty extends Part {
    public PartEmpty() {
        super(null, null);
    }

    @Override
    protected void readData(BlockReader reader, KARFile.ProgressUpdater progress) {}

    @Override
    protected void writeData(BlockWriter writer) {}

    @Override
    public String toString() {
        return "empty";
    }
}
