package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.blocks.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class KARFile {
    public final File file;
    public final List<Block> blocks = new ArrayList<>();

    public KARFile(File file) throws IOException {
        this.file = file;
        RandomAccessFile fileReader = new RandomAccessFile(file,"r");
        try {
            // Very start of the KAR file
            if (!"CAT ".equals(BlockReader.readBlockType(fileReader))) throw new IllegalArgumentException("File is not a KAResource file.");
            int size = fileReader.readInt()-4;
            fileReader.skipBytes(4); // unread padding

            BlockReader reader = new BlockReader(fileReader, false, size);
            while (reader.getRemaining() > 0) {
                String blockFormat = reader.readBlockType();
                boolean littleEndian = switch (blockFormat) {
                    case "RIFF" -> true;
                    case "FORM" -> false;
                    case "CAT " -> throw new IllegalStateException("Nested category tags are not supported.");
                    default -> throw new IllegalStateException("Unknown block format: " + blockFormat);
                };
                reader.setLittleEndian(littleEndian);

                int blockSize = reader.readInt()-4;
                String blockType = reader.readBlockType();
                Block block = switch (blockType) {
                    case "TxFm" -> new TexturesBlock(false);
                    case "TrFm" -> new TexturesBlock(true);
                    case "MtFm" -> new MaterialsBlock(this);
                    case "SnFm" -> new SoundsBlock();
                    case "ObFm" -> new ObjectsBlock();
                    default -> new UnknownBlock(blockType);
                };
                block.readFull(reader.segment(blockSize));
                blocks.add(block);
            }
        } catch (Exception e) {
            throw new RuntimeException("At "+fileReader.getFilePointer(), e);
        } finally {
            fileReader.close();
        }
    }

    public void save(File file) throws IOException {
        try (RandomAccessFile fileWriter = new RandomAccessFile(file,"rw")) {
            BlockWriter writer = new BlockWriter(fileWriter, true);

            writer.writeRawString("CAT \0\0\0\0    ");
            for (Block block:blocks) {
                writer.setLittleEndian(block.isLittleEndian());
                writer.writeBlock(block);
            }

            writer.setLittleEndian(false);
            writer.seek(4);
            writer.writeInt(writer.getSize()-8);
        }
    }
}
