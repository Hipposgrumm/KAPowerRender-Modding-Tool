package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.MaterialTexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;

public class PartUnknown extends Part {
    public PartUnknown() {
        super(null, null);
    }

    public PartUnknown(MaterialTexturesBlock.TexturesData textures, MaterialsBlock.MaterialsData materials) {
        super(textures, materials);
    }

    @Override
    public void readData(BlockReader reader, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading Unknown Part");
        BYTE_DATA = reader.readBytes(reader.getRemaining());
        reader.seek(0);
    }

    @Override
    public void writeData(BlockWriter writer, KARFile.ProgressUpdater progress) {
        progress.setMessage("Writing Unknown Part");
        writer.writeBytes(BYTE_DATA);
    }

    @Override
    public String toString() {
        return "Unknown";
    }
}
