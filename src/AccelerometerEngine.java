import java.awt.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an accelerometer data processor.
 * @author RoliSoft
 */
public class AccelerometerEngine extends DataProcessorEngine {

    private final double g = 9.80665;
    private double _x, _y, _pX, _pY;

    public AccelerometerEngine() {

    }

    @Override
    public void processData(String data) {
        Logger.getLogger(AccelerometerEngine.class.getName()).log(Level.INFO, data);

        StringTokenizer st = new StringTokenizer(data, ",");

        double x = Double.parseDouble(st.nextToken());
        double y = Double.parseDouble(st.nextToken());
        double z = st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0;

        MouseHandler mouseHandler = getMouseHandler();

        if (mouseHandler == null) {
            return;
        }

        if (!mouseHandler.isRunning()) {
            mouseHandler.start();
        }

        y -= 6;

        if (x < 1 && x > -1 && y < 1 && y > -1) {
            mouseHandler.setHeading(0, 0);
            return;
        }

        _x = -((20 / g) * x);
        _y = -((20 / g) * (y - (g / 2.0f)));
        if (Math.abs(_pX) < 1) {
            x += _pX;
        }
        if (Math.abs(_pY) < 1) {
            y += _pY;
        }

        mouseHandler.setHeading(x, y);

        _pX = _x;
        _pY = _y;
    }

    @Override
    public String toString() {
        return "Accelerometer";
    }

}