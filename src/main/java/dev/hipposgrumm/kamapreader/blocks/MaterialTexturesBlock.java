package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.util.*;

public class MaterialTexturesBlock extends Block.Singular {
    public TexturesData data;

    @Override
    protected void readWithAll(BlockReader reader, KARFile.ProgressUpdater progress) {
        data = new TexturesData(reader, "Reading TextureSet", progress);
    }

    @Override
    public void writeWithAll(BlockWriter writer, KARFile.ProgressUpdater progress) {
        data.write(writer.segment(), "Writing TextureSet", progress);
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        try {
            return List.of(
                    new DatingProfileEntry("Content", new SubBachelorPreviewEntry(data.textureList)),
                    getMetadataDatingProfileEntry()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(getMetadataDatingProfileEntry());
        }
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return data.textureList;
    }

    @Override
    public String getBlockType() {
        return "TxFm";
    }

    @Override
    public String toString() {
        return "MaterialTextures";
    }

    public static class TexturesData {
        private byte[] head;
        public final ArrayList<Texture> textureList = new ArrayList<>();
        public final Map<Integer, Texture> textures = new HashMap<>();

        public TexturesData(BlockReader reader, String statusMessage, KARFile.ProgressUpdater progress) {
            head = reader.readBlockHead();
            if (head[0] != BlockType.MATERIAL_TEXTURES)
                throw new IllegalStateException(String.format("Block data value (0x%02X) does not match for type MATERIAL_TEXTURES", head[0]));

            // Offset values need this at exactly the position of "head"
            int remainingSize = reader.readIntLittle();
            reader.move(-8);
            reader = reader.segment(remainingSize + 8);
            reader.move(8);

            // These values are probably used as-is in the actual game, therefore
            //     allowing textures to be read in a different order. This could
            //     also allow for an infinite loop if set up as such.
            int offsetNext = reader.readIntLittle();
            while (offsetNext != 0) {
                if (statusMessage != null) progress.setMessage(statusMessage+" "+textureList.size());
                progress.updateWith(reader);
                offsetNext = reader.readIntLittle();
                BlockReader texreader = reader.segment(offsetNext != 0
                        ? offsetNext - reader.getPointer()
                        : reader.getRemaining()
                );
                Texture tex = new Texture(texreader);
                textures.put(tex.getUid().get(), tex);
                textureList.add(tex);
            }
        }

        public void write(BlockWriter writer, String statusMessage, KARFile.ProgressUpdater progress) {
            // TODO: Make sure to actually test this.
            writer = writer.segment();

            writer.writeBlockHead(head);
            writer.writeIntLittle(0);

            int i = 0;
            int count = 0;
            writer.writeIntLittle(writer.getPointer()+4);
            Iterator<Texture> texIter = textureList.iterator();
            while (texIter.hasNext()) {
                if (statusMessage != null) progress.setMessage(statusMessage+" "+i+"/"+count);
                Texture tex = texIter.next();
                int offset = writer.getPointer();
                writer.writeIntLittle(0);

                tex.write(writer.segment());

                if (!texIter.hasNext()) continue;
                writer.seek(offset);
                writer.writeIntLittle(writer.getSize());
                writer.seek(writer.getSize());
                i++;
            }
            writer.seek(4);
            writer.writeInt(writer.getSize()-8);
        }
    }
}
