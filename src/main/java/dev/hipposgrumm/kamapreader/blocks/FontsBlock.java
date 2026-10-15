package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.EnumChoices;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UtString;

import java.util.ArrayList;
import java.util.List;

public class FontsBlock extends Block.ArrayChecked {
    public List<FontDefinition> fonts = new ArrayList<>();
    private FontDefinition[] fontsArrayBuilder;

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading FontDefinitions");
        fontsArrayBuilder = new FontDefinition[count];
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        fontsArrayBuilder[index] = new FontDefinition(reader);
    }

    @Override
    protected void readPlaceholderFromArray(int index) {
        fontsArrayBuilder[index] = new FontDefinition.Placeholder();
    }

    @Override
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {
        fonts = new ArrayList<>(List.of(fontsArrayBuilder));
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        progress.setMessage("Writing FontDefinitions");
        return fonts.size();
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        FontDefinition definition = fonts.get(index);
        if (definition instanceof PlaceholderValue) return false;

        definition.write(writer.segment());
        return true;
    }

    @Override
    protected void writeForArrayEnd(BlockWriter writer, KARFile.ProgressUpdater progress) {
        writer.writeShort((short)0);
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(getMetadataDatingProfileEntry());
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return fonts;
    }

    @Override
    public String getBlockType() {
        return "FtFm";
    }

    @Override
    public String toString() {
        return "Font Definitions";
    }

    public static class FontDefinition implements DatingBachelor {
        private byte[] head;
        public UtString fontName;
        private short unknown;

        protected FontDefinition() {}

        public FontDefinition(BlockReader reader) {
            head = reader.readBlockHead();
            if (head[0] != BlockType.FONT_DEFINITION) throw new IllegalStateException(String.format("Non-font type 0x%2X found in font definition.", head[0]));

            reader = reader.segment(reader.readInt());

            fontName = reader.readUtString();
            unknown = reader.readShort();
        }

        public void write(BlockWriter writer) {
            writer.writeBlockHead(head);
            writer.writeUtString(fontName);
            writer.writeShort(unknown);
        }

        @Override
        public List<? extends DatingProfileEntry> getDatingProfile() {
            return List.of(new DatingProfileEntry("Font Name", false, fontName));
        }

        @Override
        public String toString() {
            return fontName.toString();
        }

        public enum Encoding implements EnumChoices {
            UNKNOWN(-1),
            UTF8(0),
            UTF16(3);

            public final int identifier;

            Encoding(int identifier) {
                this.identifier = identifier;
            }

            @Override
            public Enum<? extends EnumChoices> getSelf() {
                return this;
            }

            @Override
            public List<? extends Enum<? extends EnumChoices>> choices() {
                return List.of(UTF8, UTF16);
            }
        }

        private static final class Placeholder extends FontDefinition implements PlaceholderValue {
            public Placeholder() {
                super();
            }
        }
    }
}
