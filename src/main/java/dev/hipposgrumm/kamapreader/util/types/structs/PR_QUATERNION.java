package dev.hipposgrumm.kamapreader.util.types.structs;

public class PR_QUATERNION {
    public float X, Y, Z;
    public float W = 1;

    public PR_QUATERNION() {}
    public PR_QUATERNION(float X, float Y, float Z, float W) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.W = W;
    }
}