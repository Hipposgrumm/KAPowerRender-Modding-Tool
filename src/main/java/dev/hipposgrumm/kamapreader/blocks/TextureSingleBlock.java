package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.util.List;

public class TextureSingleBlock extends Block {
    private ResourceCheckBlock rsck;
    private ArCkBlock arck;

    private byte[] head;
    public Texture texture;

    @Override
    protected void read(BlockReader reader) {
        rsck = ResourceCheckBlock.read(reader, "TrFm");
        arck = ArCkBlock.read(reader, "TrFm");

        head = reader.readBytes(4);
        if (head[0] != BlockType.TEXTURE_SINGLE)
            throw new IllegalStateException(String.format("Block data value (0x%02X) does not match for type TEXTURE_SINGLE", head[0]));

        BlockReader texreader = reader.segment(reader.readIntLittle());
        texture = new Texture(texreader);
    }

    @Override
    public void write(BlockWriter writer) {
        rsck.write(writer.segment());
        arck.write(writer.segment());

        writer.writeBytes(head);

        writer = writer.segment();
        writer.writeIntLittle(0);

        texture.write(writer.segment());

        writer.seek(0);
        writer.writeIntLittle(writer.getSize()-4);
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return texture.getDatingProfile();
    }

    @Override
    public String getBlockType() {
        return "TrFm";
    }

    @Override
    public String toString() {
        return "Texture - "+texture.toString();
    }
}
