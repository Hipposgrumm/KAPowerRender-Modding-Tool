package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.parts.Part;
import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartsBlock extends Block {
    private ResourceCheckBlock rsck;
    private ArCkBlock arck;

    private final List<Part> parts = new ArrayList<>();
    private byte[] neglectedBytes = null;

    @Override
    protected void read(BlockReader reader) throws IOException {
        rsck = ResourceCheckBlock.read(reader, "PtFm");
        arck = ArCkBlock.read(reader, "PtFm");

        while (reader.getRemaining() > 0) {
            int pos = reader.getPointer();
            try {
                Part part = Part.read(reader);
                parts.add(part);
            } catch (Exception e) {
                reader.seek(pos);
                neglectedBytes = reader.readBytes(reader.getRemaining());
                throw e;
            }
        }
    }

    @Override
    public void write(BlockWriter writer) throws IOException {
        rsck.write(writer.segment());
        arck.write(writer.segment());

        for (Part part:parts) {
            part.write(writer.segment());
            writer.seek(writer.getSize());
        }

        if (neglectedBytes != null) writer.writeBytes(neglectedBytes);
    }

    public void fillMaterials(MaterialsBlock.MaterialsData[] materials) {

    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return null;
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return parts;
    }

    @Override
    public String getBlockType() {
        return "PtFm";
    }

    @Override
    public String toString() {
        return "Parts";
    }
}
