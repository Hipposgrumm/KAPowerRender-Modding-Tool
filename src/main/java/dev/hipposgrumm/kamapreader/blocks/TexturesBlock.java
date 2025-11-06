package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TexturesBlock extends Block {
    private ResourceCheckBlock rsck;
    private final boolean hasArCk;
    private ArCkBlock arck;
    private byte[] unknown;

    private final List<Texture> textures = new ArrayList<>();

    public TexturesBlock(boolean arckVariant) {
        this.hasArCk = arckVariant;
    }

    @Override
    protected void read(BlockReader reader) throws IOException {
        rsck = ResourceCheckBlock.read(reader, getBlockType());
        if (hasArCk) {
            arck = ArCkBlock.read(reader, "TrFm");
        } else unknown = reader.readBytes(12);

        reader.setLittleEndian(true);
        while (reader.getRemaining() > 0) {
            textures.add(new Texture(reader, arck != null));
        }
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        rsck.write(writer.segment());
        if (arck != null) arck.write(writer.segment());

        writer.setLittleEndian(true);
        writer.writeBytes(unknown);
        for (Texture tex:textures) {
            tex.write(writer.segment());
        }
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return List.of(new SubBachelorPreviewEntry(
                () -> textures
        ));
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return textures;
    }

    @Override
    public String getBlockType() {
        return arck != null ? "TrFm" : "TxFm";
    }

    @Override
    public String toString() {
        return "Textures ("+getBlockType()+")";
    }
}
