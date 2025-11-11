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
import java.util.*;

public class TexturesBlock extends Block {
    private ResourceCheckBlock rsck;
    private final boolean hasArCk;
    private ArCkBlock arck;
    private byte[] unknown;

    private final List<Texture> textureList = new ArrayList<>();
    public final Map<Integer, Texture> textures = new HashMap<>();

    public TexturesBlock(boolean arckVariant) {
        this.hasArCk = arckVariant;
    }

    @Override
    protected void read(BlockReader reader) throws IOException {
        rsck = ResourceCheckBlock.read(reader, getBlockType());
        if (hasArCk) {
            arck = ArCkBlock.read(reader, "TrFm");
        } else unknown = reader.readBytes(12);

        while (reader.getRemaining() > 0) {
            Texture tex = new Texture(reader, arck != null);
            textures.put(tex.getUid().get(), tex);
            textureList.add(tex);
        }
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        rsck.write(writer.segment());
        if (arck != null) arck.write(writer.segment());

        if (unknown != null) writer.writeBytes(unknown);
        for (Texture tex:textureList) {
            tex.write(writer.segment());
        }
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return List.of(new SubBachelorPreviewEntry(
                () -> textureList
        ));
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return textureList;
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
