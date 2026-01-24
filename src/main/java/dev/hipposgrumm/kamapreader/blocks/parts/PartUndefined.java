package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PartUndefined extends Part {
    public PartUndefined(TexturesBlock.TexturesData textures, MaterialsBlock.MaterialsData materials) {
        super(textures, materials);
    }

    @Override
    protected void readData(BlockReader reader) {
        BYTE_DATA = reader.readBytes(reader.getSize());
        reader.seek(0);
    }

    @Override
    public void writeData(BlockWriter writer) {
        writer.writeBytes(BYTE_DATA);
    }

    @Override
    public String toString() {
        return "Unknown";
    }
}
