package org.example.graph;


public class Edge {
    public int to;
    public int weight;
    public int minWeight = Integer.MAX_VALUE;
    public int maxWeight = Integer.MIN_VALUE;


    public Edge(int to, int weight) {
        this.to = to;
        this.weight = weight;
    }


    public Edge(int to, int min, int max) {
        this.to = to;
        this.minWeight = min;
        this.maxWeight = max;
    }
}

