# Assignment 4: Smart City / Smart Campus Scheduling

## Overview

This project implements graph algorithms for analyzing task dependencies in smart city/campus scheduling scenarios. It consolidates two course topics:

1. **Strongly Connected Components (SCC)** & **Topological Ordering**
2. **Shortest Paths in DAGs**

The system processes datasets containing city-service tasks (street cleaning, repairs, camera/sensor maintenance) and internal analytics subtasks. It detects and compresses cyclic dependencies, then plans optimal execution paths for acyclic components.

## Project Structure

```
Assignment4_DAA/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/
│   │   │       ├── graph/
│   │   │       │   ├── Graph.java          # Graph data structure
│   │   │       │   ├── Edge.java           # Edge representation
│   │   │       │   ├── scc/
│   │   │       │   │   └── KosarajuSCC.java    # SCC algorithm
│   │   │       │   ├── topo/
│   │   │       │   │   └── TopologicalSort.java # Topological sort
│   │   │       │   └── dagsp/
│   │   │       │       └── DAGPathFinder.java   # Shortest/longest paths
│   │   │       ├── Main.java               # Main entry point
│   │   │       ├── Metrics.java           # Performance instrumentation
│   │   │       └── DatasetGenerator.java  # Dataset generation utility
│   │   └── resources/
│   └── test/
│       └── java/
│           └── org/example/graph/
│               ├── scc/KosarajuSCCTest.java
│               ├── topo/TopologicalSortTest.java
│               └── dagsp/DAGPathFinderTest.java
├── data/                                  # Generated datasets
│   ├── small_1.json, small_2.json, small_3.json
│   ├── medium_1.json, medium_2.json, medium_3.json
│   └── large_1.json, large_2.json, large_3.json
└── pom.xml                                # Maven configuration
```

## Algorithms Implemented

### 1. Strongly Connected Components (Kosaraju's Algorithm)

**Package:** `org.example.graph.scc`

- **Algorithm:** Kosaraju's two-pass DFS algorithm
- **Time Complexity:** O(V + E)
- **Space Complexity:** O(V + E)
- **Features:**
  - Detects all strongly connected components
  - Builds condensation graph (DAG of SCCs)
  - Tracks DFS visits and edge traversals

### 2. Topological Sort

**Package:** `org.example.graph.topo`

- **Algorithm:** DFS-based topological sorting
- **Time Complexity:** O(V + E)
- **Space Complexity:** O(V)
- **Features:**
  - Computes valid topological order
  - Detects cycles (returns null if cycle found)
  - Tracks DFS visits and edge traversals

### 3. Shortest/Longest Paths in DAG

**Package:** `org.example.graph.dagsp`

- **Algorithm:** Dynamic programming over topological order
- **Time Complexity:** O(V + E)
- **Space Complexity:** O(V)
- **Features:**
  - Single-source shortest paths
  - Single-source longest paths (critical path)
  - Path reconstruction
  - Supports edge weights and condensation edges (min/max weights)
  - Tracks edge relaxations

## Weight Model

The project uses **edge weights** to represent task durations or costs. Each edge has:
- Standard weight: `w` (for regular edges)
- Min/Max weights: `minWeight`, `maxWeight` (for condensation edges between SCCs)

## Datasets

The project includes 9 datasets organized by size:

### Small Datasets (6-10 nodes)
- `small_1.json`: 8 nodes, single cycle (size 3)
- `small_2.json`: 10 nodes, pure DAG, sparse
- `small_3.json`: 7 nodes, 2 SCCs

### Medium Datasets (10-20 nodes)
- `medium_1.json`: 15 nodes, 3 SCCs
- `medium_2.json`: 18 nodes, dense graph
- `medium_3.json`: 12 nodes, sparse graph

### Large Datasets (20-50 nodes)
- `large_1.json`: 30 nodes, 5 SCCs
- `large_2.json`: 40 nodes, dense graph
- `large_3.json`: 25 nodes, sparse graph

## Building and Running

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build the Project

```bash
mvn clean compile
```

### Generate Datasets

```bash
mvn exec:java -Dexec.mainClass="org.example.DatasetGenerator"
```

This will generate all 9 datasets in the `data/` directory.

### Run the Main Program

```bash
# Run with default dataset (data/small_1.json)
mvn exec:java -Dexec.mainClass="org.example.Main"

# Run with specific dataset
mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="data/medium_1.json"
```

Or compile and run directly:

