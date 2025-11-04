package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class TopologicalSort {
    private Graph g;
    private Metrics metrics;
    private Stack<Integer> stack;
    private int[] states;

    TopologicalSort(Graph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
        this.stack = new Stack<>();
        this.states = new int[g.V];
    }

    public List<Integer> run() {
        for (int i = 0; i < g.V; i++) {
            if (states[i] == 0) {
                if (hasCycleDfs(i)) {
                    return null;
                }
            }
        }

        List<Integer> order = new ArrayList<>(g.V);
        while (!stack.isEmpty()) {
            order.add(stack.pop());
        }
        return order;
    }

    private boolean hasCycleDfs(int u) {
        metrics.increment("topo.dfs.visits");
        states[u] = 1; // Mark as visiting

        for (Edge e : g.adj.get(u)) {
            metrics.increment("topo.dfs.edges");
            int v = e.to;
            if (states[v] == 1) {
                return true; // Cycle!
            }
            if (states[v] == 0) {
                if (hasCycleDfs(v)) {
                    return true;
                }
            }
        }

        states[u] = 2; // Mark as visited
        stack.push(u);
        return false;
    }
}