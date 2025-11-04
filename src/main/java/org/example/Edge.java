package org.example;

import java.util.ArrayList;
import java.util.List;

class Edge {
    int to;
    int weight;
    int minWeight = Integer.MAX_VALUE;
    int maxWeight = Integer.MIN_VALUE;

    Edge(int to, int weight) {
        this.to = to;
        this.weight = weight;
    }

    Edge(int to, int min, int max) {
        this.to = to;
        this.minWeight = min;
        this.maxWeight = max;
    }
}
