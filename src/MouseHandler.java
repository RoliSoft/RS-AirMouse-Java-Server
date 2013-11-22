import java.awt.*;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a mouse handler.
 * @author RoliSoft
 */
public class MouseHandler {

    private static Robot _robot;
    private static PointerInfo _mouse;
    private static Dimension _screen;

    private Thread _thd;
    private long _time;
    private double _x, _y;

    static {
        try {
            _robot  = new Robot();
            _mouse  = MouseInfo.getPointerInfo();
            _screen = Toolkit.getDefaultToolkit().getScreenSize();
        } catch (AWTException ex) {
            Logger.getLogger(DataProcessorEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Point getCursorPos() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    public static Dimension getScreenSize() {
        return _screen;
    }

    public static void moveTo(int x, int y) {
        _robot.mouseMove(x, y);
    }

    public static void press() {
        _robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void release() {
        _robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void start() {
        if (isRunning()) {
            return;
        }

        _thd = new Thread(() -> {

            while (true) {
                if (System.currentTimeMillis() - _time > 1000) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    continue;
                }

                Point mouse = MouseInfo.getPointerInfo().getLocation();
                moveTo((int)(Math.round(mouse.getX() + _x) % _screen.getWidth()), (int)(Math.round(mouse.getY() + _y) % _screen.getHeight()));

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        _thd.start();
    }

    public boolean isRunning() {
        return _thd != null && _thd.isAlive();
    }

    public void stop() {
        if (isRunning()) {
            _thd.stop();
            _thd = null;
        }
    }

    public void setHeading(double x, double y) {
        _x = x;
        _y = y;

        _time = System.currentTimeMillis();
    }

}
