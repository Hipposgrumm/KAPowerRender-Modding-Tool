package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;

public class PR_MATRIX {
    private final float[][] MATRIX = new float[][] {
            {0,0,0,0},
            {0,0,0,0},
            {0,0,0,0},
            {0,0,0,0}
    };

    public PR_MATRIX() {}
    public PR_MATRIX(BlockReader reader) {
        for (float[] j:MATRIX) {
            for (int i=0;i<j.length;i++) {
                j[i] = reader.readFloat();
            }
        }
    }

    public void write(BlockWriter writer) {
        for (float[] j:MATRIX) {
            for (float i:j) {
                writer.writeFloat(i);
            }
        }
    }

    public float get(int i, int j) {
        return MATRIX[j][i];
    }

    public void set(int i, int j, float val) {
        MATRIX[j][i] = val;
    }
}
