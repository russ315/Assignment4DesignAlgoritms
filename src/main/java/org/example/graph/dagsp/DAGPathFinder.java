package org.example.graph.dagsp;

import org.example.graph.Graph;
import org.example.graph.Edge;
import org.example.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DAGPathFinder {
    private Graph g;
    private List<Integer> topoOrder;
    private Metrics metrics;


    public static class PathResult {
        public long[] distances;
        public int[] parent;
        public int V;
        public boolean isLongestPath;


        public PathResult(int V, boolean isLongestPath) {
            this.V = V;
            this.isLongestPath = isLongestPath;
            distances = new long[V];
            parent = new int[V];
            java.util.Arrays.fill(parent, -1);
            java.util.Arrays.fill(distances, isLongestPath ? Long.MIN_VALUE : Long.MAX_VALUE);
        }


        public void printDistances(String nodePrefix) {
            for (int i = 0; i < V; i++) {
                if (distances[i] == Long.MAX_VALUE || distances[i] == Long.MIN_VALUE) {
                    System.out.printf("  %s %d: UNREACHABLE\n", nodePrefix, i);
                } else {
                    System.out.printf("  %s %d: %d\n", nodePrefix, i, distances[i]);
                }
            }
        }


        public List<Integer> reconstructPath(int target) {
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


        public Path getLongestPath() {
            long maxDist = Long.MIN_VALUE;
            int targetNode = -1;
            for (int i = 0; i < V; i++) {
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


    public static class Path {
        public List<Integer> path;
        public long distance;


        public Path(List<Integer> path, long distance) {
            this.path = path;
            this.distance = distance;
        }
    }


    public DAGPathFinder(Graph g, List<Integer> topoOrder, Metrics metrics) {
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
                metrics.increment("dagsp.relaxations");
                int v = e.to;

                long weight = findLongest ? e.maxWeight : e.minWeight;
                if (findLongest && e.maxWeight == Integer.MIN_VALUE) {
                    weight = e.weight;
                } else if (!findLongest && e.minWeight == Integer.MAX_VALUE) {
                    weight = e.weight;
                }

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

