package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

class KosarajuSCC {
    private Graph g;
    private Metrics metrics;
    private boolean[] visited;
    private int[] sccMap;
    private List<List<Integer>> sccs;
    private Stack<Integer> finishStack;
    private int sccCount;
    private Graph condensationGraph;

    KosarajuSCC(Graph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
        this.visited = new boolean[g.V];
        this.sccMap = new int[g.V];
        this.finishStack = new Stack<>();
        this.sccs = new ArrayList<>();
    }

    public void run() {
        for (int i = 0; i < g.V; i++) {
            if (!visited[i]) {
                dfs1(i);
            }
        }

        Graph gT = g.getTranspose();

        Arrays.fill(visited, false);
        sccCount = 0;
        while (!finishStack.isEmpty()) {
            int u = finishStack.pop();
            if (!visited[u]) {
                List<Integer> currentScc = new ArrayList<>();
                dfs2(gT, u, currentScc);
                sccs.add(currentScc);
                sccCount++;
            }
        }

        buildCondensationGraph();
    }

    private void dfs1(int u) {
        metrics.increment("scc.dfs1.visits"); // [cite: 35]
        visited[u] = true;
        for (Edge e : g.adj.get(u)) {
            metrics.increment("scc.dfs1.edges");
            if (!visited[e.to]) {
                dfs1(e.to);
            }
        }
        finishStack.push(u);
    }

    private void dfs2(Graph gT, int u, List<Integer> currentScc) {
        metrics.increment("scc.dfs2.visits");
        visited[u] = true;
        currentScc.add(u);
        sccMap[u] = sccCount;
        for (Edge e : gT.adj.get(u)) {
            metrics.increment("scc.dfs2.edges");
            if (!visited[e.to]) {
                dfs2(gT, e.to, currentScc);
            }
        }
    }

    private void buildCondensationGraph() {
        condensationGraph = new Graph(sccCount);
        for (int u = 0; u < g.V; u++) {
            for (Edge e : g.adj.get(u)) {
                int v = e.to;
                int sccU = sccMap[u];
                int sccV = sccMap[v];
                if (sccU != sccV) {
                    condensationGraph.addCondensationEdge(sccU, sccV, e.weight);
                }
            }
        }
    }

    public List<List<Integer>> getSccs() { return sccs; }
    public Graph getCondensationGraph() { return condensationGraph; }
    public int getSccId(int nodeId) { return sccMap[nodeId]; }
}