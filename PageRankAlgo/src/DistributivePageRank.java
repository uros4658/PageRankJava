import mpi.MPI;
import mpi.MPIException;

public class DistributivePageRank {
    public static double[] calculatePageRankDistributive(int[][] graph, double dampingFactor, int maxIterations, String[] args) throws MPIException {
        MPI.Init(args);
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
                    sum += pageRanks[j] / Main.countOutgoingLinks(graph, j);
                }
            }
            newPageRanks[rank] = (1 - dampingFactor) / numVertices + dampingFactor * sum;
            // Gather the new PageRanks from all processes
            MPI.COMM_WORLD.Allgather(newPageRanks, rank, 1, MPI.DOUBLE, pageRanks, 0, numVertices, MPI.DOUBLE);

            // Update PageRanks for the next iteration
            pageRanks = newPageRanks;
        }
        MPI.Finalize();
        return pageRanks;
    }
}
