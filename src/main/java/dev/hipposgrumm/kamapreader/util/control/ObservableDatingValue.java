package dev.hipposgrumm.kamapreader.util.control;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.control.display.SoundDisplay;
import dev.hipposgrumm.kamapreader.util.control.display.TexturesDisplay;
import dev.hipposgrumm.kamapreader.util.types.Previewable;
import dev.hipposgrumm.kamapreader.util.types.SnSound;
import dev.hipposgrumm.kamapreader.util.types.SubBachelorPreviewEntry;
import dev.hipposgrumm.kamapreader.util.types.WorldObjectFlags;
import dev.hipposgrumm.kamapreader.util.types.structs.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

@SuppressWarnings("unchecked")
public class ObservableDatingValue extends ObservableValueBase<Node> {
    private final FirstThing controller;
    private final TreeItem<?> item;
    private final DatingProfileEntry<?> entry;

    public ObservableDatingValue(FirstThing controller, TableColumn.CellDataFeatures<Pair<TreeItem<?>, DatingProfileEntry<?>>, Node> cdf) {
        this.controller = controller;
        item = cdf.getValue().getKey();
        entry = cdf.getValue().getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Node getValue() {
        if (entry instanceof SubBachelorPreviewEntry pvs) {
            GridPane grid = new GridPane();

            int i=0;
            for (Previewable pv:pvs.get()) {
                VBox box = new VBox(25,
                        pv.getPreviewGraphic(),
                        new Label(pv.getPreviewName())
                );
                box.setAlignment(Pos.CENTER);
                Button btn = new Button("", box);
                btn.setMinWidth(75);
                btn.setMinHeight(100);
                int index = i;
                btn.setOnAction(event -> {
                    controller.tree.getSelectionModel().select((TreeItem<DatingBachelor>) item.getChildren().get(index));
                });
                grid.add(btn, i%4, i/4);
                i++;
            }
            return grid;
        }
        Object value = entry.get();
        return switch (value) {
            case null -> new Label("null");
            case String s -> text(s, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<String>) entry).set(newValue);
            });
            case Boolean b -> checkbox(b, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Boolean>) entry).set(newValue);
            });
            case Integer i -> intSpinner(i, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Integer>) entry).set(newValue);
            });
            case Long i -> uintSpinner(i, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Long>) entry).set(newValue);
            });
            case Float f -> floatSpinner(f, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Float>) entry).set(newValue.floatValue());
            });
            case PR_POINT p -> {
                Control X = floatSpinner(p.X, (observable, oldValue, newValue) -> {
                    p.X = newValue.floatValue();
                });
                Control Y = floatSpinner(p.Y, (observable, oldValue, newValue) -> {
                    p.Y = newValue.floatValue();
                });
                Control Z = floatSpinner(p.Z, (observable, oldValue, newValue) -> {
                    p.Z = newValue.floatValue();
                });
                X.setMaxWidth(90);
                Y.setMaxWidth(90);
                Z.setMaxWidth(90);
                HBox box = new HBox(0,
                        new Label("X:"), X,
                        new Label(" Y:"), Y,
                        new Label(" Z:"), Z
                );
                box.setAlignment(Pos.CENTER_LEFT);
                yield box;
            }
            case PR_QUATERNION q -> {
                Control X = floatSpinner(q.X, (observable, oldValue, newValue) -> {
                    q.X = newValue.floatValue();
                });
                Control Y = floatSpinner(q.Y, (observable, oldValue, newValue) -> {
                    q.Y = newValue.floatValue();
                });
                Control Z = floatSpinner(q.Z, (observable, oldValue, newValue) -> {
                    q.Z = newValue.floatValue();
                });
                Control W =  floatSpinner(q.W, (observable, oldValue, newValue) -> {
                    q.W = newValue.floatValue();
                });
                X.setMaxWidth(63);
                Y.setMaxWidth(63);
                Z.setMaxWidth(63);
                W.setMaxWidth(63);
                HBox box = new HBox(0,
                        new Label("X:"), X,
                        new Label(" Y:"), Y,
                        new Label(" Z:"), Z,
                        new Label(" W:"), W
                );
                box.setAlignment(Pos.CENTER_LEFT);
                yield box;
            }
            case PR_VIEWPORT v -> {
                Control X = intSpinner(v.X, (observable, oldValue, newValue) -> {
                    v.X = newValue;
                });
                Control Y = intSpinner(v.Y, (observable, oldValue, newValue) -> {
                    v.Y = newValue;
                });
                Control WIDTH = intSpinner(v.WIDTH, (observable, oldValue, newValue) -> {
                    v.WIDTH = newValue;
                });
                Control HEIGHT =  intSpinner(v.HEIGHT, (observable, oldValue, newValue) -> {
                    v.HEIGHT = newValue;
                });
                X.setMaxWidth(67);
                Y.setMaxWidth(67);
                WIDTH.setMaxWidth(67);
                HEIGHT.setMaxWidth(67);
                GridPane grid = new GridPane();
                grid.setHgap(5);
                grid.add(new Label("X:"), 0, 0);
                grid.add(X, 1, 0);
                grid.add(new Label(" Y:"), 2, 0);
                grid.add(Y, 3, 0);
                grid.add(new Label("Width:"), 0, 1);
                grid.add(WIDTH, 1, 1);
                grid.add(new Label(" Height:"), 2, 1);
                grid.add(HEIGHT, 3, 1);
                grid.setAlignment(Pos.CENTER_LEFT);
                yield grid;
            }
            case ASPECTRATIO a -> {
                Control X = floatSpinner(a.X, (observable, oldValue, newValue) -> {
                    a.X = newValue.floatValue();
                });
                Control Y = floatSpinner(a.Y, (observable, oldValue, newValue) -> {
                    a.Y = newValue.floatValue();
                });
                X.setMaxWidth(96);
                Y.setMaxWidth(96);
                HBox box = new HBox(0, X, new Label(" X "), Y);
                box.setAlignment(Pos.BOTTOM_LEFT);
                yield box;
            }
            case FLOATCOLOR_RGB c -> {
                Control R = floatSpinner(c.R, (observable, oldValue, newValue) -> {
                    c.R = newValue.floatValue();
                });
                Control G = floatSpinner(c.G, (observable, oldValue, newValue) -> {
                    c.G = newValue.floatValue();
                });
                Control B = floatSpinner(c.B, (observable, oldValue, newValue) -> {
                    c.B = newValue.floatValue();
                });
                R.setMaxWidth(90);
                G.setMaxWidth(90);
                B.setMaxWidth(90);
                HBox box = new HBox(0,
                        new Label("R:"), R,
                        new Label(" G:"), G,
                        new Label(" B:"), B
                );
                box.setAlignment(Pos.CENTER_LEFT);
                yield box;
            }
            case WorldObjectFlags fl -> new VBox(0,
                    checkbox(fl.getVisible(), "Visible", (observable, oldValue, newValue) -> {
                        fl.setVisible(newValue);
                    })
            );
            case BITMAP_TEXTURE[] texArr -> TexturesDisplay.create(controller, item.getValue(), texArr, (DatingProfileEntry<BITMAP_TEXTURE[]>) entry);
            case SnSound sn -> SoundDisplay.create(controller, (DatingProfileEntry<SnSound>) entry);
            default -> new Label(value.toString());
        };
    }

    private static void emptyEvent(ObservableValue<?> observable, Object oldValue, Object newValue) {}

    private void markChanged(ObservableValue<?> observable, Object oldValue, Object newValue) {
        item.setGraphic(new Label("*"));
    }

    private TextField text(String s, ChangeListener<String> listener) {
        TextField text = new TextField(s);
        if (entry.readOnly()) {
            text.setEditable(false);
        } else {
            text.textProperty().addListener(listener);
            text.textProperty().addListener(this::markChanged);
        }
        return text;
    }

    private CheckBox checkbox(boolean b, ChangeListener<Boolean> listener) {
        return checkbox(b, "", listener);
    }

    private CheckBox checkbox(boolean b, String name, ChangeListener<Boolean> listener) {
        CheckBox check = new CheckBox(name);
        check.setSelected(b);
        if (entry.readOnly()) {
            check.setDisable(true);
        } else {
            check.selectedProperty().addListener(listener);
            check.selectedProperty().addListener(this::markChanged);
        }
        return check;
    }

    private Control intSpinner(int i, ChangeListener<Integer> listener) {
        if (entry.readOnly()) return text(Integer.toString(i), ObservableDatingValue::emptyEvent);
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, i));
        spinner.setEditable(true);
        spinner.valueProperty().addListener(listener);
        spinner.valueProperty().addListener(this::markChanged);
        return spinner;
    }

    private Control uintSpinner(long i, ChangeListener<Long> listener) {
        if (entry.readOnly()) text(Long.toString(i), ObservableDatingValue::emptyEvent);
        Spinner<Long> spinner = new Spinner<>();
        spinner.setValueFactory(new UIntSpinnerValueFactory(0L, 0xFFFFFFFFL, i));
        spinner.setEditable(true);
        spinner.valueProperty().addListener(listener);
        spinner.valueProperty().addListener(this::markChanged);
        return spinner;
    }

    private Control floatSpinner(float f, ChangeListener<Double> listener) {
        if (entry.readOnly()) return text(Float.toString(f), ObservableDatingValue::emptyEvent);
        Spinner<Double> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Float.MIN_VALUE, Float.MAX_VALUE, f));
        spinner.setEditable(true);
        spinner.valueProperty().addListener(listener);
        spinner.valueProperty().addListener(this::markChanged);
        return spinner;
    }
}
