import java.util.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Algorithms {

    // General parameters

    public double[][] distanceMatrix;
    public int[] tour;
    int[] neighbourSolution;
    ArrayList<Integer> visitedCities = new ArrayList<>();
    ArrayList<int[]> listOfTours = new ArrayList<>();
    private int numberOfCities;

    // Specific to GA
    private int populationSize = 25;
    private int[][] population;
    private int bestTourIndex;
    private double currentBestDistance;

    public Algorithms(ArrayList<Integer> numberOfCities, double[][] distanceMatrix, int[] tour) {
        this.numberOfCities = numberOfCities.size();
        this.tour = tour;
        this.distanceMatrix = distanceMatrix;
    }

    /*<--------Utility functions-------->*/

    // Returns a random integer, with min & max both included
    static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    // The higher the temperature the higher the chance of a negative value being accepted. This is done with the scope of getting past local optima
    static boolean acceptanceProbability(double currentBestDistance, double neighbourDistance, double temperature) {
        if (neighbourDistance < currentBestDistance)
            return true;
        else {
            double delta = currentBestDistance - neighbourDistance;
            double probability = Math.exp(delta / temperature);
            return probability > Math.random();
        }
    }

    //Returns the nearest neighbour for a chosen node/city
    public int getNearestNeighbor(int index) {
        boolean visited;
        double minDistance = Double.MAX_VALUE;
        int nearestCityId = -1;
        int currentCityId = getIndex(index); // gets the location of cityId to test
        for (int i = 0; i < numberOfCities; ++i) {
            if (i != currentCityId) {
                double distance = this.distanceMatrix[i][index];
                visited = visitedCities.contains(i);
                if (visitedCities.size() == neighbourSolution.length - 1) {
                    return index;
                }
                if ((distance < minDistance) && !visited) {
                    nearestCityId = i;
                    minDistance = distance;
                }
            }
        }
        visitedCities.add(nearestCityId);
        return nearestCityId;
    }

    public double getDistance(int[] tour) {
        double totalDistance = 0;

        for (int i = 0; i < numberOfCities; i++) {
            int cityOne = tour[i];
            int cityTwo = tour[(i + 1) % numberOfCities];
            totalDistance += this.distanceMatrix[cityOne][cityTwo];
        }
        return totalDistance;
    }

    // Returns the index of where the particular cityId is found in the tour

    private int getIndex(int cityId) {
        int i = 0;
        for (int t : neighbourSolution) {
            if (cityId == t) {
                return i; // The index is returned of where the cityId is present in the tour array
            }
            i++;
        }
        return -1; // Nearest node is reset to -1 in the case that it is not found
    }
// Returns the index of where the particular cityId is found in the tour

    private int getIndex(int[] a, int index) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == index) {
                return i;
            }
        }
        return -1;
    }



    // Runs the nearest neighbor algorithm on all start cities. The shortest path from those is chosen.
    // <----- Algorithm 1 - NN & SA ----->
    public void runNearestNeighbor(boolean simulatedAnnealing) {
        long startNearestNeighbor = 0;
        if (simulatedAnnealing) {
            startNearestNeighbor = System.nanoTime();
        }
        int firstNode = tour[0];
        for (int i = 0; i < tour.length; i++) {
            int neighbor;
            // Creates a new tour
            neighbourSolution = tour.clone();

            neighbourSolution[0] = neighbourSolution[i];
            neighbourSolution[i] = firstNode;

            for (int k = 0; k < neighbourSolution.length - 1; k++) {
                neighbor = getNearestNeighbor(neighbourSolution[k]); // sending the cityId to the getNearestNeighbor algorithm
                // temporary becomes the location if the neighbor cityId
                int temporary = getIndex(neighbor);
                // The location of the neighborcityId becomes the cityId that is next in line
                neighbourSolution[temporary] = neighbourSolution[(k + 1) % tour.length];
                //the one that is next in line becomes the neighbor
                neighbourSolution[(k + 1) % tour.length] = neighbor;
            }
            listOfTours.add(neighbourSolution);
            // As a new path is attempted, all visited cities are cleared
            visitedCities.clear();
        }
        for (int[] listOfTour : listOfTours) {
            if (getDistance(listOfTour) < getDistance(tour)) {
                tour = listOfTour;
            }
        }

        if (simulatedAnnealing) {
            runSimulatedAnnealing(startNearestNeighbor);
        }
    }


    public void runSimulatedAnnealing(long startNearestNeighbor) {
//        System.out.println(getDistance(tour) + "initial distance"); // Debugging
//        System.out.println((this.toString()) + "initial path"); // Debugging

        //Set initial temp
        double temp = 1000000;

        //How much the temperature decreases at every iteration
        double coolingRate = 0.00009675; // I found that changing this by a factor of 10 (removing a 0) makes it faster by around a factor of 10 but reduces the frequency of finding the global optimum
        double coolingRateManipulator;
        int timesToIterate;

        if (numberOfCities < 30) {
            timesToIterate = 100;
            coolingRateManipulator = 0.75;
        } else if (numberOfCities < 100) {
            coolingRateManipulator = 0.05;
            timesToIterate = 1;
        } else {
            timesToIterate = 1;
            coolingRateManipulator = 0.0005;
        }

        int acceptanceCounter = 0;

        for (int i = 0; i < timesToIterate; i++) {

            // If there has been no change in tour for 2000 iterations, then the loop will terminate early
            while ((temp > 0.00000001 && acceptanceCounter < 2000)) {

                // Creating a copy of the currently optimal tour found by the nearest neighbor algorithm

                neighbourSolution = tour.clone();

                // Getting two random cities
                int tripPos1 = randomInt(0, numberOfCities);
                int tripPos2 = randomInt(0, numberOfCities);

                while ((tripPos1 == tripPos2)) {
                    tripPos1 = randomInt(0, numberOfCities);
                }

                // Swapping 2 random cities
                int citySwap1 = neighbourSolution[tripPos1];
                int citySwap2 = neighbourSolution[tripPos2];

                neighbourSolution[tripPos2] = citySwap1;
                neighbourSolution[tripPos1] = citySwap2;

                // Get energy/distance of current solution and the new solution solutions
                double bestDistance = getDistance(tour);
                double neighbourDistance = getDistance(neighbourSolution); // New neighbour solution

                // Deciding whether to accept accept the neighbour

                if (acceptanceProbability(bestDistance, neighbourDistance, temp)) {
                    tour = neighbourSolution;
                    acceptanceCounter = 0;
                } else {
                    acceptanceCounter += 1;
                }

                // Three random cooling strategies are used
                if (Math.random() < 0.4) {
                    temp *= 1 - (coolingRate * coolingRateManipulator);
                } else if (Math.random() < 0.8) {
                    temp -= (coolingRate * coolingRateManipulator);
                } else {
                    temp -= Math.random() * (coolingRate * coolingRateManipulator);
                }
            }
//        temp =100;

        }
        // Calculating how long the computation took

        long endSimulatedAnnealing = System.nanoTime();
        long timeSimulatedAnnealing = TimeUnit.MILLISECONDS.convert(endSimulatedAnnealing - startNearestNeighbor, TimeUnit.NANOSECONDS);
        System.out.println("[i]. Optimal solution distance: " + getDistance(tour));
        System.out.println("[ii]. " + this.toString());
        System.out.println("[iii]. Computation took " + timeSimulatedAnnealing + "ms");

    }


