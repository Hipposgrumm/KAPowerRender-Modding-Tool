package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.PROReader;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.ViewerAppHandle;
import dev.hipposgrumm.kamapreader.util.types.Material;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartObject extends Part {
    private PROReader.PROData mesh_data;

    public PartObject(TexturesBlock.TexturesData textures, MaterialsBlock.MaterialsData materials) {
        super(textures, materials);
    }

    @Override
    protected void readData(BlockReader reader) {
        BYTE_DATA = reader.readBytes(12);
        mesh_data = PROReader.readPRO(reader.segment(reader.getRemaining()));
    }

    @Override
    public void writeData(BlockWriter writer) {
        writer.writeBytes(BYTE_DATA);
        PROReader.writePRO(writer, mesh_data);
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.MODELS_CLEAR, new byte[0]);
        sendMeshData("Object", mesh_data);

        List<DatingProfileEntry<?>> entries = new ArrayList<>();
        if (textures != null) entries.add(new SubBachelorPreviewEntry("Textures",
                () -> textures.textureList
        ));
        if (materials != null) entries.add(new SubBachelorPreviewEntry("Materials",
                () -> materials.materialList
        ));
        return entries;
    }

    @Override
    public void fillMaterials(Map<Integer, Material> mats) {
        for (PROReader.PROData.MaterialDat dat:mesh_data.Materials) {
            Material material = mats.get(dat.UID);
            if (material == null) continue;
            materials.materials.put(dat.UID, new MaterialsBlock.MaterialRef(material, materials.materialList.size()));
            materials.materialList.add(material);
        }
    }

    @Override
    public String toString() {
        return "Object";
    }
}
