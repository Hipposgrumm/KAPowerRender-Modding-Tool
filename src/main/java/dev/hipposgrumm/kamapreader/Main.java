package dev.hipposgrumm.kamapreader;

import dev.hipposgrumm.kamapreader.util.ViewerAppHandle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static boolean debugenv = false;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("main.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        ((FirstThing)loader.getController()).stage = stage;
        stage.setTitle("KAPowerRender Modding Tool");
        stage.setScene(scene);
        stage.show();

        if (debugenv) System.out.println("Hello Debugger! o/");
        else System.out.println("Hello o/");
    }

    public static void main(String[] args) {
        for (String arg:args) {
            if (arg.equals("debugenv")) {
                debugenv = true;
            }
        }

        launch();
    }

    @Override
    public void stop() {
        ViewerAppHandle.terminateProgram();
    }
}