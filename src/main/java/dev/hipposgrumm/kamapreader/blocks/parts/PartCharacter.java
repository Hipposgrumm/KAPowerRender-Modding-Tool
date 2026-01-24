package dev.hipposgrumm.kamapreader.blocks.parts;

import dev.hipposgrumm.kamapreader.blocks.MaterialsBlock;
import dev.hipposgrumm.kamapreader.blocks.TexturesBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.reader.PROReader;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.TextFileString;

import java.util.List;

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
        return List.of(new DatingProfileEntry<>("Name",
                () -> name
        ), new SubBachelorPreviewEntry("Textures",
                () -> textures.textureList
        ), new SubBachelorPreviewEntry("Materials",
                () -> materials.materialList
        ), new DatingProfileEntry<>("Character Script File",
                () -> file_scr
        ), new DatingProfileEntry<>("LOD Settings",
                () -> file_ls
        ));
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
