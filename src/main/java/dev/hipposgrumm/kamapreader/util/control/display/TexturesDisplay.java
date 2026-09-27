package dev.hipposgrumm.kamapreader.util.control.display;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.Icon;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import dev.hipposgrumm.kamapreader.util.types.Texture;
import dev.hipposgrumm.kamapreader.util.types.structs.BITMAP_TEXTURE;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TexturesDisplay implements DatingProfileValue {
    private final Texture main;
    private BITMAP_TEXTURE[] textures;
    private boolean isModified = false;

    public TexturesDisplay(Texture main, BITMAP_TEXTURE[] textures) {
        this.main = main;
        this.textures = textures;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        HBox[] images = new HBox[textures.length];
        ImageView[] imageViews = new ImageView[images.length];
        Tooltip noAWTTooltip = new Tooltip(Texture.NO_AWT_MSG);
        for (int i=0;i<textures.length;i++) {
            int lambdaSafeIndex = i;
            ImageView image = new ImageView(textures[i].getJavaFXImage());
            Button saveButton = new Button("Save Texture", Icon.download());
            Button changeButton = new Button("Change Texture", Icon.upload());
            if (i == 0) {
                double minwidth = controller.tableValue.getMinWidth();
                controller.tableValue.setMinWidth(image.getImage().getWidth() + 150);
                controller.tableValue.setMinWidth(minwidth);
            }
            if (Texture.HAS_AWT) {
                saveButton.setOnAction(event -> saveOne(controller, textures[lambdaSafeIndex]));
            } else {
                saveButton.setTooltip(noAWTTooltip);
            }
            if (readonly) changeButton.setDisable(true);
            else if (Texture.HAS_AWT) {
                changeButton.setOnAction(event -> changeOne(controller, lambdaSafeIndex, imageViews, onChanged));
            } else {
                changeButton.setTooltip(noAWTTooltip);
            }
            imageViews[i] = image;
            images[i] = new HBox(5,
                    image,
                    new VBox(saveButton, changeButton)
            );
        }
        Button saveButton = new Button("Save All", Icon.download());
        Button changeButton = new Button("Change All", Icon.upload());
        if (Texture.HAS_AWT) {
            saveButton.setOnAction(event -> saveAll(controller, textures));
        } else {
            saveButton.setTooltip(noAWTTooltip);
        }
        if (textures.length == 0 || readonly) {
            changeButton.setDisable(true);
        } else {
            if (Texture.HAS_AWT) {
                changeButton.setOnAction(event -> changeAll(controller, imageViews, onChanged));
            } else {
                changeButton.setTooltip(noAWTTooltip);
            }
        }
        VBox box = new VBox(5);
        box.getChildren().addAll(images);
        box.getChildren().add(saveButton);
        box.getChildren().add(changeButton);
        return box;
    }

    @Override
    public boolean isModified() {
        return isModified;
    }

    private void saveOne(FirstThing controller, BITMAP_TEXTURE tex) {
        String defname = (main != null) ? main.getFileName()+".png" : null;
        try {
            File file = controller.popupSaveFile("Save Texture", defname, "PNG", "*.png");
            if (file == null) return;
            if (!file.getName().endsWith(".png")) file = new File(file.getPath()+".png");
            if ((!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?"))) return;

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                Texture.saveTexture(tex, outputStream);
            }
        } catch (Exception e) {
            controller.popupError("Error Saving", "An exception was thrown when exporting.", e);
        }
    }

    private void saveAll(FirstThing controller, BITMAP_TEXTURE[] textures) {
        if (textures.length == 0) {
            controller.popupNotice("No textures", "No textures to save", "Texture list size is 0.");
        }
        String defname = (main != null) ? main.getFileName()+".png" : null;
        try {
            File file = controller.popupSaveFile("Save Textures", defname, "PNG", "*.png");
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

                try (FileOutputStream outputStream = new FileOutputStream(out)) {
                    Texture.saveTexture(tex, outputStream);
                }
                lod++;
            }
        } catch (Exception e) {
            controller.popupError("Error Saving", "An exception was thrown when exporting.", e);
        }
    }

    private void changeOne(FirstThing controller, int index, ImageView[] imageViews, Runnable onChanged) {
        try {
            File file = controller.popupOpenFile("Choose Texture", null, "PNG", "*.png");
            if (file == null) return;
            textures[index] = loadReplacementTexture(file, textures[index]);
            imageViews[index].setImage(textures[index].getJavaFXImage());
            isModified = true;
            onChanged.run();
        } catch (Exception e) {
            controller.popupError("Error", "An exception was thrown when changing image.", e);
        }
    }

    private void changeAll(FirstThing controller, ImageView[] imageViews, Runnable onChanged) {
        try {
            File file = controller.popupOpenFile("Choose Texture", null, "PNG", "*.png");
            if (file == null) return;
            textures = main.changeImage(loadReplacementTexture(file, textures[0]));
            for (int i=0;i<textures.length;i++)
                imageViews[i].setImage(textures[i].getJavaFXImage());
            isModified = true;
            onChanged.run();
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
