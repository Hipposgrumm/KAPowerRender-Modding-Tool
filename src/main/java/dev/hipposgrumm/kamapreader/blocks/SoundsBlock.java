package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.SnSound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoundsBlock extends Block {
    private ResourceCheckBlock rsck;
    private ArCkBlock arck;
    private byte[] tailingbytes;

    private final List<SnSound> sounds = new ArrayList<>();

    @Override
    protected void read(BlockReader reader) throws IOException {
        rsck = ResourceCheckBlock.read(reader, "SnFm");
        arck = ArCkBlock.read(reader, "SnFm");
        boolean littleEndian = reader.isLittleEndian();
        while (reader.getRemaining() >= 40) {
            byte[] un = reader.readBytes(40);
            reader.setLittleEndian(true);
            int size = reader.readInt();
            int un2 = reader.readInt();
            SnSound snd = new SnSound(reader.readBytes(size));
            snd.UNKNOWN1 = un;
            snd.UNKNOWN2 = un2;
            sounds.add(snd);
            reader.setLittleEndian(littleEndian);
        }
        tailingbytes = reader.readBytes(reader.getRemaining());
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        rsck.write(writer.segment());
        arck.write(writer.segment());
        boolean littleEndian = writer.isLittleEndian();
        for (SnSound snd:sounds) {
            writer.writeBytes(snd.UNKNOWN1);
            writer.setLittleEndian(true);
            writer.writeInt(snd.getData().length);
            writer.writeInt(snd.UNKNOWN2);
            writer.writeBytes(snd.getData());
            writer.setLittleEndian(littleEndian);
        }
        writer.writeBytes(tailingbytes);
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return null;
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return sounds;
    }

    @Override
    public String getBlockType() {
        return "SnFm";
    }

    @Override
    public String toString() {
        return "Sounds";
    }
}
