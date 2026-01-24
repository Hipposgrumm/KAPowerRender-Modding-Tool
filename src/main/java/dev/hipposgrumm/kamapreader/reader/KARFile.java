package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.blocks.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KARFile {
    public final File file;
    public final List<Block> blocks = new ArrayList<>();

    public KARFile(File file) throws IOException {
        this.file = file;
        BlockReader reader = new BlockReader(file);
        // Very start of the KAR file
        if (!"CAT ".equals(reader.readBlockType())) throw new IllegalArgumentException("File is not a KAResource file.");
        int size = reader.readIntBig();
        reader.move(4); // unread padding
        reader = reader.segment(size-4);

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
                case "PtFm" -> new PartsBlock();
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
        MaterialsBlock.MaterialsData[] materials = blocks.stream()
                .filter(b -> b instanceof MaterialsBlock)
                .map(b -> ((MaterialsBlock)b).data).toArray(MaterialsBlock.MaterialsData[]::new);
        for (Block block:blocks) {
            if (block instanceof PartsBlock bl) bl.fillMaterials(materials);
        }
    }

    public void save(File file) throws IOException {
        BlockWriter writer = new BlockWriter();
        writer.writeRawString("CAT \0\0\0\0    ");
        for (Block block:blocks) {
            writer.setLittleEndian(block.isLittleEndian());

            BlockWriter subWriter = writer.segment();
            subWriter.writeRawString(writer.isLittleEndian() ?
                    "RIFF\0\0\0\0" :
                    "FORM\0\0\0\0"
            );
            subWriter.writeRawString(block.getBlockType());

            block.write(subWriter);

            subWriter.seek(4);
            subWriter.writeInt(subWriter.getSize()-8);
            subWriter.seek(subWriter.getSize());
        }

        writer.setLittleEndian(false);
        writer.seek(4);
        writer.writeInt(writer.getSize()-8);

        writer.writeout(file);
    }
}
