package dev.hipposgrumm.kamapreader.util.types.structs;

import java.util.function.Function;

public class BITMAP_TEXTURE {
    public int WIDTH, HEIGHT;
    public int UNKNOWN1, UNKNOWN2;
    public Format FORMAT;
    private int[] IMAGE;
    private javafx.scene.image.Image JAVAFXIMAGE;

    public BITMAP_TEXTURE() {}
    public BITMAP_TEXTURE(int WIDTH, int HEIGHT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
    }

    public Number[] getData() {
        Number[] bytes = new Number[IMAGE.length];
        for (int i=0;i<IMAGE.length;i++)
            bytes[i] = FORMAT.fromARGB.apply(IMAGE[i]);
        return bytes;
    }

    public int[] getImage() {
        return IMAGE;
    }

    public void setData(Number[] bytes) {
        IMAGE = new int[WIDTH * HEIGHT];
        for (int i=0;i<IMAGE.length;i++) {
            if (i >= bytes.length) break;
            IMAGE[i] = FORMAT.toARGB.apply(bytes[i].intValue());
        }
        setImage(IMAGE); // Run all other stuff in this method.
    }

    public void setImage(int[] image) {
        IMAGE = image;

        JAVAFXIMAGE = null;
        javafx.scene.image.WritableImage img = new javafx.scene.image.WritableImage(WIDTH, HEIGHT);
        javafx.scene.image.PixelWriter writer = img.getPixelWriter();
        int[] pixels = IMAGE;
        for (int i=0;i<pixels.length;i++)
            writer.setArgb(i%WIDTH, i/WIDTH, pixels[i]);
        JAVAFXIMAGE = img;
    }

    public javafx.scene.image.Image getJavaFXImage() {
        return JAVAFXIMAGE;
    }

    static final float CONVERSION_MAGIC_5BIT = 255/31f;
    static final float CONVERSION_MAGIC_6BIT = 255/63f;
    public enum Format {
        UNKNOWN ( 0, 8,0,0,0,0, b -> 0, i -> 0),

        A8R8G8B8(21, 0,8,8,8,8, b -> b, i -> i),
        X8R8G8B8(22, 8,0,8,8,8, b -> b, i -> i),
        R5G6B5  (23, 0,0,5,6,5,
                b -> 0xFF000000 |
                        (Math.round(((b >> 11) & 31) * CONVERSION_MAGIC_5BIT) << 16) |
                        (Math.round(((b >> 5) & 63) * CONVERSION_MAGIC_6BIT) << 8) |
                        Math.round((b & 31) * CONVERSION_MAGIC_5BIT),
                i -> (short) ((Math.round(((i >> 16) & 0xFF) / CONVERSION_MAGIC_5BIT) << 11) |
                        (Math.round(((i >> 8) & 0xFF) / CONVERSION_MAGIC_6BIT) << 5) |
                        Math.round((i & 0xFF) / CONVERSION_MAGIC_5BIT))
        ),
        A1R5G5B5(25, 0,1,5,5,5,
                b -> ((b & 0x8000) != 0 ? 0xFF000000 : 0x00000000) |
                        (Math.round(((b >> 10) & 31) * CONVERSION_MAGIC_5BIT) << 16) |
                        (Math.round(((b >> 5) & 31) * CONVERSION_MAGIC_5BIT) << 8) |
                        Math.round((b & 31) * CONVERSION_MAGIC_5BIT),
                i -> (short) (((i >> 24) != 0 ? 0x8000 : 0x0000) |
                        (Math.round(((i >> 16) & 0xFF) / CONVERSION_MAGIC_5BIT) << 10) |
                        (Math.round(((i >> 8) & 0xFF) / CONVERSION_MAGIC_5BIT) << 5) |
                        Math.round((i & 0xFF) / CONVERSION_MAGIC_5BIT))
        );

        public final int id;
        public final byte xBits, aBits, rBits, gBits, bBits;
        final Function<Integer, Integer> toARGB;
        final Function<Integer, Number> fromARGB;

        Format(int id, int xBits, int aBits, int rBits, int gBits, int bBits, Function<Integer, Integer> toARGB, Function<Integer, Number> fromARGB) {
            this.id = id;
            this.xBits = (byte) xBits;
            this.aBits = (byte) aBits;
            this.rBits = (byte) rBits;
            this.gBits = (byte) gBits;
            this.bBits = (byte) bBits;
            int size = size();
            if (size == 0 || size % 8 != 0) throw new IllegalArgumentException("Size of image format is not divisible by 8.");
            this.toARGB = toARGB;
            this.fromARGB = fromARGB;
        }

        public int size() {
            return xBits + aBits + rBits + gBits + bBits;
        }

        public int bytesize() {
            return size() / 8;
        }

        // -------- Code Used for testing in Java Playground --------
        // -------- Conclusion: Converting between these Bit Depths is LOSSLESS --------
        //IO.println("Testing 5BIT");
        //final float CONVERSION_MAGIC_5BIT = 255/31f;
        //for (int i=0;i<32;i++) {
        //    int j = Math.round(i * CONVERSION_MAGIC_5BIT);
        //    int k = Math.round(j / CONVERSION_MAGIC_5BIT);
        //    if (i != k) IO.println(i+" -> "+j+" -> "+k);
        //}
        //IO.println("Testing 6BIT");
        //final float CONVERSION_MAGIC_6BIT = 255/63f;
        //for (int i=0;i<64;i++) {
        //    int j = Math.round(i * CONVERSION_MAGIC_6BIT);
        //    int k = Math.round(j / CONVERSION_MAGIC_6BIT);
        //    if (i != k) IO.println(i+" -> "+j+" -> "+k);
        //}
        //IO.println("Complete");
        // -------- Conclusion: Converting between these Bit Depths is LOSSLESS --------
    }
}
