package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.Material;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialsBlock extends Block {
    private final KARFile file;
    private ResourceCheckBlock rsck;
    private int unknown;

    private final List<Material> materialList = new ArrayList<>();
    public final Map<Integer, MaterialRef> materials = new HashMap<>();

    public MaterialsBlock(KARFile file) {
        this.file = file;
    }

    @Override
    protected void read(BlockReader reader) throws IOException {
        rsck = ResourceCheckBlock.read(reader, getBlockType());

        unknown = reader.readInt();
        reader = reader.segment(reader.readInt());

        int count = reader.readInt();
        for (int i=0;i<count;i++) {
            Material mat = new Material(file, reader.segment(0x42c));
            materials.put(mat.getUid().get(), new MaterialRef(mat, this, materialList.size()));
            materialList.add(mat);
        }
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        rsck.write(writer.segment());
        writer = writer.segment();

        writer.writeInt(unknown);
        writer.writeInt(0);

        writer.writeInt(materialList.size());
        for (Material mat:materialList) {
            mat.write(writer.segment());
        }

        writer.seek(4);
        writer.writeInt(writer.getSize()-8);
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return List.of(new SubBachelorPreviewEntry(
                () -> materialList
        ));
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return materialList;
    }

    @Override
    public String getBlockType() {
        return "MtFm";
    }

    @Override
    public String toString() {
        return "Materials";
    }

    public record MaterialRef(Material material, MaterialsBlock block, int index) {}
}
