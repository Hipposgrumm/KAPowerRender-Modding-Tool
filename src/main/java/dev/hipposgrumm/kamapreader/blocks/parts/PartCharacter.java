package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.reader.PROReader;
import dev.hipposgrumm.kamapreader.util.ViewerAppHandle;
import dev.hipposgrumm.kamapreader.util.types.Material;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_FACE;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_VERTEX;
import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.TextFileString;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartCharacter extends Part {
    private SizeLimitedString name;
    private byte[] MYSTERY1;
    private TextFileString file_scr;
    private byte[] MYSTERY2;
    private TextFileString file_ls;
    private byte[] MYSTERY3;
    private PROReader.PROData mesh_data;
    private byte[] MYSTERY4;

    public PartCharacter(TexturesBlock.TexturesData textures, MaterialsBlock.MaterialsData materials) {
        super(textures, materials);
    }

    @Override
    protected void readData(BlockReader reader) {
        name = reader.readStringFixed(64);
        String filename = name.toString();
        if (filename.toUpperCase().endsWith(".CHM")) filename = filename.substring(0, filename.length()-4);
        MYSTERY1 = reader.readBytes(0x9B);
        file_scr = new TextFileString(filename+".SCR", reader.readString(reader.readInt()));
        MYSTERY2 = reader.readBytes(0x7A);
        file_ls = new TextFileString(filename+".LS", reader.readString(reader.readInt()));
        MYSTERY3 = reader.readBytes(0x7A);
        mesh_data = PROReader.readPRO(reader.segment(reader.readInt()));
        BYTE_DATA = reader.readBytes(reader.getRemaining());
    }

    @Override
    public void writeData(BlockWriter writer) {
        writer.writeTerminatedStringFixed(name);
        writer.writeBytes(MYSTERY1);
        writer.writeSizedString(file_scr.getContents());
        writer.writeBytes(MYSTERY2);
        writer.writeSizedString(file_ls.getContents());
        writer.writeBytes(MYSTERY3);
        int lastIndex = writer.getPointer();
        BlockWriter proBlockWriter = writer.segment();
        proBlockWriter.writeInt(0); // size - filled in
        PROReader.writePRO(proBlockWriter, mesh_data);
        writer.seek(lastIndex);
        int size = proBlockWriter.getSize();
        writer.writeInt(size-4);
        writer.seek(lastIndex+size);
        writer.writeBytes(BYTE_DATA);
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        ViewerAppHandle.sendMessage(ViewerAppHandle.Messages.MODELS_CLEAR, new byte[0]);
        sendMeshData(name.toString(), mesh_data);

        List<DatingProfileEntry<?>> entries = new ArrayList<>();
        entries.add(new DatingProfileEntry<>("Name",
                () -> name
        ));
        if (textures != null) entries.add(new SubBachelorPreviewEntry("Textures",
                () -> textures.textureList
        ));
        entries.add(new SubBachelorPreviewEntry("Materials",
                () -> materials.materialList
        ));
        entries.add(new DatingProfileEntry<>("Character Script File",
                () -> file_scr
        ));
        entries.add(new DatingProfileEntry<>("LOD Settings",
                () -> file_ls
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
        return name.toString();
    }
}
