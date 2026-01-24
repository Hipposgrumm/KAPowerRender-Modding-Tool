package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.util.*;

public class TexturesBlock extends Block {
    private ResourceCheckBlock rsck;
    private final boolean hasArCk;
    private ArCkBlock arck;
    public TexturesData data;

    public TexturesBlock(boolean arckVariant) {
        this.hasArCk = arckVariant;
    }

    @Override
    protected void read(BlockReader reader) {
        rsck = ResourceCheckBlock.read(reader, getBlockType());
        if (hasArCk) arck = ArCkBlock.read(reader, "TrFm");
        data = new TexturesData(reader, hasArCk);
    }

    @Override
    public void write(BlockWriter writer) {
        rsck.write(writer.segment());
        if (arck != null) arck.write(writer.segment());
        data.write(writer.segment());
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return List.of(new SubBachelorPreviewEntry(
                () -> data.textureList
        ));
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return data.textureList;
    }

    @Override
    public String getBlockType() {
        return arck != null ? "TrFm" : "TxFm";
    }

    @Override
    public String toString() {
        return "Textures ("+getBlockType()+")";
    }

    public static class TexturesData {
        private Integer unknown1;
        private Integer unknown2;
        public final List<Texture> textureList = new ArrayList<>();
        public final Map<Integer, Texture> textures = new HashMap<>();

        public TexturesData(BlockReader reader, boolean hasArCk) {
            if (!hasArCk) {
                unknown1 = reader.readIntLittle();
                reader = reader.segment(reader.readIntLittle());
                unknown2 = reader.readIntLittle();
            }

            while (reader.getRemaining() > 0) {
                Texture tex = new Texture(reader, hasArCk);
                textures.put(tex.getUid().get(), tex);
                textureList.add(tex);
            }
        }

        public void write(BlockWriter writer) {
            if (unknown1 != null) {
                writer.writeIntLittle(unknown1);
                writer = writer.segment();
                writer.writeIntLittle(0);
                writer.writeIntLittle(unknown2);
            }
            for (Texture tex:textureList) {
                tex.write(writer.segment());
            }
            if (unknown1 != null) {
                writer.seek(0);
                writer.writeInt(writer.getSize()-4);
            }
        }
    }
}
