package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.SnSound;

import java.util.ArrayList;
import java.util.List;

public class SoundsBlock extends Block {
    private ResourceCheckBlock rsck;
    private ArCkBlock arck;
    private byte[] tailingbytes;

    private final List<SnSound> sounds = new ArrayList<>();

    @Override
    protected void read(BlockReader reader) {
        rsck = ResourceCheckBlock.read(reader, "SnFm");
        arck = ArCkBlock.read(reader, "SnFm");
        while (reader.getRemaining() >= 40) {
            byte[] un = reader.readBytes(40);
            int size = reader.readIntLittle();
            int un2 = reader.readIntLittle();
            SnSound snd = new SnSound(reader.readBytes(size));
            snd.UNKNOWN1 = un;
            snd.UNKNOWN2 = un2;
            sounds.add(snd);
        }
        tailingbytes = reader.readBytes(reader.getRemaining());
    }

    @Override
    public void write(BlockWriter writer) {
        rsck.write(writer.segment());
        arck.write(writer.segment());
        for (SnSound snd:sounds) {
            writer.writeBytes(snd.UNKNOWN1);
            writer.writeIntLittle(snd.getData().length);
            writer.writeIntLittle(snd.UNKNOWN2);
            writer.writeBytes(snd.getData());
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
