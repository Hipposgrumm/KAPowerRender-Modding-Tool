package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.util.*;

public class TextureArrayBlock extends Block {
    private ResourceCheckBlock rsck;
    public TexturesData data;

    @Override
    protected void read(BlockReader reader) {
        rsck = ResourceCheckBlock.read(reader, "TxFm");
        data = new TexturesData(reader);
    }

    @Override
    public void write(BlockWriter writer) {
        rsck.write(writer.segment());
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
        return "TxFm";
    }

    @Override
    public String toString() {
        return "TextureSet";
    }

    public static class TexturesData {
        private byte[] head;
        public final ArrayList<Texture> textureList = new ArrayList<>();
        public final Map<Integer, Texture> textures = new HashMap<>();

        public TexturesData(BlockReader reader) {
            head = reader.readBlockHead();
            if (head[0] != BlockType.TEXTURE_ARRAY)
                throw new IllegalStateException(String.format("Block data value (0x%02X) does not match for type TEXTURE_ARRAY", head[0]));

            // Offset values need this at exactly the position of "head"
            int remainingSize = reader.readIntLittle();
            reader.move(-8);
            reader = reader.segment(remainingSize + 8);
            reader.move(8);

            // These values are probably used as-is in the actual game, therefore
            //     allowing textures to be read in a different order. This could
            //     also allow for an infinite loop if set up as such.
            int offsetNext = reader.readIntLittle();
            while (offsetNext != 0) {
                offsetNext = reader.readIntLittle();
                BlockReader texreader = reader.segment(offsetNext != 0
                        ? offsetNext - reader.getPointer()
                        : reader.getRemaining()
                );
                Texture tex = new Texture(texreader);
                textures.put(tex.getUid().get(), tex);
                textureList.add(tex);
            }
        }

        public void write(BlockWriter writer) {
            // TODO: Make sure to actually test this.
            writer = writer.segment();

            writer.writeBlockHead(head);
            writer.writeIntLittle(0);

            writer.writeIntLittle(writer.getPointer()+4);
            Iterator<Texture> texIter = textureList.iterator();
            while (texIter.hasNext()) {
                Texture tex = texIter.next();
                int offset = writer.getPointer();
                writer.writeIntLittle(0);

                tex.write(writer.segment());

                if (!texIter.hasNext()) continue;
                writer.seek(offset);
                writer.writeIntLittle(writer.getSize());
                writer.seek(writer.getSize());
            }
            writer.seek(4);
            writer.writeInt(writer.getSize()-8);
        }
    }
}
