package org.example.graph.topo;

import org.example.Metrics;
import org.example.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class TopologicalSortTest {
    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new Metrics();
    }

    @Test
    void testSingleNode() {
        Graph g = new Graph(1);
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        assertNotNull(order);
        assertEquals(1, order.size());
        assertEquals(0, order.get(0));
    }

    @Test
    void testSimpleDAG() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        assertNotNull(order);
        assertEquals(4, order.size());
        int pos0 = order.indexOf(0);
        int pos1 = order.indexOf(1);
        int pos2 = order.indexOf(2);
        int pos3 = order.indexOf(3);
        assertTrue(pos0 < pos1);
        assertTrue(pos1 < pos2);
        assertTrue(pos2 < pos3);
    }

    @Test
    void testComplexDAG() {
        Graph g = new Graph(5);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 1);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        assertNotNull(order);
        assertEquals(5, order.size());
        
        int pos0 = order.indexOf(0);
        int pos3 = order.indexOf(3);
        int pos4 = order.indexOf(4);
        assertTrue(pos0 < pos3);
        assertTrue(pos3 < pos4);
    }

    @Test
    void testCycleDetection() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        assertNull(order);
    }

    @Test
    void testEmptyGraph() {
        Graph g = new Graph(3);
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        assertNotNull(order);
        assertEquals(3, order.size());
    }
}

