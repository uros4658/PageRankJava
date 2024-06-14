import matplotlib.pyplot as plt

# Data before removing printGraph
edges_vertices_before = [
    (30, 7),
    (60, 15),
    (120, 30),
    (280, 60),
    (560, 120),
    (1080, 240),
    (3000, 600),
    (10000, 2000),
    (20000, 4000)
]

sequential_before = [44, 48, 65, 128, 289, 700, 2613, 23789, 93927]
parallel_before = [55, 68, 106, 213, 436, 953, 3227, 26316, 92906]
distributive_before = [216, 231, 232, 328, 498, 929, 2761, 24472, 93412]

# Data after removing printGraph
edges_vertices_after = [
    (30, 7),
    (60, 15),
    (120, 30),
    (280, 60),
    (560, 120),
    (1080, 240),
    (3000, 600),
    (10000, 2000),
    (20000, 4000),
    (50000, 2000)
]

sequential_after = [30, 33, 28, 42, 51, 73, 135, 461, 1790, 990]
parallel_after = [42, 45, 71, 123, 194, 367, 838, 2666, 5675, 2756]
distributive_after = [210, 211, 203, 211, 224, 236, 256, 378, 586, 489]

# Function to plot the data
def plot_data(edges_vertices, sequential, parallel, distributive, title, filename):
    edges, vertices = zip(*edges_vertices)

    plt.figure(figsize=(10, 6))
    plt.plot(edges, sequential, marker='o', label='Sequential')
    plt.plot(edges, parallel, marker='s', label='Parallel')
    plt.plot(edges, distributive, marker='^', label='Distributive')

    plt.xscale('log')
    plt.yscale('log')
    plt.xlabel('Number of Edges')
    plt.ylabel('Execution Time (ms)')
    plt.title(title)
    plt.legend()
    plt.grid(True, which="both", ls="--")
    plt.savefig(filename, format='jpg', dpi=300)
    plt.close()

# Plotting the data before removing printGraph
plot_data(edges_vertices_before, sequential_before, parallel_before, distributive_before, 
          'Execution Times Before Removing printGraph', 'execution_times_before.jpg')

# Plotting the data after removing printGraph
plot_data(edges_vertices_after, sequential_after, parallel_after, distributive_after, 
          'Execution Times After Removing printGraph', 'execution_times_after.jpg')
