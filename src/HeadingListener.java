/**
 * Represents an interface to push out processed sensor data to anyone who is listening.
 *
 * @author RoliSoft
 */
public interface HeadingListener {

    /**
     * Sets a new heading based on the translated sensor data.
     *
     * @param x The value of the X axis.
     * @param y The value of the Y axis.
     */
    public void setHeading(double x, double y);

    /**
     * Sets a new coordinate based on the translated sensor data.
     *
     * @param x The value of the X coordinate.
     * @param y The value of the Y coordinate.
     */
    public void setCoordinate(double x, double y);

}
