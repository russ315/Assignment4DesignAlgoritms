package org.example.graph.dagsp;

import org.example.Metrics;
import org.example.graph.Graph;
import org.example.graph.topo.TopologicalSort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class DAGPathFinderTest {
    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new Metrics();
    }

    @Test
    void testShortestPathSimple() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 3);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        DAGPathFinder finder = new DAGPathFinder(g, order, metrics);
        DAGPathFinder.PathResult result = finder.run(0, false);
        
        assertEquals(0, result.distances[0]);
        assertEquals(1, result.distances[1]);
        assertEquals(3, result.distances[2]);
        assertEquals(6, result.distances[3]);
    }

    @Test
    void testLongestPathSimple() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 3);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        DAGPathFinder finder = new DAGPathFinder(g, order, metrics);
        DAGPathFinder.PathResult result = finder.run(0, true);
        
        assertEquals(0, result.distances[0]);
        assertEquals(1, result.distances[1]);
        assertEquals(3, result.distances[2]);
        assertEquals(6, result.distances[3]);
    }

    @Test
    void testUnreachableNodes() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);

        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        DAGPathFinder finder = new DAGPathFinder(g, order, metrics);
        DAGPathFinder.PathResult result = finder.run(0, false);
        
        assertEquals(0, result.distances[0]);
        assertEquals(1, result.distances[1]);
        assertEquals(Long.MAX_VALUE, result.distances[2]);
        assertEquals(Long.MAX_VALUE, result.distances[3]);
    }

    @Test
    void testPathReconstruction() {
        Graph g = new Graph(5);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 3);
        g.addEdge(0, 4, 10);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        DAGPathFinder finder = new DAGPathFinder(g, order, metrics);
        DAGPathFinder.PathResult result = finder.run(0, false);
        
        List<Integer> path = result.reconstructPath(3);
        assertFalse(path.isEmpty());
        assertEquals(0, path.get(0));
        assertEquals(3, path.get(path.size() - 1));
    }

    @Test
    void testCriticalPath() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 5);
        g.addEdge(0, 2, 3);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 4);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        DAGPathFinder finder = new DAGPathFinder(g, order, metrics);
        DAGPathFinder.PathResult result = finder.run(0, true);
        
        DAGPathFinder.Path criticalPath = result.getLongestPath();
        assertNotNull(criticalPath);
        assertFalse(criticalPath.path.isEmpty());
        assertTrue(criticalPath.distance >= 7);
    }

    @Test
    void testCondensationEdges() {
        Graph g = new Graph(3);
        g.addCondensationEdge(0, 1, 5);
        g.addCondensationEdge(0, 1, 3);
        g.addCondensationEdge(1, 2, 2);
        
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.run();
        
        DAGPathFinder finder = new DAGPathFinder(g, order, metrics);
        DAGPathFinder.PathResult shortest = finder.run(0, false);
        DAGPathFinder.PathResult longest = finder.run(0, true);
        
        assertTrue(shortest.distances[2] <= longest.distances[2]);
    }
}

