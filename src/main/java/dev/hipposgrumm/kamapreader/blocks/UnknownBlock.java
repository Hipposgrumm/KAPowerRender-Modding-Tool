package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;

import java.util.List;

public class UnknownBlock extends Block {
    private final String name;
    private byte[] dataAll = null;
    private byte[][] dataArray = null;
    private int lastTruePointer = -1;

    public UnknownBlock(String name) {
        this.name = name;
    }

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        dataArray = new byte[count][];
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        int currTruePointer = reader.getTruePointer();
        if (index > 0) {
            byte[] lastArray = dataArray[index-1];
            byte[] accurateSizeArray = new byte[currTruePointer-lastTruePointer];
            System.arraycopy(lastArray, 0, accurateSizeArray, 0, accurateSizeArray.length);
            dataArray[index-1] = accurateSizeArray;
        }
        lastTruePointer = currTruePointer;

        dataArray[index] = reader.readBytes(reader.getRemaining());
    }

    @Override
    protected void readPlaceholderFromArray(int index) {
        dataArray[index] = null;
    }

    @Override
    protected void readWithAll(BlockReader reader, KARFile.ProgressUpdater progress) {
        dataAll = reader.readBytes(reader.getRemaining());
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        return dataArray.length;
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        if (dataArray[index] == null) return false;
        writer.writeBytes(dataArray[index]);
        return true;
    }

    @Override
    public void writeWithAll(BlockWriter writer, KARFile.ProgressUpdater progress) {
        writer.writeBytes(dataAll);
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(
                DatingProfileEntry.simple("data", // TODO: Hex Editor
                        () -> dataAll,
                        d -> dataAll = d
                ),
                getMetadataDatingProfileEntry()
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
