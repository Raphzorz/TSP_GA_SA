
import java.util.Objects;

public class City {
    private double x;
    private double y;
    private int cityId;
    private Vertex vertex;

    // The vector is created for building of the distance matrix
    City(int cityId, double x, double y){
        this.cityId = cityId;
        this.x = x;
        this.y = y;
        this.vertex = new Vertex(x, y);
    }

    public void setX(int x) {
        this.x = x;
    }
    double getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }
    double getY() {
        return y;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
    int getCityId() {
        return cityId;
    }
    public Vertex getVertex() {
        return this.vertex;
    }

    // Required to check for duplicate cities

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Double.compare(city.getX(), getX()) == 0 &&
                Double.compare(city.getY(), getY()) == 0 &&
                Objects.equals(getVertex(), city.getVertex());
    }

    // Required to check for duplicate cities. If the ID is different but the coordinates are the same then the city will still be removed

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getVertex());
    }
}