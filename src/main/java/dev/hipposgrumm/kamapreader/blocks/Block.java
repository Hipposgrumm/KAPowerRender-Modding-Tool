package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;

import java.io.IOException;

public abstract class Block implements DatingBachelor {
    private boolean successful = false;
    protected boolean littleEndian;

    public final void readFull(BlockReader reader) throws IOException {
        try {
            littleEndian = reader.isLittleEndian();
            read(reader);
            successful = true;
        } catch (Exception e) {
            // IOExceptions should go straight to the top.
            // Anything else can just get logged silently.
            if (e instanceof IOException) throw e;
            e.printStackTrace();
        }
        // Regardless of the outcome, go back to normal... unless there's an IOException.
        reader.seek(reader.getSize());
    }

    public final boolean successfullyRead() {
        return successful;
    }

    public final boolean isLittleEndian() {
        return littleEndian;
    }

    protected abstract void read(BlockReader reader) throws IOException;

    public abstract void write(BlockWriter writer) throws IOException;

    public abstract String getBlockType();

    public abstract String toString();
}
