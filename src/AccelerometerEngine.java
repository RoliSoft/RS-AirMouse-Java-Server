import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an accelerometer data processor.
 * @author RoliSoft
 */
public class AccelerometerEngine extends DataProcessorEngine {

    private double _cX, _cY;

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

        if (_cX == 0 && _cY == 0) {
            _cX = x;
            _cY = y;
            return;
        }

        x -= _cX;
        y -= _cY;

        if (x < 1 && x > -1) {
            x = 0;
        }

        if (y < 1 && y > -1) {
            y = 0;
        }

        mouseHandler.setHeading(-x, y);
    }

    @Override
    public void recalibrate() {
        _cX = _cY = 0;
    }

    @Override
    public String toString() {
        return "Accelerometer";
    }

}