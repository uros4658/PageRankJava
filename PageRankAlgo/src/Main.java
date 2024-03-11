import java.util.*;
import java.io.*;
import mpi.*;

public class Main {

    public static int[][] generateRandomGraph(int numVertices, int numEdges) {
        // Make a random weighted graph
        int[][] graph = new int[numVertices][numVertices];
        Random random = new Random();

        for (int i = 0; i < numEdges; i++) {
            int source = random.nextInt(numVertices);
            int target = random.nextInt(numVertices);

            // Ensure we don't create self-loops
            while (source == target) {
                target = random.nextInt(numVertices);
            }

            // Assign a random weight (between 1 and 10) to the edge
            int weight = random.nextInt(10) + 1;
            graph[source][target] = weight;
        }

        return graph;
    }

    public static void printGraph(int[][] graph) {
        // printing out the graph
        int numVertices = graph.length;
        System.out.println("Weighted Graph (Adjacency Matrix):");
        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < numVertices; j++) {
                System.out.print(graph[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void findMaxIndex(double[] pageRanks) {
        int maxIndex = 0;
        double maxValue = pageRanks[0];

        for (int i = 1; i < pageRanks.length; i++) {
            if (pageRanks[i] > maxValue) {
                maxValue = pageRanks[i];
                maxIndex = i;
            }
        }

        System.out.println("The top ranked page is "+ maxIndex + " with the value " + maxValue);
    }
    public static double[] calculatePageRank(int[][] graph, double dampingFactor, int maxIterations) {

        // All the calculations are done using the formula from this video
        // https://www.youtube.com/watch?v=CsvyPNdQAHg&ab_channel=AbdulBari
        // 11:54 for the full formula

        int numVertices = graph.length;
        double[] pageRanks = new double[numVertices];
        double[] newPageRanks = new double[numVertices];

        // Initialize PageRanks
        for (int i = 0; i < numVertices; i++) {
            pageRanks[i] = 1.0 / numVertices;
        }

        // Perform iterations
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (int i = 0; i < numVertices; i++) {
                double sum = 0.0;

                for (int j = 0; j < numVertices; j++) {
                    if (graph[j][i] != 0) {
                        sum += pageRanks[j] / countOutgoingLinks(graph, j);
                    }
                }

                newPageRanks[i] = (1 - dampingFactor) / numVertices + dampingFactor * sum;
            }

            // Update PageRanks for the next iteration
            pageRanks = newPageRanks;
        }

        return pageRanks;
    }
    public static double[] calculatePageRankParallel(int[][] graph, double dampingFactor, int maxIterations) {
        int numVertices = graph.length;
        double[] pageRanks = new double[numVertices];
        double[] newPageRanks = new double[numVertices];

        // Initialize PageRanks
        for (int i = 0; i < numVertices; i++) {
            pageRanks[i] = 1.0 / numVertices;
        }

        // Perform iterations
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            final double[] finalPageRanks = pageRanks.clone();
            Thread[] threads = new Thread[numVertices];
            for (int i = 0; i < numVertices; i++) {
                final int vertex = i;
                threads[i] = new Thread(() -> {
                    double sum = 0.0;
                    for (int j = 0; j < numVertices; j++) {
                        if (graph[j][vertex] != 0) {
                            sum += finalPageRanks[j] / countOutgoingLinks(graph, j);
                        }
                    }
                    newPageRanks[vertex] = (1 - dampingFactor) / numVertices + dampingFactor * sum;
                });
                threads[i].start();
            }

            // Wait for all threads to finish
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Update PageRanks for the next iteration
            pageRanks = newPageRanks;
        }

        return pageRanks;
    }

    public static double[] calculatePageRankDistributive(int[][] graph, double dampingFactor, int maxIterations) throws MPIException {
        int numVertices = graph.length;
        double[] pageRanks = new double[numVertices];
        double[] newPageRanks = new double[numVertices];

        // Initialize PageRanks
        for (int i = 0; i < numVertices; i++) {
            pageRanks[i] = 1.0 / numVertices;
        }

        // Get the rank of the current process
        int rank = MPI.COMM_WORLD.Rank();

        // Perform iterations
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            double sum = 0.0;
            for (int j = 0; j < numVertices; j++) {
                if (graph[j][rank] != 0) {
                    sum += pageRanks[j] / countOutgoingLinks(graph, j);
                }
            }
            newPageRanks[rank] = (1 - dampingFactor) / numVertices + dampingFactor * sum;
            // Gather the new PageRanks from all processes
            MPI.COMM_WORLD.Allgather(newPageRanks, rank, 1, MPI.DOUBLE, pageRanks, 0, numVertices, MPI.DOUBLE);

            // Update PageRanks for the next iteration
            pageRanks = newPageRanks;
        }

        return pageRanks;
    }


