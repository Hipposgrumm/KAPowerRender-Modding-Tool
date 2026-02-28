package dev.hipposgrumm.kamapviewer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import dev.hipposgrumm.kamapviewer.Main;
import dev.hipposgrumm.kamapviewer.ui.component.ButtonStyledBackground;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class UIHandler implements Disposable {
    private final Stage stage = new Stage();
    private final List<AlignedActor> actors = new ArrayList<>();

    private Label label_grid;
    private Label label_vertexcolor;
    private Label label_doubleside;
    private Label label_camspeed;

    private boolean usertoggle_grid;
    private int usertoggle_vertcolors;
    private boolean usertoggle_doubleside;
    private float usertoggle_camspeed;

    public UIHandler() {
        create();
    }

    private void create() {
        stage.clear();
        actors.clear();

        usertoggle_grid = !Main.usertoggle_grid;
        usertoggle_vertcolors = -1;
        usertoggle_doubleside = !Main.usertoggle_doubleside;
        usertoggle_camspeed = -Main.usertoggle_camspeed;

        Skin skinDefault = new Skin(Gdx.files.internal("ui/uiskin.json"));

        {
            actors.add(new AlignedActor(
                new Label("Hold F1 to hide UI", skinDefault),
                Align.topLeft, 5, 30
            ));
            label_grid = new Label("grid label", skinDefault);
            actors.add(new AlignedActor(label_grid, Align.topLeft, 5, 45));
            label_vertexcolor = new Label("vertexcolor label", skinDefault);
            actors.add(new AlignedActor(label_vertexcolor, Align.topLeft, 5, 60));
            label_doubleside = new Label("doubleside label", skinDefault);
            actors.add(new AlignedActor(label_doubleside, Align.topLeft, 5, 75));
            label_camspeed = new Label("camspeed label", skinDefault);
            actors.add(new AlignedActor(label_camspeed, Align.topLeft, 5, 90));
        }

        {
            ButtonStyledBackground topbarBG = new ButtonStyledBackground(skinDefault);
            actors.add(new AlignedActor(topbarBG, Align.top, 0, 0, (width, height) -> {
                topbarBG.setWidth(width);
            }));
            createTopTab(skinDefault, "Export", 65f, 0f,
                new TopTabOption("Coming Soon", 125f, null)
            );
            createTopTab(skinDefault, "About", 60f, 65f,
                new TopTabOption("Debug: Recreate UI", 150f, this::create)
            );
        }

        for (AlignedActor actor:actors) {
            stage.addActor(actor.actor);
        }
        repositionActors(stage.getCamera().viewportWidth, stage.getCamera().viewportHeight);
    }

    private void createTopTab(Skin skin, String name, float width, float offset, TopTabOption... elements) {
        float widest = 0f;
        TextButton[] options = new TextButton[elements.length];
        for (int i=0;i<elements.length;i++) {
            TopTabOption elem = elements[i];
            if (elem.width > widest) {
                widest = elem.width;
                for (int j=0;j<i;j++) {
                    options[i].setWidth(widest);
                }
            }
            TextButton opt = new TextButton(elem.name, skin);
            opt.setWidth(widest);
            opt.getLabel().setAlignment(Align.left);
            opt.padLeft(10f);
            if (elem.function != null) {
                opt.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        for (TextButton actor : options) actor.setVisible(false);
                        elem.function.run();
                    }
                });
            } else {
                opt.setDisabled(true);
            }
            opt.setVisible(false);
            options[i] = opt;
        }

        TextButton tab = new TextButton(name, skin);
        tab.setWidth(width);
        tab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (tab.getUserObject() == Boolean.TRUE) {
                    tab.setUserObject(Boolean.FALSE);
                    for (TextButton actor:options)
                        actor.setVisible(false);
                } else {
                    tab.setUserObject(Boolean.TRUE);
                    for (TextButton actor:options)
                        actor.setVisible(true);
                }
            }
        });

        actors.add(new AlignedActor(tab, Align.topLeft, offset, 0));
        for (int i=0;i<options.length;i++) {
            actors.add(new AlignedActor(options[i], Align.topLeft, offset, (i+1)*24f));
        }
    }

    private record TopTabOption(String name, float width, Runnable function) {}

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        stage.getCamera().viewportWidth = width;
        stage.getCamera().viewportHeight = height;
        repositionActors((float) width, (float) height);
    }

    // TODO: Test resizing before releasing anything.
    private void repositionActors(float width, float height) {
        float widthOff = (stage.getWidth()-width)/2f;
        float heightOff = (stage.getHeight()-height)/2f;
        for (AlignedActor actor:actors) {
            if (actor.resizeListener != null)
                actor.resizeListener.accept(width, height);

            float x, y;

            if ((actor.alignment & Align.top) != 0)         // Top
                y = (height-actor.y)+heightOff;
            else if ((actor.alignment & Align.bottom) != 0) // Bottom
                y = actor.y+heightOff;
            else                                            // Center
                y = (height/2f)+actor.y+heightOff;

            if ((actor.alignment & Align.right) != 0)       // Right
                x = (width-actor.x)+widthOff;
            else if ((actor.alignment & Align.left) != 0)   // Left
                x = actor.x+widthOff;
            else                                            // Center
                x = (width/2f)+actor.x+widthOff;

            actor.actor.setPosition(x, y, actor.alignment);
        }
    }

    public void render() {
        if (Main.usertoggle_hideUI) return;

        if (usertoggle_grid != Main.usertoggle_grid) {
            usertoggle_grid = Main.usertoggle_grid;
            label_grid.setText("Grid Visible [G]: "+usertoggle_grid);
        }
        if (usertoggle_vertcolors != Main.usertoggle_vertcolors) {
            usertoggle_vertcolors = Main.usertoggle_vertcolors;
            label_vertexcolor.setText("Vertex Colors [V]: "+switch (usertoggle_vertcolors) {
                case Main.VERTCOLORTOGGLE_OFF -> "Off";
                case Main.VERTCOLORTOGGLE_BLENDED -> "Blended";
                case Main.VERTCOLORTOGGLE_ONLY -> "Only";
                default -> "UNKNOWN VALUE";
            });
        }
        if (usertoggle_doubleside != Main.usertoggle_doubleside) {
            usertoggle_doubleside = Main.usertoggle_doubleside;
            label_doubleside.setText("Force Double-Sided [C]: "+usertoggle_doubleside);
        }
        if (usertoggle_camspeed != Main.usertoggle_camspeed) {
            usertoggle_camspeed = Main.usertoggle_camspeed;
            label_camspeed.setText(String.format("Camera Speed [+/-]: %.1f", usertoggle_camspeed));
        }

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
        final BiConsumer<Float, Float> resizeListener;

        /// @param alignment {@link Align}
        public AlignedActor(Actor actor, int alignment, float x, float y) {
            this.actor = actor;
            this.alignment = alignment;
            this.x = x;
            this.y = y;
            this.resizeListener = null;
        }

        /// @param alignment {@link Align}
        public AlignedActor(Actor actor, int alignment, float x, float y, BiConsumer<Float, Float> resizeListener) {
            this.actor = actor;
            this.alignment = alignment;
            this.x = x;
            this.y = y;
            this.resizeListener = resizeListener;
        }
    }
}
