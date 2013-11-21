import java.awt.Dimension;
import java.awt.Point;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an accelerometer data processor.
 * @author RoliSoft
 */
public class AccelerometerEngine extends DataProcessorEngine {

    public AccelerometerEngine() {

    }

    @Override
    public void processData(String data) {
        Logger.getLogger(AccelerometerEngine.class.getName()).log(Level.INFO, data);

        StringTokenizer st = new StringTokenizer(data, ",");

        double x = Double.parseDouble(st.nextToken());
        double y = Double.parseDouble(st.nextToken());
        double z = st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0;

        Point pos = getCursorPos();
        Dimension scr = getScreenSize();

        moveMouse(
                (int)((pos.x + (x * -25)) % scr.width),
                (int)((pos.y + (y *  25)) % scr.height)
        );

        //moveMouse((int)x % getWidth(), (int)y % getHeight());
    }

    @Override
    public String toString() {
        return "Linear acceleration via Accelerometer";
    }

}