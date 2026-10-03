package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TextureArrayBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;

public class PartUndefined extends Part {
    public PartUndefined(TextureArrayBlock.TexturesData textures, MaterialsBlock.MaterialsData materials) {
        super(textures, materials);
    }

    @Override
    protected void readData(BlockReader reader, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading Undefined Part");
        BYTE_DATA = reader.readBytes(reader.getSize());
        reader.seek(0);
    }

    @Override
    public void writeData(BlockWriter writer, KARFile.ProgressUpdater progress) {
        progress.setMessage("Writing Undefined Part");
        writer.writeBytes(BYTE_DATA);
    }

    @Override
    public String toString() {
        return "Unknown";
    }
}
