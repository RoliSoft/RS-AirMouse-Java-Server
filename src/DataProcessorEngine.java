import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an abstract class to accept raw sensor data and process it.
 * @author RoliSoft
 */
public abstract class DataProcessorEngine {

    public static final int ACCELEROMETER = 1;
    public static final int GYROSCOPE = 2;

    private static Robot _robot;
    private static PointerInfo _mouse;
    private static Dimension _screen;

    static {
        try {
            _robot  = new Robot();
            _mouse  = MouseInfo.getPointerInfo();
            _screen = Toolkit.getDefaultToolkit().getScreenSize();
        } catch (AWTException ex) {
            Logger.getLogger(DataProcessorEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public abstract void processData(String data);

    public static Dimension getScreenSize() {
        return _screen;
    }

    public static Point getCursorPos() {
        return _mouse.getLocation();
    }

    public static void moveMouse(int x, int y) {
        _robot.mouseMove(x, y);
    }

    public static DataProcessorEngine createFromType(int type) throws IllegalArgumentException {
        switch (type) {
            case ACCELEROMETER:
                return new AccelerometerEngine();

            case GYROSCOPE:
                return new GyroscopeEngine();

            default:
                throw new IllegalArgumentException("Unknown data type.");
        }
    }

}