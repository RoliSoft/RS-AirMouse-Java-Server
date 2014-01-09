import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an accelerometer data processor.
 *
 * @author RoliSoft
 */
public class AccelerometerEngine extends DataProcessorEngine {

    private double _cX, _cY;

    /**
     * Initializes the current instance.
     */
    public AccelerometerEngine() {

    }

    /**
     * Processes the data received from the client device. Upon the data is processed, the data will be passed
     * to the active mouse handler on this instance as returned by {@see DataProcessorEngine.getMouseHandler()}.
     *
     * @param data The data for this provider is preferably two comma-separated floating-point numbers,
     *             representing the X and Y values returned by the accelerometer.
     *
     * @throws IllegalArgumentException This exception is thrown if the received data is not properly formatted
     *                                  as at least two comma-separated floating-point numbers.
     */
    @Override
    public void processData(String data) throws IllegalArgumentException {
        Logger.getLogger(AccelerometerEngine.class.getName()).log(Level.INFO, data);

        // TODO exceptions

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

    /**
     * Recalibrates the accelerometer's starting position, meaning that the current position will be considered as
     * 0,0 and further accelerometer measurements will translate to mouse-movements relative to the current position.
     */
    @Override
    public void recalibrate() {
        _cX = _cY = 0;
    }

    /**
     * Returns the textual representation of the current instance.
     *
     * @return Name of the sensor.
     */
    @Override
    public String toString() {
        return "Accelerometer";
    }

}