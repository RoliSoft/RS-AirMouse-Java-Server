import java.io.*;
import java.util.ArrayList;

/**
 * Manages the TCP and UDP servers.
 *
 * @author RoliSoft
 */
public class ServerManager {

    private TcpServer _tcpServer;
    private UdpServer _udpServer;
    private ArrayList<ClientListener> _listeners;

    /**
     * Initializes the current instance.
     */
    public ServerManager() {
        _listeners = new ArrayList<>();
    }

    /**
     * Gets a value indicating whether the TCP server is listening or not.
     *
     * @return Value indicating whether TCP server is alive.
     */
    public boolean isListening() {
        return _tcpServer != null && _tcpServer.isListening();
    }

    /**
     * Gets a value indicating whether the TCP server has a connected client or not.
     *
     * @return Value indicating whether a client is connected.
     */
    public boolean isConnected() {
        return _tcpServer != null && _tcpServer.isConnected();
    }

    /**
     * Gets the port of the TCP server, or -1 if it is not listening.
     *
     * @return Port of the TCP server or -1.
     */
    public int getPort() {
        return _tcpServer != null ? _tcpServer.getPort() : -1;
    }

    /**
     * Registers a new {@see ClientListener} on this instance.
     * When something happens, these registered instances will be notified in chronological order of their registration.
     *
     * @param dl An instance implementing the {@see ClientListener} interface.
     */
    public void addListener(ClientListener dl) {
        _listeners.add(dl);
    }

    /**
     * De-registers the specified instance from the list of notified instances.
     *
     * @param dl A previously-added instance.
     *
     * @return True if the listener was successfully removed; otherwise, false.
     */
    public boolean removeListener(ClientListener dl) {
        return _listeners.remove(dl);
    }

    /**
     * Gets the current list of client listeners.
     *
     * @return A list of client listeners.
     */
    public ArrayList<ClientListener> getListeners() {
        return _listeners;
    }

    /**
     * Starts both the TCP and UDP server in the background asynchronously.
     *
     * @throws IOException Forwarded exception shall the servers fail to start.
     *                     Such issue may occur if two instances are running of the server,
     *                     and/or the UDP broadcast port is already taken.
     */
    public void start() throws IOException {
        stop();

        _tcpServer = new TcpServer(this);
        _udpServer = new UdpServer(this);

        _tcpServer.start();
        _udpServer.start();
    }

    /**
     * Stops both the TCP and UDP servers, if any of them are running.
     * Please note, connected clients will be disconnected upon closing the bound sockets.
     */
    public void stop() {
        if (_tcpServer != null) {
            _tcpServer.stop();
        }

        if (_udpServer != null) {
            _udpServer.stop();
        }
    }

    /**
     * Disconnects the currently connected client, if there is one.
     * The TCP server will continue to accept new clients at this point.
     */
    public void disconnect() {
        if (_tcpServer != null && _tcpServer.isConnected()) {
            _tcpServer.disconnect();
        }
    }
}