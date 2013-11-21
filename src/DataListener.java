import java.net.InetAddress;

/**
 * Represents an interface to convey messages from the connected client.
 * @author RoliSoft
 */
public interface DataListener {

    public void clientConnected(InetAddress addr, String name, int type);
    public void dataReceived(String data);
    public void connectionError(Object data);
    public void clientDisconnected();

}