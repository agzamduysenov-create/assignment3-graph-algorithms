package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// random graphs for experiments and tests
// same seed -> same graph, no self loops and no duplicate edges
// weights are from 1 to MAX_WEIGHT
public class GraphGenerator {
    public static final int MAX_WEIGHT = 100;

    // connected undirected graph with exactly e edges
    // first random spanning tree (v-1 edges) so graph is always connected,
    // then random extra edges until we have e edges
    public static Graph connectedUndirected(int v, int e, long seed) {
        if (e < v - 1 || e > (long) v * (v - 1) / 2) {
            throw new IllegalArgumentException("need V-1 <= E <= V(V-1)/2");
        }
        Random r = new Random(seed);
        Graph g = new Graph(v, false);
        Set<Long> used = new HashSet<>();
        int[] perm = shuffledVertices(v, r);
        for (int i = 1; i < v; i++) {
            int a = perm[i];
            int b = perm[r.nextInt(i)];   // connect to some vertex before it
            used.add(undirectedKey(a, b, v));
            g.addEdge(a, b, 1 + r.nextInt(MAX_WEIGHT));
        }
        while (g.edgeCount() < e) {
            int a = r.nextInt(v);
            int b = r.nextInt(v);
            if (a != b && used.add(undirectedKey(a, b, v))) {
                g.addEdge(a, b, 1 + r.nextInt(MAX_WEIGHT));
            }
        }
        return g;
    }

    // random directed graph with exactly e edges (random pairs u -> v, u != v)
    // when e is much bigger than v it almost always has cycles
    public static Graph randomDirected(int v, int e, long seed) {
        if (e > (long) v * (v - 1)) {
            throw new IllegalArgumentException("too many edges");
        }
        Random r = new Random(seed);
        Graph g = new Graph(v, true);
        Set<Long> used = new HashSet<>();
        while (g.edgeCount() < e) {
            int a = r.nextInt(v);
            int b = r.nextInt(v);
            if (a != b && used.add((long) a * v + b)) {
                g.addEdge(a, b, 1 + r.nextInt(MAX_WEIGHT));
            }
        }
        return g;
    }

    // random DAG with exactly e edges
    // we take random order of vertices and edges go only from earlier to later vertex,
    // so cycle is not possible
    public static Graph randomDAG(int v, int e, long seed) {
        if (e > (long) v * (v - 1) / 2) {
            throw new IllegalArgumentException("too many edges for DAG");
        }
        Random r = new Random(seed);
        Graph g = new Graph(v, true);
        int[] order = shuffledVertices(v, r);
        Set<Long> used = new HashSet<>();
        while (g.edgeCount() < e) {
            int i = r.nextInt(v);
            int j = r.nextInt(v);
            if (i == j) {
                continue;
            }
            int lo = Math.min(i, j);
            int hi = Math.max(i, j);
            if (used.add((long) lo * v + hi)) {
                g.addEdge(order[lo], order[hi], 1 + r.nextInt(MAX_WEIGHT));
            }
        }
        return g;
    }

    // 0..v-1 in random order (Fisher-Yates shuffle)
    private static int[] shuffledVertices(int v, Random r) {
        int[] perm = new int[v];
        for (int i = 0; i < v; i++) {
            perm[i] = i;
        }
        for (int i = v - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }
        return perm;
    }

    // one number for pair (a, b), order does not matter
    private static long undirectedKey(int a, int b, int v) {
        return (long) Math.min(a, b) * v + Math.max(a, b);
    }
}
