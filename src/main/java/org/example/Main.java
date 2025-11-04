package org.example;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        String filename = "src/main/tasks.json";

        Metrics metrics = new Metrics();

        try {
            metrics.start("TotalTime");
            metrics.start("LoadData");
            GraphData data = GraphData.loadFromFile(filename);
            Graph g = new Graph(data.n);
            for (GraphData.EdgeData e : data.edges) {
                g.addEdge(e.u, e.v, e.w);
            }
            metrics.stop("LoadData");

            System.out.println("Loaded graph with " + data.n + " nodes and " + data.edges.size() + " edges.");
            System.out.println("Original Source Node: " + data.source);
            System.out.println("Weight Model: " + data.weight_model + " [cite: 19]");
            System.out.println("---");

            System.out.println("### 1.1 Strongly Connected Components ###");
            metrics.start("SCC");
            KosarajuSCC sccFinder = new KosarajuSCC(g, metrics);
            sccFinder.run();
            metrics.stop("SCC");

            List<List<Integer>> sccs = sccFinder.getSccs();
            System.out.println("Found " + sccs.size() + " SCCs:");
            for (int i = 0; i < sccs.size(); i++) {
                System.out.println("  SCC " + i + " (Size " + sccs.get(i).size() + "): " + sccs.get(i)); // [cite: 13]
            }

            System.out.println("\nBuilding condensation graph... ");
            Graph condensationGraph = sccFinder.getCondensationGraph();
            System.out.println("Condensation graph has " + condensationGraph.V + " nodes (one per SCC). Edges:");
            condensationGraph.print();
            System.out.println("---");


            System.out.println("### 1.2 Topological Sort of Condensation DAG ###");
            metrics.start("TopoSort");
            TopologicalSort topoSort = new TopologicalSort(condensationGraph, metrics);
            List<Integer> topoOrder = topoSort.run(); //
            metrics.stop("TopoSort");

            if (topoOrder == null) {
                System.out.println("Error: Cycle detected in condensation graph. This should not happen.");
            } else {
                System.out.println("Topological Order of SCCs: " + topoOrder); // [cite: 17]
            }
            System.out.println("---");


            System.out.println("### 1.3 Shortest & Longest Paths on DAG ###");

            int sourceSccId = sccFinder.getSccId(data.source);
            System.out.println("Original source node " + data.source + " is in SCC " + sourceSccId + ".");

            metrics.start("DAG-SSSP");
            DAGPathFinder spFinder = new DAGPathFinder(condensationGraph, topoOrder, metrics);
            DAGPathFinder.PathResult shortestPaths = spFinder.run(sourceSccId, false);
            metrics.stop("DAG-SSSP");

            System.out.println("\n**Single-Source Shortest Paths (from SCC " + sourceSccId + ")**");
            shortestPaths.printDistances("SCC");

            metrics.start("DAG-LongestPath");
            DAGPathFinder.PathResult longestPaths = spFinder.run(sourceSccId, true);
            metrics.stop("DAG-LongestPath");

            System.out.println("\n**Single-Source Longest Paths (from SCC " + sourceSccId + ")**");
            longestPaths.printDistances("SCC");

            DAGPathFinder.Path criticalPath = longestPaths.getLongestPath();
            System.out.println("\n**Critical (Longest) Path from source**");
            System.out.println("  Path (of SCCs): " + criticalPath.path);
            System.out.println("  Length: " + (criticalPath.distance == Long.MIN_VALUE ? "N/A" : criticalPath.distance));

            int targetScc = -1;
            for(int i = topoOrder.size() - 1; i >= 0; i--) {
                if (shortestPaths.distances[topoOrder.get(i)] != Long.MAX_VALUE) {
                    targetScc = topoOrder.get(i);
                    break;
                }
            }
            if (targetScc != -1) {
                System.out.println("\n**Example Shortest Path (to SCC " + targetScc + ")**");
                System.out.println("  Path (of SCCs): " + shortestPaths.reconstructPath(targetScc));
            }
            System.out.println("---");


            metrics.stop("TotalTime");
            System.out.println("### Instrumentation Report ###");
            metrics.printReport(); // [cite: 33]

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static class GraphData {
        boolean directed;
        int n;
        List<EdgeData> edges;
        int source;
        String weight_model;

        static class EdgeData {
            int u, v, w;
        }

        public static GraphData loadFromFile(String filename) throws IOException {
            try (FileReader reader = new FileReader(filename)) {
                return new Gson().fromJson(reader, GraphData.class);
            }
        }
    }
    static class DAGPathFinder {
        private Graph g;
        private List<Integer> topoOrder;
        private Metrics metrics;


        static class PathResult {
            long[] distances;
            int[] parent;
            int V;
            boolean isLongestPath;

            PathResult(int V, boolean isLongestPath) {
                this.V = V;
                this.isLongestPath = isLongestPath;
                distances = new long[V];
                parent = new int[V];
                Arrays.fill(parent, -1);
                Arrays.fill(distances, isLongestPath ? Long.MIN_VALUE : Long.MAX_VALUE);
            }

            void printDistances(String nodePrefix) {
                for (int i = 0; i < V; i++) {
                    if (distances[i] == Long.MAX_VALUE || distances[i] == Long.MIN_VALUE) {
                        System.out.printf("  %s %d: UNREACHABLE\n", nodePrefix, i);
                    } else {
                        System.out.printf("  %s %d: %d\n", nodePrefix, i, distances[i]);
                    }
                }
            }

            List<Integer> reconstructPath(int target) {
                List<Integer> path = new ArrayList<>();
                if (distances[target] == Long.MAX_VALUE || distances[target] == Long.MIN_VALUE) {
                    return path; // No path
                }
                for (int at = target; at != -1; at = parent[at]) {
                    path.add(at);
                }
                Collections.reverse(path);
                return path;
            }

            Path getLongestPath() {
                long maxDist = Long.MIN_VALUE;
                int targetNode = -1;
                for (int i=0; i < V; i++) {
                    if (distances[i] > maxDist) {
                        maxDist = distances[i];
                        targetNode = i;
                    }
                }
                if (targetNode == -1 || maxDist == Long.MIN_VALUE) {
                    return new Path(Collections.emptyList(), Long.MIN_VALUE);
                }
                return new Path(reconstructPath(targetNode), maxDist);
            }
        }

        static class Path {
            List<Integer> path;
            long distance;
            Path(List<Integer> path, long distance) { this.path = path; this.distance = distance; }
        }

        DAGPathFinder(Graph g, List<Integer> topoOrder, Metrics metrics) {
            this.g = g;
            this.topoOrder = topoOrder;
            this.metrics = metrics;
        }


        public PathResult run(int source, boolean findLongest) {
            PathResult result = new PathResult(g.V, findLongest);
            result.distances[source] = 0;

            for (int u : topoOrder) {
                if (result.distances[u] == (findLongest ? Long.MIN_VALUE : Long.MAX_VALUE)) {
                    continue;
                }

                for (Edge e : g.adj.get(u)) {
                    metrics.increment("dagsp.relaxations"); // [cite: 35]
                    int v = e.to;

                    long weight = findLongest ? e.maxWeight : e.minWeight;

                    long newDist = result.distances[u] + weight;

                    if (findLongest) {
                        if (newDist > result.distances[v]) {
                            result.distances[v] = newDist;
                            result.parent[v] = u;
                        }
                    } else {
                        if (newDist < result.distances[v]) {
                            result.distances[v] = newDist;
                            result.parent[v] = u;
                        }
                    }
                }
            }
            return result;
        }
    }
}