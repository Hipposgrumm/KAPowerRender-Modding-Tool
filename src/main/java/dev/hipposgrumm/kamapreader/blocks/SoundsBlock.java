package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.SnSound;

import java.util.ArrayList;
import java.util.List;

public class SoundsBlock extends Block.ArrayChecked {
    public List<SnSound> sounds = new ArrayList<>();
    private SnSound[] soundsArrayBuilder;

    private final String soundPrefix;

    public SoundsBlock(String filename, KARFile file) {
        int index = 0;
        for (Block b : file.blocks) {
            if (b instanceof SoundsBlock) {
                index++;
            }
        }

        int extPos = filename.lastIndexOf('.');
        filename = filename.substring(0, extPos);

        soundPrefix = filename+'+'+index+'_';
    }

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading Sounds");
        soundsArrayBuilder = new SnSound[count];
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        String name = soundPrefix+index;
        progress.setMessage("Reading "+name);
        SnSound snd = new SnSound(name);
        snd.HEAD = reader.readBlockHead();

        snd.UNKNOWN1 = reader.readShort();
        snd.UNKNOWN2 = reader.readShort();
        snd.UNKNOWN3 = reader.readShort();
        reader.move(2);
        snd.UNKNOWN4 = reader.readInt();
        snd.UNKNOWN5 = reader.readInt();

        reader = reader.segment(reader.getRemaining());
        snd.UNKNOWN6 = reader.readBytes(4);
        snd.UNKNOWN7 = reader.readInt();
        snd.UNKNOWN8 = reader.readInt();
        snd.UNKNOWN9 = reader.readInt();
        snd.UNKNOWN10 = reader.readInt();
        int size = reader.readIntLittle();
        int fileDataOffset = reader.readIntLittle();
        reader.seek(fileDataOffset);
        snd.setData(reader.readBytes(size));
        soundsArrayBuilder[index] = snd;
    }

    @Override
    protected void readPlaceholderFromArray(int index) {
        soundsArrayBuilder[index] = new PlaceholderSound();
    }

    @Override
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {
        sounds = new ArrayList<>(List.of(soundsArrayBuilder));
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        progress.setMessage("Writing Sounds");
        return sounds.size();
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        SnSound snd = sounds.get(index);
        if (snd instanceof PlaceholderValue) return false;

        progress.setMessage("Writing "+snd);
        writer.writeBlockHead(snd.HEAD);

        writer.writeShort(snd.UNKNOWN1);
        writer.writeShort(snd.UNKNOWN2);
        writer.writeShort(snd.UNKNOWN3);
        writer.writeBytes(new byte[2]);
        writer.writeInt(snd.UNKNOWN4);
        writer.writeInt(snd.UNKNOWN5);

        writer = writer.segment();
        writer.writeBytes(snd.UNKNOWN6);
        writer.writeInt(snd.UNKNOWN7);
        writer.writeInt(snd.UNKNOWN8);
        writer.writeInt(snd.UNKNOWN9);
        writer.writeInt(snd.UNKNOWN10);

        writer.writeIntLittle(snd.getData().length);
        writer.writeIntLittle(writer.getPointer()+4);
        writer.writeBytes(snd.getData());
        return true;
    }

    @Override
    protected void writeForArrayEnd(BlockWriter writer, KARFile.ProgressUpdater progress) {
        int unalignedBytes = writer.getTruePointer() % 4;
        if (unalignedBytes != 0)
            // The function for adding these padding bytes should have been
            // `4 - unalignedBytes`. However, due to an oversight on part of
            // the developers this padding is handled incorrectly. Despite this,
            // the game runs anyway, so no-one ever bothered to fix it.
            // This quirk is retained here to keep parity with the original files.
            writer.writeBytes(new byte[unalignedBytes]);
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(getMetadataDatingProfileEntry());
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

    private static final class PlaceholderSound extends SnSound implements PlaceholderValue {
        public PlaceholderSound() {
            super("PLACEHOLDER");
        }
    }
}