```bash
mvn compile
java -cp target/classes:target/dependency/* org.example.Main data/small_1.json
```

### Run Tests

```bash
mvn test
```

## Usage Example

```bash
$ mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="data/small_1.json"

=== Smart City Scheduling Analysis ===
Dataset: data/small_1.json
Loaded graph with 8 nodes and 7 edges.
Original Source Node: 0
Weight Model: edge
---
### 1.1 Strongly Connected Components ###
Found 3 SCCs:
  SCC 0 (Size 3): [0, 1, 2]
  SCC 1 (Size 1): [3]
  SCC 2 (Size 4): [4, 5, 6, 7]

Building condensation graph...
Condensation graph has 3 nodes (one per SCC). Edges:
  Node 0 -> 1 (w: 1)
  Node 1 -> 2 (w: 2)
---
### 1.2 Topological Sort of Condensation DAG ###
Topological Order of SCCs: [0, 1, 2]
---
### 1.3 Shortest & Longest Paths on DAG ###
Original source node 0 is in SCC 0.

**Single-Source Shortest Paths (from SCC 0)**
  SCC 0: 0
  SCC 1: 1
  SCC 2: 3

**Single-Source Longest Paths (from SCC 0)**
  SCC 0: 0
  SCC 1: 1
  SCC 2: 3

**Critical (Longest) Path from source**
  Path (of SCCs): [0, 1, 2]
  Length: 3
---
### Instrumentation Report ###
Counters:
  scc.dfs1.visits          : 8
  scc.dfs1.edges           : 7
  scc.dfs2.visits          : 8
  scc.dfs2.edges           : 7
  topo.dfs.visits          : 3
  topo.dfs.edges           : 2
  dagsp.relaxations        : 2

Timings:
  LoadData                 : 0.1234 ms
  SCC                      : 0.2345 ms
  TopoSort                 : 0.0123 ms
  DAG-SSSP                 : 0.0045 ms
  DAG-LongestPath          : 0.0045 ms
  TotalTime                : 0.4567 ms
```

## Instrumentation

The `Metrics` class tracks:

### Counters
- `scc.dfs1.visits`: Number of vertices visited in first DFS pass
- `scc.dfs1.edges`: Number of edges traversed in first DFS pass
- `scc.dfs2.visits`: Number of vertices visited in second DFS pass
- `scc.dfs2.edges`: Number of edges traversed in second DFS pass
- `topo.dfs.visits`: Number of vertices visited in topological sort DFS
- `topo.dfs.edges`: Number of edges traversed in topological sort DFS
- `dagsp.relaxations`: Number of edge relaxations in path finding

### Timings
All timings are measured using `System.nanoTime()` and reported in milliseconds.

## Testing

JUnit 5 tests are provided for:

1. **KosarajuSCCTest**: Tests SCC detection, cycle handling, condensation graph building
2. **TopologicalSortTest**: Tests topological ordering, cycle detection, edge cases
3. **DAGPathFinderTest**: Tests shortest/longest paths, path reconstruction, unreachable nodes

Run tests with:
```bash
mvn test
```

## Code Quality

- **Packages:** Organized into `graph.scc`, `graph.topo`, `graph.dagsp`
- **Documentation:** Javadoc comments for all public classes and methods
- **Modularity:** Clear separation of concerns
- **Testing:** Comprehensive JUnit tests with edge cases

## Dataset Format

Datasets are stored as JSON files with the following structure:

```json
{
  "directed": true,
  "n": 8,
  "edges": [
    {"u": 0, "v": 1, "w": 3},
    {"u": 1, "v": 2, "w": 2}
  ],
  "source": 0,
  "weight_model": "edge"
}
```

- `directed`: Always `true` (directed graph)
- `n`: Number of vertices
- `edges`: Array of edges with source (`u`), target (`v`), and weight (`w`)
- `source`: Source vertex for path finding
- `weight_model`: Always `"edge"` (edge weights)

## Performance Analysis

The instrumentation provides detailed metrics for analyzing algorithm performance:

- **SCC Algorithm:** Performance depends on graph density and number of cycles
- **Topological Sort:** Linear time for DAGs
- **DAG Path Finding:** Linear time, very efficient compared to general shortest path algorithms

## Future Improvements

- Support for node durations (alternative to edge weights)
- Visualization of graphs and SCCs
- Parallel SCC detection for large graphs
- Additional path finding algorithms (all-pairs shortest paths)

## License

This project is part of a course assignment.

## Author

Assignment 4 - Design and Analysis of Algorithms

