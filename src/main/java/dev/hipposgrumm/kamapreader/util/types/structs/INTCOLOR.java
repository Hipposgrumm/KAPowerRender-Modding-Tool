package dev.hipposgrumm.kamapreader.util.types.structs;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.control.ContainingDatingProfileValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class INTCOLOR implements ContainingDatingProfileValue.Containable<INTCOLOR> {
    private final Format format;
    public int color;

    public INTCOLOR(Format format, int color) {
        this.format = format;
        this.color = color;
    }

    public int getRed() {
        return (color >> format.SHIFT_R) & 0xFF;
    }

    public int getGreen() {
        return (color >> format.SHIFT_G) & 0xFF;
    }

    public int getBlue() {
        return (color >> format.SHIFT_B) & 0xFF;
    }

    public int getAlpha() {
        return (color >> format.SHIFT_A) & 0xFF;
    }

    public void setRed(int r) {
        color = (color & (0xFF<<format.SHIFT_R)) | ((r & 0xFF) << format.SHIFT_R);
    }

    public void setGreen(int g) {
        color = (color & (0xFF<<format.SHIFT_G)) | ((g & 0xFF) << format.SHIFT_G);
    }

    public void setBlue(int b) {
        color = (color & (0xFF<<format.SHIFT_B)) | ((b & 0xFF) << format.SHIFT_B);
    }

    public void setAlpha(int a) {
        color = (color & (0xFF<<format.SHIFT_A)) | ((a & 0xFF) << format.SHIFT_A);
    }

    public INTCOLOR as(Format format) {
        if (format == this.format) return this;
        INTCOLOR color = new INTCOLOR(format, -1);
        color.setRed(getRed());
        color.setGreen(getGreen());
        color.setBlue(getBlue());
        color.setAlpha(getAlpha());
        return color;
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        Control R = DatingProfileValue.ubyteSpinner(new UByte((short) this.getRed()), readonly, (observable, oldValue, newValue) -> {
            this.setRed(newValue);
            onChanged.run();
        });
        Control G = DatingProfileValue.ubyteSpinner(new UByte((short) this.getGreen()), readonly, (observable, oldValue, newValue) -> {
            this.setGreen(newValue);
            onChanged.run();
        });
        Control B = DatingProfileValue.ubyteSpinner(new UByte((short) this.getBlue()), readonly, (observable, oldValue, newValue) -> {
            this.setBlue(newValue);
            onChanged.run();
        });
        Control A = DatingProfileValue.ubyteSpinner(new UByte((short) this.getAlpha()), readonly, (observable, oldValue, newValue) -> {
            this.setAlpha(newValue);
            onChanged.run();
        });
        R.setMaxWidth(63);
        G.setMaxWidth(63);
        B.setMaxWidth(63);
        A.setMaxWidth(63);
        HBox box = new HBox(0,
                new Label("R:"), R,
                new Label(" G:"), G,
                new Label(" B:"), B,
                new Label(" A:"), A
        );
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    @Override
    public ContainingDatingProfileValue.Containable<? extends INTCOLOR> makeCopy() {
        return new INTCOLOR(format, color);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof INTCOLOR other)) return false;
        return  other.format == format &&
                other.color  == color;
    }

    public enum Format {
        RGBA(3, 2, 1, 0),
        ARGB(2, 1, 0, 3);

        final int SHIFT_R;
        final int SHIFT_G;
        final int SHIFT_B;
        final int SHIFT_A;

        Format(int R, int G, int B, int A) {
            this.SHIFT_R = R*8;
            this.SHIFT_G = G*8;
            this.SHIFT_B = B*8;
            this.SHIFT_A = A*8;
        }
    }
}
