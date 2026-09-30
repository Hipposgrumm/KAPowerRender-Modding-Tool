package dev.hipposgrumm.kamapviewer.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import dev.hipposgrumm.kamapviewer.Main;
import dev.hipposgrumm.kamapviewer.util.VertexcolorMode;

public class OtherKeysHandler implements InputProcessor {
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F1) {
            Main.usertoggle_hideUI = true;
        }
        if (keycode == Input.Keys.G) {
            Main.usertoggle_grid = !Main.usertoggle_grid;
            return true;
        } else if (keycode == Input.Keys.V) {
            Main.usertoggle_vertcolors = cycleEnum(Main.usertoggle_vertcolors);
            return true;
        }else if (keycode == Input.Keys.C) {
            Main.usertoggle_doubleside = cycleEnum(Main.usertoggle_doubleside);
            return true;
        } else if (keycode == Input.Keys.EQUALS || keycode == Input.Keys.NUMPAD_ADD) {
            Main.usertoggle_camspeed += 2.5f;
            return true;
        } else if (keycode == Input.Keys.MINUS || keycode == Input.Keys.NUMPAD_SUBTRACT) {
            if (Main.usertoggle_camspeed >= 2.5f)
                Main.usertoggle_camspeed -= 2.5f;
            return true;
        }
        return false;
    }

    private static <T extends Enum<T>> T cycleEnum(T oldval) {
        T[] values = oldval.getDeclaringClass().getEnumConstants();
        return values[(oldval.ordinal()+1) % values.length];
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.F1) {
            Main.usertoggle_hideUI = false;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
