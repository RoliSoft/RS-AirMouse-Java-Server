import java.awt.*;

/**
 * Implements a new thread which runs in the background and smoothly moves the mouse to the specified coordinates.
 */
public class MouseMoverThread extends Thread {

    private long _time;
    private double _x, _y;

    /**
     * Initializes this instance.
     */
    public MouseMoverThread() {

    }

    /**
     * Sets the heading of the mouse to the specified coordinates.  In order for this work, the underlying
     * mouse handler thread will have to be started using {@link this.start()}. Please note, this function
     * will not automatically start the thread and won't fail if such thread is not already initialized, in
     * order to allow pre-setting and/or pausing of the mouse movements.
     *
     * @param x The X value of the coordinate to start navigating to.
     * @param y The Y value of the coordinate to start navigating to.
     */
    public void setHeading(double x, double y) {
        _x = x;
        _y = y;

        _time = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() - _time > 1000) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                continue;
            }

            Point mouse = MouseInfo.getPointerInfo().getLocation();
            Dimension screen = MouseHandler.getScreenSize();

            int nextX = (int)(Math.round(mouse.getX() + _x) % screen.getWidth());
            int nextY = (int)(Math.round(mouse.getY() + _y) % screen.getHeight());

            if (nextX < 0) {
                nextX = (int)screen.getWidth();
            }

            if (nextY < 0) {
                nextY = (int)screen.getHeight();
            }

            MouseHandler.moveTo(nextX, nextY);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
