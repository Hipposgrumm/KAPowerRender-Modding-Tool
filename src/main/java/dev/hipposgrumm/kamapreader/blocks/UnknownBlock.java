package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;

import java.util.List;

public class UnknownBlock extends Block {
    private final String name;
    private byte[] data;

    public UnknownBlock(String name) {
        this.name = name;
    }

    @Override
    protected void read(BlockReader reader, KARFile.ProgressUpdater progress) {
        data = reader.readBytes(reader.getRemaining());
    }

    @Override
    public void write(BlockWriter writer) {
        writer.writeBytes(data);
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(
                DatingProfileEntry.simple("data", // TODO: Hex Editor
                        () -> data,
                        d -> data = d
                )
        );
    }

    @Override
    public String getBlockType() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
