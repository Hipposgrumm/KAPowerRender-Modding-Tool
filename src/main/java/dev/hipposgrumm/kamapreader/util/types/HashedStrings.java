package dev.hipposgrumm.kamapreader.util.types;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

// https://stackoverflow.com/a/3430209/20170780
public class HashedStrings {
    private static final Map<Integer,String> int2str = new HashMap<>();

    /// Handle called by main class that forces JVM to initialize it.
    public static void init() {}

    public static int fromString(String string) {
        CRC32 crc = new CRC32();
        crc.update(string.getBytes(StandardCharsets.UTF_8));
        return  (int) crc.getValue();
    }

    public static String tryFromHash(int hash) {
        return int2str.getOrDefault(hash, null);
    }

    static {
        String path;
        if (System.getProperty("user.dir").endsWith("bin")) {
            // Assume built jar.
            path = "../hashed_strings.txt";
        } else {
            path = "hashed_strings.txt";
        }

        File stringsFile = new File(path);
        if (stringsFile.exists()) {
            try {
                for (String line:Files.readAllLines(stringsFile.toPath())) {
                    // The comments in the file also get hashed, but this doesn't hurt anything.
                    if (line.isBlank()) continue;
                    int hash = fromString(line);
                    int2str.put(hash, line);
                }
            } catch (IOException | SecurityException e) {
                throw new RuntimeException(e);
            }
        } else System.err.println("hashed_strings.txt is missing");
    }
}
