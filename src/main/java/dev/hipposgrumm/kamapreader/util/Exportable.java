package dev.hipposgrumm.kamapreader.util;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.types.Texture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Exportable {
    /// Get file name (without extension).
    String getFileName();

    /// Get file extension (without dot).
    String getFileExtension();

    void writeExportData(FileOutputStream outputStream) throws IOException;

    static void massExport(FirstThing controller, DatingBachelor[] selection) {
        List<Exportable> exportables = Arrays.stream(selection)
                .filter(bachelor -> bachelor instanceof Exportable)
                .map(bachelor -> (Exportable) bachelor)
                .toList();
        if (exportables.isEmpty()) {
            controller.popupNotice("Invalid Selection", "Nothing to export.", "Nothing in selection can be exported.");
            return;
        }
        if (!Texture.HAS_AWT) {
            long textureCount = exportables.stream().filter(exportable -> exportable instanceof Texture).count();
            if (textureCount >= exportables.size()) {
                controller.popupNotice("Incompatible!", "Cannot export!", Texture.NO_AWT_MSG);
                return;
            } else if (textureCount > 0) {
                controller.popupNotice("Some incompatible!", "Cannot export texture.", Texture.NO_AWT_MSG+" \nTextures will not be exported.");
            }
        }

        File folder;
        try {
            folder = controller.popupSaveDirectory("Export to Folder");
            if (folder == null) return;
        } catch (Exception e) {
            controller.popupError("Error Saving", "An exception was thrown when selecting folder.", e);
            return;
        }

        List<String> errorFilenames = new ArrayList<>();
        List<Exception> errorExceptions = new ArrayList<>();
        for (Exportable exportable:exportables) {
            String filename = exportable.getFileName()+"."+exportable.getFileExtension();
            try {
                File file = new File(folder, filename);
                if ((!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "The file "+filename+" already exists!", "Would you like to overwrite it?")))
                    continue;

                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    exportable.writeExportData(outputStream);
                }
            } catch (Exception e) {
                errorFilenames.add(filename);
                errorExceptions.add(e);
            }
        }
        for (int i=0;i<errorExceptions.size();i++) {
            controller.popupError("Error Saving", "An exception was thrown when exporting "+errorFilenames.get(i), errorExceptions.get(i));
        }
    }
}
