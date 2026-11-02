package dev.hipposgrumm.kamapreader.util.types;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.CRC32;

// https://stackoverflow.com/a/3430209/20170780
public class HashedStrings {
    private static final Map<String,Integer> str2int = new Hashtable<>();
    private static final Map<Integer,String> int2str = new Hashtable<>();

    /// Handle called by main class that forces JVM to initialize it.
    public static void init() {}

    public static synchronized int fromString(String string) {
        Integer hash = str2int.get(string);
        if (hash != null) return hash;

        CRC32 crc = new CRC32();
        crc.update(string.getBytes(StandardCharsets.UTF_8));
        int newHash = (int) crc.getValue();
        str2int.put(string, newHash);
        int2str.put(newHash, string);
        return newHash;
    }

    public static synchronized String tryFromValue(int hash) {
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
                    if (!line.isBlank())
                        fromString(line.substring(0, Math.min(
                                line.indexOf('\n'),
                                line.indexOf('\r')
                        )));
                }
            } catch (IOException | SecurityException e) {
                throw new RuntimeException(e);
            }
        } else System.err.println("hashed_strings.txt is missing");
    }
}
