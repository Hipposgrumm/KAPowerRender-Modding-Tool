package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.util.ArrayList;
import java.util.List;

public class TextureSingleBlock extends Block.ArrayChecked {
    private byte[] head;
    public Texture texture;

    private boolean isPlaceholder = false;

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading Texture");
        if (count != 1) System.out.println("WARNING: Dubious texture count for single texture: "+count);
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        if (index != 0) return;
        isPlaceholder = false;

        head = reader.readBlockHead();
        if (head[0] != BlockType.TEXTURE_SINGLE)
            throw new IllegalStateException(String.format("Block data value (0x%02X) does not match for type TEXTURE_SINGLE", head[0]));

        BlockReader texreader = reader.segment(reader.readIntLittle());
        texture = new Texture(texreader);
    }

    @Override
    protected void readPlaceholderFromArray(int index) {
        if (index != 0) return;

        isPlaceholder = true;
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        progress.setMessage("Writing Texture "+texture);
        return 1;
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        if (isPlaceholder) return false;

        writer.writeBlockHead(head);

        writer = writer.segment();
        writer.writeIntLittle(0);

        texture.write(writer.segment());

        writer.seek(0);
        writer.writeIntLittle(writer.getSize()-4);
        return true;
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        List<DatingProfileEntry> list = new ArrayList<>(texture.getDatingProfile());
        list.add(getMetadataDatingProfileEntry());
        return list;
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
