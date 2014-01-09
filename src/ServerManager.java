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
public class ServerManager {

    /**
     * The port to be used to receive broadcast UDP message.
     * This has to be a static, non-configurable port in order for the server discovery to work.
     */
    public static final int BCAST_PORT = 8337;

    private TcpServer _tcpServer;
    private DatagramSocket _udpServer;
    private Thread _tcpThread, _udpThread;
    private DataInputStream _tcpInStream;
    private DataOutputStream _tcpOutStream;
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
        return _tcpServer != null ? -1 : _tcpServer.getPort();
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
        _udpServer = new DatagramSocket(BCAST_PORT);

        _tcpServer.start();
        startUdpListenAsync();
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
            _udpServer.close();
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
        if (_tcpServer != null && _tcpServer.isConnected()) {
            _tcpServer.disconnect();
        }
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
                        Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Logger.getLogger(ServerManager.class.getName()).log(Level.INFO, "Packet received from {0}", packet.getAddress().getHostAddress());

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

                    byte[] send = ("RS-AirMouse " + laddr + " " + _tcpServer.getPort()).getBytes();

                    DatagramPacket resp;
                    try {
                        resp = new DatagramPacket(send, send.length, packet.getSocketAddress());
                        _udpServer.send(resp);
                    } catch (SocketException ex) {
                        Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        });

        _udpThread.start();
    }

}