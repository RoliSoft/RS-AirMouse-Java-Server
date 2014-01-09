import java.net.InetAddress;

/**
 * Represents an interface to convey messages from the connected client.
 *
 * @author RoliSoft
 */
public interface DataListener {

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
     * Occurs when data is received from the remote client.
     * This data is a one-line ASCII text, which may be sent along to the active sensor data preprocessor,
     * or handled locally by the implementing class.
     *
     * @param data One-line ASCII text containing commands.
     *             The protocol is similar to that of IRC.
     */
    public void dataReceived(String data);

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