package dev.hipposgrumm.kamapreader;

import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.control.ObservableDatingValue;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.*;
import java.net.URL;
import java.util.*;

public class FirstThing implements Initializable {
    Window stage;
    private KARFile karFile;
    private String file_chooser_last = System.getProperty("user.home");
    private FileChooser file_chooser;

    @FXML private MenuBar menuBar;
    @FXML private HBox dragTarget;
    @FXML public TreeView<DatingBachelor> tree;
    @FXML public TreeItem<DatingBachelor> treeRoot;
    @FXML public TableView<Pair<TreeItem<?>,DatingProfileEntry<?>>> table;
    public TableColumn<Pair<TreeItem<?>,DatingProfileEntry<?>>,String> tableName;
    public TableColumn<Pair<TreeItem<?>,DatingProfileEntry<?>>,Node> tableValue;

    private final Dialog<String> helpPopup = new Dialog<>();
    private final Dialog<ButtonType> question = new Dialog<>();
    private final Dialog<String> notice = new Dialog<>();
    private final Dialog<String> popupError = new Dialog<>();
    private final TextArea errorTextbox = new TextArea();

    @FXML @Override
    public void initialize(URL location, ResourceBundle resources) {
        file_chooser = new FileChooser();

        helpPopup.setTitle("Help");
        helpPopup.setHeaderText("This is the Help menu.");
        helpPopup.setContentText("You're welcome.");
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
        tableName.setCellValueFactory((cdf) -> new SimpleStringProperty(cdf.getValue().getValue().getName()));
        tableName.setSortable(false);
        tableName.setReorderable(false);
        tableValue = new TableColumn<>("Value");
        tableValue.setMinWidth(320);
        tableValue.setCellValueFactory(cbf -> new ObservableDatingValue(this, cbf));
        tableValue.setSortable(false);
        tableValue.setReorderable(false);
        table.setSelectionModel(null);
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            selectElement(newValue);
        });

        dragTarget.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dragTarget.setOnDragDropped(event -> {
            if (event.getDragboard().hasFiles()) {
                doLoad(event.getDragboard().getFiles().get(0));
            }
            event.setDropCompleted(event.getDragboard().hasFiles());
            event.consume();
        });
    }

    public void selectElement(TreeItem<DatingBachelor> value) {
        DatingBachelor element = value.getValue();
        if (element == null) return;
        if (element instanceof TreeBase) return;
        table.getItems().clear();
        table.getColumns().clear();
        double maxwidth = tableValue.getMaxWidth();
        tableValue.setMaxWidth(tableValue.getMinWidth());
        tableValue.setMaxWidth(maxwidth);
        List<? extends DatingProfileEntry<?>> entries = element.getDatingProfile();
        if (entries != null) {
            table.getColumns().add(tableName);
            table.getColumns().add(tableValue);
            for (DatingProfileEntry<?> entry : entries)
                table.getItems().add(new Pair<>(value, entry));
        }
    }

    @FXML
    protected void load() {
        File f = popupOpenFile("Open", null, "KAResource", ".kar");
        if (f != null) doLoad(f);
    }

    private void doLoad(File f) {
        try {
            karFile = new KARFile(f);
            setPreview();
        } catch (IOException e) {
            popupError("Error", "Could not load file", e);
        }
    }

    @FXML
    protected void helpButton(ActionEvent event) {
        helpPopup.showAndWait();
    }

    @FXML
    protected void handleKeyInput(KeyEvent event) {
        if (event.isControlDown() && event.getCode().equals(KeyCode.S))
            save(null);
    }

    @FXML
    protected void save(ActionEvent actionEvent) {
        if (karFile == null) {

            return;
        }
        File path = popupSaveFile("Save", karFile.file.getName(), "KAResource", ".kar");
        if (path == null) {
            popupNotice("Save Cancelled", "Cancelled saving.", "You didn't select a save location.");
            return;
        }
        try {
            if (path.createNewFile() || popupQuestion("Overwrite Warning", "This file already exists!", "Would you like to overwrite the file?")) {
                karFile.save(path);
                popupNotice("Saved", "File saved.", "The file was saved.");
            }
        } catch (Exception e) {
            popupError("Error Saving", "An exception was thrown when saving.", e);
        }
    }

    public File popupOpenFile(String title, String defaultName, String extDesc, String... ext) {
        file_chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(extDesc, ext));
        file_chooser.setInitialFileName(defaultName);
        file_chooser.setTitle(title);
        file_chooser.setInitialDirectory(new File(file_chooser_last));
        File f = file_chooser.showOpenDialog(stage);

        if (f != null) {
            String last = f.getParent();
            if (last != null) file_chooser_last = last;
        }

        return f;
    }

    public File popupSaveFile(String title, String defaultName, String extDesc, String... ext) {
        file_chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(extDesc, ext));
        file_chooser.setInitialFileName(defaultName);
        file_chooser.setTitle(title);
        file_chooser.setInitialDirectory(new File(file_chooser_last));
        File f = file_chooser.showSaveDialog(stage);

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

    public void popupError(String title, String message, Exception error) {
        StringWriter string = new StringWriter();
        error.printStackTrace(new PrintWriter(string));
        errorTextbox.setText(string.toString());

        popupError.setTitle(title);
        popupError.setHeaderText(message);
        popupError.show();
    }

    private void setPreview() {
        treeRoot.setValue(new TreeBase(karFile.file.getName()));
        treeRoot.getChildren().clear();
        table.getItems().clear();
        table.getColumns().clear();
        ((Label) table.getPlaceholder()).setText("Select an element in the tree to edit it.");
        karFile.blocks.forEach(b -> addTreeItem(treeRoot, b));
    }

    private void addTreeItem(TreeItem<DatingBachelor> base, DatingBachelor item) {
        TreeItem<DatingBachelor> added = new TreeItem<>(item);
        base.getChildren().add(added);
        List<? extends DatingBachelor> subs = item.getSubBachelors();
        if (subs != null)
            for (DatingBachelor sub:subs)
                addTreeItem(added, sub);
    }

    private record TreeBase(String name) implements DatingBachelor {
        @Override
        public String toString() {
            return name;
        }

        @Override
        public List<? extends DatingProfileEntry<?>> getDatingProfile() {
            return null;
        }
    }
}