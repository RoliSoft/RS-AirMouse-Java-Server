import java.net.InetAddress;

/**
 * Represents an interface to report connection status and convey messages from the connected client.
 *
 * @author RoliSoft
 */
public interface ClientListener {

    /**
     * Occurs when a new client has connected to the local endpoint.
     *
     * @param addr The IP address of the connecting client.
     * @param name The name of the connecting device's name.
     *             This can be either a hostname or a device name as returned by Android.
     * @param type The type of the sensor which the connecting client initially offers.
     *             This may be changed throughout the session by crafting the appropriate package
     *             to be received with {@link this.dataReceived(String)}.
     */
    public void clientConnected(InetAddress addr, String name, int type);

    /**
     * Occurs when sensor data is received from the remote client.
     *
     * @param data Sensor data to be processed.
     */
    public void sensorDataReceived(double[] data);

    /**
     * Occurs when the remote device has changed the sensor type.
     *
     * @param type Sensor ID to continue processing the data.
     */
    public void sensorChangeReceived(int type);

    /**
     * Occurs when the remote device has requested a sensor recalibration.
     */
    public void sensorRecalibrateRequest();

    /**
     * Occurs when a click was requested from the remote device.
     *
     * @param release Value indicating whether this is a new click or not.
     *                If set to false, this is a new click and 'pressed' event will be sent.
     *                If set to true, this is a click finish and 'released' event will be sent.
     */
    public void clickRequested(boolean release);

    /**
     * Occurs when a the connection has been lost due to a connection error.
     *
     * @param data This argument may contain null, Exception or String in order to explain the cause.
     */
    public void connectionError(Object data);

    /**
     * Occurs when the client has gracefully disconnected from the server.
     */
    public void clientDisconnected();

}