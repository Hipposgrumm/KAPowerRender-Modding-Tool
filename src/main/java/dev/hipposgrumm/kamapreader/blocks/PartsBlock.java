package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.blocks.parts.Part;
import dev.hipposgrumm.kamapreader.blocks.parts.PartEmpty;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Now that we have array check, we need to refactor this.
public class PartsBlock extends Block.ArrayChecked {
    public List<Part> parts = new ArrayList<>();
    private Part[] partsArrayBuilder;
    private byte[] neglectedBytes = null;

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        partsArrayBuilder = new Part[count];
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        int pos = reader.getPointer();
        try {
            Part part = Part.read(reader, progress);
            if (part == null) {
                reader.seek(pos);
                neglectedBytes = reader.readBytes(reader.getRemaining());
                return;
            }
            partsArrayBuilder[index] = part;
        } catch (Exception e) {
            reader.seek(pos);
            neglectedBytes = reader.readBytes(reader.getRemaining());
            throw e;
        }
    }

    @Override
    protected void readPlaceholderFromArray(int index) {
        partsArrayBuilder[index] = new PlaceholderPart();
    }

    @Override
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {
        parts = new ArrayList<>(List.of(partsArrayBuilder));
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        return parts.size();
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        Part part = parts.get(index);
        if (part instanceof PlaceholderValue) return false;

        part.write(writer.segment(), progress);
        return true;
    }

    @Override
    protected void writeForArrayEnd(BlockWriter writer, KARFile.ProgressUpdater progress) {
        if (neglectedBytes != null) writer.writeBytes(neglectedBytes);
    }

    public void fillMaterials(Map<Integer, Material> materials) {
        for (Part part:parts) {
            part.fillMaterials(materials);
        }
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(getMetadataDatingProfileEntry());
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

    private static final class PlaceholderPart extends PartEmpty implements PlaceholderValue {}
}
