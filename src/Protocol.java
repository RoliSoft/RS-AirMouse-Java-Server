import java.io.IOException;

/**
 * Represents a protocol which handles all data in and out of the TCP stream.
 *
 * @author RoliSoft
 */
public abstract class Protocol {

    private TcpServer _server;

    /**
     * Initializes the current instance.
     *
     * @param server The server on which this protocol is spoken.
     */
    public Protocol(TcpServer server) {
        _server = server;
    }

    /**
     * Gets the server instance this protocol is associated to.
     *
     * @return Associated server instance.
     */
    public TcpServer getServer() {
        return _server;
    }

    /**
     * Initiates a handshake with the client.
     *
     * @throws IOException Occurs when handshake or stream is invalid.
     */
    public abstract void handshake() throws IOException;


    /**
     * Reads the next packet from the stream and processes it.
     *
     * @return Value indicating whether to continue reading.
     *
     * @throws IOException Occurs when the stream becomes invalid.
     */
    public abstract boolean readNext() throws IOException;

}
