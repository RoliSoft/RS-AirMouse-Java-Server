import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Multi-threaded UDP server implementation to listen and answer discovery broadcasts.
 *
 *  @author RoliSoft
 */
public class UdpServer implements Runnable {

    /**
     * The port to be used to receive broadcast UDP message.
     * This has to be a static, non-configurable port in order for the server discovery to work.
     */
    public static final int BCAST_PORT = 8337;

    private ServerManager _serverManager;
    private Thread _thread;
    private DatagramSocket _server;

    /**
     * Initializes this instance.
     *
     * @param serverManager The manager instance.
     */
    public UdpServer(ServerManager serverManager) {
        _serverManager = serverManager;
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
     * @throws java.io.IOException Forwarded exception shall the servers fail to start.
     *                     Such issue may occur if two instances are running of the server,
     *                     and/or the UDP broadcast port is already taken.
     */
    public void start() throws IOException {
        stop();

        _server = new DatagramSocket(BCAST_PORT);
        _thread = new Thread(this);
        _thread.start();
    }

    /**
     * Stops the UDP server, if it is running.
     * Please note, connected clients on the TCP server will not be affected by this.
     */
    public void stop() {
        if (_server != null) {
            _server.close();
        }

        if (_thread != null) {
            _thread.stop();
            _thread = null;
        }
    }

    /**
     * Runs in a separate thread, where it waits indefinitely for UDP broadcast messages,
     * and if the header matches, replies with the location of the local TCP server.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while (_thread != null) {
            byte[] recv = new byte[15000];

            DatagramPacket packet = new DatagramPacket(recv, recv.length);

            try {
                _server.receive(packet);
            } catch (IOException ex) {
                Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            Logger.getLogger(ServerManager.class.getName()).log(Level.INFO, "Packet received from {0}", packet.getAddress().getHostAddress());

            String data = new String(packet.getData()).trim();

            if (!data.contentEquals("RS-AirMouse discover")) {
                continue;
            }

            byte[] send = ("RS-AirMouse " + getLocalAddress() + " " + _serverManager.getPort()).getBytes();

            DatagramPacket resp;
            try {
                resp = new DatagramPacket(send, send.length, packet.getSocketAddress());
                _server.send(resp);
            } catch (SocketException ex) {
                Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Gets the local address to which the client can connect back to.
     *
     * @return Local IP address.
     */
    public String getLocalAddress() {
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

        return laddr;
    }
}
