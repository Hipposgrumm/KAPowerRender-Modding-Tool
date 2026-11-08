package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.UtTable;

import java.util.ArrayList;
import java.util.List;

public class TablesBlock extends Block.ArrayChecked {
    public List<UtTable> tables = new ArrayList<>();
    private UtTable[] tablesArrayBuilder;

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        if (resourceVersion.major() > 1) throw new UnsupportedOperationException("Resource version "+resourceVersion.major()+" for UtTable is currently unsupported.");

        tablesArrayBuilder = new UtTable[count];
        for (int i=0;i<count;i++) tablesArrayBuilder[i] = new TablePlaceholder();
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        tablesArrayBuilder[index] = UtTable.readTable(reader, resourceVersion.major());
    }

    @Override
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {
        tables = new ArrayList<>(List.of(tablesArrayBuilder));
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        return tables.size();
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        UtTable table = tables.get(index);
        if (table instanceof PlaceholderValue) return false;

        //table.write(writer);
        return true;
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(getMetadataDatingProfileEntry());
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return tables;
    }

    @Override
    public String getBlockType() {
        return "TbFm";
    }

    @Override
    public String toString() {
        return "Tables";
    }

    private static final class TablePlaceholder extends UtTable implements PlaceholderValue {}
}
