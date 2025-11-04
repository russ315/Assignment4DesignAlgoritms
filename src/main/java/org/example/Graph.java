package org.example;


import java.util.ArrayList;
import java.util.List;

class Graph {
    int V; // Number of vertices
    List<List<Edge>> adj;

    Graph(int V) {
        this.V = V;
        adj = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }
    }

    void addEdge(int u, int v, int w) {
        adj.get(u).add(new Edge(v, w));
    }

    void addCondensationEdge(int u, int v, int w) {
        for (Edge e : adj.get(u)) {
            if (e.to == v) {
                // Update existing edge weights
                e.minWeight = Math.min(e.minWeight, w);
                e.maxWeight = Math.max(e.maxWeight, w);
                return;
            }
        }
        adj.get(u).add(new Edge(v, w, w));
    }

    Graph getTranspose() {
        Graph gT = new Graph(V);
        for (int u = 0; u < V; u++) {
            for (Edge e : adj.get(u)) {
                gT.addEdge(e.to, u, e.weight);
            }
        }
        return gT;
    }

    void print() {
        for (int u = 0; u < V; u++) {
            if (adj.get(u).isEmpty()) continue;
            System.out.print("  Node " + u + " -> ");
            List<String> neighbors = new ArrayList<>();
            for (Edge e : adj.get(u)) {
                // Check if it's a condensation edge
                if (e.minWeight != Integer.MAX_VALUE) {
                    neighbors.add(String.format("%d (minW: %d, maxW: %d)", e.to, e.minWeight, e.maxWeight));
                } else {
                    neighbors.add(String.format("%d (w: %d)", e.to, e.weight));
                }
            }
            System.out.println(String.join(", ", neighbors));
        }
    }
}