// <----- Algorithm 2 - GA ----->

    public void runGeneticAlgorithm() {
        long startGeneticAlgorithm = System.nanoTime();
        bestTourIndex = 0;
        currentBestDistance = getDistance(tour);
        population = new int[populationSize][numberOfCities];
        // Runs the NN to get a good approximate for the best length
        runNearestNeighbor(false);
        // Creating the initial population
        for (int i = 1; i < populationSize; i++) {
            population[0] = tour; // Setting the first entry as the tour generated from the nearest neighbor. This is the only tour not randomised
            population[i] = generatePopulation();
        }
        getFittest(); // sets the current tour as the best tour so far

        // The larger amount of cities, the more it will run in the hope of finding a better solution
        int generations = 500;
        if (numberOfCities < 30) generations = 200;
        if (numberOfCities < 60) generations = 300;

        for (int i = 0; i < generations; i++) {
            selection();
            crossOver();
            mutation();
            getFittest();
        }

        long endGeneticAlgorithm = System.nanoTime();
        long timeGeneticAlgorithm = TimeUnit.MILLISECONDS.convert(endGeneticAlgorithm - startGeneticAlgorithm, TimeUnit.NANOSECONDS);
        System.out.println("[i]. Optimal solution distance: " + getDistance(tour));
        System.out.println("[ii]. Solution " + this.toString());
        System.out.println("[iii]. Computation took " + timeGeneticAlgorithm + "ms");
    }

    private void selection() {
        int[][] bestTours = new int[populationSize][numberOfCities];
        bestTours[0] = population[bestTourIndex];
        bestTours[1] = mutate(tour.clone());
        bestTours[2] = tour.clone();
        // Putting ramdom tours in the rest of the population. Does not replace the afore-generated parents.
        for (int i = 3; i < populationSize; i++) {
            bestTours[i] = population[(int) (Math.random() * populationSize)];
        }
        population = bestTours;
    }

    private void crossOver() {
        int[] crossOverList = new int[populationSize];
        int numberOfCrossOvers = 0;
        for (int i = 0; i < populationSize; i++) {
            double crossoverProbability = 0.9;
            if (Math.random() < crossoverProbability) {
                crossOverList[numberOfCrossOvers] = i;
                numberOfCrossOvers++;
            }
        }
        // As the array has not been filled up completely, only up till the last element is retained.
        crossOverList = Arrays.copyOfRange(crossOverList, 0, numberOfCrossOvers);
        // These tour indexes are randomised/shuffled
        randomise(crossOverList);
        // This sub-tour is used to create new tours
        for (int i = 0; i < crossOverList.length - 1; i += 2) {
            population[crossOverList[i]] = crossOver(crossOverList[i], crossOverList[i + 1], 0);
            population[crossOverList[i + 1]] = crossOver(crossOverList[i], crossOverList[i + 1], 1);
        }
    }


    private int[] crossOver(int x, int y, int state) {

        int[] newTour = new int[numberOfCities];
        int[] parent1 = population[x].clone();
        int[] parent2 = population[y].clone();

        int derivedX = 0, derivedY = 0;

        int child = parent1[randomInt(0, parent1.length)]; // A random city from the parent1 is selected

        newTour[0] = child; // The first node is placed as the random city obtained earlier

        for (int i = 1; i < numberOfCities; i++) {
            int indexOfX = getIndex(parent1, child); // Get the location of the child city in parent 1
            int indexOfY = getIndex(parent2, child); // Get the location of the child city in parent 2

            if (state == 0) {   // State 0 determines that the parent1 tour shall be changed
                derivedX = parent1[(indexOfX + parent1.length - 1) % parent1.length];
                derivedY = parent2[(indexOfY + parent2.length - 1) % parent2.length];
            } else if (state == 1) { // State 1 determines that the parent2 tour shall be changed
                derivedX = parent1[(indexOfX + parent1.length + 1) % parent1.length];
                derivedY = parent2[(indexOfY + parent2.length + 1) % parent2.length];
            }

            if (parent1.length - 1 - indexOfX >= 0) // As long as it is within range, the array has not finished
                // In parent 1, from the index of the previously derivedX is copied to parent 1 itself, destroying the previous indexOfX to avoid duplicates
                System.arraycopy(parent1, indexOfX + 1, parent1, indexOfX, parent1.length - 1 - indexOfX);
            parent1 = Arrays.copyOfRange(parent1, 0, parent1.length - 1); // Elements are moved along
            if (parent2.length - 1 - indexOfY >= 0)
                // In parent 1, from the index of the previously derivedY is copied to parent 2 itself, destroying the previous indexOfy to avoid duplicates
                System.arraycopy(parent2, indexOfY + 1, parent2, indexOfY, parent2.length - 1 - indexOfY);
            parent2 = Arrays.copyOfRange(parent2, 0, parent2.length - 1); // Elements are moved along

            // The shortest edge is calculated
            if (distanceMatrix[child][derivedX] < distanceMatrix[child][derivedY]) child = derivedX;
            else child = derivedY;

            newTour[i] = child;
        }
        return newTour;
    }


    // Mutates at the specified mutation rate

    private void mutation() {
        for (int i = 0; i < populationSize; i++) {
            double mutationProbability = 0.015;
            if (Math.random() < mutationProbability) {
                population[i] = mutate(population[i]);
            }
        }
    }


    // Uses the same procedure used by Simulated Annealing -> A simple swap of two cities.
    private int[] mutate(int[] tour) {
        int tripPos1, tripPos2;

        tripPos1 = randomInt(0, numberOfCities);
        tripPos2 = randomInt(0, numberOfCities);
        while ((tripPos1 == tripPos2)) {
            tripPos1 = randomInt(0, numberOfCities);
        }

        int tmp = tour[tripPos1];
        tour[tripPos1] = tour[tripPos2];
        tour[tripPos2] = tmp;

        return tour;
    }

    // Creates the initial poppulation
    private int[] generatePopulation() {
        int[] tour = new int[numberOfCities];
        for (int i = 0; i < numberOfCities; i++) {
            tour[i] = i;
        }
        return randomise(tour);
    }

    // Randomising of tour in the same way as the initial tour is randomised in TspFilePreparer
    private int[] randomise(int[] tour) {
        for (int i = 0; i < tour.length; i++) {
            tour[i] = i;
        }
        int index, temp;
        Random random = new Random();
        for (int i = tour.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            temp = tour[index];
            tour[index] = tour[i];
            tour[i] = temp;
        }
        return tour;
    }

    // Gets the best tour from the whole population
    private void getFittest() {

        currentBestDistance = getDistance(population[0]);
        for (int i = 1; i < populationSize; i++) {
            if (getDistance(population[i]) < currentBestDistance) {
                currentBestDistance = getDistance(population[i]);
                bestTourIndex = i;
                tour = population[bestTourIndex];
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("The optimal solution order is: ");
        for (int cityId : tour) {
            stringBuilder.append(cityId + 1);
            stringBuilder.append("=>");
        }
        return stringBuilder.toString() + (tour[0] + 1); // Adding the first tour at the end to visually complete the solution. This distance calculation has already been factored in previously however.
    }
}
