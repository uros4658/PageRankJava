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


    public static int countOutgoingLinks(int[][] graph, int vertex) {
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
        /*
        if (args.length != 5) {
            System.out.println("Usage: java Main <dampingFactor> <maxIterations> <numEdges> <numVertices> <mode>");
            System.exit(1);
        }
        double dampingFac = Double.parseDouble(args[0]);
        int maxNumOfIter = Integer.parseInt(args[1]);
        int numOfEdges = Integer.parseInt(args[2]);
        int numOfVertices = Integer.parseInt(args[3]);
        int mode = Integer.parseInt(args[4]);
        */
        double dampingFac = 0.85;
        int maxNumOfIter = 5;
        int numOfEdges = 20000;
        int numOfVertices = 4000;
        int mode = 1;
        // Make the graph
        long start_time = System.currentTimeMillis();
        int[][] graph = generateRandomGraph(numOfVertices, numOfEdges);

        // Calling the function
        double[] pageRanks;
        if (mode == 1) {
            pageRanks = SequentialPageRank.calculatePageRank(graph, dampingFac, maxNumOfIter);
        } else if (mode == 2) {
            pageRanks = ParallelPageRank.calculatePageRankParallel(graph, dampingFac, maxNumOfIter);
        } else if (mode == 3) {
            pageRanks = DistributivePageRank.calculatePageRankDistributive(graph, dampingFac, maxNumOfIter, args);
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
    }

}