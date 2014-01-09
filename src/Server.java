import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multi-threaded yet single-client TCP server implementation which also binds
 * to a UDP port to listen and answer discovery broadcasts.
 *
 * @author RoliSoft
 */
public class Server {

    /**
     * The port to be used to receive broadcast UDP message.
     * This has to be a static, non-configurable port in order for the server discovery to work.
     */
    public static final int BCAST_PORT = 8337;

    private ServerSocket _tcpServer;
    private DatagramSocket _udpServer;
    private Thread _tcpThread, _udpThread;
    private Socket _client;
    private DataInputStream _tcpInStream;
    private DataOutputStream _tcpOutStream;
    private ArrayList<ClientListener> _listeners;
    private Protocol _protocol;

    /**
     * Initializes the current instance.
     */
    public Server() {
        _listeners = new ArrayList<>();
    }

    /**
     * Gets a value indicating whether the TCP server is listening or not.
     *
     * @return Value indicating whether TCP server is alive.
     */
    public boolean isListening() {
        return _tcpServer != null && _tcpServer.isBound();
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
        return !isListening() ? -1 : _tcpServer.getLocalPort();
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
     * Gets the current instance of the TCP input stream.
     *
     * @return The input stream to read from.
     */
    public DataInputStream getInputStream() {
        return _tcpInStream;
    }

    /**
     * Gets the current instance of the TCP output stream.
     *
     * @return The output stream to write to.
     */
    public DataOutputStream getOutputStream() {
        return _tcpOutStream;
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
     * Gets the currently connected client or null.
     *
     * @return Connected client or null.
     */
    public Socket getClient() {
        return _client;
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

        _tcpServer = new ServerSocket(0);
        _udpServer = new DatagramSocket(BCAST_PORT);

        startTcpAcceptAsync();
        startUdpListenAsync();
    }

    /**
     * Stops both the TCP and UDP servers, if any of them are running.
     * Please note, connected clients will be disconnected upon closing the bound sockets.
     */
    public void stop() {
        if (_tcpServer != null) {
            try {
                _tcpServer.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.FINE, null, ex);
            }
        }

        if (_udpServer != null) {
            _udpServer.close();
        }

        if (_tcpThread != null) {
            _tcpThread.stop();
            _tcpThread = null;
        }

        if (_udpThread != null) {
            _udpThread.stop();
            _udpThread = null;
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
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        _client = null;
    }

    /**
     * Starts the TCP server asynchronously.
     * This socket is used to accept incoming connections and to communicate with them using the specified protocol.
     */
    private void startTcpAcceptAsync() {
        // TODO extract this

        _tcpThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (_tcpThread != null) {
                    try {
                        _client = _tcpServer.accept();

                        _tcpInStream  = new DataInputStream(new BufferedInputStream(_client.getInputStream()));
                        _tcpOutStream = new DataOutputStream(new BufferedOutputStream(_client.getOutputStream()));

                        _protocol = new PlainTextProtocol(Server.this);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);

                        for (ClientListener dl : _listeners) {
                            dl.connectionError(ex);
                        }

                        continue;
                    }

                    Logger.getLogger(Server.class.getName()).log(Level.INFO, "Client connected from {0}", _client.getInetAddress().getHostAddress());

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
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }

                        for (ClientListener dl : _listeners) {
                            dl.clientDisconnected();
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);

                        for (ClientListener dl : _listeners) {
                            dl.connectionError(ex);
                        }

                        if (_client.isConnected()) {
                            try {
                                _client.close();
                            } catch (IOException ex1) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }

                        _client = null;
                    }
                }
            }

        });

        _tcpThread.start();
    }

    /**
     * Starts the UDP server asynchronously.
     * This socket is used to receive and answer broadcast pings that are used for server discovery.
     */
    private void startUdpListenAsync() {
        // TODO extract this

        _udpThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (_udpThread != null) {
                    byte[] recv = new byte[15000];

                    DatagramPacket packet = new DatagramPacket(recv, recv.length);

                    try {
                        _udpServer.receive(packet);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Logger.getLogger(Server.class.getName()).log(Level.INFO, "Packet received from {0}", packet.getAddress().getHostAddress());

                    String data = new String(packet.getData()).trim();

                    if (!data.contentEquals("RS-AirMouse discover")) {
                        continue;
                    }

                    String laddr = "";

                    try {
                        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

                        for (NetworkInterface netint : Collections.list(nets)) {
                            if (laddr.length() != 0) {
                                break;
                            }

                            if (!netint.isUp() || netint.isLoopback() || netint.isVirtual() || netint.getDisplayName().contains("VMware")) {
                                continue;
                            }

                            for (InetAddress iaddr : Collections.list(netint.getInetAddresses())) {
                                if (!iaddr.isSiteLocalAddress() || iaddr instanceof Inet6Address) {
                                    continue;
                                }

                                laddr = iaddr.getHostAddress();
                                break;
                            }
                        }

                        if (laddr.length() == 0) {
                            laddr = InetAddress.getLocalHost().getHostAddress();
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    byte[] send = ("RS-AirMouse " + laddr + " " + _tcpServer.getLocalPort()).getBytes();

                    DatagramPacket resp;
                    try {
                        resp = new DatagramPacket(send, send.length, packet.getSocketAddress());
                        _udpServer.send(resp);
                    } catch (SocketException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        });

        _udpThread.start();
    }

}