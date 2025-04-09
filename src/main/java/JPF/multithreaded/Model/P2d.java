package JPF.multithreaded.Model;

import multithreaded.Model.V2d;

/**
 * 2-dimensional point
 * Objects are completely state-less
 */
public class P2d {
    private final double x;
    private final double y;

    public P2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public P2d sum(V2d v) {
        return new P2d(x + v.getX(), y + v.getY());
    }

    public V2d sub(P2d v) {
        return new V2d(x - v.getX(), y - v.getY());
    }

    public double distance(P2d p) {
        double dx = p.getX() - x;
        double dy = p.getY() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "P2d(" + x + "," + y + ")";
    }
}
