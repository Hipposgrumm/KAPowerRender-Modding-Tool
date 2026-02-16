package dev.hipposgrumm.kamapviewer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public class UIHandler implements Disposable {
    private final Stage stage = new Stage();
    private final List<AlignedActor> actors = new ArrayList<>();

    public UIHandler() {
        create();
    }

    private void create() {
        stage.clear();
        actors.clear();

        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        TextButton button = new TextButton("Test button", skin);
        button.setWidth(400f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                create();
            }
        });
        actors.add(new AlignedActor(button, Align.topLeft, 20, 40));
        stage.addActor(button);
        repositionActors(stage.getCamera().viewportWidth, stage.getCamera().viewportHeight);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        stage.getCamera().viewportWidth = width;
        stage.getCamera().viewportHeight = height;
        repositionActors((float) width, (float) height);
    }

    // TODO: Test resizing before releasing anything.
    private void repositionActors(float width, float height) {
        for (AlignedActor actor:actors) {
            float x, y;

            if ((actor.alignment & Align.top) != 0)         // Top
                y = height-actor.y;
            else if ((actor.alignment & Align.bottom) != 0) // Bottom
                y = actor.y;
            else                                            // Center
                y = (height/2f)+actor.y;

            if ((actor.alignment & Align.right) != 0)       // Right
                x = width-actor.x;
            else if ((actor.alignment & Align.left) != 0)   // Left
                x = actor.x;
            else                                            // Center
                x = (width/2f)+actor.x;

            actor.actor.setPosition(x, y, actor.alignment);
        }
    }

    public void render() {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public InputProcessor getInput() {
        return stage;
    }

    @Override
    public void dispose() {
        actors.clear();
        stage.dispose();
    }

    private static class AlignedActor {
        final Actor actor;
        final int alignment;
        float x,y;

        /// @param alignment {@link Align}
        public AlignedActor(Actor actor, int alignment, float x, float y) {
            this.actor = actor;
            this.alignment = alignment;
            this.x = x;
            this.y = y;
        }
    }
}
