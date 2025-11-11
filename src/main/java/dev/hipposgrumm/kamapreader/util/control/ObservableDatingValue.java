package dev.hipposgrumm.kamapreader.util.control;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.Icon;
import dev.hipposgrumm.kamapreader.util.control.display.SoundDisplay;
import dev.hipposgrumm.kamapreader.util.control.display.TexturesDisplay;
import dev.hipposgrumm.kamapreader.util.types.*;
import dev.hipposgrumm.kamapreader.util.types.structs.*;
import dev.hipposgrumm.kamapreader.util.types.wrappers.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicReference;

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
            case UniqueIdentifier s -> {
                AtomicReference<TextField> text = new AtomicReference<>();
                text.set(text(s.toString(), (observable, oldValue, newValue) -> {
                    String val = oldValue;
                    try {
                        s.set(Integer.parseInt(newValue, 16));
                        val = newValue.toUpperCase();
                    } catch (NumberFormatException ignored) {}
                    text.get().setText(val);
                }));
                yield text.get();
            }
            case SizeLimitedString s -> {
                AtomicReference<TextField> text = new AtomicReference<>();
                text.set(text(s.toString(), (observable, oldValue, newValue) -> {
                    if (newValue.length() > s.getSize()) {
                        newValue = newValue.substring(0, s.getSize());
                        text.get().setText(newValue);
                    }
                    s.setString(newValue);
                }));
                yield text.get();
            }
            case TextFileString s -> {
                TextArea text = new TextArea(s.getContents());
                if (entry.readOnly()) {
                    text.setEditable(false);
                } else {
                    text.textProperty().addListener((observable, oldValue, newValue) -> {
                        s.setContents(newValue);
                    });
                    text.textProperty().addListener(this::markChanged);
                }

                Button saveButton = new Button("Save File", Icon.export());
                saveButton.setOnAction(event -> {
                    try {
                        String name = s.getName();
                        int extPos = name.lastIndexOf('.');
                        String ext = extPos >= 0 ?
                                name.substring(extPos) :
                                ".txt";
                        File file = controller.popupSaveFile("Save File", extPos >= 0 ? name : name+".txt", "Text file ("+ext.substring(1).toUpperCase()+")", ext);
                        if (file == null) return;
                        if (!file.getName().endsWith(ext)) file = new File(file.getPath()+ext);
                        if (!file.createNewFile() && !controller.popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?")) return;

                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            outputStream.write(s.getContents().getBytes());
                        }
                    } catch (Exception e) {
                        controller.popupError("Error Saving", "An exception was thrown when saving.", e);
                    }
                });
                yield new VBox(text, saveButton);
            }
            case Boolean b -> checkbox(b, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Boolean>) entry).set(newValue);
            });
            case Byte b -> byteSpinner(b, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Integer>) entry).set(newValue);
            });
            case UByte b -> ubyteSpinner(b, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Integer>) entry).set(newValue);
            });
            case Short s -> shortSpinner(s, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Integer>) entry).set(newValue);
            });
            case UShort s -> ushortSpinner(s, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Integer>) entry).set(newValue);
            });
            case Integer i -> intSpinner(i, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Integer>) entry).set(newValue);
            });
            case UInteger i -> uintSpinner(i, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Long>) entry).set(newValue);
            });
            case Float f -> floatSpinner(f, (observable, oldValue, newValue) -> {
                ((DatingProfileEntry<Float>) entry).set(newValue.floatValue());
            });
            case EnumChoices e -> {
                // This is gonna explode I can sense it.
                ComboBox<? extends Enum<?>> dropdown = new ComboBox<>(FXCollections.observableList(e.choices()));
                ((ComboBox<Enum<?>>) dropdown).setValue(e.getSelf()); // TODO: Does this actually work properly?
                if (entry.readOnly()) {
                    dropdown.setDisable(true);
                } else {
                    dropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
                        ((DatingProfileEntry<Enum<?>>) entry).set(newValue);
                    });
                    dropdown.valueProperty().addListener(this::markChanged);
                }
                yield dropdown;
            }
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
                double width = 90;
                HBox box = new HBox(0,
                        new Label("R:"), R,
                        new Label(" G:"), G,
                        new Label(" B:"), B
                );
                if (c instanceof FLOATCOLOR_RGBA cc) {
                    Control A = floatSpinner(cc.A, (observable, oldValue, newValue) -> {
                        c.B = newValue.floatValue();
                    });
                    width = 63;
                    A.setMaxWidth(width);
                    box.getChildren().addAll(new Label(" A:"), A);
                }
                R.setMaxWidth(width);
                G.setMaxWidth(width);
                B.setMaxWidth(width);
                box.setAlignment(Pos.CENTER_LEFT);
                yield box;
            }
            case COLOR_RGBA c -> {
                Control R = ubyteSpinner(new UByte((short) c.getRed()), (observable, oldValue, newValue) -> {
                    c.setRed(newValue);
                });
                Control G = ubyteSpinner(new UByte((short) c.getGreen()), (observable, oldValue, newValue) -> {
                    c.setGreen(newValue);
                });
                Control B = ubyteSpinner(new UByte((short) c.getBlue()), (observable, oldValue, newValue) -> {
                    c.setBlue(newValue);
                });
                Control A = ubyteSpinner(new UByte((short) c.getAlpha()), (observable, oldValue, newValue) -> {
                    c.setAlpha(newValue);
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
                yield box;
            }
            case Flags fl -> {
                VBox box = new VBox();
                for (int i=0;i<32;i++) {
                    String name = fl.getName(i);
                    if (name != null) {
                        final int lambdaSafeIndex = i;
                        box.getChildren().add(checkbox(fl.get(i), name, (observable, oldValue, newValue) -> {
                            fl.set(lambdaSafeIndex, newValue);
                        }));
                    }
                }
                yield box;
            }
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

    private Control byteSpinner(byte b, ChangeListener<Integer> listener) {
        if (entry.readOnly()) return text(Byte.toString(b), ObservableDatingValue::emptyEvent);
        return spinner(listener, new SpinnerValueFactory.IntegerSpinnerValueFactory(Byte.MIN_VALUE, Byte.MAX_VALUE, b));
    }

    private Control ubyteSpinner(UByte b, ChangeListener<Integer> listener) {
        if (entry.readOnly()) return text(Short.toString(b.get()), ObservableDatingValue::emptyEvent);
        return spinner(listener, new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0xFF, b.get()));
    }

    private Control shortSpinner(short s, ChangeListener<Integer> listener) {
        if (entry.readOnly()) return text(Short.toString(s), ObservableDatingValue::emptyEvent);
        return spinner(listener, new SpinnerValueFactory.IntegerSpinnerValueFactory(Short.MIN_VALUE, Short.MAX_VALUE, s));
    }

    private Control ushortSpinner(UShort s, ChangeListener<Integer> listener) {
        if (entry.readOnly()) return text(Integer.toString(s.get()), ObservableDatingValue::emptyEvent);
        return spinner(listener, new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0xFFFF, s.get()));
    }

    private Control intSpinner(int i, ChangeListener<Integer> listener) {
        if (entry.readOnly()) return text(Integer.toString(i), ObservableDatingValue::emptyEvent);
        return spinner(listener, new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, i));

    }

    private Control uintSpinner(UInteger i, ChangeListener<Long> listener) {
        if (entry.readOnly()) text(Long.toString(i.get()), ObservableDatingValue::emptyEvent);
        return spinner(listener, new LongSpinnerValueFactory(0L, 0xFFFFFFFFL, i.get()));
    }

    private Control floatSpinner(float f, ChangeListener<Double> listener) {
        if (entry.readOnly()) return text(Float.toString(f), ObservableDatingValue::emptyEvent);
        return spinner(listener, new SpinnerValueFactory.DoubleSpinnerValueFactory(Float.MIN_VALUE, Float.MAX_VALUE, f));
    }

    private <T> Control spinner(ChangeListener<T> listener, SpinnerValueFactory<T> factory) {
        Spinner<T> spinner = new Spinner<>();
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        spinner.valueProperty().addListener(listener);
        spinner.valueProperty().addListener(this::markChanged);
        return spinner;
    }
}
