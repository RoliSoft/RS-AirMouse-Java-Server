import java.lang.String;

/**
 * Represents an abstract class to accept raw sensor data and process it.
 * When overridden, this class is responsible of processing the String data received from the client device,
 * and passing the results of the filter along to the active mouse handler.
 *
 * @author RoliSoft
 */
public abstract class DataProcessorEngine {

    /**
     * The number representing the accelerometer sensor.
     */
    public static final int ACCELEROMETER = 1;

    /**
     * The number representing the gyroscope sensor.
     */
    public static final int GYROSCOPE = 2;

    /**
     * Processes the data received from the client device. Upon the data is processed, the data will be passed
     * to the active mouse handler on this instance as returned by {@see DataProcessorEngine.getMouseHandler()}.
     *
     * @param data The data received from the client.
     *
     * @throws IllegalArgumentException This exception is thrown if the received data is not properly formatted
     *                                  as specified in the documentation of the data argument.
     */
    public abstract void processData(double[] data);

    /**
     * Recalibrates the sensor's starting position, meaning that the current position will be considered as
     * 0,0 and further sensor measurements will translate to mouse-movements relative to the current position.
     */
    public abstract void recalibrate();

    /**
     * Initiates a new instance of the requested sensor data preprocessor and returns it for use.
     *
     * @param type The ID which was assigned to the sensor data preprocessor to initiate.
     *             See the {@link DataProcessorEngine} class for constants which were assigned to sensor types.
     *
     * @see this.ACCELEROMETER
     * @see this.GYROSCOPE
     *
     * @return A new instance of the requested sensor data preprocessor.
     *
     * @throws IllegalArgumentException This exception is thrown when an invalid sensor type was specified.
     */
    public static DataProcessorEngine createFromType(int type) throws IllegalArgumentException {
        switch (type) {
            case ACCELEROMETER:
                return new AccelerometerEngine();

            case GYROSCOPE:
                return new GyroscopeEngine();

            default:
                throw new IllegalArgumentException("Unknown sensor type. Consult the documentation for valid values.");
        }
    }

}