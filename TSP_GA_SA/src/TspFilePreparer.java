
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;

public class TspFilePreparer {

    private ArrayList<Integer> numberOfCities;
    private ArrayList<Vertex> vertices;
    public double[][] distanceMatrix;
    public int[] tour;


    // Takes the file name, opens and pareses it.
    public TspFilePreparer(File file) {

        // Initialize the class variables
        this.numberOfCities = new ArrayList<>();
        this.vertices = new ArrayList<>();
        ArrayList<City> cityList = new ArrayList<>();

        try {
            BufferedReader b = new BufferedReader(new FileReader(file));
            String line;
            while((line = b.readLine()) != null) {
                try {
                    String[] matches = line.trim().split("\\s+");
                    cityList.add(new City( (Integer.parseInt(matches[0])), Double.parseDouble(matches[1]), Double.parseDouble(matches[2])));
                } catch(IllegalArgumentException e) {
                    System.out.println("Wrong file format!!");
                    System.exit(0);
                }
            }
            b.close();

            cityList = checkForDuplicates(cityList); // Removing all duplicate cities using Linked hash set and equals method.
            // Adding each city to the list of cities
            for (City city : cityList) {
                addId(city.getCityId());
                addVertex(city.getVertex());
            }
            // The distance matrix is created so as to be able to make use of the nearest neighbor algorithm
            distanceMatrix = initDistanceMatrix();
            tour = createShuffledTour();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<City> checkForDuplicates(ArrayList<City> cityList) {

        LinkedHashSet<City> cityListNoDupes = new LinkedHashSet<>(cityList);
        return new ArrayList<>(cityListNoDupes);
    }
    // An ID is added to each city
    private void addId(int id) {
        this.numberOfCities.add(id);
    }

    // Adds a vertex to the array of coordinates. The vertex is the location of the city
    private void addVertex(Vertex vertex) {
        this.vertices.add(vertex);
    }

    public ArrayList<Integer> getNumberOfCities() {
        return this.numberOfCities;
    }

    // Creating a random starting tour using the Fisher–Yates shuffle
    private int[] createShuffledTour() {

        // init array
        int[] tour = new int[numberOfCities.size()];
        for (int i = 0; i < numberOfCities.size(); i++) {
            tour[i] = i;
        }
        int index, temp;
        Random random = new Random();
        for (int i = tour.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = tour[index];
            tour[index] = tour[i];
            tour[i] = temp;
        }
        return tour;
    }

    private double[][] initDistanceMatrix() {
        // This must be loaded before any shuffling takes place or else the distance table will be incorrect due to incorrect ID order
        double[][] distanceMatrix = new double[numberOfCities.size()][numberOfCities.size()];
        // The matrix is populated to avoid having to perform calculations multiple times throughout the algorithms, thus increasing processing time
        for (int i = 0; i < numberOfCities.size(); ++i) {
            for (int j = 0; j < numberOfCities.size(); ++j) {
                double distance = vertices.get(i).vertexDistance(vertices.get(j));
                distanceMatrix[i][j] = distance;
            }
        }

        return distanceMatrix;
    }
}
