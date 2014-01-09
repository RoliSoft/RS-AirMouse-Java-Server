import java.awt.*;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a mouse handler.
 * This class helps the sensor data preprocessor implementations to calculate relative position based on the
 * sensor measurements and it also allows them to move the mouse.
 * An underlying thread can be started with {@link this.start()} which will use the headings set by
 * {@link this.setHeading(double, double)} to animate the movement of the mouse, instead of directly moving it.
 * Since sensor measurements are not real-time or accurate enough, this is the best solution to remove jerkiness
 * and still retain accuracy.
 *
 * @author RoliSoft
 */
public class MouseHandler {

    private static Robot _robot;
    private static PointerInfo _mouse;
    private static Dimension _screen;
    private static MouseMoverThread _thd;

    /**
     * Initializes the static values for private use of this class.
     */
    static {
        try {
            _robot  = new Robot();
            _mouse  = MouseInfo.getPointerInfo();
            _screen = Toolkit.getDefaultToolkit().getScreenSize();
        } catch (AWTException ex) {
            Logger.getLogger(DataProcessorEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the current position of the cursor.
     * {@see DataProcessorEngine.processData(String)} will calculate a position relative to this.
     *
     * @return Current position of the cursor.
     */
    public static Point getCursorPos() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    /**
     * Gets the current dimension of the screen. This value is cached and not updated throughout the lifetime
     * of the application due to a performance hit it would otherwise introduce.
     * {@see DataProcessorEngine.processData(string)} will use this to detect coordinate overflows and handle
     * it accordingly.
     *
     * @return Current dimension of the screen.
     */
    public static Dimension getScreenSize() {
        return _screen;
    }

    /**
     * Moves the mouse to the specified coordinates using the underlying {@see Robot} class.
     *
     * @param x The X value of the new coordinate.
     * @param y The Y value of the new coordinate.
     */
    public static void moveTo(int x, int y) {
        _robot.mouseMove(x, y);
    }

    /**
     * Sets the heading of the mouse to the specified coordinates.  In order for this work, the underlying
     * mouse handler thread will have to be started using {@link this.start()}, therefore if the thread
     * is not yet running, this call will start it.
     *
     * @param x The X value of the coordinate to start navigating to.
     * @param y The Y value of the coordinate to start navigating to.
     */
    public static void setHeading(double x, double y) {
        if (!isRunning()) {
            start();
        }

        _thd.setHeading(x, y);
    }

    /**
     * Initiates a click event using the underlying {@see Robot} class.
     * Please note, you will have to release the mouse button in order to "finish clicking" with {@link this.press()}.
     */
    public static void press() {
        _robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Finishes the previously initiated click event using the underlying {@see Robot} class.
     * While it would be more simpler to only register a single click, separating the "press" and "release" events
     * allows for drag-and-drop operations to occur.
     */
    public static void release() {
        _robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Starts a new underlying thread, if one is not running already.
     * The purpose of the underlying thread is to accept values from {@link this.setHeading(double, double)} and
     * animate the movement of the mouse to it. This is being done in order to filter out jerkiness without losing
     * accuracy.
     */
    public static void start() {
        if (isRunning()) {
            return;
        }

        _thd = new MouseMoverThread();
        _thd.start();
    }

    /**
     * Gets a value indicating whether the underlying thread is currently being run or not.
     *
     * @return Value indicating whether the underlying thread is alive.
     */
    public static boolean isRunning() {
        return _thd != null && _thd.isAlive();
    }

    /**
     * Stops the underlying thread if such thread exists and is active.
     */
    public static void stop() {
        if (isRunning()) {
            _thd.stop();
            _thd = null;
        }
    }

}
