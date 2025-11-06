package dev.hipposgrumm.kamapreader.util.control.display;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.Icon;
import dev.hipposgrumm.kamapreader.util.types.Texture;
import dev.hipposgrumm.kamapreader.util.types.structs.BITMAP_TEXTURE;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class TexturesDisplay {
    public static VBox create(FirstThing controller, Object parent, BITMAP_TEXTURE[] texArr, DatingProfileEntry<BITMAP_TEXTURE[]> entry) {
        HBox[] images = new HBox[texArr.length];
        ImageView[] imageViews = new ImageView[images.length];
        for (int i=0;i<texArr.length;i++) {
            int lambdaSafeIndex = i;
            ImageView image = new ImageView(texArr[i].getJavaFXImage());
            Button saveButton = new Button("Save Texture", Icon.download());
            Button changeButton = new Button("Change Texture", Icon.upload());
            if (i == 0) {
                double minwidth = controller.tableValue.getMinWidth();
                controller.tableValue.setMinWidth(image.getImage().getWidth() + 125);
                controller.tableValue.setMinWidth(minwidth);
            }
            saveButton.setOnAction(event -> saveOne(controller, parent, texArr[lambdaSafeIndex]));
            if (entry.readOnly()) {
                changeButton.setDisable(true);
            } else {
                changeButton.setOnAction(event -> changeOne(controller, texArr[lambdaSafeIndex], lambdaSafeIndex, imageViews, entry));
            }
            imageViews[i] = image;
            images[i] = new HBox(5,
                    image,
                    new VBox(saveButton, changeButton)
            );
        }
        Button saveButton = new Button("Save All", Icon.download());
        Button changeButton = new Button("Change All", Icon.upload());
        saveButton.setOnAction(event -> saveAll(controller, parent, texArr));
        if (texArr.length == 0 || entry.readOnly()) {
            changeButton.setDisable(true);
        } else {
            changeButton.setOnAction(event -> changeAll(controller, texArr[0], imageViews, entry));
        }
        VBox box = new VBox(5);
        box.getChildren().addAll(images);
        box.getChildren().add(saveButton);
        box.getChildren().add(changeButton);
        return box;
    }

    private static void saveOne(FirstThing controller, Object parent, BITMAP_TEXTURE tex) {
        String defname = null;
        if (parent instanceof Texture t) defname = t+".png";
        try {
            File file = controller.popupSaveFile("Save Texture", defname, "PNG", ".png");
            if (file == null) return;
            if (!file.getName().endsWith(".png")) file = new File(file.getPath()+".png");
            if ((!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?"))) return;

            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(tex.WIDTH, tex.HEIGHT, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, tex.WIDTH, tex.HEIGHT, tex.getImage(), 0, tex.WIDTH);
            ImageIO.write(image, "PNG", file);
        } catch (Exception e) {
            controller.popupError("Error Saving", "An exception was thrown when exporting.", e);
        }
    }

    private static void saveAll(FirstThing controller, Object parent, BITMAP_TEXTURE[] textures) {
        String defname = null;
        if (parent instanceof Texture t) defname = t+".png";
        try {
            File file = controller.popupSaveFile("Save Textures", defname, "PNG", ".png");
            if (file == null) return;
            if (!file.getName().endsWith(".png")) file = new File(file.getPath());
            if ((!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?"))) return;

            int lod = 0;
            for (BITMAP_TEXTURE tex:textures) {
                File out = file;
                if (lod != 0) {
                    String path = file.getPath();
                    int extPos = path.lastIndexOf('.');
                    int sepPos = path.lastIndexOf(File.separatorChar);
                    out = new File(
                            extPos > sepPos ?
                                    path.substring(0, extPos)+"_LOD"+lod+path.substring(extPos)
                                    : path+"_LOD"+lod
                    );
                }
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(tex.WIDTH, tex.HEIGHT, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                image.setRGB(0, 0, tex.WIDTH, tex.HEIGHT, tex.getImage(), 0, tex.WIDTH);
                ImageIO.write(image, "PNG", out);
                lod++;
            }
        } catch (Exception e) {
            controller.popupError("Error Saving", "An exception was thrown when exporting.", e);
        }
    }

    private static void changeOne(FirstThing controller, BITMAP_TEXTURE tex, int index, ImageView[] imageViews, DatingProfileEntry<BITMAP_TEXTURE[]> entry) {
        try {
            File file = controller.popupOpenFile("Choose Texture", null, "PNG", ".png");
            if (file == null) return;
            entry.get()[index] = loadReplacementTexture(file, tex);
            imageViews[index].setImage(entry.get()[index].getJavaFXImage());
        } catch (Exception e) {
            controller.popupError("Error", "An exception was thrown when changing image.", e);
        }
    }

    private static void changeAll(FirstThing controller, BITMAP_TEXTURE tex, ImageView[] imageViews, DatingProfileEntry<BITMAP_TEXTURE[]> entry) {
        try {
            File file = controller.popupOpenFile("Choose Texture", null, "PNG", ".png");
            if (file == null) return;
            entry.set(new BITMAP_TEXTURE[] {loadReplacementTexture(file, tex)});
            BITMAP_TEXTURE[] images = entry.get();
            for (int i=0;i<images.length;i++)
                imageViews[i].setImage(images[i].getJavaFXImage());
        } catch (Exception e) {
            controller.popupError("Error", "An exception was thrown when changing image.", e);
        }
    }

    public static BITMAP_TEXTURE loadReplacementTexture(File file, BITMAP_TEXTURE tex) throws IOException {
        java.awt.Image src = ImageIO.read(file);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(tex.WIDTH, tex.HEIGHT, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        java.awt.Graphics2D g2d = img.createGraphics();
        try {
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(src, 0, 0, tex.WIDTH, tex.HEIGHT, null);
        } finally {
            g2d.dispose();
        }

        BITMAP_TEXTURE newtex = new BITMAP_TEXTURE(tex.WIDTH, tex.HEIGHT);
        newtex.UNKNOWN1 = tex.UNKNOWN1;
        newtex.UNKNOWN2 = tex.UNKNOWN2;
        newtex.FORMAT = tex.FORMAT;
        int[] pixels = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int[] data = new int[tex.WIDTH*tex.HEIGHT];
        for (int i=0;i<data.length;i++) {
            int addition = (img.getAlphaRaster() != null ? 0x00000000 : 0xFF000000);
            data[i] = pixels[i] | addition;
        }
        newtex.setImage(data);
        return newtex;
    }
}
