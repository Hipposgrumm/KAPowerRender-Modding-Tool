package dev.hipposgrumm.kamapviewer.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import dev.hipposgrumm.kamapviewer.Main;
import dev.hipposgrumm.kamapviewer.util.ReaderAppConnection;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired(args)) return; // This handles macOS support and helps on Windows.
        createLuigiApplication(args);
        ReaderAppConnection.sendTermination();
    }

    private static Lwjgl3Application createLuigiApplication(String[] args) {
        // This shows as an error in the IDE, however, it still runs apparently.
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(Main.WINDOW_TITLE);
        // Vsync limits the frames per second to what your hardware can display, and helps eliminate
        // screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        // Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        // refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        // If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        // useful for testing performance, but can also be very stressful to some hardware.
        // You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        // You can change these files; they are in lwjgl3/src/main/resources/ .
        // They can also be loaded from the root of assets/ .
        configuration.setWindowIcon(Main.WINDOW_ICON);

        // This should improve compatibility with Windows machines with buggy OpenGL drivers, Macs
        // with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
        // This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
        // You can choose to remove the following line and the mentioned dependency if you want; they
        // are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return new Lwjgl3Application(new Main(args), configuration);
    }
}
