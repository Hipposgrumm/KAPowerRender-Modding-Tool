package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.Exportable;
import dev.hipposgrumm.kamapreader.util.types.structs.BITMAP_TEXTURE;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UniqueIdentifier;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class Texture implements DatingBachelor, Previewable, Exportable {
    public static final boolean HAS_AWT;
    public static final String NO_AWT_MSG = "java.awt is not available. You're probably on a Mac.";

    static {
        boolean awt;
        try {
            Class.forName("java.awt.Image");
            awt = true;
        } catch(ClassNotFoundException e) {
            awt = false;
        }
        HAS_AWT = awt;
    }

    private final UniqueIdentifier uid;
    private final byte[] unknown;
    private final BITMAP_TEXTURE[] textures;

    public Texture(BlockReader reader) {
        reader = reader.segment(reader.getRemaining());
        this.uid = new UniqueIdentifier(reader.readIntLittle());

        reader.move(80);
        this.unknown = reader.readBytes(7);
        byte count = reader.readByte();
        if (count > 10) {
            System.out.println("Texture "+uid+" has more than 10 mipmaps. "+(count-10)+" extras will not be read.");
            count = 10;
        }
        this.textures = new BITMAP_TEXTURE[count];
        reader.move(-88);
        BlockReader offsetData = reader.segment(40);
        BlockReader sizesData = reader.segment(40);
        reader.move(8);
        for (int i=0;i<count;i++) {
            reader.seek(offsetData.readIntLittle());
            BlockReader texReader = reader.segment(sizesData.readIntLittle());

            BITMAP_TEXTURE tex = new BITMAP_TEXTURE(texReader.readUShortLittleDirect(), texReader.readUShortLittleDirect());
            tex.UNKNOWN1 = texReader.readIntLittle();
            tex.UNKNOWN2 = texReader.readIntLittle();
            tex.FORMAT = switch (texReader.readIntLittle()) {
                case 21 -> BITMAP_TEXTURE.Format.A8R8G8B8;
                case 22 -> BITMAP_TEXTURE.Format.X8R8G8B8;
                case 23 -> BITMAP_TEXTURE.Format.R5G6B5;
                case 25 -> BITMAP_TEXTURE.Format.A1R5G5B5;
                default -> BITMAP_TEXTURE.Format.UNKNOWN;
            };
            int bytesize = tex.FORMAT.bytesize();
            Number[] bytes = new Number[tex.WIDTH * tex.HEIGHT];
            ByteBuffer buffer = ByteBuffer.wrap(texReader.readBytes(bytes.length * bytesize));
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            switch (bytesize) {
                case 2 -> {
                    for (int j=0;j<bytes.length;j++)
                        bytes[j] = buffer.getShort();
                }
                case 4 -> {
                    for (int j=0;j<bytes.length;j++)
                        bytes[j] = buffer.getInt();
                }
                default -> throw new UnsupportedOperationException("Unknown bytesize for image: "+bytesize);
            }
            tex.setData(bytes);
            textures[i] = tex;
        }
    }

    public void write(BlockWriter writer) {
        writer.writeIntLittle(uid.get());
        for (int i=0;i<10;i++) writer.writeInt(0xCDCDCDCD); // offsets
        for (int i=0;i<10;i++) writer.writeInt(0xCDCDCDCD); // sizes
        writer.writeBytes(unknown);
        writer.writeByte((byte) textures.length);
        for (int i=0;i<textures.length;i++) {
            BITMAP_TEXTURE tex = textures[i];
            int offset = writer.getPointer();

            writer.writeUShortLittleDirect(tex.WIDTH);
            writer.writeUShortLittleDirect(tex.HEIGHT);
            writer.writeIntLittle(tex.UNKNOWN1);
            writer.writeIntLittle(tex.UNKNOWN2);
            writer.writeIntLittle(tex.FORMAT.id);
            int bytesize = tex.FORMAT.bytesize();
            Number[] bytes = tex.getData();
            ByteBuffer texturedata = ByteBuffer.allocate(bytes.length * bytesize);
            texturedata.order(ByteOrder.LITTLE_ENDIAN);
            switch (bytesize) {
                case 2 -> {
                    for (Number num:bytes)
                        texturedata.putShort(num.shortValue());
                }
                case 4 -> {
                    for (Number num:bytes)
                        texturedata.putInt(num.intValue());
                }
                default -> throw new UnsupportedOperationException("Unknown bytesize for image: "+bytesize);
            }
            writer.writeBytes(texturedata.array());

            writer.seek(4+(4*i));
            writer.writeIntLittle(offset);
            writer.move(40);
            writer.writeIntLittle(writer.getSize() - offset);
            writer.seek(writer.getSize());
        }
    }

    public BITMAP_TEXTURE getViewable() {
        return textures[0];
    }

    public UniqueIdentifier getUid() {
        return uid;
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return Arrays.asList(new DatingProfileEntry.ReadOnly<>("UID",
                () -> uid
        ), new DatingProfileEntry<>("Texture",
                () -> textures,
                this::changeImage
        ));
    }

    @Override
    public Node getPreviewGraphic() {
        if (textures.length == 0) return null;
        BITMAP_TEXTURE tex = textures[0];
        ImageView image = new ImageView(tex.getJavaFXImage());
        image.setPreserveRatio(true);
        if (tex.HEIGHT > tex.WIDTH) {
            if (tex.HEIGHT > 50) image.setFitHeight(50);
        } else {
            if (tex.WIDTH > 50) image.setFitWidth(50);
        }
        return image;
    }

    private void changeImage(BITMAP_TEXTURE[] baseArr) {
        BITMAP_TEXTURE base = baseArr[0];
        textures[0] = base;

        // All LODs are half the size of the first one.
        for (int i=1;i<textures.length;i++) {
            BITMAP_TEXTURE smaller = new BITMAP_TEXTURE(base.WIDTH/2, base.HEIGHT/2);
            smaller.UNKNOWN1 = textures[i].UNKNOWN1;
            smaller.UNKNOWN2 = textures[i].UNKNOWN2;
            smaller.FORMAT = base.FORMAT;
            int[] image = base.getImage();
            int[] data = new int[smaller.WIDTH * smaller.HEIGHT];
            int bWidth = base.WIDTH;
            for (int j=0;j<data.length;j++) {
                int j2 = j*2;
                int xOff = j2 % bWidth, yOff = (j2 / bWidth) * (bWidth*2);
                int im1 = image[xOff+yOff];
                int im2 = image[xOff+1+yOff];
                int im3 = image[xOff+yOff+bWidth];
                int im4 = image[xOff+1+yOff+bWidth];

                int a = ((im1 >> 24) + (im2 >> 24) + (im3 >> 24) + (im4 >> 24)) / 4;
                int r = (((im1 >> 16) & 0xFF) + ((im2 >> 16) & 0xFF) + ((im3 >> 16) & 0xFF) + ((im4 >> 16) & 0xFF)) / 4;
                int g = (((im1 >> 8) & 0xFF) + ((im2 >> 8) & 0xFF) + ((im3 >> 8) & 0xFF) + ((im4 >> 8) & 0xFF)) / 4;
                int b = ((im1 & 0xFF) + (im2 & 0xFF) + (im3 & 0xFF) + (im4 & 0xFF)) / 4;

                data[j] = (a << 24) | (r << 16) | (g << 8) | b;
            }
            smaller.setImage(data);
            textures[i] = smaller;
            base = smaller;
        }
    }

    @Override
    public ContextMenuOption[] getContextMenu() {
        return new ContextMenuOption[] {
                new ContextMenuOption("Export", Exportable::massExport)
        };
    }

    @Override
    public String getFileName() {
        return uid.toString();
    }

    @Override
    public String getFileExtension() {
        return "png";
    }

    @Override
    public void writeExportData(FileOutputStream outputStream) throws IOException {
        if (!HAS_AWT) throw new IllegalStateException("awt is missing. Image cannot be exported. It should not have got this far.");
        saveTexture(textures[0], outputStream);
    }

    public static void saveTexture(BITMAP_TEXTURE tex, FileOutputStream outputStream) throws IOException {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(tex.WIDTH, tex.HEIGHT, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, tex.WIDTH, tex.HEIGHT, tex.getImage(), 0, tex.WIDTH);
        ImageIO.write(image, "PNG", outputStream);
    }

    @Override
    public String toString() {
        return uid.toString();
    }
}
