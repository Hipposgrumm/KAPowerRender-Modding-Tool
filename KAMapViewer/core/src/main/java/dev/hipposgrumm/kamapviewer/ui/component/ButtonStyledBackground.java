package dev.hipposgrumm.kamapviewer.ui.component;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ButtonStyledBackground extends Actor {
    private Drawable bg;

    public ButtonStyledBackground(Skin skin) {
        super();
        bg = skin.get(Button.ButtonStyle.class).up;
        setWidth(bg.getMinWidth());
        setHeight(bg.getMinHeight());
    }

    public void setStyle(Button.ButtonStyle style) {
        if (style == null) throw new IllegalArgumentException("style cannot be null.");
        this.bg = style.up;
    }

    public Drawable getDrawable() {
        return bg;
    }

    public void draw(Batch batch, float parentAlpha) {
        getDrawable().draw(batch, getX(), getY(), getWidth(), getHeight());
    }
}
