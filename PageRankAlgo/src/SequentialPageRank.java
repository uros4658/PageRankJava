public class SequentialPageRank {
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
                        sum += pageRanks[j] / Main.countOutgoingLinks(graph, j);
                    }
                }

                newPageRanks[i] = (1 - dampingFactor) / numVertices + dampingFactor * sum;
            }

            // Update PageRanks for the next iteration
            pageRanks = newPageRanks;
        }

        return pageRanks;
    }
}
