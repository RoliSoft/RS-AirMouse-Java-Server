import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a gyroscope data processor.
 * @author RoliSoft
 */
public class GyroscopeEngine extends DataProcessorEngine {

    public GyroscopeEngine() {

    }

    @Override
    public void processData(String data) {
        Logger.getLogger(GyroscopeEngine.class.getName()).log(Level.INFO, data);

    }

    @Override
    public String toString() {
        return "Angular rotation via Gyroscope";
    }

}