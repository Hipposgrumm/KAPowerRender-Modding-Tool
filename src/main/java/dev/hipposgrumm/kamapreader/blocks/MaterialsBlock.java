package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.Material;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.util.*;

public class MaterialsBlock extends Block.Singular {
    private final List<Map<Integer, Texture>> textureMaps;
    public MaterialsData data;

    public MaterialsBlock(KARFile file) {
        this.textureMaps = new ArrayList<>();
        for (Block b : file.blocks) {
            if (!b.successfullyRead()) continue;
            Map<Integer, Texture> textures = switch (b) {
                //case TextureSetBlock bl -> Collections.singletonMap(bl.texture.getUid().get(), bl.texture);
                case MaterialTexturesBlock bl -> bl.data.textures;
                default -> null;
            };
            if (textures != null) {
                this.textureMaps.add(textures);
            }
        }
    }

    @Override
    protected void readWithAll(BlockReader reader, KARFile.ProgressUpdater progress) {
        data = new MaterialsData(reader, textureMaps, "Reading Materials", progress);
    }

    @Override
    public void writeWithAll(BlockWriter writer, KARFile.ProgressUpdater progress) {
        data.write(writer.segment(), "Writing Materials", progress);
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(
                new DatingProfileEntry("Content", new SubBachelorPreviewEntry(data.materialList)),
                getMetadataDatingProfileEntry()
        );
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return data.materialList;
    }

    @Override
    public String getBlockType() {
        return "MtFm";
    }

    @Override
    public String toString() {
        return "Materials";
    }

    public record MaterialRef(Material material, int index) {}

    public static class MaterialsData {
        private int unknown;
        public final List<Material> materialList = new ArrayList<>();
        public final Map<Integer, MaterialRef> materials = new HashMap<>();

        protected MaterialsData() {}
        public MaterialsData(BlockReader reader, List<Map<Integer, Texture>> textureMaps, String statusMessage, KARFile.ProgressUpdater progress) {
            unknown = reader.readInt();
            reader = reader.segment(reader.readInt());

            int count = reader.readInt();
            for (int i=0;i<count;i++) {
                if (statusMessage != null) progress.setMessage(statusMessage+" "+i+"/"+count);
                progress.updateWith(reader);
                Material mat = new Material(textureMaps, reader.segment(0x42c));
                materials.put(mat.getUid().get(), new MaterialRef(mat, materialList.size()));
                materialList.add(mat);
            }
        }

        public void write(BlockWriter writer, String statusMessage, KARFile.ProgressUpdater progress) {
            writer.writeInt(unknown);
            writer.writeInt(0);

            int i = 0;
            int count = materialList.size();
            writer.writeInt(count);
            for (Material mat:materialList) {
                if (statusMessage != null) progress.setMessage(statusMessage+" "+i+"/"+count);
                mat.write(writer.segment());
                i++;
            }

            writer.seek(4);
            writer.writeInt(writer.getSize()-8);
        }
    }
}
