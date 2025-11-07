package org.example;

import com.google.gson.Gson;
import org.example.graph.Graph;
import org.example.graph.scc.KosarajuSCC;
import org.example.graph.topo.TopologicalSort;
import org.example.graph.dagsp.DAGPathFinder;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "data/small_1.json";

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

            System.out.println("=== Smart City Scheduling Analysis ===");
            System.out.println("Dataset: " + filename);
            System.out.println("Loaded graph with " + data.n + " nodes and " + data.edges.size() + " edges.");
            System.out.println("Original Source Node: " + data.source);
            System.out.println("Weight Model: " + data.weight_model);
            System.out.println("---");

            System.out.println("### 1.1 Strongly Connected Components ###");
            metrics.start("SCC");
            KosarajuSCC sccFinder = new KosarajuSCC(g, metrics);
            sccFinder.run();
            metrics.stop("SCC");

            List<List<Integer>> sccs = sccFinder.getSccs();
            System.out.println("Found " + sccs.size() + " SCCs:");
            for (int i = 0; i < sccs.size(); i++) {
                System.out.println("  SCC " + i + " (Size " + sccs.get(i).size() + "): " + sccs.get(i));
            }

            System.out.println("\nBuilding condensation graph... ");
            Graph condensationGraph = sccFinder.getCondensationGraph();
            System.out.println("Condensation graph has " + condensationGraph.V + " nodes (one per SCC). Edges:");
            condensationGraph.print();
            System.out.println("---");

            System.out.println("### 1.2 Topological Sort of Condensation DAG ###");
            metrics.start("TopoSort");
            TopologicalSort topoSort = new TopologicalSort(condensationGraph, metrics);
            List<Integer> topoOrder = topoSort.run();
            metrics.stop("TopoSort");

            if (topoOrder == null) {
                System.out.println("Error: Cycle detected in condensation graph. This should not happen.");
                return;
            } else {
                System.out.println("Topological Order of SCCs: " + topoOrder);
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
            for (int i = topoOrder.size() - 1; i >= 0; i--) {
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
            metrics.printReport();

        } catch (Exception e) {
            System.err.println("Error processing dataset: " + e.getMessage());
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
}
