package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.EnumChoices;
import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;

import java.util.ArrayList;
import java.util.List;

public class FontsBlock extends Block {
    private ResourceCheckBlock rsck;
    private ArCkBlock arck;
    public List<FontDefinition> fonts = new ArrayList<>();

    @Override
    protected void read(BlockReader reader) {
        rsck = ResourceCheckBlock.read(reader, "FtFm");
        arck = ArCkBlock.read(reader, "FtFm");

        while (reader.getRemaining() > 0) {
            byte type = reader.readByte();
            if (type == 0x00) break;
            reader.move(-1);

            fonts.add(new FontDefinition(reader));
        }
    }

    @Override
    public void write(BlockWriter writer) {
        rsck.write(writer.segment());
        arck.write(writer.segment());

        for (FontDefinition font:fonts) {
            font.write(writer.segment());
        }
        writer.writeShort((short)0);
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return null;
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
        private int unknownFlags;
        public Encoding encoding;
        private UInteger gamePointer;
        public SizeLimitedString fontName;
        private UInteger uselessPointer;
        private short unknown;

        public FontDefinition(BlockReader reader) {
            head = reader.readBlockHead();
            if (head[0] != BlockType.FONT_DEFINITION) throw new IllegalStateException(String.format("Non-font type 0x%2X found in font definition.", head[0]));

            reader = reader.segment(reader.readInt());

            unknownFlags = reader.readInt(); // string flags - not sure what they're used for
            int encodingVal = reader.readInt();
            encoding = switch (encodingVal) {
                case 0 -> Encoding.UTF8;
                case 3 -> Encoding.UTF16;
                default -> {
                    System.err.println("Read font with unknown encoding id "+encodingVal);
                    yield Encoding.UNKNOWN;
                }
            };
            gamePointer = reader.readUInt(); // pointer location used internally by the game

            int size = reader.readInt();
            int maxsize = reader.readInt();
            uselessPointer = reader.readUInt(); // placeholder pointer; is overwritten by the game
            fontName = new SizeLimitedString(reader.readString(size), maxsize);
            reader.move(maxsize-size);
            unknown = reader.readShort();
        }

        public void write(BlockWriter writer) {
            writer.writeBlockHead(head);

            writer.writeInt(unknownFlags);
            writer.writeInt(encoding.identifier);
            writer.writeUInt(gamePointer);

            int size = fontName.toString().length();
            int maxsize = fontName.getSize();
            writer.writeInt(size);
            writer.writeInt(maxsize);
            writer.writeUInt(uselessPointer);
            writer.writeRawString(fontName.toString());
            for (int i=size;i<maxsize;i++) writer.writeByte((byte)0xCD);
            writer.writeShort(unknown);
        }

        @Override
        public List<? extends DatingProfileEntry<?>> getDatingProfile() {
            return List.of(new DatingProfileEntry<>("Font Name",
                    () -> fontName
            ));
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
    }
}