    private static int countOutgoingLinks(int[][] graph, int vertex) {
        int count = 0;
        for (int i = 0; i < graph[vertex].length; i++) {
            if (graph[vertex][i] != 0) {
                count++;
            }
        }
        return count;
    }
    public static void writeGraphToCSV(int[][] graph, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 0; i < graph.length; i++) {
                for (int j = 0; j < graph[i].length; j++) {
                    if (graph[i][j] != 0) {
                        writer.write(i + "," + j + "," + graph[i][j] + "\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writePageRankToCSV(double[] pageRanks, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("vertex,score\n");

            // Write to csv
            for (int i = 0; i < pageRanks.length; i++) {
                writer.write(i + "," + pageRanks[i] + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MPIException {

        // Initialize the MPI environment
        MPI.Init(args);

        // Getting user input
        Scanner scan = new Scanner(System.in);
        double dampingFac;
        int maxNumOfIter, numOfEdges, numOfVertices, mode;
        System.out.print("Enter the damping factor for this instance: ");
        dampingFac = scan.nextDouble();
        System.out.print("Enter the maximum number of iterations for this instance: ");
        maxNumOfIter = scan.nextInt();
        System.out.print("Enter the number of edges for this instance: ");
        numOfEdges = scan.nextInt();
        System.out.print("Enter the number of vertices for this instance: ");
        numOfVertices = scan.nextInt();
        System.out.print("Enter the mode for this instance (1 for sequential, 2 for parallel, 3 for distributive): ");
        mode = scan.nextInt();

        // for the log
        long start_time = System.currentTimeMillis();
        // Make the graph
        int[][] graph = generateRandomGraph(numOfVertices, numOfEdges);
        printGraph(graph);

        // Calling the function
        double[] pageRanks;
        if (mode == 1) {
            pageRanks = calculatePageRank(graph, dampingFac, maxNumOfIter);
        } else if (mode == 2) {
            pageRanks = calculatePageRankParallel(graph, dampingFac, maxNumOfIter);
        } else if (mode == 3) {
            pageRanks = calculatePageRankDistributive(graph, dampingFac, maxNumOfIter);
        } else {
            throw new IllegalArgumentException("Invalid mode. Enter 1 for sequential, 2 for parallel, 3 for distributive.");
        }

        System.out.println("\nPage Ranks:");
        for (int i = 0; i < numOfVertices; i++) {
            System.out.println("Page " + i + ": " + pageRanks[i]);
        }

        findMaxIndex(pageRanks);

        // Write the graph to a CSV file
        writeGraphToCSV(graph, "graph.csv");
        // Write the PageRank results to a CSV file
        writePageRankToCSV(pageRanks, "pagerank.csv");

        long end_time = System.currentTimeMillis();
        long elapsed_time = end_time - start_time;

        System.out.println("The elapsed for the program: "+ elapsed_time + " milliseconds ");

        // Finalize the MPI environment
        MPI.Finalize();
    }

}