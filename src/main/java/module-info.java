module dev.hipposgrumm.kamapreader {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jshell;
    requires java.desktop;

    opens dev.hipposgrumm.kamapreader to javafx.fxml;
    exports dev.hipposgrumm.kamapreader;
    exports dev.hipposgrumm.kamapreader.util;
    exports dev.hipposgrumm.kamapreader.util.control;
    exports dev.hipposgrumm.kamapreader.util.control.display;
    exports dev.hipposgrumm.kamapreader.util.types;
    exports dev.hipposgrumm.kamapreader.util.types.structs;
    exports dev.hipposgrumm.kamapreader.reader;
    exports dev.hipposgrumm.kamapreader.blocks;
    exports dev.hipposgrumm.kamapreader.blocks.subblock;
    exports dev.hipposgrumm.kamapreader.blocks.worldobjects;
}