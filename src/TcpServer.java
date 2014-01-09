import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Multi-threaded yet single-client TCP server implementation.
 *
 *  @author RoliSoft
 */
public class TcpServer implements Runnable {

    private ServerManager _serverManager;
    private Thread _thread;
    private Socket _client;
    private ServerSocket _server;
    private DataInputStream _inputStream;
    private DataOutputStream _outputStream;
    private Protocol _protocol;

    /**
     * Initializes this instance.
     *
     * @param serverManager The manager instance.
     */
    public TcpServer(ServerManager serverManager) {
        _serverManager = serverManager;
    }

    /**
     * Gets a value indicating whether the TCP server is listening or not.
     *
     * @return Value indicating whether TCP server is alive.
     */
    public boolean isListening() {
        return _server != null && _server.isBound();
    }

    /**
     * Gets a value indicating whether the TCP server has a connected client or not.
     *
     * @return Value indicating whether a client is connected.
     */
    public boolean isConnected() {
        return _client != null && _client.isConnected();
    }

    /**
     * Gets the port of the TCP server, or -1 if it is not listening.
     *
     * @return Port of the TCP server or -1.
     */
    public int getPort() {
        return !isListening() ? -1 : _server.getLocalPort();
    }

    /**
     * Gets the currently connected client or null.
     *
     * @return Connected client or null.
     */
    public Socket getClient() {
        return _client;
    }

    /**
     * Gets the current list of client listeners.
     *
     * @return A list of client listeners.
     */
    public ArrayList<ClientListener> getListeners() {
        return _serverManager.getListeners();
    }

    /**
     * Gets the current instance of the TCP input stream.
     *
     * @return The input stream to read from.
     */
    public DataInputStream getInputStream() {
        return _inputStream;
    }

    /**
     * Gets the current instance of the TCP output stream.
     *
     * @return The output stream to write to.
     */
    public DataOutputStream getOutputStream() {
        return _outputStream;
    }

    /**
     * Gets the current instance of the server manager.
     *
     * @return Server manager instance.
     */
    public ServerManager getServerManager() {
        return _serverManager;
    }

    /**
     * Starts the TCP server in the background asynchronously.
     *
     * @throws IOException Forwarded exception shall the servers fail to start.
     *                     Such issue may occur if two instances are running of the server,
     *                     and/or the UDP broadcast port is already taken.
     */
    public void start() throws IOException {
        stop();

        _server = new ServerSocket(0);
        _thread = new Thread(this);
        _thread.start();
    }

    /**
     * Stops the TCP server, if it is running.
     * Please note, connected clients will be disconnected upon closing the bound sockets.
     */
    public void stop() {
        if (_server != null) {
            try {
                _server.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerManager.class.getName()).log(Level.FINE, null, ex);
            }
        }

        if (_thread != null) {
            _thread.stop();
            _thread = null;
        }
    }

    /**
     * Disconnects the currently connected client, if there is one.
     * The TCP server will continue to accept new clients at this point.
     */
    public void disconnect() {
        if (_client != null && _client.isConnected()) {
            try {
                _client.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        _client = null;
    }

    /**
     * Runs in a separate thread, where it will wait indefinitely until a client connects,
     * then initiates a handshake, and if all is good, a protocol will be instantiated which
     * will then receive any further communication through the socket.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while (_thread != null) {
            try {
                _client = _server.accept();

                _inputStream  = new DataInputStream(new BufferedInputStream(_client.getInputStream()));
                _outputStream = new DataOutputStream(new BufferedOutputStream(_client.getOutputStream()));

                _protocol = new PlainTextProtocol(this);
            } catch (IOException ex) {
                Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);

                for (ClientListener dl : getListeners()) {
                    dl.connectionError(ex);
                }

                continue;
            }

            Logger.getLogger(ServerManager.class.getName()).log(Level.INFO, "Client connected from {0}", _client.getInetAddress().getHostAddress());

            try {
                _protocol.handshake();

                boolean loop = true;
                while (loop && _client != null) {
                    try {
                        loop = _protocol.readNext();
                    } catch (IOException ex) {
                        loop = false;
                    }
                }

                if (_client != null && _client.isConnected()) {
                    try {
                        _client.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

                for (ClientListener dl : getListeners()) {
                    dl.clientDisconnected();
                }

            } catch (IOException ex) {
                Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);

                for (ClientListener dl : getListeners()) {
                    dl.connectionError(ex);
                }

                if (_client.isConnected()) {
                    try {
                        _client.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

                _client = null;
            }
        }
    }
}
