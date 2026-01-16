package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

import java.io.IOException;

public class PartUndefined extends Part {
    public PartUndefined(TexturesBlock.TexturesData textures, MaterialsBlock.MaterialsData materials) {
        super(textures, materials);
    }

    @Override
    protected void readData(BlockReader reader) throws IOException {
        BYTE_DATA = reader.readBytes(reader.getSize());
        reader.seek(0);
    }

    @Override
    public void writeData(BlockWriter writer) throws IOException {
        writer.writeBytes(BYTE_DATA);
    }

    @Override
    public String toString() {
        return "Unknown";
    }
}
