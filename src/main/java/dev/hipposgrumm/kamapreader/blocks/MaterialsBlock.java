package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.Material;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialsBlock extends Block {
    private final List<Map<Integer, Texture>> textureMaps;
    private ResourceCheckBlock rsck;
    public MaterialsData data;

    public MaterialsBlock(KARFile file) {
        this.textureMaps = file.blocks.stream()
                .filter(b -> b instanceof TexturesBlock)
                .map(b -> ((TexturesBlock) b).data.textures)
                .toList();
    }

    @Override
    protected void read(BlockReader reader) {
        rsck = ResourceCheckBlock.read(reader, "MtFm");
        data = new MaterialsData(reader, textureMaps);
    }

    @Override
    public void write(BlockWriter writer) {
        rsck.write(writer.segment());
        data.write(writer.segment());
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return List.of(new SubBachelorPreviewEntry(
                () -> data.materialList
        ));
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
        public MaterialsData(BlockReader reader, List<Map<Integer, Texture>> textureMaps) {
            unknown = reader.readInt();
            reader = reader.segment(reader.readInt());

            int count = reader.readInt();
            for (int i=0;i<count;i++) {
                Material mat = new Material(textureMaps, reader.segment(0x42c));
                materials.put(mat.getUid().get(), new MaterialRef(mat, materialList.size()));
                materialList.add(mat);
            }
        }

        public void write(BlockWriter writer) {
            writer.writeInt(unknown);
            writer.writeInt(0);

            writer.writeInt(materialList.size());
            for (Material mat:materialList) {
                mat.write(writer.segment());
            }

            writer.seek(4);
            writer.writeInt(writer.getSize()-8);
        }
    }
}
