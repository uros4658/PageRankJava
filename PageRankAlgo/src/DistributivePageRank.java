import mpi.MPI;
import mpi.MPIException;

public class DistributivePageRank {
    public static double[] calculatePageRankDistributive(int[][] graph, double dampingFactor, int maxIterations, String[] args) throws MPIException {
        MPI.Init(args);
        int numVertices = graph.length;
        double[] pageRanks = new double[numVertices];
        double[] newPageRanks = new double[numVertices];
        double[] allNewPageRanks = new double[numVertices * MPI.COMM_WORLD.Size()];

        // Initialize PageRanks
        for (int i = 0; i < numVertices; i++) {
            pageRanks[i] = 1.0 / numVertices;
        }

        // Get the rank of the current process
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

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
            MPI.COMM_WORLD.Allgather(newPageRanks, rank, 1, MPI.DOUBLE, allNewPageRanks, 0, 1, MPI.DOUBLE);

            // Update PageRanks for the next iteration
            for (int i = 0; i < size; i++) {
                pageRanks[i] = allNewPageRanks[i];
            }
        }

        MPI.Finalize();
        return pageRanks;
    }
}
