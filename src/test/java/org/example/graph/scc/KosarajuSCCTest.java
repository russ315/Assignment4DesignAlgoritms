package org.example.graph.scc;

import org.example.Metrics;
import org.example.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class KosarajuSCCTest {
    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new Metrics();
    }

    @Test
    void testSingleNode() {
        Graph g = new Graph(1);
        KosarajuSCC scc = new KosarajuSCC(g, metrics);
        scc.run();
        
        List<List<Integer>> sccs = scc.getSccs();
        assertEquals(1, sccs.size());
        assertEquals(1, sccs.get(0).size());
        assertEquals(0, sccs.get(0).get(0));
    }

    @Test
    void testSimpleCycle() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        
        KosarajuSCC scc = new KosarajuSCC(g, metrics);
        scc.run();
        
        List<List<Integer>> sccs = scc.getSccs();
        assertEquals(1, sccs.size());
        assertEquals(3, sccs.get(0).size());
    }

    @Test
    void testDAG() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        
        KosarajuSCC scc = new KosarajuSCC(g, metrics);
        scc.run();
        
        List<List<Integer>> sccs = scc.getSccs();
        assertEquals(4, sccs.size());
    }

    @Test
    void testMultipleSCCs() {
        Graph g = new Graph(6);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 5, 1);
        g.addEdge(5, 3, 1);
        g.addEdge(0, 3, 1);
        
        KosarajuSCC scc = new KosarajuSCC(g, metrics);
        scc.run();
        
        List<List<Integer>> sccs = scc.getSccs();
        assertEquals(2, sccs.size());
        
        Graph condensation = scc.getCondensationGraph();
        assertNotNull(condensation);
        assertEquals(2, condensation.V);
    }

    @Test
    void testCondensationGraph() {
        Graph g = new Graph(5);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(0, 3, 2);
        
        KosarajuSCC scc = new KosarajuSCC(g, metrics);
        scc.run();
        
        Graph condensation = scc.getCondensationGraph();
        assertNotNull(condensation);
        assertTrue(condensation.V >= 2);
    }
}

