package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.control.display.BachelorProfileValueList;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UtString;

import java.util.ArrayList;
import java.util.List;

public class StringsBlock extends Block.Singular {
    private byte[] head;
    public List<UtString> strings;

    @Override
    protected void readWithAll(BlockReader reader, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading Strings");
        head = reader.readBlockHead();
        if (head[0] != BlockType.STRINGS_DEFINITION) throw new IllegalStateException(String.format("Block data value (0x%02X) does not match for strings definition", head[0]));

        int count = reader.readInt();
        strings = new ArrayList<>(count);
        for (int i=0;i<count;i++) {
            strings.add(reader.readUtString());
        }
    }

    @Override
    protected void writeWithAll(BlockWriter writer, KARFile.ProgressUpdater progress) {
        progress.setMessage("Writing Strings");
        writer.writeBlockHead(head);

        writer.writeInt(strings.size());
        for (UtString str:strings) {
            writer.writeUtString(str);
        }
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(
                new DatingProfileEntry("Entries", false, new BachelorProfileValueList<>(
                        strings,
                        () -> new UtString(0x100)
                )),
                getMetadataDatingProfileEntry()
        );
    }

    @Override
    public String getBlockType() {
        return "StFm";
    }

    @Override
    public String toString() {
        return "Strings";
    }
}
