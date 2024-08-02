import java.util.*;

public class BankersAlgorithm {
    // System state variables
    private int numProcesses;
    private int numResources;
    private int[] available;    // Available resources
    private int[][] maximum;    // Maximum demand of each process
    private int[][] allocation; // Currently allocated resources to each process
    private int[][] need;       // Remaining need of each process

    // Constructor
    public BankersAlgorithm() {
        initializeSystemState();
        calculateNeedMatrix();
        printState();
    }

    // Initialize the system state based on user input
    private void initializeSystemState() {
        Scanner scanner = new Scanner(System.in);

        // Get number of processes and resources
        System.out.print("Enter number of processes: ");
        numProcesses = scanner.nextInt();

        System.out.print("Enter the number of resources: ");
        numResources = scanner.nextInt();

        // Initialize available resources
        System.out.print("Enter number of each resource, separated by white space: ");
        available = new int[numResources];
        for (int i = 0; i < numResources; i++) {
            available[i] = scanner.nextInt();
        }

        // Initialize matrices
        maximum = new int[numProcesses][numResources];
        allocation = new int[numProcesses][numResources];
        need = new int[numProcesses][numResources];

        // Get maximum resource claims for each process
        System.out.println("Enter the maximum resource claim for each process:");
        for (int i = 0; i < numProcesses; i++) {
            System.out.print("Process " + i + ": ");
            for (int j = 0; j < numResources; j++) {
                maximum[i][j] = scanner.nextInt();
            }
        }

        // Initialize allocation to 0 (no resources allocated initially)
        for (int i = 0; i < numProcesses; i++) {
            Arrays.fill(allocation[i], 0);
        }
    }

    // Calculate the need matrix (need = maximum - allocation)
    private void calculateNeedMatrix() {
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                need[i][j] = maximum[i][j] - allocation[i][j];
            }
        }
    }

    // Print the current system state
    private void printState() {
        System.out.println("Claim Matrix:");
        printMatrix(maximum);
        
        System.out.println("Allocation Matrix:");
        printMatrix(allocation);
        
        System.out.println("Available Resources:");
        for (int i = 0; i < numResources; i++) {
            System.out.print(available[i] + "\t");
        }
        System.out.println();
    }

    // Helper method to print a matrix
    private void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + "\t");
            }
            System.out.println();
        }
    }

    // Check if the system is in a safe state after a request
    private boolean isSafe(int process, int[] request) {
        // Check if request is less than need and available
        for (int i = 0; i < numResources; i++) {
            if (request[i] > need[process][i] || request[i] > available[i]) {
                return false;
            }
        }

        // Temporarily allocate resources
        for (int i = 0; i < numResources; i++) {
            available[i] -= request[i];
            allocation[process][i] += request[i];
            need[process][i] -= request[i];
        }

        // Check if this state is safe
        boolean[] finish = new boolean[numProcesses];
        int[] work = Arrays.copyOf(available, numResources);
        int count = 0;

        while (count < numProcesses) {
            boolean found = false;
            for (int i = 0; i < numProcesses; i++) {
                if (!finish[i]) {
                    boolean canAllocate = true;
                    for (int j = 0; j < numResources; j++) {
                        if (need[i][j] > work[j]) {
                            canAllocate = false;
                            break;
                        }
                    }
                    if (canAllocate) {
                        for (int j = 0; j < numResources; j++) {
                            work[j] += allocation[i][j];
                        }
                        finish[i] = true;
                        found = true;
                        count++;
                    }
                }
            }
            if (!found) break;
        }

        // If unsafe, rollback the allocation
        if (count < numProcesses) {
            for (int i = 0; i < numResources; i++) {
                available[i] += request[i];
                allocation[process][i] -= request[i];
                need[process][i] += request[i];
            }
            return false;
        }

        return true;
    }

    // Process a resource request
    public boolean request(int process, int resource, int amount) {
        int[] request = new int[numResources];
        request[resource] = amount;
        if (isSafe(process, request)) {
            System.out.println("Request granted.");
            return true;
        } else {
            System.out.println("Request denied.");
            return false;
        }
    }

    // Process a resource release
    public void release(int process, int resource, int amount) {
        allocation[process][resource] -= amount;
        available[resource] += amount;
        need[process][resource] += amount;
        System.out.println("Resources released.");
    }

    public static void main(String[] args) {
        BankersAlgorithm banker = new BankersAlgorithm();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter your next command in form request(i, j, k) or release(i, j, k): ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            
            try {
                // Split input into command and parameters
                String[] parts = input.split("[()]");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid format");
                }
                
                String command = parts[0].trim().toLowerCase();
                String[] params = parts[1].split(",");
                if (params.length != 3) {
                    throw new IllegalArgumentException("Invalid number of parameters");
                }

                // Parse parameters
                int process = Integer.parseInt(params[0].trim());
                int resource = Integer.parseInt(params[1].trim());
                int amount = Integer.parseInt(params[2].trim());

                // Execute command
                if (command.equals("request")) {
                    banker.request(process, resource, amount);
                } else if (command.equals("release")) {
                    banker.release(process, resource, amount);
                } else {
                    System.out.println("Invalid command. Use 'request' or 'release'.");
                    continue;
                }

                // Print updated state
                banker.printState();
            } catch (Exception e) {
                System.out.println("Invalid command format. Please use the format: request(i, j, k) or release(i, j, k)");
            }
        }

        scanner.close();
    }
}