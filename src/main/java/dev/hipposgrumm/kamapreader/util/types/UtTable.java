package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.Icon;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import dev.hipposgrumm.kamapreader.util.types.structs.PR_POINT;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UtString;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class UtTable implements DatingBachelor, DatingProfileValue {
    public static final int VERSION_BARBIE = 0;
    public static final int VERSION_BLASTER_AND_JUMPSTART = 1;
    // Note: If the size of a std::vector<T> exceeds 0x3FFFFFFF, the game
    //     will crash with a "vector<T> too long" error. Though you'd
    //     likely run into memory constraints long before then.
    public static final int VECTOR_MAX_SIZE = 0x3FFFFFFF;

    private final ArrayList<Column> columns = new ArrayList<>();
    private final ObservableList<Column> columnsObserved = FXCollections.observableList(columns);

    private final ObservableList<ColumnType> TABLE_COLUMNS = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(
            ColumnType.U32, ColumnType.U32_ARRAY,
            ColumnType.S32, ColumnType.S32_ARRAY,
            ColumnType.FLOAT, ColumnType.FLOAT_ARRAY,
            ColumnType.STRING, ColumnType.STRING_ARRAY,
            ColumnType.VECTOR3D, ColumnType.VECTOR3D_ARRAY,
            ColumnType.DICTIONARY, ColumnType.DICTIONARY_ARRAY,
            ColumnType.TABLE, ColumnType.TABLE_ARRAY,
            ColumnType.DATA, ColumnType.DATA_ARRAY
    ));

    protected UtTable() {}

    public Column getColumn(int column) {
        if (column >= columns.size()) throw new IndexOutOfBoundsException("Expected column #"+(column+1)+" from a "+ columnsObserved.size()+"-column table.");
        return columns.get(column);
    }

    public void ensureColumns(int num) {
        if (num > VECTOR_MAX_SIZE) throw new IllegalArgumentException("Requested table with more than "+VECTOR_MAX_SIZE+" columns. This would crash the game.");
        if (columns.size() >= num) return;
        columns.ensureCapacity(num);
        while (columns.size() < num) {
            columnsObserved.add(new Column(ColumnType.NONE));
        }
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        // Special handling is used for this type.
        return List.of();
    }

    @Override
    public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
        TableView<IntegerExpression> table = new TableView<>();
        fillTable(table, controller, onChanged, readonly);
        return table;
    }

    public void fillTable(TableView<IntegerExpression> table, FirstThing controller, Runnable onChanged, boolean readonly) {
        {
            TableColumn<IntegerExpression,Integer> rowNumColumn = new TableColumn<>();
            rowNumColumn.setPrefWidth(20.0);
            rowNumColumn.setCellFactory(factory -> new TableCell<>() {
                {
                    getStyleClass().setAll("column-header-background");
                    setStyle("-fx-font-weight: bold");
                    setAlignment(Pos.CENTER);
                }

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setHeight(0);
                        setText(null);
                    } else {
                        setHeight(-1);
                        if (item != null) setText(Integer.toString(item));
                    }
                }
            });
            rowNumColumn.setCellValueFactory(cdf -> cdf.getValue().asObject());
            table.getColumns().add(rowNumColumn);
        }

        int rowCount = 0;
        final int charOverlapPoint = ('Z'-'A'+1);
        for (int col = 0; col < this.columns.size(); col++) {
            TableColumn<IntegerExpression,Cell> tableColumn = new TableColumn<>();
            tableColumn.setMinWidth(150.0);
            Column column = this.columns.get(col);
            {
                Label name = new Label(
                        (char)((col % charOverlapPoint)+'A')+( // Initial Letter
                                (col >= charOverlapPoint) // Secondary number, if it's more than 1
                                        ? String.valueOf((col / charOverlapPoint) + 1)
                                        : ""
                        )
                );
                ComboBox<ColumnType> typeSelector = new ComboBox<>(TABLE_COLUMNS);
                name.setPrefHeight(25.0);
                name.prefHeightProperty().bind(typeSelector.heightProperty());
                typeSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
                    if (oldValue == newValue) return;
                    column.type.setValue(newValue);
                    table.refresh();
                });
                column.type.setCustomChangeListener((observable, oldValue, newValue) -> {
                    if (oldValue == newValue) return;
                    typeSelector.setValue(newValue);
                });
                typeSelector.setValue(column.type.getValue());
                typeSelector.setMaxWidth(180.0);
                HBox container = new HBox(5, name, typeSelector);
                container.setAlignment(Pos.CENTER);
                tableColumn.setGraphic(container);
            }
            tableColumn.setCellFactory(factory -> new TableCell<>() {
                @Override
                protected void updateItem(Cell item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setHeight(0);
                    } else {
                        setHeight(-1);
                        if (item == null) setGraphic(new Label("NULL"));
                        else {
                            setGraphic(switch (column.type.getValue()) {
                                case NONE -> new Label("NONE");
                                case U32 -> makeDisplayU32(item, controller, onChanged, readonly);
                                case U32_ARRAY -> new Label("U32 Array Placeholder");
                                case S32 -> makeDisplayS32(item, controller, onChanged, readonly);
                                case S32_ARRAY -> new Label("S32 Array Placeholder");
                                case FLOAT -> makeDisplayFloat(item, controller, onChanged, readonly);
                                case FLOAT_ARRAY -> new Label("Float Array Placeholder");
                                case STRING -> makeDisplayString(item, controller, onChanged, readonly);
                                case STRING_ARRAY -> new Label("UtString Array Placeholder");
                                case VECTOR3D -> makeDisplayVector(item, controller, onChanged, readonly);
                                case VECTOR3D_ARRAY -> new Label("MpVector3D Array Placeholder");
                                case DICTIONARY -> new Label("UtDict Placeholder");
                                case DICTIONARY_ARRAY -> new Label("UtDict Array Placeholder");
                                case TABLE -> new Label("UtTable Placeholder");
                                case TABLE_ARRAY -> new Label("UtTable Array Placeholder");
                                case DATA -> new Label("UtData Placeholder");
                                case DATA_ARRAY -> new Label("UtData Array Placeholder");
                            });
                        }
                    }
                }
            });
            int columnRows = column.rows.size();
            if (columnRows > rowCount) rowCount = columnRows;
            tableColumn.setCellValueFactory(cdf ->
                    column.rows.get(cdf.getValue().get()).getObservable()
            );
            table.getColumns().add(tableColumn);
        }
        for (int i=0;i<rowCount;i++) table.getItems().add(new IntegerExpression() {
            @Override public void addListener(ChangeListener<? super Number> listener) {}
            @Override public void addListener(InvalidationListener listener) {}
            @Override public void removeListener(ChangeListener<? super Number> listener) {}
            @Override public void removeListener(InvalidationListener listener) {}
            @Override public int get() {
                return table.getItems().indexOf(this);
            }
        });
    }

    @Override
    public void onDestroyDisplay(boolean isAppExit) {
        for (Column column:this.columns) {
            column.type.setCustomChangeListener(null);
        }
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public String toString() {
        return "Table";
    }

    /**
     * Reads a table from file.
     * @param version Resource version used for table.<br>
     *                0 - Barbie Fashion Show<br>
     *                1 - 3DVW, Math Blaster
     */
    public static UtTable readTable(BlockReader reader, int version) {
        UtTable table = new UtTable();
        int columnCount = reader.readInt();
        table.ensureColumns(columnCount);

        reader.readBytes(8); // unknown1
        for (int i=0;i<columnCount;i++) {
            reader.readInt(); // unknown2, probably flags
            int typeVal = reader.readInt();
            ColumnType type = typeFromVal(typeVal, version);

            int nextColumnOffset = reader.readInt();
            BlockReader columnReader = reader.segment(nextColumnOffset-8);
            int arrayType = getArrayType(type, version);
            Function<BlockReader, Object> func = switch (type) {
                case NONE -> throw new IllegalStateException("How did we get here?");
                case U32 -> BlockReader::readUInt;
                case U32_ARRAY -> cellreader -> {
                    cellreader = beginReadArray(cellreader, arrayType);
                    int[] array = new int[cellreader.readInt()];
                    for (int k=0;k<array.length;k++) {
                        array[k] = cellreader.readInt();
                    }
                    return new UInteger.Array(array);
                };
                case S32 -> BlockReader::readInt;
                case S32_ARRAY -> cellreader -> {
                    cellreader = beginReadArray(cellreader, arrayType);
                    int[] array = new int[cellreader.readInt()];
                    for (int k=0;k<array.length;k++) {
                        array[k] = cellreader.readInt();
                    }
                    return array;
                };
                case FLOAT -> BlockReader::readFloat;
                case FLOAT_ARRAY -> cellreader -> {
                    cellreader = beginReadArray(cellreader, arrayType);
                    float[] array = new float[cellreader.readInt()];
                    for (int k=0;k<array.length;k++) {
                        array[k] = cellreader.readFloat();
                    }
                    return array;
                };
                case STRING -> UtTable::readColumnString;
                case STRING_ARRAY -> cellreader -> {
                    cellreader = beginReadArray(cellreader, arrayType);
                    UtString[] array = new UtString[cellreader.readInt()];
                    for (int k=0;k<array.length;k++) {
                        array[k] = UtTable.readColumnString(cellreader);
                    }
                    return array;
                };
                case VECTOR3D -> UtTable::readColumnVector;
                case VECTOR3D_ARRAY -> cellreader -> {
                    cellreader = beginReadArray(cellreader, arrayType);
                    PR_POINT[] array = new PR_POINT[cellreader.readInt()];
                    for (int k=0;k<array.length;k++) {
                        array[k] = UtTable.readColumnVector(cellreader);
                    }
                    return array;
                };
                case DICTIONARY -> null;
                case DICTIONARY_ARRAY -> null;
                case TABLE -> null;
                case TABLE_ARRAY -> null;
                case DATA -> null;
                case DATA_ARRAY -> null;
            };

            int rowCount = columnReader.readInt();
            Column column = table.getColumn(i);
            column.ensureRows(rowCount);
            column.type.setValue(type);
            for (int j=0;j<rowCount;j++) {
                column.getCell(j).setValue(func.apply(columnReader));
            }
        }
        return table;
    }

    private static ColumnType typeFromVal(int val, int version) {
        return switch (version) {
            case VERSION_BARBIE -> switch (val) {
                case 0x01 -> ColumnType.U32;
                case 0x02 -> ColumnType.S32;
                case 0x03 -> ColumnType.FLOAT;
                case 0x04 -> ColumnType.STRING;
                case 0x05 -> ColumnType.VECTOR3D;
                case 0x06 -> ColumnType.DICTIONARY;
                case 0x07 -> ColumnType.TABLE;
                case 0x08 -> ColumnType.DATA;
                case 0x0C -> ColumnType.U32_ARRAY;
                case 0x0D -> ColumnType.S32_ARRAY;
                case 0x0E -> ColumnType.FLOAT_ARRAY;
                case 0x0F -> ColumnType.STRING_ARRAY;
                case 0x10 -> ColumnType.VECTOR3D_ARRAY;
                case 0x12 -> ColumnType.DICTIONARY_ARRAY;
                case 0x13 -> ColumnType.TABLE_ARRAY;
                case 0x14 -> ColumnType.DATA_ARRAY;
                default -> throw new IllegalStateException("Unknown UtVariant type: "+val);
            };
            case VERSION_BLASTER_AND_JUMPSTART -> switch (val) {
                case 0x02 -> ColumnType.U32;
                case 0x03 -> ColumnType.U32_ARRAY;
                case 0x04 -> ColumnType.S32;
                case 0x05 -> ColumnType.S32_ARRAY;
                case 0x06 -> ColumnType.FLOAT;
                case 0x07 -> ColumnType.FLOAT_ARRAY;
                case 0x08 -> ColumnType.STRING;
                case 0x09 -> ColumnType.STRING_ARRAY;
                case 0x0A -> ColumnType.VECTOR3D;
                case 0x0B -> ColumnType.VECTOR3D_ARRAY;
                case 0x0C -> ColumnType.DICTIONARY;
                case 0x0D -> ColumnType.DICTIONARY_ARRAY;
                case 0x0E -> ColumnType.TABLE;
                case 0x0F -> ColumnType.TABLE_ARRAY;
                case 0x10 -> ColumnType.DATA;
                case 0x11 -> ColumnType.DATA_ARRAY;
                default -> throw new IllegalStateException("Unknown UtVariant type: "+val);
            };
            default -> throw new IllegalStateException("Unsupported resource version: "+version);
        };
    }

    private static int valFromType(ColumnType type, int version) {
        return switch (version) {
            case VERSION_BARBIE -> switch (type) {
                case U32 -> 0x01;
                case S32 -> 0x02;
                case FLOAT -> 0x03;
                case STRING -> 0x04;
                case VECTOR3D -> 0x05;
                case DICTIONARY -> 0x06;
                case TABLE -> 0x07;
                case DATA -> 0x08;
                case U32_ARRAY -> 0x0C;
                case S32_ARRAY -> 0x0D;
                case FLOAT_ARRAY -> 0x0E;
                case STRING_ARRAY -> 0x0F;
                case VECTOR3D_ARRAY -> 0x10;
                case DICTIONARY_ARRAY -> 0x12;
                case TABLE_ARRAY -> 0x13;
                case DATA_ARRAY -> 0x14;
                default -> throw new IllegalStateException("Type \""+type+"\" cannot be saved.");
            };
            case VERSION_BLASTER_AND_JUMPSTART -> switch (type) {
                case U32 -> 0x02;
                case U32_ARRAY -> 0x03;
                case S32 -> 0x04;
                case S32_ARRAY -> 0x05;
                case FLOAT -> 0x06;
                case FLOAT_ARRAY -> 0x07;
                case STRING -> 0x08;
                case STRING_ARRAY -> 0x09;
                case VECTOR3D -> 0x0A;
                case VECTOR3D_ARRAY -> 0x0B;
                case DICTIONARY -> 0x0C;
                case DICTIONARY_ARRAY -> 0x0D;
                case TABLE -> 0x0E;
                case TABLE_ARRAY -> 0x0F;
                case DATA -> 0x10;
                case DATA_ARRAY -> 0x11;
                default -> throw new IllegalStateException("Type \""+type+"\" cannot be saved.");
            };
            default -> throw new IllegalStateException("Unsupported resource version: "+version);
        };
    }

    private static int getArrayType(ColumnType type, int version) {
        return switch (version) {
            case VERSION_BARBIE -> switch (type) {
                case U32_ARRAY -> 0x01;
                case S32_ARRAY -> 0x02;
                case FLOAT_ARRAY -> 0x03;
                case STRING_ARRAY -> 0x04;
                case VECTOR3D_ARRAY -> 0x05;
                case DICTIONARY_ARRAY -> 0x06;
                case TABLE_ARRAY -> 0x07;
                case DATA_ARRAY -> 0x08;
                default -> -1;
            };
            case VERSION_BLASTER_AND_JUMPSTART -> switch (type) {
                case U32_ARRAY -> 0x02;
                case S32_ARRAY -> 0x04;
                case FLOAT_ARRAY -> 0x06;
                case STRING_ARRAY -> 0x08;
                case VECTOR3D_ARRAY -> 0x0A;
                case DICTIONARY_ARRAY -> 0x0C;
                case TABLE_ARRAY -> 0x0E;
                case DATA_ARRAY -> 0x10;
                default -> -1;
            };
            default -> -1;
        };
    }

    private static BlockReader beginReadArray(BlockReader reader, int arrayType) {
        reader.readInt(); // unknown2, probably flags
        int checkedType = reader.readInt();
        if (checkedType != arrayType) throw new IllegalStateException("Type in array ("+checkedType+") does not expected type ("+arrayType+").");
        int nextOffset = reader.readInt();
        return reader.segment(nextOffset-12);
    }

    private static UtString readColumnString(BlockReader reader) {
        int endOffset = reader.readInt();
        reader = reader.segment((endOffset > 0)
                ? endOffset-4
                : reader.getRemaining()
        );
        int stringSize = reader.readInt();
        int encodingVal = reader.readInt();

        UtString.Encoding encoding = switch (encodingVal) {
            case 0 -> UtString.Encoding.US_ASCII;
            case 3 -> UtString.Encoding.UTF16;
            default -> {
                System.err.println("Read UtString with unknown encoding id " + encodingVal);
                yield UtString.Encoding.UNKNOWN;
            }
        };

        return new UtString(
                new String(reader.readBytes(stringSize), encoding.charset),
                stringSize / encoding.charsize,
                encoding
        );
    }

    private static PR_POINT readColumnVector(BlockReader reader) {
        PR_POINT point = new PR_POINT();
        point.X = reader.readFloat();
        point.Y = reader.readFloat();
        point.Z = reader.readFloat();
        return point;
    }

    private static Node makeDisplayU32(Cell item, FirstThing controller, Runnable onChanged, boolean readonly) {
        UIntWrapperWithHash val = item.getAsU32();
        AtomicReference<TextField> textref = new AtomicReference<>();
        String str = val.getString();
        boolean hasHashedString = str != null;
        Control intSpinner = DatingProfileValue.uintSpinner(val.getValue(), readonly, (observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) return;
            val.set(new UInteger((int) newValue.longValue()));
            textref.get().setText(val.getString());
            onChanged.run();
        });
        TextField text = DatingProfileValue.text(hasHashedString ? str : "", readonly, (observable, oldValue, newValue) -> {
            if (Objects.equals(newValue, oldValue)) return;
            if (newValue != null)
                //noinspection unchecked
                ((Spinner<Long>) intSpinner).getValueFactory().setValue(Long.valueOf(newValue));
            onChanged.run();
        });
        textref.set(text);
        Button swapper = new Button();
        AtomicBoolean hashedStringMode = new AtomicBoolean(!hasHashedString);
        swapper.setOnAction(event -> {
            boolean mode = !hashedStringMode.getPlain();
            hashedStringMode.setPlain(mode);
            swapper.setGraphic(mode
                    ? Icon.hashtag(15)
                    : Icon.text(15)
            );
            intSpinner.setVisible(!mode);
            intSpinner.setManaged(!mode);
            text.setVisible(mode);
            text.setManaged(mode);
        });
        swapper.fire(); // fire the action to do init
        return new HBox(intSpinner, text, swapper);
    }

    private static Node makeDisplayS32(Cell item, FirstThing controller, Runnable onChanged, boolean readonly) {
        return DatingProfileValue.intSpinner(item.getAsS32(), readonly, (observable, oldValue, newValue) -> {
            item.setValue(newValue.floatValue());
            onChanged.run();
        });
    }

    private static Node makeDisplayFloat(Cell item, FirstThing controller, Runnable onChanged, boolean readonly) {
        return DatingProfileValue.floatSpinner(item.getAsFloat(), readonly, (observable, oldValue, newValue) -> {
            item.setValue(newValue.floatValue());
            onChanged.run();
        });
    }

    private static Node makeDisplayString(Cell item, FirstThing controller, Runnable onChanged, boolean readonly) {
        TextField realText = (TextField) item.getAsString().createDisplay(controller, onChanged, readonly);
        realText.setVisible(false);
        realText.setManaged(false);
        TextArea displayedText = new TextArea();
        displayedText.setPrefHeight(60.0);
        displayedText.textProperty().bindBidirectional(realText.textProperty());
        return new VBox(realText, displayedText);
    }

    private static Node makeDisplayVector(Cell item, FirstThing controller, Runnable onChanged, boolean readonly) {
        PR_POINT point = item.getAsVector();
        Control X = DatingProfileValue.floatSpinner(point.X, readonly, (observable, oldValue, newValue) -> {
            point.X = newValue.floatValue();
            onChanged.run();
        });
        Control Y = DatingProfileValue.floatSpinner(point.Y, readonly, (observable, oldValue, newValue) -> {
            point.Y = newValue.floatValue();
            onChanged.run();
        });
        Control Z = DatingProfileValue.floatSpinner(point.Z, readonly, (observable, oldValue, newValue) -> {
            point.Z = newValue.floatValue();
            onChanged.run();
        });
        X.setMaxWidth(90);
        Y.setMaxWidth(90);
        Z.setMaxWidth(90);
        GridPane pane = new GridPane();
        pane.addRow(0, new Label("X:"), X);
        pane.addRow(1, new Label("Y:"), Y);
        pane.addRow(2, new Label("Z:"), Z);
        pane.setHgap(5.0);
        pane.setAlignment(Pos.CENTER_LEFT);
        return pane;
    }

    public static final class Column {
        final ArrayList<Cell> rows;
        public TypeObservable type;

        public Column(ColumnType type) {
            this.type = new TypeObservable(type);
            rows = new ArrayList<>(0);
        }

        public int getSize() {
            return rows.size();
        }

        public Cell getCell(int index) {
            if (index >= rows.size()) throw new IndexOutOfBoundsException("Expected row #"+(index+1)+" from a "+rows.size()+"-row column.");
            return rows.get(index);
        }

        public void ensureRows(int num) {
            if (num > VECTOR_MAX_SIZE) throw new IllegalArgumentException("Requested column with more than "+VECTOR_MAX_SIZE+" rows. This would crash the game.");

            rows.ensureCapacity(num);
            while (rows.size() < num) {
                rows.add(new Cell(null));
            }
        }

        public static final class TypeObservable extends ObservableValueBase<ColumnType> {
            private ColumnType type;
            private ChangeListener<? super ColumnType> customChangeListener;

            public TypeObservable(ColumnType type) {
                this.type = type;
            }

            @Override
            public ColumnType getValue() {
                return type;
            }

            public void setValue(ColumnType type) {
                this.type = type;
                fireValueChangedEvent();
            }

            public void setCustomChangeListener(ChangeListener<? super ColumnType> listener) {
                if (customChangeListener != null) removeListener(customChangeListener);
                if (listener != null) addListener(listener);
                customChangeListener = listener;
            }
        }
    }

    public static class Cell {
        private final ObservableValue<Cell> observable = new ObjectExpression<>() {
            @Override
            public Cell get() {
                return Cell.this;
            }

            @Override public void addListener(ChangeListener<? super Cell> listener) {}
            @Override public void addListener(InvalidationListener listener) {}
            @Override public void removeListener(ChangeListener<? super Cell> listener) {}
            @Override public void removeListener(InvalidationListener listener) {}
        };
        private Object value;

        public Cell(Object value) {
            setValue(value);
        }

        public void setValue(Object value) {
            this.value = switch (value) {
                case UInteger val -> new UIntWrapperWithHash(val);
                case UInteger.Array arr -> //noinspection RedundantCast
                        (Object)convertArray_int_to_T(arr.array(), UIntWrapperWithHash::new);
                case null, default -> value;
            };
        }

        public Object getValue() {
            return value;
        }

        public ObservableValue<Cell> getObservable() {
            return observable;
        }

        public UIntWrapperWithHash getAsU32() {
            if (hasEmptyValue()) return new UIntWrapperWithHash(new UInteger(0));

            Object value = switch (this.value) {
                case UtString str -> str.toString();
                case UtString[] arr -> convertArray_F_to_T(arr, UtString::toString);
                default -> this.value;
            };

            return switch (value) {
                case UIntWrapperWithHash val -> val;
                case UIntWrapperWithHash[] arr -> arr[0];

                case Integer val -> new UIntWrapperWithHash(val);
                case int[] arr -> new UIntWrapperWithHash(arr[0]);
                case Integer[] arr -> new UIntWrapperWithHash(arr[0]);

                case Float f -> new UIntWrapperWithHash(f.intValue());
                case float[] arr -> new UIntWrapperWithHash((int) arr[0]);
                case Float[] arr -> new UIntWrapperWithHash(arr[0].intValue());

                case String str -> {
                    try {
                        long l = (long) Double.parseDouble(str);
                        if (l <= 0xFFFFFFFFL) yield  new UIntWrapperWithHash((int) l);
                        else yield new UIntWrapperWithHash();
                    } catch (NumberFormatException e) {
                        yield new UIntWrapperWithHash();
                    }
                }
                case String[] arr -> {
                    try {
                        long l = (long) Double.parseDouble(arr[0]);
                        if (l <= 0xFFFFFFFFL) yield  new UIntWrapperWithHash((int) l);
                        else yield new UIntWrapperWithHash();
                    } catch (NumberFormatException e) {
                        yield new UIntWrapperWithHash();
                    }
                }

                case PR_POINT p -> new UIntWrapperWithHash((p.X >= 0f) ? (int) p.X : 0);
                case PR_POINT[] arr -> new UIntWrapperWithHash((arr[0].X >= 0f) ? (int) arr[0].X : 0);

                default -> new UIntWrapperWithHash();
            };
        }

        public UIntWrapperWithHash[] getAsU32Array() {
            if (hasEmptyValue()) return new UIntWrapperWithHash[0];

            Object value = switch (this.value) {
                case UtString str -> str.toString();
                case UtString[] arr -> convertArray_F_to_T(arr, UtString::toString);
                default -> this.value;
            };

            return switch (value) {
                case UIntWrapperWithHash val -> new UIntWrapperWithHash[] { val };
                case UIntWrapperWithHash[] arr -> arr;

                case Integer val -> new UIntWrapperWithHash[] { new UIntWrapperWithHash(val) };
                case int[] arr -> convertArray_int_to_T(arr, UIntWrapperWithHash::new);
                case Integer[] arr -> convertArray_F_to_T(arr, UIntWrapperWithHash::new);

                case Float f -> new UIntWrapperWithHash[] { new UIntWrapperWithHash(f.intValue()) };
                case float[] arr -> convertArray_float_to_T(arr, f -> new UIntWrapperWithHash((int) f));
                case Float[] arr -> convertArray_F_to_T(arr, f -> new UIntWrapperWithHash(f.intValue()));

                case String str -> {
                    try {
                        long l = (long) Double.parseDouble(str);
                        if (l <= 0xFFFFFFFFL) yield new UIntWrapperWithHash[] { new UIntWrapperWithHash((int) l) };
                        else yield new UIntWrapperWithHash[] { new UIntWrapperWithHash() };
                    } catch (NumberFormatException e) {
                        yield new UIntWrapperWithHash[] { new UIntWrapperWithHash() };
                    }
                }
                case String[] arr -> convertArray_F_to_T(arr, str -> {
                    try {
                        long l = (long) Double.parseDouble(str);
                        if (l <= 0xFFFFFFFFL) return new UIntWrapperWithHash((int) l);
                        else return new UIntWrapperWithHash();
                    } catch (NumberFormatException e) {
                        return new UIntWrapperWithHash();
                    }
                });

                case PR_POINT p -> new UIntWrapperWithHash[] { new UIntWrapperWithHash((p.X >= 0f) ? (int) p.X : 0) };
                case PR_POINT[] arr -> convertArray_F_to_T(arr, p -> new UIntWrapperWithHash((p.X >= 0f) ? (int) p.X : 0));

                default -> new UIntWrapperWithHash[] { new UIntWrapperWithHash() };
            };
        }

        public int getAsS32() {
            if (hasEmptyValue()) return 0;

            Object value = switch (this.value) {
                case UtString str -> str.toString();
                case UtString[] arr -> convertArray_F_to_T(arr, UtString::toString);
                default -> this.value;
            };

            return switch (value) {
                case UIntWrapperWithHash val -> val.getValue().innerVal();
                case UIntWrapperWithHash[] arr -> arr[0].getValue().innerVal();

                case Integer val -> val;
                case int[] arr -> arr[0];
                case Integer[] arr -> arr[0];

                case Float f -> f.intValue();
                case float[] arr -> (int) arr[0];
                case Float[] arr -> arr[0].intValue();

                case String str -> {
                    try {
                        yield (int) Float.parseFloat(str);
                    } catch (NumberFormatException e) {
                        yield 0;
                    }
                }
                case String[] arr -> {
                    try {
                        yield (int) Float.parseFloat(arr[0]);
                    } catch (NumberFormatException e) {
                        yield 0;
                    }
                }

                case PR_POINT p -> (p.X >= 0f) ? (int) p.X : 0;
                case PR_POINT[] arr -> (int) arr[0].X;

                default -> 0;
            };
        }

        public int[] getAsS32Array() {
            if (hasEmptyValue()) return new int[0];

            Object value = switch (this.value) {
                case UtString str -> str.toString();
                case UtString[] arr -> convertArray_F_to_T(arr, UtString::toString);
                default -> this.value;
            };

            return switch (value) {
                case UIntWrapperWithHash val -> new int[] { val.getValue().innerVal() };
                case UIntWrapperWithHash[] arr -> convertArray_F_to_int(arr, i -> i.getValue().innerVal());

                case Integer val -> new int[] { val };
                case int[] arr -> arr;
                case Integer[] arr -> convertArray_F_to_int(arr, Integer::intValue);

                case Float f -> new int[] { f.intValue() };
                case float[] arr -> convertArray_float_to_int(arr);
                case Float[] arr -> convertArray_F_to_int(arr, Float::intValue);

                case String str -> {
                    try {
                        yield new int[] { (int) Float.parseFloat(str) };
                    } catch (NumberFormatException e) {
                        yield new int[] { 0 };
                    }
                }
                case String[] arr -> convertArray_F_to_int(arr, str -> {
                    try {
                        return (int) Float.parseFloat(str);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });

                case PR_POINT p -> new int[] { (int) p.X };
                case PR_POINT[] arr -> convertArray_F_to_int(arr, p -> (int) p.X);

                default -> new int[] { 0 };
            };
        }

        public float getAsFloat() {
            if (hasEmptyValue()) return 0f;

            Object value = switch (this.value) {
                case UtString str -> str.toString();
                case UtString[] arr -> convertArray_F_to_T(arr, UtString::toString);
                default -> this.value;
            };

            return switch (value) {
                case UIntWrapperWithHash val -> val.getValue().innerVal();
                case UIntWrapperWithHash[] arr -> arr[0].getValue().innerVal();

                case Integer val -> val;
                case int[] arr -> arr[0];
                case Integer[] arr -> arr[0];

                case Float f -> f;
                case float[] arr -> arr[0];
                case Float[] arr -> arr[0];

                case String str -> {
                    try {
                        yield Float.parseFloat(str);
                    } catch (NumberFormatException e) {
                        yield 0f;
                    }
                }
                case String[] arr -> {
                    try {
                        yield Float.parseFloat(arr[0]);
                    } catch (NumberFormatException e) {
                        yield 0f;
                    }
                }

                case PR_POINT p -> p.X;
                case PR_POINT[] arr -> arr[0].X;

                default -> 0f;
            };
        }

        public float[] getAsFloatArray() {
            if (hasEmptyValue()) return new float[0];

            Object value = switch (this.value) {
                case UtString str -> str.toString();
                case UtString[] arr -> convertArray_F_to_T(arr, UtString::toString);
                default -> this.value;
            };

            return switch (value) {
                case UIntWrapperWithHash val -> new float[] { val.getValue().innerVal() };
                case UIntWrapperWithHash[] arr -> convertArray_F_to_float(arr, i -> i.getValue().innerVal());

                case Integer val -> new float[] { val };
                case int[] arr -> convertArray_int_to_float(arr);
                case Integer[] arr -> convertArray_F_to_float(arr, Integer::floatValue);

                case Float f -> new float[] { f };
                case float[] arr -> arr;
                case Float[] arr -> convertArray_F_to_float(arr, Float::floatValue);

                case String str -> {
                    try {
                        yield new float[] { Float.parseFloat(str) };
                    } catch (NumberFormatException e) {
                        yield new float[] { 0f };
                    }
                }
                case String[] arr -> convertArray_F_to_float(arr, str -> {
                    try {
                        return Float.parseFloat(str);
                    } catch (NumberFormatException e) {
                        return 0f;
                    }
                });

                case PR_POINT p -> new float[] { p.X };
                case PR_POINT[] arr -> convertArray_F_to_float(arr, p -> p.X);

                default -> new float[] { 0f };
            };
        }

        public UtString getAsString() {
            if (hasEmptyValue()) return new UtString("", 0x100);

            Object value;
            switch (this.value) {
                case UtString str -> { return str; }
                case UtString[] arr -> value = convertArray_F_to_T(arr, UtString::toString);
                default -> value = this.value;
            }

            return new UtString(switch (value) {
                case UIntWrapperWithHash val -> val.toString();
                case UIntWrapperWithHash[] arr -> Arrays.toString(arr);

                case Integer val -> val.toString();
                case int[] arr -> Arrays.toString(arr);
                case Integer[] arr -> Arrays.toString(arr);

                case Float f -> f.toString();
                case float[] arr -> Arrays.toString(arr);
                case Float[] arr -> Arrays.toString(arr);

                case String str -> str;
                case String[] arr -> Arrays.toString(arr);

                case PR_POINT p -> Float.toString(p.X);
                case PR_POINT[] arr -> Arrays.toString(convertArray_F_to_float(arr, p -> p.X));

                case Object[] arr -> Arrays.toString(arr);
                default -> value.toString();
            }, 0x100);
        }

        public UtString[] getAsStringArray() {
            if (hasEmptyValue()) return new UtString[0];

            Object value;
            switch (this.value) {
                case UtString str -> { return new UtString[] { str }; }
                case UtString[] arr -> value = convertArray_F_to_T(arr, UtString::toString);
                default -> value = this.value;
            }

            return convertArray_F_to_T(switch (value) {
                case UIntWrapperWithHash val -> new String[] { val.toString() };
                case UIntWrapperWithHash[] arr -> convertArray_F_to_T(arr, UIntWrapperWithHash::toString);

                case Integer val -> new String[] { val.toString() };
                case int[] arr -> convertArray_int_to_T(arr, i -> Integer.toString(i));
                case Integer[] arr -> convertArray_F_to_T(arr, Object::toString);

                case Float f -> new String[] { f.toString() };
                case float[] arr -> convertArray_float_to_T(arr, f -> Float.toString(f));
                case Float[] arr -> convertArray_F_to_T(arr, Object::toString);

                case String str -> new String[] { str };
                case String[] arr -> arr;

                case PR_POINT p -> new String[] { Float.toString(p.X) };
                case PR_POINT[] arr -> convertArray_F_to_T(arr, p -> Float.toString(p.X));

                case Object[] arr -> convertArray_F_to_T(arr, Object::toString);
                default ->  new String[] { value.toString() };
            }, str -> new UtString(str, 0x100));
        }

        public PR_POINT getAsVector() {
            if (hasEmptyValue()) return new PR_POINT();
            return switch (value) {
                case PR_POINT p -> p;
                case PR_POINT[] arr -> arr[0];
                default -> {
                    PR_POINT point = new PR_POINT();
                    point.X = getAsFloat();
                    yield point;
                }
            };
        }

        public PR_POINT[] getAsVectorArray() {
            if (hasEmptyValue()) return new PR_POINT[0];
            return switch (value) {
                case PR_POINT p -> new PR_POINT[] { p };
                case PR_POINT[] arr -> arr;
                default -> convertArray_float_to_T(getAsFloatArray(), f -> {
                    PR_POINT point = new PR_POINT();
                    point.X = f;
                    return point;
                });
            };

        }



        private boolean hasEmptyValue() {
            return switch (value) {
                case null -> true;
                case int[] arr -> arr.length == 0;
                case float[] arr -> arr.length == 0;
                case Object[] arr -> arr.length == 0;
                default -> false;
            };
        }

        @SuppressWarnings("unchecked")
        private static <F,T> T[] convertArray_F_to_T(F[] arr, Function<F, T> convert) {
            T[] vals = (T[]) new Object[arr.length];
            for (int i=0;i<arr.length;i++) vals[i] = convert.apply(arr[i]);
            return vals;
        }

        @FunctionalInterface private interface FunctionToInt<T> { int apply(T t); }
        private static <F> int[] convertArray_F_to_int(F[] arr, FunctionToInt<F> convert) {
            int[] vals = new int[arr.length];
            for (int i=0;i<arr.length;i++) vals[i] = convert.apply(arr[i]);
            return vals;
        }

        @FunctionalInterface private interface FunctionFromInt<R> { R apply(int t); }
        @SuppressWarnings("unchecked")
        private static <T> T[] convertArray_int_to_T(int[] arr, FunctionFromInt<T> convert) {
            T[] vals = (T[]) new Object[arr.length];
            for (int i=0;i<arr.length;i++) vals[i] = convert.apply(arr[i]);
            return vals;
        }

        @FunctionalInterface private interface FunctionToFloat<T> { float apply(T t); }
        private static <F> float[] convertArray_F_to_float(F[] arr, FunctionToFloat<F> convert) {
            float[] vals = new float[arr.length];
            for (int i=0;i<arr.length;i++) vals[i] = convert.apply(arr[i]);
            return vals;
        }

        @FunctionalInterface private interface FunctionFromFloat<R> { R apply(float t); }
        @SuppressWarnings("unchecked")
        private static <T> T[] convertArray_float_to_T(float[] arr, FunctionFromFloat<T> convert) {
            T[] vals = (T[]) new Object[arr.length];
            for (int i=0;i<arr.length;i++) vals[i] = convert.apply(arr[i]);
            return vals;
        }

        private static int[] convertArray_float_to_int(float[] arr) {
            int[] vals = new int[arr.length];
            for (int i=0;i<arr.length;i++) vals[i] = (int) arr[i];
            return vals;
        }

        private static float[] convertArray_int_to_float(int[] arr) {
            float[] vals = new float[arr.length];
            for (int i=0;i<arr.length;i++) vals[i] = arr[i]; // implicit cast
            return vals;
        }
    }

    public enum ColumnType {
        NONE("NONE"),

        U32("ULong"), U32_ARRAY("ULong[]"),
        S32("Long"), S32_ARRAY("Long[]"),
        FLOAT("Float"), FLOAT_ARRAY("Float[]"),
        STRING("UtString"), STRING_ARRAY("UtString[]"),
        VECTOR3D("MpVector3D"), VECTOR3D_ARRAY("MpVector3D[]"),

        DICTIONARY("UtDict"), DICTIONARY_ARRAY("UtDict[]"),
        TABLE("UtTable"), TABLE_ARRAY("UtTable[]"),
        DATA("UtData"), DATA_ARRAY("UtData[]");

        private final String name;
        ColumnType(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return name;
        }
    }

    public static final class UIntWrapperWithHash {
        private UInteger value;
        private String stringFromHash;

        UIntWrapperWithHash() {
            this.value = new UInteger(0);
            this.stringFromHash = null;
        }

        UIntWrapperWithHash(int value) {
            this(new UInteger(value));
        }

        UIntWrapperWithHash(UInteger value) {
            set(value);
        }

        public UInteger getValue() {
            return value;
        }

        public String getString() {
            return stringFromHash;
        }

        private void set(UInteger value) {
            this.value = value;
            this.stringFromHash = HashedStrings.tryFromHash(value.innerVal());
        }

        private void set(String string) {
            this.value = new UInteger(HashedStrings.fromString(string));
            this.stringFromHash = string;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
