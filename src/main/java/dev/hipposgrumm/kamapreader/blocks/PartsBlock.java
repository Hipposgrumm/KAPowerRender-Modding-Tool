package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.parts.Part;
import dev.hipposgrumm.kamapreader.blocks.subblock.ArCkBlock;
import dev.hipposgrumm.kamapreader.blocks.subblock.ResourceCheckBlock;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartsBlock extends Block {
    private ResourceCheckBlock rsck;
    private ArCkBlock arck;

    private final List<Part> parts = new ArrayList<>();
    private byte[] neglectedBytes = null;

    @Override
    protected void read(BlockReader reader) {
        rsck = ResourceCheckBlock.read(reader, "PtFm");
        arck = ArCkBlock.read(reader, "PtFm");

        while (reader.getRemaining() > 0) {
            int pos = reader.getPointer();
            try {
                Part part = Part.read(reader);
                if (part == null) break;
                parts.add(part);
            } catch (Exception e) {
                reader.seek(pos);
                neglectedBytes = reader.readBytes(reader.getRemaining());
                throw e;
            }
        }
    }

    @Override
    public void write(BlockWriter writer) {
        rsck.write(writer.segment());
        arck.write(writer.segment());

        for (Part part:parts) {
            part.write(writer.segment());
        }

        if (neglectedBytes != null) writer.writeBytes(neglectedBytes);
    }

    public void fillMaterials(Map<Integer, Material> materials) {
        for (Part part:parts) {
            part.fillMaterials(materials);
        }
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
