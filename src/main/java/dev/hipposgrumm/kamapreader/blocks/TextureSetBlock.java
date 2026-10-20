package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.Exportable;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.BlockType;
import dev.hipposgrumm.kamapreader.util.types.Previewable;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.Texture;
import javafx.scene.Node;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextureSetBlock extends Block.ArrayChecked {
    public List<TextureValue> textures;
    private TextureValue[] texturesArrayBuilder;

    @Override
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
        progress.setMessage("Reading TextureSet");
        texturesArrayBuilder = new TextureValue[count];
    }

    @Override
    protected void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
        byte[] head = reader.readBlockHead();
        if (head[0] != BlockType.TEXTURE_ARRAY)
            throw new IllegalStateException(String.format("Block data value (0x%02X) does not match for type TEXTURE_ARRAy", head[0]));

        BlockReader texreader = reader.segment(reader.readIntLittle());
        texturesArrayBuilder[index] = new TextureValue(head, new Texture(texreader));
    }

    @Override
    protected void readPlaceholderFromArray(int index) {
        texturesArrayBuilder[index] = new PlaceholderTextureValue();
    }

    @Override
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {
        textures = new ArrayList<>(List.of(texturesArrayBuilder));
    }

    @Override
    protected int writeForArrayBegin(KARFile.ProgressUpdater progress) {
        progress.setMessage("Writing TextureSet");
        return textures.size();
    }

    @Override
    public boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
        TextureValue tex = textures.get(index);
        if (tex instanceof PlaceholderValue) return false;

        writer.writeBlockHead(tex.head);

        writer = writer.segment();
        writer.writeIntLittle(0);

        tex.texture.write(writer.segment());

        writer.seek(0);
        writer.writeIntLittle(writer.getSize()-4);
        return true;
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(
                new DatingProfileEntry("Content", new SubBachelorPreviewEntry(textures)),
                getMetadataDatingProfileEntry()
        );
    }

    @Override
    public List<? extends DatingBachelor> getSubBachelors() {
        return textures;
    }

    @Override
    public String getBlockType() {
        return "TrFm";
    }

    @Override
    public String toString() {
        return "TextureSet";
    }

    public static class TextureValue implements DatingBachelor, Previewable, Exportable {
        private byte[] head;
        public Texture texture;

        public TextureValue(byte[] head, Texture texture) {
            this.head = head;
            this.texture = texture;
        }

        @Override
        public List<? extends DatingProfileEntry> getDatingProfile() {
            return texture.getDatingProfile();
        }

        @Override
        public String getPreviewName() {
            return texture.getPreviewName();
        }

        @Override
        public Node getPreviewGraphic() {
            return texture.getPreviewGraphic();
        }

        @Override
        public String getFileName() {
            return texture.getFileName();
        }

        @Override
        public String getFileExtension() {
            return texture.getFileExtension();
        }

        @Override
        public void writeExportData(FileOutputStream outputStream) throws IOException {
            texture.writeExportData(outputStream);
        }

        @Override
        public ContextMenuOption[] getContextMenu() {
            return texture.getContextMenu();
        }

        @Override
        public List<? extends DatingBachelor> getSubBachelors() {
            return texture.getSubBachelors();
        }

        @Override
        public void onSelected() {
            texture.onSelected();
        }
    }

    private static class PlaceholderTextureValue extends TextureValue implements PlaceholderValue {
        public PlaceholderTextureValue() {
            super(null, null);
        }
    }
}
