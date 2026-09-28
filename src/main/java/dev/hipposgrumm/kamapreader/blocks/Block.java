package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;

public abstract class Block implements DatingBachelor {
    private boolean successful = false;
    protected boolean littleEndian;

    public final void readFull(BlockReader reader, KARFile.ProgressUpdater progress) {
        try {
            littleEndian = reader.isLittleEndian();
            read(reader, progress);
            successful = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final boolean successfullyRead() {
        return successful;
    }

    public final boolean isLittleEndian() {
        return littleEndian;
    }

    protected abstract void read(BlockReader reader, KARFile.ProgressUpdater progress);

    public abstract void write(BlockWriter writer);

    public abstract String getBlockType();

    public abstract String toString();
}
