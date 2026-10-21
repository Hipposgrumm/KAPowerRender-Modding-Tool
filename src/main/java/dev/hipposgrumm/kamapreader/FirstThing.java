package dev.hipposgrumm.kamapreader;

import dev.hipposgrumm.kamapreader.reader.KADataFile;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.reader.KASFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.PlaceholderValue;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.control.ObservableDatingValue;
import dev.hipposgrumm.kamapreader.util.control.ProgressPopup;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class FirstThing implements Initializable {
    public static Consumer<File[]> onFilesDroppedHook = null;

    Window stage;
    private KADataFile kaDataFile;
    private String file_chooser_last = System.getProperty("user.home");
    private FileChooser file_chooser;
    private DirectoryChooser directory_chooser;

    @FXML private MenuBar menuBar;
    @FXML private HBox dragTarget;
    @FXML public TreeView<DatingBachelor> tree;
    @FXML public BachelorTreeItem treeRoot;
    @FXML public TableView<ObservableDatingValue> table;
    public TableColumn<ObservableDatingValue,String> tableName;
    public TableColumn<ObservableDatingValue,Node> tableValue;

    private final Dialog<String> helpPopup = new Dialog<>();
    private final Dialog<ButtonType> question = new Dialog<>();
    private final Dialog<String> notice = new Dialog<>();
    private final Dialog<String> popupError = new Dialog<>();
    private final TextArea errorTextbox = new TextArea();

    private boolean keyListenerInitialized = false;
    private Consumer<KeyEvent> keyListener = null;

    @FXML @Override
    public void initialize(URL location, ResourceBundle resources) {
        file_chooser = new FileChooser();
        file_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Unset File Type", "*")
        );
        directory_chooser = new DirectoryChooser();
        onFilesDroppedHook = this::onFilesDragged;

        helpPopup.setTitle("Help");
        helpPopup.setHeaderText("This is the Help menu.");
        helpPopup.setContentText("You're welcome.\n// TODO: Replace this joke with an actual help menu.");
        helpPopup.getDialogPane().getButtonTypes().add(new ButtonType("Thanks!", ButtonBar.ButtonData.CANCEL_CLOSE));
        ButtonType unhelpfulButton = new ButtonType("No, this is not help!", ButtonBar.ButtonData.CANCEL_CLOSE);
        helpPopup.getDialogPane().getButtonTypes().add(unhelpfulButton);
        helpPopup.getDialogPane().lookupButton(unhelpfulButton).setDisable(true);

        question.getDialogPane().getButtonTypes().add(new ButtonType("Yes",ButtonBar.ButtonData.YES));
        question.getDialogPane().getButtonTypes().add(new ButtonType("No",ButtonBar.ButtonData.NO));

        notice.getDialogPane().getButtonTypes().add(new ButtonType("Ok",ButtonBar.ButtonData.CANCEL_CLOSE));

        popupError.getDialogPane().getButtonTypes().add(new ButtonType("Ok",ButtonBar.ButtonData.CANCEL_CLOSE));
        popupError.getDialogPane().setContent(errorTextbox);
        errorTextbox.setEditable(false);

        menuBar.setFocusTraversable(true);

        tableName = new TableColumn<>("Name");
        tableName.setMinWidth(100);
        tableName.setCellValueFactory(cdf -> cdf.getValue().nameProperty);
        tableName.setSortable(false);
        tableName.setReorderable(false);
        tableValue = new TableColumn<>("Value");
        tableValue.setMinWidth(320);
        tableValue.setCellValueFactory(TableColumn.CellDataFeatures::getValue);
        tableValue.setSortable(false);
        tableValue.setReorderable(false);
        table.setSelectionModel(null);
        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            selectElement(newValue);
        });
        tree.setCellFactory(treeView -> new BachelorTreeCell(this));

        dragTarget.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dragTarget.setOnDragDropped(event -> {
            if (event.getDragboard().hasFiles()) {
                onFilesDragged(event.getDragboard().getFiles().toArray(new File[0]));
            }
            event.setDropCompleted(event.getDragboard().hasFiles());
            event.consume();
        });
    }

    private void cleanupTableItems() {
        for (ObservableDatingValue value:table.getItems()) value.decommission();
        table.getItems().clear();
    }

    private void selectElement(TreeItem<DatingBachelor> value) {
        DatingBachelor bachelor = value.getValue();
        if (bachelor == null) return;

        cleanupTableItems();
        table.getColumns().clear();
        double maxwidth = tableValue.getMaxWidth();
        tableValue.setMaxWidth(tableValue.getMinWidth());
        tableValue.setMaxWidth(maxwidth);

        if (bachelor instanceof PlaceholderValue) return;

        bachelor.onSelected();
        if (value instanceof BachelorTreeItem item && item.entries != null) {
            table.getColumns().add(tableName);
            table.getColumns().add(tableValue);
            for (DatingProfileEntry entry:item.entries) {
                table.getItems().add(new ObservableDatingValue(this, value, entry));
            }
        }
    }

    private static final String FILE_OPEN_EXT_DESC = String.join(", ", KARFile.FILE_DESCRIPTOR, KASFile.FILE_DESCRIPTOR);
    private static final String[] FILE_OPEN_EXT_LIST = new String[] { KARFile.FILE_EXT, KASFile.FILE_EXT };

    @FXML
    protected void load() {
        File f = popupOpenFile("Open", null, FILE_OPEN_EXT_DESC, FILE_OPEN_EXT_LIST);
        if (f != null) doLoad(f);
    }

    private void onFilesDragged(File[] files) {
        if (Platform.isFxApplicationThread()) {
            if (files.length == 0) return;
            File file = files[0];

            String last = file.getParent();
            if (last != null) file_chooser_last = last;

            doLoad(file);
        } else Platform.runLater(() -> {
            if (files.length == 0) return;
            File file = files[0];

            String last = file.getParent();
            if (last != null) file_chooser_last = last;

            doLoad(file);
        });
    }

    private void doLoad(File f) {
        clearPreview();
        kaDataFile = null;
        try {
            KADataFile.LoadFile<?> task = KADataFile.getLoadFunction(f);
            if (task == null) return;
            try {
                openProgress(task);
                task.valueProperty().addListener((observable, oldVal, newVal) -> {
                    if (newVal == null) return;
                    kaDataFile = newVal;
                    setPreview();
                });
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
                new RuntimeException("Unable to load progress popup.", e).printStackTrace();
                // Load without progressbar.
                kaDataFile = task.get();
                setPreview();
            }
        } catch (Exception e) {
            popupError("Error", "Could not load file", e);
        }
    }

    private void openProgress(Task<?> task) throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Progress");
        stage.initOwner(this.stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("progress.fxml"));
        Scene scene = new Scene(loader.load(), 300, 150);
        stage.setScene(scene);
        stage.show();

        ProgressPopup popup = loader.getController();
        popup.status.textProperty().bind(task.messageProperty());
        popup.progress.progressProperty().bind(task.progressProperty());

        task.stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case SUCCEEDED, FAILED, CANCELLED -> stage.close();
            }
        });
    }

    @FXML
    protected void helpButton(ActionEvent event) {
        helpPopup.showAndWait();
    }

    @FXML
    protected void save(ActionEvent actionEvent) {
        if (kaDataFile == null) return;
        File path = popupSaveFile("Save", kaDataFile.filename.toString(), kaDataFile.fileDescriptor(), kaDataFile.fileChooserExt());
        if (path == null) {
            popupNotice("Save Cancelled", "Cancelled saving.", "You didn't select a save location.");
            return;
        }
        try {
            if (path.createNewFile() || popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?")) {
                KADataFile.SaveFile<?> task = kaDataFile.getSaveFunction(path);
                try {
                    openProgress(task);
                    task.stateProperty().addListener((obs, oldState, newState) -> {
                        switch (newState) {
                            case SUCCEEDED -> popupNotice("Saved", "File saved.", "The file was saved.");
                            case FAILED -> {
                                Throwable error = task.getException();
                                if (error != null) popupError("Error Saving", "An exception was thrown when saving.", error);
                                else popupNotice("Failure!", "Task reported uncaught failure without throwing an exception.", "The file may not have been saved.");
                            }
                        }
                    });
                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                } catch (IOException e) {
                    new RuntimeException("Unable to load progress popup.", e).printStackTrace();
                    // Run without progressbar.
                    task.run();
                    popupNotice("Saved", "File saved.", "The file was saved.");
                }
            }
        } catch (Exception e) {
            popupError("Error Saving", "An exception was thrown when saving.", e);
        }
    }

    private void setupFileDialog(String title, String defaultName, String extDesc, String... ext) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(extDesc, ext);
        file_chooser.getExtensionFilters().set(1, filter);
        file_chooser.setSelectedExtensionFilter(filter);
        file_chooser.setInitialFileName(defaultName);
        file_chooser.setTitle(title);
        file_chooser.setInitialDirectory(new File(file_chooser_last));
    }

    public File popupOpenFile(String title, String defaultName, String extDesc, String... ext) {
        setupFileDialog(title, defaultName, extDesc, ext);
        File f = file_chooser.showOpenDialog(stage);

        if (f != null) {
            String last = f.getParent();
            if (last != null) file_chooser_last = last;
        }

        return f;
    }

    public File popupSaveFile(String title, String defaultName, String extDesc, String... ext) {
        setupFileDialog(title, defaultName, extDesc, ext);
        File f = file_chooser.showSaveDialog(stage);

        if (f != null) {
            String last = f.getParent();
            if (last != null) file_chooser_last = last;
        }

        return f;
    }

    public File popupSaveDirectory(String title) {
        directory_chooser.setTitle(title);
        directory_chooser.setInitialDirectory(new File(file_chooser_last));
        File f = directory_chooser.showDialog(stage);

        if (f != null) {
            String last = f.getParent();
            if (last != null) file_chooser_last = last;
        }

        return f;
    }

    public boolean popupQuestion(String title, String header, String message) {
        question.setTitle(title);
        question.setHeaderText(header);
        question.setContentText(message);
        Optional<ButtonType> response = question.showAndWait();
        return response.isPresent() && response.get().getButtonData().isCancelButton();
    }

    public void popupNotice(String title, String header, String message) {
        notice.setTitle(title);
        notice.setHeaderText(header);
        notice.setContentText(message);
        notice.show();
    }

    public void popupError(String title, String message, Throwable error) {
        StringWriter string = new StringWriter();
        error.printStackTrace(new PrintWriter(string));
        errorTextbox.setText(string.toString());

        popupError.setTitle(title);
        popupError.setHeaderText(message);
        popupError.show();
    }

    private void clearPreview() {
        treeRoot.getChildren().clear();
        cleanupTableItems();
        table.getColumns().clear();
        ((Label) table.getPlaceholder()).setText("Select an element in the tree to edit it.");
    }

    private void setPreview() {
        treeRoot.setValue(kaDataFile);
        treeRoot.isModified = false;
        treeRoot.doUpdate();
        switch (kaDataFile) {
            case KARFile kar -> kar.blocks.forEach(b -> addTreeItem(treeRoot, b));
            default -> {} // make compiler happy
        }
        selectElement(treeRoot);
    }

    private void addTreeItem(TreeItem<DatingBachelor> base, DatingBachelor item) {
        TreeItem<DatingBachelor> added = new BachelorTreeItem(item);
        base.getChildren().add(added);
        try {
            if (item instanceof PlaceholderValue) return;
            List<? extends DatingBachelor> subs = item.getSubBachelors();
            if (subs != null)
                for (DatingBachelor sub:subs)
                    addTreeItem(added, sub);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class BachelorTreeCell extends TreeCell<DatingBachelor> {
        private final FirstThing controller;

        public BachelorTreeCell(FirstThing controller) {
            this.controller = controller;
            cellChanged(null, null, getTreeItem());
            treeItemProperty().addListener(this::cellChanged);
        }

        private void cellChanged(ObservableValue<? extends TreeItem<DatingBachelor>> observable, TreeItem<DatingBachelor> oldVal, TreeItem<DatingBachelor> newVal) {
            if (oldVal instanceof BachelorTreeItem item) item.cell = null;
            if (newVal instanceof BachelorTreeItem item) item.cell = this;
        }

        @Override
        protected void updateItem(DatingBachelor item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            } else if (item instanceof PlaceholderValue) {
                setText((getTreeItem() instanceof BachelorTreeItem treeitem && treeitem.isModified)
                        ? "* PLACEHOLDER"
                        : "PLACEHOLDER"
                );
                setContextMenu(new ContextMenu());
                return;
            }

            String label = item.toString();
            if (getTreeItem() instanceof BachelorTreeItem treeitem && treeitem.isModified) {
                label = "* "+label;
            }

            setGraphic(getTreeItem().getGraphic());
            setText(label);
            ContextMenu menu = new ContextMenu();
            for (DatingBachelor.ContextMenuOption option:item.getContextMenu()) {
                MenuItem menuItem = new MenuItem(option.name());
                menuItem.setOnAction(e -> {
                    List<TreeItem<DatingBachelor>> items = getTreeView().selectionModelProperty().get().getSelectedItems();
                    DatingBachelor[] objects = new DatingBachelor[items.size()];
                    for (int i=0;i<objects.length;i++) objects[i] = items.get(i).getValue();
                    option.function().accept(controller, objects);
                });
                menu.getItems().add(menuItem);
            }
            setContextMenu(menu);
        }
    }

    public static class BachelorTreeItem extends TreeItem<DatingBachelor> {
        List<? extends DatingProfileEntry> entries;
        BachelorTreeCell cell;
        public boolean isModified;

        public BachelorTreeItem(DatingBachelor item) {
            super(item);
            valueProperty().addListener(this::onValueUpdated);
            if (item != null) onValueUpdated(null, null, item);
        }

        protected void onValueUpdated(ObservableValue<? extends DatingBachelor> observable, DatingBachelor oldVal, DatingBachelor newVal) {
            if (newVal == null) this.entries = null;
            else if (newVal instanceof PlaceholderValue) this.entries = null;
            else this.entries = newVal.getDatingProfile();
        }

        public void doUpdate() {
            if (cell != null) {
                DatingBachelor value = getValue();
                cell.updateItem(value, value == null);
            }
        }
    }
}