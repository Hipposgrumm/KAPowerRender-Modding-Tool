package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.types.structs.BITMAP_TEXTURE;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UniqueIdentifier;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class Texture implements DatingBachelor, Previewable {
    private final byte[] unknown0;
    private final int unknown1;
    private final UniqueIdentifier uid;
    private final byte[] unknown2;
    private final BITMAP_TEXTURE[] textures;

    public Texture(BlockReader reader, boolean arckVariant) {
        if (arckVariant) unknown0 = reader.readBytes(4);
        else unknown0 = null;
        this.unknown1 = reader.readIntLittle();
        this.uid = new UniqueIdentifier(reader.readIntLittle());
        this.unknown2 = reader.readBytes(87);
        byte b = reader.readByte();
        this.textures = new BITMAP_TEXTURE[b];
        for (int i=0;i<textures.length;i++) {
            BITMAP_TEXTURE tex = new BITMAP_TEXTURE(reader.readUShortLittleDirect(), reader.readUShortLittleDirect());
            tex.UNKNOWN1 = reader.readIntLittle();
            tex.UNKNOWN2 = reader.readIntLittle();
            tex.FORMAT = switch (reader.readIntLittle()) {
                case 21 -> BITMAP_TEXTURE.Format.A8R8G8B8;
                case 22 -> BITMAP_TEXTURE.Format.X8R8G8B8;
                case 23 -> BITMAP_TEXTURE.Format.R5G6B5;
                case 25 -> BITMAP_TEXTURE.Format.A1R5G5B5;
                default -> BITMAP_TEXTURE.Format.UNKNOWN;
            };
            int bytesize = tex.FORMAT.bytesize();
            Number[] bytes = new Number[tex.WIDTH * tex.HEIGHT];
            ByteBuffer buffer = ByteBuffer.wrap(reader.readBytes(bytes.length * bytesize));
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
        if (unknown0 != null) writer.writeBytes(unknown0);
        writer.writeIntLittle(unknown1);
        writer.writeIntLittle(uid.get());
        writer.writeBytes(unknown2);
        writer.writeByte((byte) textures.length);
        for (BITMAP_TEXTURE tex:textures) {
            writer.writeUShortLittleDirect(tex.WIDTH);
            writer.writeUShortLittleDirect(tex.HEIGHT);
            writer.writeIntLittle(tex.UNKNOWN1);
            writer.writeIntLittle(tex.UNKNOWN2);
            writer.writeIntLittle(tex.FORMAT.id);
            int bytesize = tex.FORMAT.bytesize();
            Number[] bytes = tex.getData();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length * bytesize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            switch (bytesize) {
                case 2 -> {
                    for (Number num:bytes)
                        buffer.putShort(num.shortValue());
                }
                case 4 -> {
                    for (Number num:bytes)
                        buffer.putInt(num.intValue());
                }
                default -> throw new UnsupportedOperationException("Unknown bytesize for image: "+bytesize);
            }
            writer.writeBytes(buffer.array());
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
    public String toString() {
        return uid.toString();
    }
}
