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

    private MouseHandler _mouseHandler;

    public void setMouseHandler(MouseHandler mouseHandler) {
        _mouseHandler = mouseHandler;
    }

    public MouseHandler getMouseHandler() {
        return _mouseHandler;
    }

    public abstract void processData(String data);
    public abstract void recalibrate();

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