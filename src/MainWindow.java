import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main window of the AirMouse application.
 *
 * @author RoliSoft
 */
public class MainWindow extends JFrame implements ActionListener, WindowListener, ClientListener, HeadingListener {

    private JButton jToggleServerButton;
    private JButton jDisconnectButton;
    private JLabel jTypeStaticLabel;
    private JLabel jClientStaticLabel;
    private JLabel jServerStaticLabel;
    private JLabel jServerLabel;
    private JLabel jClientLabel;
    private JLabel jTypeLabel;
    private JLabel jStatusLabel;
    private JPanel jMainPanel;
    private JPanel jContentPanel;
    private JPanel jLeftPanel;
    private JPanel jRightPanel;
    private JLabel jXLabel;
    private JLabel jYLabel;
    private JProgressBar jXProgressBar;
    private JProgressBar jYProgressBar;

    private ServerManager _serverManager;
    private InetAddress _clientAddr;
    private String _clientName;
    private DataProcessorEngine _engine;

    /**
     * Initializes the current instance and sets up the user interface.
     */
    public MainWindow() {
        super("AirMouse");

        setContentPane(jMainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(this);

        jStatusLabel.setBorder(BorderFactory.createTitledBorder(""));
        jToggleServerButton.addActionListener(this);
        jDisconnectButton.addActionListener(this);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * The main entry-point of the application. Starts a new {@see MainWindow} instance.
     *
     * @param args Arguments received from the operating system.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        new MainWindow().setVisible(true);
    }

    /**
     * Handles button clicks.
     *
     * @param evt Event data.
     */
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == jToggleServerButton) {
            MainWindow.this.jToggleServerButtonActionPerformed(evt);
        }
        else if (evt.getSource() == jDisconnectButton) {
            MainWindow.this.jDisconnectButtonActionPerformed(evt);
        }
    }

    /**
     * This function is not currently used, but its implementation is required by {@see ActionListener}.
     *
     * @param evt Event data.
     */
    public void windowActivated(java.awt.event.WindowEvent evt) {

    }

    /**
     * This function is not currently used, but its implementation is required by {@see ActionListener}.
     *
     * @param evt Event data.
     */
    public void windowClosed(java.awt.event.WindowEvent evt) {

    }

    /**
     * This function is not currently used, but its implementation is required by {@see ActionListener}.
     *
     * @param evt Event data.
     */
    public void windowClosing(java.awt.event.WindowEvent evt) {

    }

    /**
     * This function is not currently used, but its implementation is required by {@see ActionListener}.
     *
     * @param evt Event data.
     */
    public void windowDeactivated(java.awt.event.WindowEvent evt) {
    }

    /**
     * This function is not currently used, but its implementation is required by {@see ActionListener}.
     *
     * @param evt Event data.
     */
    public void windowDeiconified(java.awt.event.WindowEvent evt) {

    }

    /**
     * This function is not currently used, but its implementation is required by {@see ActionListener}.
     *
     * @param evt Event data.
     */
    public void windowIconified(java.awt.event.WindowEvent evt) {

    }

    /**
     * Occurs when the window is initialized and being shown to the user.
     * Calls {@see formWindowOpened(WindowEvent)} for further user-land initialization.
     *
     * @param evt Event data.
     */
    public void windowOpened(java.awt.event.WindowEvent evt) {
        if (evt.getSource() == MainWindow.this) {
            MainWindow.this.formWindowOpened(evt);
        }
    }

    /**
     * Called by {@see ActionListener.windowOpened(WindowEvent)} on first show to set up the interface.
     * Upon the UI has been initialized, the underlying servers will be started automatically.
     *
     * @param evt Event data.
     */
    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        jStatusLabel.setText("Ready.");
        setConnectionLabels();
        startServer();
        MouseHandler.addListener(this);
    }

    /**
     * Handles clicks to jToggleServerButton, by starting and stopping the underlying server.
     *
     * @param evt Event data.
     */
    private void jToggleServerButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (_serverManager == null || !_serverManager.isListening()) {
            startServer();
        } else {
            if (_serverManager != null && _serverManager.isConnected()) {
                jDisconnectButtonActionPerformed(evt);
            }

            stopServer();
        }
    }

    /**
     * Handles clicks to jDisconnectButton, by disconnecting the underlying client, if there are any.
     *
     * @param evt Event data.
     */
    private void jDisconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {
        jStatusLabel.setText("Disconnecting client...");
        jDisconnectButton.setEnabled(false);
        jDisconnectButton.setText("Disconnecting...");

        if (_serverManager != null && _serverManager.isConnected()) {
            _serverManager.disconnect();
        }

        jDisconnectButton.setText("Drop client");
        setConnectionLabels();
    }

    /**
     * Starts the underlying server and updates UI elements accordingly.
     */
    private void startServer() {
        if (_serverManager != null) {
            stopServer();
        }

        jStatusLabel.setText("Starting server...");
        jToggleServerButton.setEnabled(false);
        jToggleServerButton.setText("Starting...");

        _serverManager = new ServerManager();
        _serverManager.addListener(this);

        try {
            _serverManager.start();
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            jStatusLabel.setText("Server is not running.");
            setConnectionLabels();
            JOptionPane.showMessageDialog(this, "Failed to start server:\r\n" + ex.getMessage(), "AirMouse Server Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        jStatusLabel.setText("Server is running.");
        setConnectionLabels();
    }

    /**
     * Stops the underling server and updates UI elements accordingly.
     */
    private void stopServer() {
        jStatusLabel.setText("Stopping server...");
        jToggleServerButton.setEnabled(false);
        jToggleServerButton.setText("Stopping...");

        _serverManager.disconnect();
        _serverManager.stop();
        _serverManager = null;

        jStatusLabel.setText("Server is not running.");
        setConnectionLabels();
    }

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
    @Override
    public void clientConnected(InetAddress addr, String name, int type) {
        if (name.length() == 0) {
            name = addr.getHostName();
        }

        _clientAddr = addr;
        _clientName = name.replace('_', ' ');

        try {
            _engine = DataProcessorEngine.createFromType(type);
        } catch (IllegalArgumentException ex) {
            _engine = null;
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        jStatusLabel.setText("Client connected!");

        setConnectionLabels();
    }

    /**
     * Occurs when sensor data is received from the remote client.
     *
     * @param data Sensor data to be processed.
     */
    public void sensorDataReceived(double[] data) {
        if (_engine == null) {
            return;
        }

        _engine.processData(data);
    }

    /**
     * Occurs when the remote device has changed the sensor type.
     *
     * @param type Sensor ID to continue processing the data.
     */
    public void sensorChangeReceived(int type) {
        try {
            _engine = DataProcessorEngine.createFromType(type);
        } catch (IllegalArgumentException ex) {
            _engine = null;
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        setConnectionLabels();
    }

    /**
     * Occurs when the remote device has requested a sensor recalibration.
     */
    public void sensorRecalibrateRequest() {
        if (_engine == null) {
            return;
        }

        _engine.recalibrate();
    }

    /**
     * Occurs when a click was requested from the remote device.
     *
     * @param release Value indicating whether this is a new click or not.
     *                If set to false, this is a new click and 'pressed' event will be sent.
     *                If set to true, this is a click finish and 'released' event will be sent.
     */
    public void clickRequested(boolean release) {
        if (_engine == null) {
            return;
        }

        if (release) {
            MouseHandler.release();
        } else {
            MouseHandler.press();
        }
    }

    /**
     * Occurs when a the connection has been lost due to a connection error.
     *
     * @param data This argument may contain null, Exception or String in order to explain the cause.
     */
    @Override
    public void connectionError(Object data) {
        JOptionPane.showMessageDialog(this, "Client connection error:\r\n" + (data instanceof Exception ? ((Exception) data).getMessage() : (String) data), "AirMouse Network Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Occurs when the client has gracefully disconnected from the server.
     */
    @Override
    public void clientDisconnected() {
        _clientAddr = null;
        _engine = null;

        if (MouseHandler.isRunning()) {
            MouseHandler.stop();
        }

        if (_serverManager == null || !_serverManager.isListening()) {
            jStatusLabel.setText("Server is not running.");
        } else {
            jStatusLabel.setText("Server is running.");
        }

        setConnectionLabels();
    }

    /**
     * Updates the UI elements to reflect the actual state of the server, client and status.
     */
    private void setConnectionLabels() {
        // Update the server status label.

        if (_serverManager == null || !_serverManager.isListening()) {
            jServerLabel.setText("N/A");
            jServerLabel.setForeground(UIManager.getDefaults().getColor("Button.disabledForeground"));

            jToggleServerButton.setEnabled(true);
            jToggleServerButton.setText("Start server");
        } else {
            jServerLabel.setText(getLocalEndpoint());
            jServerLabel.setForeground(UIManager.getDefaults().getColor("Button.foreground"));

            jToggleServerButton.setEnabled(true);
            jToggleServerButton.setText("Stop server");
        }

        // Update the client status label.

        if (_clientAddr == null) {
            jClientLabel.setText("N/A");
            jClientLabel.setForeground(UIManager.getDefaults().getColor("Button.disabledForeground"));

            jDisconnectButton.setEnabled(false);
        } else {
            jClientLabel.setText(_clientName + " [" + _clientAddr.getHostAddress() + "]");
            jClientLabel.setForeground(UIManager.getDefaults().getColor("Button.foreground"));

            jDisconnectButton.setEnabled(true);
        }

        // Update the selected sensor label.

        if (_engine == null) {
            jTypeLabel.setText("N/A");
            jTypeLabel.setForeground(UIManager.getDefaults().getColor("Button.disabledForeground"));
        } else {
            jTypeLabel.setText(_engine.toString());
            jTypeLabel.setForeground(UIManager.getDefaults().getColor("Button.foreground"));
        }

        // Update the progress bars.

        setHeading(0, 0);
    }

    /**
     * If the server is running, try to find the best possible local IP address to display to the user.
     * Otherwise fallback to "*" as the listening address, this however, does no carry any meaning if the user has to
     * manually enter the IP address on the connecting device.
     *
     * @return Local listening address.
     */
    private String getLocalEndpoint() {
        String hosts = "";

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface netint : Collections.list(nets)) {
                if (!netint.isUp() || netint.isLoopback() || netint.isVirtual() || netint.getDisplayName().contains("VMware")) {
                    continue;
                }

                for (InetAddress iaddr : Collections.list(netint.getInetAddresses())) {
                    if (!iaddr.isSiteLocalAddress() || iaddr instanceof Inet6Address) {
                        continue;
                    }

                    if (hosts.length() > 0) {
                        if (hosts.charAt(0) != '[') {
                            hosts = "[" + hosts;
                        }

                        hosts += ", ";
                    }

                    hosts += iaddr.getHostAddress();
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (hosts.length() == 0) {
            hosts = "*:" + _serverManager.getPort();
        } else {
            hosts += (hosts.length() > 15 ? "]" : "") + ":" + _serverManager.getPort();
        }

        return hosts;
    }

    /**
     * Sets a new coordinate based on the translated sensor data.
     *
     * @param x The value of the X coordinate.
     * @param y The value of the Y coordinate.
     */
    @Override
    public void setCoordinate(double x, double y) {

    }

    /**
     * Sets a new heading based on the translated sensor data.
     *
     * @param x The value of the X axis.
     * @param y The value of the Y axis.
     */
    @Override
    public void setHeading(double x, double y) {
        x = Math.abs(x);
        y = Math.abs(y);

        DecimalFormat df = new DecimalFormat("0.000");

        if (x > 10) {
            jXProgressBar.setForeground(Color.red);
        } else if (jXProgressBar.getForeground() == Color.red) {
            jXProgressBar.setForeground(UIManager.getDefaults().getColor("ProgressBar.foreground"));
        }

        jXProgressBar.setValue((int)(x * 8));
        jXProgressBar.setString(df.format(x));

        if (y > 10) {
            jYProgressBar.setForeground(Color.red);
        } else if (jYProgressBar.getForeground() == Color.red) {
            jYProgressBar.setForeground(UIManager.getDefaults().getColor("ProgressBar.foreground"));
        }

        jYProgressBar.setValue((int)(y * 7));
        jYProgressBar.setString(df.format(y));
    }
}
