import java.io.File;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MainMenu {

    private static Scanner scan = new Scanner(System.in);

    public static void startProgram() {

        System.out.println("Loading files, please wait! :)");

        File folder = new File("data/");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;

        // Loads all the files with a txt extention in data
        for (int i = 0; i < listOfFiles.length; i++)
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().toLowerCase().endsWith("txt")) {
                System.out.println("File " + (i+1) + ": " + listOfFiles[i].getName());
            }

        int choice;
        do {
            System.out.print("Type the File number that you wish to test \n");
            choice= scan.nextInt();
        } while (choice >= listOfFiles.length+1 || choice < 0);
        choice-=1;
        // Read the file
        TspFilePreparer tspFilePreparer = new TspFilePreparer(listOfFiles[(choice)]);
        System.out.println("You have selected " + listOfFiles[choice].getName()+ "\n");
        // Create the instance of the problem
        Algorithms tspInstance = new Algorithms(tspFilePreparer.getNumberOfCities(), tspFilePreparer.distanceMatrix,tspFilePreparer.tour);
        welcomeMenu(tspInstance);
    }
    static void welcomeMenu(Algorithms tspInstance) {

        System.out.println("Please type the corresponding number to run the algorithm \n");
        System.out.println("[1] Nearest Neighbor & Simulated Annealing");
        System.out.println("[2] Nearest Neighbor & Genetic Algorithm");
        System.out.println("[q] Quit");
        String algorithmChoice = scan.next();
        switch (algorithmChoice.toLowerCase()) {
            case "1":
                for (int i = 0; i <100 ; i++) {
                    tspInstance.runNearestNeighbor(true);
                }

                break;
            case "2":
                tspInstance.runGeneticAlgorithm();
                break;
            case "q":
                System.out.println("Bye");
                System.exit(0);
            default:
                System.out.println("That is not a valid key. Please enter either 1,2 or q");
                welcomeMenu(tspInstance);
                break;
        }
    }
}



