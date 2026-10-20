package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.control.display.BachelorProfileValueList;
import dev.hipposgrumm.kamapreader.util.types.Flags;
import dev.hipposgrumm.kamapreader.util.types.Version;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UtString;

import java.util.ArrayList;
import java.util.List;

public class LevelDefinesBlock extends Block.ArrayChecked {
    public List<LevelDefinition> levelDefinitions = new ArrayList<>();
    private LevelDefinition[] levelDefinitionsArrayBuilder;

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        levelDefinitionsArrayBuilder = new LevelDefinition[count];
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        levelDefinitionsArrayBuilder[index] = new LevelDefinition(reader, resourceVersion, index);
    }

    @Override
    protected void readPlaceholderFromArray(int index) {
        levelDefinitionsArrayBuilder[index] = new LevelDefinition.Placeholder(index);
    }

    @Override
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {
        levelDefinitions = new ArrayList<>(List.of(levelDefinitionsArrayBuilder));
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        return levelDefinitions.size();
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        LevelDefinition definition = levelDefinitions.get(index);
        if (definition instanceof PlaceholderValue) return false;

        definition.write(writer);
        return true;
    }

    @Override
    public String getBlockType() {
        return "LvFm";
    }

    @Override
    public String toString() {
        return "Level Definitions";
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return null;
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return levelDefinitions;
    }

    public static class LevelDefinition implements DatingBachelor {
        private UnknownFlag someFlag;
        private int unknown1;
        private byte[] unknown2;
        private float[] unknown3;
        private List<UtString> strings;

        private int index;

        protected LevelDefinition(int index) {
            this.index = index;
        }

        public LevelDefinition(BlockReader reader, Version version, int index) {
            this.index = index;

            someFlag = new UnknownFlag(reader.readInt());
            unknown1 = reader.readInt();
            if (version.major() >= 1)  // This is an assumption, there's no real way of knowing for sure.
                unknown2 = reader.readBytes(8);
            unknown3 = new float[] {
                    reader.readFloat(),
                    reader.readFloat(),
                    reader.readFloat(),
                    reader.readFloat()
            };

            int count = reader.readInt();
            this.strings = new ArrayList<>(count);
            for (int i=0;i<count;i++) {
                BlockReader subReader = reader.segment(0x118);
                strings.add(subReader.readUtString());
            }
        }

        public void write(BlockWriter writer) {
            writer.writeInt(someFlag.getValue());
            writer.writeInt(unknown1);
            if (unknown2 != null) writer.writeBytes(unknown2);
            for (float f:unknown3) writer.writeFloat(f);
            for (UtString string:strings) writer.writeUtString(string);
        }

        @Override
        public List<? extends DatingProfileEntry> getDatingProfile() {
            return List.of(
                    new DatingProfileEntry("Entries", false, new BachelorProfileValueList<>(
                            strings,
                            () -> new UtString(0x100)
                    ))
            );
        }

        @Override
        public String toString() {
            return "Level "+index;
        }

        private static final class Placeholder extends LevelDefinition implements PlaceholderValue {
            public Placeholder(int index) {
                super(index);
            }
        }
    }

    public static class UnknownFlag extends Flags {
        public static final BoolEntry Unk1 = new BoolEntry("Unk1", 0);
        public static final BoolEntry Unk2 = new BoolEntry("Unk2", 1);

        public UnknownFlag(int value) {
            super(value);
        }

        @Override
        public Entry[] getEntries() {
            return new Entry[] {Unk1, Unk2};
        }
    }
}
