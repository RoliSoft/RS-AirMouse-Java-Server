import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a plain-text protocol for passing information between server and client.
 *
 * The protocol is similar to the IRC protocol: it uses one-line ASCII texts for packets, therefore
 * packet separation is done each time a '\n' control character is met. The very first word of the
 * packet is the "command". After the command is parsed, its arguments are de-serialized to their
 * own according type and the registered {@see ClientListener} instances are notified of the event.
 *
 * @author RoliSoft
 */
public class PlainTextProtocol extends Protocol {

    /**
     * Initializes the current instance.
     *
     * @param server The server on which this protocol is spoken.
     */
    public PlainTextProtocol(Server server) {
        super(server);
    }

    /**
     * Initiates a handshake with the client.
     *
     * @throws java.io.IOException Occurs when handshake or stream is invalid.
     */
    @Override
    public void handshake() throws IOException {
        String line = getServer().getInputStream().readLine();
        StringTokenizer st = new StringTokenizer(line);

        if (!st.hasMoreTokens() || !st.nextToken().contentEquals("RS-AirMouse")) {
            throw new IOException("Handshake error, line not valid:\r\n" + line);
        }

        String host = st.nextToken();
        int type = Integer.parseInt(st.nextToken());

        for (ClientListener dl : getServer().getListeners()) {
            dl.clientConnected(getServer().getClient().getInetAddress(), host, type);
        }
    }

    /**
     * Reads the next packet from the stream and processes it.
     *
     * @return Value indicating whether to continue reading.
     *
     * @throws IOException Occurs when the stream becomes invalid.
     */
    @Override
    public boolean readNext() throws IOException {
        String line = getServer().getInputStream().readLine();

        Logger.getLogger(AccelerometerEngine.class.getName()).log(Level.INFO, line);

        if (line == null || line.trim().toLowerCase().contentEquals("quit")) {
            return false;
        }

        int idx;
        String cmd = ((idx = line.indexOf(" ")) != -1 ? line.substring(0, idx) : line).trim().toLowerCase();

        switch (cmd) {
            case "data": {
                String data = line.substring(idx + 1);
                StringTokenizer st = new StringTokenizer(data, ",");

                double[] vals = {
                        st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0,
                        st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0,
                        st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 0
                };

                for (ClientListener dl : getServer().getListeners()) {
                    dl.sensorDataReceived(vals);
                }
                break;
            }

            case "type": {
                int type = Integer.parseInt(line.substring(idx + 1));

                for (ClientListener dl : getServer().getListeners()) {
                    dl.sensorChangeReceived(type);
                }
                break;
            }

            case "reset": {
                for (ClientListener dl : getServer().getListeners()) {
                    dl.sensorRecalibrateRequest();
                }
                break;
            }

            case "tap": {
                boolean release = !cmd.substring(idx + 1).contentEquals("on");

                for (ClientListener dl : getServer().getListeners()) {
                    dl.clickRequested(release);
                }
                break;
            }
        }

        return true;
    }

}
