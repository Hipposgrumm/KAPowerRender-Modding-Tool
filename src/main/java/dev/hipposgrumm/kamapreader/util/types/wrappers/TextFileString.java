package dev.hipposgrumm.kamapreader.util.types.wrappers;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.Icon;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileOutputStream;

public class TextFileString implements DatingProfileValue {
    private String name;
    private String contents;
    private final String original_contents;

    public TextFileString(String name, String contents) {
        this.name = name;
        this.contents = contents;
        this.original_contents = contents;
    }

    public String getName() {
        return name;
    }

    public String getContents() {
        return contents;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        TextArea text = new TextArea(this.contents);
        if (readonly) text.setEditable(false);
        else {
            text.textProperty().addListener((observable, oldValue, newValue) -> {
                this.setContents(newValue);
                onChanged.run();
            });
        }

        Button saveButton = new Button("Save File", Icon.export());
        saveButton.setOnAction(event -> {
            try {
                int extPos = this.name.lastIndexOf('.');
                String ext = "*"+(extPos >= 0 ?
                        this.name.substring(extPos) :
                        ".txt");
                File file = controller.popupSaveFile("Save File", extPos >= 0 ? name : name+".txt", "Text file ("+ext.substring(1).toUpperCase()+")", ext);
                if (file == null) return;
                if (!file.getName().endsWith(ext)) file = new File(file.getPath()+ext);
                if (!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?")) return;

                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(this.contents.getBytes());
                }
            } catch (Exception e) {
                controller.popupError("Error Saving", "An exception was thrown when saving.", e);
            }
        });
        return new VBox(text, saveButton);
    }

    @Override
    public boolean isModified() {
        return !contents.equals(original_contents);
    }
}
