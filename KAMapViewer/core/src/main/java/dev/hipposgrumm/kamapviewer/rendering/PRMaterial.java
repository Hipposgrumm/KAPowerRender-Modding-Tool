package dev.hipposgrumm.kamapviewer.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import dev.hipposgrumm.kamapviewer.util.Flags;
import dev.hipposgrumm.kamapviewer.util.enums.*;

public class PRMaterial {
    public final String name;
    public final Texture[] textures = new Texture[7];
    public Color color = new Color(-1);
    public float bump00, bump01, bump10, bump11, bumpscale;
    public Color spec = new Color(-1);
    public float specpower;
    public boolean twosided = false;

    public D3DZBUFFERTYPE enableZ = D3DZBUFFERTYPE.FALSE;
    public boolean enableWriteZ = true;
    public boolean lighting = false;
    public boolean vertexColors = true;
    public boolean enableAlphaBlend = false;
    public boolean enableAlphaTest = true;
    public boolean enableSpecular = false;
    public int alphaRef = 158;
    public Color textureFactor = new Color(-1);
    public Color blendFactor = new Color(-1);
    public D3DSHADEMODE shadeMode = D3DSHADEMODE.FLAT;
    public D3DBLEND srcBlend = D3DBLEND.__;
    public D3DBLEND destBlend = D3DBLEND.__;
    public D3DCMPFUNC zFunc = D3DCMPFUNC.__;
    public D3DCMPFUNC alphaFunc = D3DCMPFUNC.__;
    public D3DMATERIALCOLORSOURCE diffuseMaterialSource = D3DMATERIALCOLORSOURCE.MATERIAL;
    public D3DMATERIALCOLORSOURCE specularMaterialSource = D3DMATERIALCOLORSOURCE.MATERIAL;
    public D3DMATERIALCOLORSOURCE ambientMaterialSource = D3DMATERIALCOLORSOURCE.MATERIAL;
    public D3DMATERIALCOLORSOURCE emissiveMaterialSource = D3DMATERIALCOLORSOURCE.MATERIAL;
    public ColorWriteFlags colorWriteFlags = new ColorWriteFlags(0);
    public D3DBLENDOP blendop = D3DBLENDOP.__;

    public final RenderStageData[] renderStages = new RenderStageData[8];

    public PRMaterial(String id) {
        this.name = id;
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r,g,b,a);
    }

    public void setBump(float bump00, float bump01, float bump10, float bump11, float bumpscale) {
        this.bump00 = bump00;
        this.bump01 = bump01;
        this.bump10 = bump10;
        this.bump11 = bump11;
        this.bumpscale = bumpscale;
    }

    public void setSpecColor(float r, float g, float b, float a, float power) {
        this.spec.set(r,g,b,a);
        this.specpower = power;
    }

    public void setTwoSided(boolean value) {
        this.twosided = value;
    }

    public void setRenderStyle(
        byte enableZ, boolean enableWriteZ,
        boolean lighting, boolean vertexColors,
        boolean enableAlphaBlend, boolean enableAlphaTest, boolean enableSpecular,
        short alphaRef, int textureFactor, int blendFactor
    ) {
        this.enableZ = D3DZBUFFERTYPE.from(enableZ);
        this.enableWriteZ = enableWriteZ;
        this.lighting = lighting;
        this.vertexColors = vertexColors;
        this.enableAlphaBlend = enableAlphaBlend;
        this.enableAlphaTest = enableAlphaTest;
        this.enableSpecular = enableSpecular;
        this.alphaRef = alphaRef;
        this.textureFactor.set(textureFactor);
        this.blendFactor.set(blendFactor);
    }

    public void setRenderFunction(
        int shadeMode, int srcBlend, int destBlend,
        int zFunc, int alphaFunc,
        int diffuseSrcMat, int specularSrcMat, int ambientSrcMat, int emissiveSrcMat,
        int colorWriteFlags,
        int blendop
    ) {
        this.shadeMode = D3DSHADEMODE.from(shadeMode);
        this.srcBlend = D3DBLEND.from(srcBlend);
        this.destBlend = D3DBLEND.from(destBlend);
        this.zFunc = D3DCMPFUNC.from(zFunc);
        this.alphaFunc = D3DCMPFUNC.from(alphaFunc);
        this.diffuseMaterialSource = D3DMATERIALCOLORSOURCE.from(diffuseSrcMat);
        this.specularMaterialSource = D3DMATERIALCOLORSOURCE.from(specularSrcMat);
        this.ambientMaterialSource = D3DMATERIALCOLORSOURCE.from(ambientSrcMat);
        this.emissiveMaterialSource = D3DMATERIALCOLORSOURCE.from(emissiveSrcMat);
        this.colorWriteFlags.setValue(colorWriteFlags);
        this.blendop = D3DBLENDOP.from(blendop);
    }

    public static class RenderStageData {
        public D3DTEXTUREOP colorop = D3DTEXTUREOP.__;
        public D3DTEXTUREOP alphaop = D3DTEXTUREOP.__;
        public TEXARGS colorarg1 = new TEXARGS(0);
        public TEXARGS colorarg2 = new TEXARGS(0);
        public TEXARGS alphaarg1 = new TEXARGS(0);
        public TEXARGS alphaarg2 = new TEXARGS(0);
        public TEXINDEX texcoordindex = TEXINDEX._0;
        public D3DTEXTURETRANSFORMFLAGS transformflags = new D3DTEXTURETRANSFORMFLAGS(0);

        public D3DTEXTUREADDRESS addressU = D3DTEXTUREADDRESS.__;
        public D3DTEXTUREADDRESS addressV = D3DTEXTUREADDRESS.__;
        public D3DTEXTUREADDRESS addressW = D3DTEXTUREADDRESS.__;
        public final Color bordercolor = new Color(0);

        public void setTextureStage(
            int colorop, int alphaop,
            int colorarg1, int colorarg2,
            int alphaarg1, int alphaarg2,
            int texcoordindex, int transformflags
        ) {
            this.colorop = D3DTEXTUREOP.from(colorop);
            this.alphaop = D3DTEXTUREOP.from(alphaop);
            this.colorarg1.setValue(colorarg1);
            this.colorarg2.setValue(colorarg2);
            this.alphaarg1.setValue(alphaarg1);
            this.alphaarg2.setValue(alphaarg2);
            this.texcoordindex = TEXINDEX.from(texcoordindex);
            this.transformflags.setValue(transformflags);
        }

        public void setSamplerStage(
            int addressU, int addressV, int addressW,
            int bordercolor
        ) {
            this.addressU = D3DTEXTUREADDRESS.from(addressU);
            this.addressV = D3DTEXTUREADDRESS.from(addressV);
            this.addressW = D3DTEXTUREADDRESS.from(addressW);
            this.bordercolor.set(bordercolor);
        }
    }

    public static class ColorWriteFlags extends Flags {
        public static final BoolEntry Red = new BoolEntry("Red", 0);
        public static final BoolEntry Green = new BoolEntry("Green", 1);
        public static final BoolEntry Blue = new BoolEntry("Blue", 2);
        public static final BoolEntry Alpha = new BoolEntry("Alpha", 3);

        public ColorWriteFlags(int value) {
            super(value);
        }

        @Override
        public Entry[] getEntries() {
            return new Entry[] {Red, Green, Blue, Alpha};
        }
    }
}
