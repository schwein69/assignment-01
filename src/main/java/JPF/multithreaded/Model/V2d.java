package JPF.multithreaded.Model;

/**
 * 2-dimensional vector
 * Objects are completely state-less
 */
public class V2d {
    private final double x;
    private final double y;

    public V2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public V2d sum(V2d v) {
        return new V2d(x + v.getX(), y + v.getY());
    }

    public double abs() {
        return Math.sqrt(x * x + y * y);
    }

    public V2d getNormalized() {
        double module = Math.sqrt(x * x + y * y);
        return new V2d(x / module, y / module);
    }

    public V2d mul(double fact) {
        return new V2d(x * fact, y * fact);
    }

    @Override
    public String toString() {
        return "V2d(" + x + "," + y + ")";
    }
}
