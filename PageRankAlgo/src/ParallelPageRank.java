public class ParallelPageRank {
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
                            sum += finalPageRanks[j] / Main.countOutgoingLinks(graph, j);
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

}
