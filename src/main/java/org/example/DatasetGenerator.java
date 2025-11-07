package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class DatasetGenerator {
    private static final Random random = new Random(42);


    public static List<EdgeData> generateDAG(int n, double density) {
        List<EdgeData> edges = new ArrayList<>();
        int maxEdges = (int) (n * (n - 1) / 2 * density);
        
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n && edges.size() < maxEdges; j++) {
                if (random.nextDouble() < density) {
                    edges.add(new EdgeData(i, j, random.nextInt(10) + 1));
                }
            }
        }
        return edges;
    }

    public static List<EdgeData> generateSingleCycle(int n, int cycleSize) {
        List<EdgeData> edges = new ArrayList<>();
        for (int i = 0; i < cycleSize; i++) {
            edges.add(new EdgeData(i, (i + 1) % cycleSize, random.nextInt(10) + 1));
        }
        
        for (int i = cycleSize; i < n; i++) {
            if (i > 0) {
                edges.add(new EdgeData(i - 1, i, random.nextInt(10) + 1));
            }
        }
        
        return edges;
    }


    public static List<EdgeData> generateMultipleSCCs(int n, int numSCCs) {
        List<EdgeData> edges = new ArrayList<>();
        int verticesPerSCC = n / numSCCs;
        
        for (int scc = 0; scc < numSCCs; scc++) {
            int start = scc * verticesPerSCC;
            int end = Math.min(start + verticesPerSCC, n);
            int size = end - start;
            
            if (size >= 2) {
                for (int i = 0; i < size - 1; i++) {
                    edges.add(new EdgeData(start + i, start + i + 1, random.nextInt(10) + 1));
                }
                edges.add(new EdgeData(start + size - 1, start, random.nextInt(10) + 1));
            }
        }
        
        for (int scc1 = 0; scc1 < numSCCs - 1; scc1++) {
            int v1 = scc1 * verticesPerSCC;
            int scc2 = scc1 + 1;
            int v2 = scc2 * verticesPerSCC;
            edges.add(new EdgeData(v1, v2, random.nextInt(10) + 1));
        }
        
        return edges;
    }
    public static List<EdgeData> generateDense(int n, double density) {
        List<EdgeData> edges = new ArrayList<>();
        Set<String> edgeSet = new HashSet<>();
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && random.nextDouble() < density) {
                    String edgeKey = i + "," + j;
                    if (!edgeSet.contains(edgeKey)) {
                        edges.add(new EdgeData(i, j, random.nextInt(10) + 1));
                        edgeSet.add(edgeKey);
                    }
                }
            }
        }
        return edges;
    }


    public static List<EdgeData> generateSparse(int n) {
        List<EdgeData> edges = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            edges.add(new EdgeData(i - 1, i, random.nextInt(10) + 1));
        }
        for (int i = 0; i < n / 2; i++) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u != v) {
                edges.add(new EdgeData(u, v, random.nextInt(10) + 1));
            }
        }
        return edges;
    }


    public static void writeDataset(String filename, int n, List<EdgeData> edges, 
                                    int source, String description) throws IOException {
        GraphData data = new GraphData();
        data.directed = true;
        data.n = n;
        data.edges = edges;
        data.source = source;
        data.weight_model = "edge";

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
        }
        System.out.println("Generated: " + filename + " (" + description + ")");
    }


    static class EdgeData {
        int u, v, w;
        EdgeData(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }


    static class GraphData {
        boolean directed;
        int n;
        List<EdgeData> edges;
        int source;
        String weight_model;
    }


    public static void main(String[] args) throws IOException {
        java.io.File dataDir = new java.io.File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        System.out.println("Generating small datasets...");
        writeDataset("data/small_1.json", 8, generateSingleCycle(8, 3), 0, 
                    "Small: 8 nodes, single cycle (size 3)");
        writeDataset("data/small_2.json", 10, generateDAG(10, 0.3), 0, 
                    "Small: 10 nodes, pure DAG, sparse");
        writeDataset("data/small_3.json", 7, generateMultipleSCCs(7, 2), 0, 
                    "Small: 7 nodes, 2 SCCs");

        System.out.println("\nGenerating medium datasets...");
        writeDataset("data/medium_1.json", 15, generateMultipleSCCs(15, 3), 0, 
                    "Medium: 15 nodes, 3 SCCs");
        writeDataset("data/medium_2.json", 18, generateDense(18, 0.2), 0, 
                    "Medium: 18 nodes, dense graph");
        writeDataset("data/medium_3.json", 12, generateSparse(12), 0, 
                    "Medium: 12 nodes, sparse graph");


        System.out.println("\nGenerating large datasets...");
        writeDataset("data/large_1.json", 30, generateMultipleSCCs(30, 5), 0, 
                    "Large: 30 nodes, 5 SCCs");
        writeDataset("data/large_2.json", 40, generateDense(40, 0.15), 0, 
                    "Large: 40 nodes, dense graph");
        writeDataset("data/large_3.json", 25, generateSparse(25), 0, 
                    "Large: 25 nodes, sparse graph");

        System.out.println("\nAll datasets generated successfully!");
    }
}

