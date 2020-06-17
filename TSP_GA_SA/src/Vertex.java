
import java.util.Objects;

class Vertex {

    // The x coordinate
    private double x;

    // The y coordinate
    private double y;

    // Constructor creates a point
    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // computes the distance between two vertexes/points
    double vertexDistance(Vertex p2) {
        return Math.sqrt(Math.pow(p2.getX() - x, 2) + Math.pow(p2.getY() - y, 2));
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }


    // Required to check for duplicate cities

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Double.compare(vertex.getX(), getX()) == 0 &&
                Double.compare(vertex.getY(), getY()) == 0;
    }

    // Required to check for duplicate cities

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
