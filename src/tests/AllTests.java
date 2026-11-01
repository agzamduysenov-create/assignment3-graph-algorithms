package tests;

import connectivity.KosarajuSCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// correctness tests without libraries, run: java -cp out tests.AllTests
// if some check fails, exit code is 1
public class AllTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        dsuBasic();
        dsuVariantsAgree();
        mstSmallKnownGraph();
        mstDisconnectedGraph();
        mstMultipleValidTrees();
        mstDuplicateWeightsBruteForce();
        mstKruskalEqualsPrimRandom();
        singleVertexGraph();
        topoValidForDAGs();
        topoDetectsCycles();
        sccKnownGraph();
        sccPartitionAndReachability();
        dijkstraKnownGraph();
        dijkstraUnreachable();
        dijkstraMatchesBellmanFord();
        dijkstraRejectsNegativeWeights();
        generatorIsReproducibleAndCorrect();

        System.out.println("\nPassed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(boolean condition, String name) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    // dsu tests

    private static void dsuBasic() {
        DSU d = new DSU(6);
        check(d.components() == 6, "DSU: starts with n singleton sets");
        check(d.union(0, 1) && d.union(1, 2), "DSU: union of different sets returns true");
        check(!d.union(0, 2), "DSU: union inside one set returns false");
        check(d.connected(0, 2) && !d.connected(0, 3), "DSU: connected() is correct");
        check(d.components() == 4, "DSU: component count after unions");
    }

    private static void dsuVariantsAgree() {
        int n = 200;
        DSU[] variants = {new DSU(n, false, false), new DSU(n, true, false), new DSU(n, false, true), new DSU(n, true, true)};
        Random r = new Random(7);
        boolean same = true;
        for (int i = 0; i < 300; i++) {
            int a = r.nextInt(n);
            int b = r.nextInt(n);
            for (DSU d : variants) {
                d.union(a, b);
            }
            int x = r.nextInt(n);
            int y = r.nextInt(n);
            boolean expected = variants[0].connected(x, y);
            for (DSU d : variants) {
                if (d.connected(x, y) != expected || d.components() != variants[0].components()) {
                    same = false;
                }
            }
        }
        check(same, "DSU: all 4 variants (with/without optimizations) give the same sets");
    }

    // mst tests

    private static void mstSmallKnownGraph() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(0, 2, 3);
        g.addEdge(2, 3, 4);
        g.addEdge(1, 3, 5);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check(k.totalWeight == 7 && p.totalWeight == 7, "MST: small graph has weight 7 (Kruskal and Prim)");
        check(k.edges.size() == 3 && p.edges.size() == 3, "MST: tree has V-1 edges");
        check(k.connected && p.connected && g.isConnected(), "MST: connected graph detected as connected");
    }

    private static void mstDisconnectedGraph() {
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 3);
        g.addEdge(1, 2, 1);
        g.addEdge(0, 2, 2);
        g.addEdge(3, 4, 7);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check(!k.connected && !p.connected && !g.isConnected(), "MST: disconnected graph detected");
        check(k.components == 2 && p.components == 2, "MST: disconnected graph has 2 components");
        check(k.totalWeight == 10 && p.totalWeight == 10, "MST: spanning forest weight is equal (10)");
    }

    private static void mstMultipleValidTrees() {
        // square with diagonal, all weights are equal -> many MSTs with same weight
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 5);
        g.addEdge(2, 3, 5);
        g.addEdge(3, 0, 5);
        g.addEdge(0, 2, 5);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check(k.totalWeight == 15 && p.totalWeight == 15, "MST: multiple valid MSTs -> same weight 15");
        check(isSpanningTree(g, k.edges) && isSpanningTree(g, p.edges), "MST: both results are valid spanning trees");
    }

    private static void mstDuplicateWeightsBruteForce() {
        Random r = new Random(11);
        boolean ok = true;
        for (int t = 0; t < 50; t++) {
            int v = 3 + r.nextInt(4);   // 3..6 vertices
            int maxE = v * (v - 1) / 2;
            int e = v - 1 + r.nextInt(Math.min(10, maxE) - (v - 1) + 1);
            Graph g = withSmallWeights(GraphGenerator.connectedUndirected(v, e, r.nextLong()), r);
            long brute = bruteForceMst(g);
            if (Kruskal.run(g).totalWeight != brute || Prim.run(g).totalWeight != brute) {
                ok = false;
            }
        }
        check(ok, "MST: duplicate weights (1..3) - Kruskal and Prim match brute force on 50 graphs");
    }

    private static void mstKruskalEqualsPrimRandom() {
        Random r = new Random(3);
        boolean ok = true;
        for (int t = 0; t < 100; t++) {
            int v = 2 + r.nextInt(60);
            int maxE = v * (v - 1) / 2;
            int e = v - 1 + r.nextInt(maxE - (v - 1) + 1);
            Graph g = GraphGenerator.connectedUndirected(v, e, t);
            MSTResult k = Kruskal.run(g);
            MSTResult p = Prim.run(g);
            boolean good = k.totalWeight == p.totalWeight && k.connected && p.connected
                    && isSpanningTree(g, k.edges) && isSpanningTree(g, p.edges);
            if (!good) {
                ok = false;
            }
        }
        check(ok, "MST: Kruskal and Prim have the same weight on 100 random connected graphs");
    }

    private static void singleVertexGraph() {
        Graph u = new Graph(1, false);
        MSTResult k = Kruskal.run(u);
        MSTResult p = Prim.run(u);
        check(k.totalWeight == 0 && p.totalWeight == 0 && k.connected && p.connected, "Single vertex: MST weight 0, connected");
        Graph d = new Graph(1, true);
        TopologicalSort.Result topo = TopologicalSort.run(d);
        check(topo.isDAG && topo.order.equals(List.of(0)), "Single vertex: topological order [0]");
        check(KosarajuSCC.run(d).count() == 1, "Single vertex: exactly one SCC");
        check(Dijkstra.run(u, 0).dist[0] == 0, "Single vertex: Dijkstra distance 0");
    }

    // topological order tests

    private static void topoValidForDAGs() {
        Graph g = new Graph(6, true);
        int[][] edges = {{5, 2}, {5, 0}, {4, 0}, {4, 1}, {2, 3}, {3, 1}};
        for (int[] x : edges) {
            g.addEdge(x[0], x[1], 1);
        }
        TopologicalSort.Result r = TopologicalSort.run(g);
        check(r.isDAG && TopologicalSort.isValidOrder(g, r.order), "Topo: hand-made DAG gives a valid order");

        boolean ok = true;
        for (int t = 0; t < 100; t++) {
            int v = 1 + t % 40;
            int e = Math.min(v * (v - 1) / 2, t * 2);
            Graph dag = GraphGenerator.randomDAG(v, e, t);
            TopologicalSort.Result res = TopologicalSort.run(dag);
            if (!res.isDAG || !TopologicalSort.isValidOrder(dag, res.order)) {
                ok = false;
            }
        }
        check(ok, "Topo: valid order for 100 random DAGs");
    }

    private static void topoDetectsCycles() {
        Graph cycle = new Graph(4, true);
        cycle.addEdge(0, 1, 1);
        cycle.addEdge(1, 2, 1);
        cycle.addEdge(2, 3, 1);
        cycle.addEdge(3, 1, 1);
        TopologicalSort.Result r = TopologicalSort.run(cycle);
        check(!r.isDAG && r.order.isEmpty(), "Topo: cycle 1->2->3->1 is detected");
        check(new HashSet<>(r.blockedVertices).equals(Set.of(1, 2, 3)), "Topo: blocked vertices are the cycle {1,2,3}");

        Graph selfLoop = new Graph(2, true);
        selfLoop.addEdge(0, 1, 1);
        selfLoop.addEdge(1, 1, 1);
        check(!TopologicalSort.run(selfLoop).isDAG, "Topo: self-loop is detected as a cycle");

        boolean ok = true;
        for (int t = 0; t < 50; t++) {
            Graph dag = GraphGenerator.randomDAG(30, 60, t);
            if (!TopologicalSort.run(dag).isDAG) {
                ok = false;
            }
            // add reversed copy of edge u -> v, so v -> u makes a cycle
            Edge e = dag.edges().get(t % dag.edgeCount());
            dag.addEdge(e.to, e.from, 1);
            if (TopologicalSort.run(dag).isDAG) {
                ok = false;
            }
        }
        check(ok, "Topo: DAG + back edge detected as cyclic (50 graphs)");
    }

    // scc tests

    private static void sccKnownGraph() {
        Graph g = new Graph(8, true);
        int[][] edges = {{0, 1}, {1, 2}, {2, 0}, {2, 3}, {3, 4}, {4, 5}, {5, 3}, {5, 6}, {6, 7}};
        for (int[] x : edges) {
            g.addEdge(x[0], x[1], 1);
        }
        KosarajuSCC.Result r = KosarajuSCC.run(g);
        Set<Set<Integer>> got = new HashSet<>();
        for (List<Integer> c : r.components) {
            got.add(new HashSet<>(c));
        }
        Set<Set<Integer>> expected = Set.of(Set.of(0, 1, 2), Set.of(3, 4, 5), Set.of(6), Set.of(7));
        check(got.equals(expected), "SCC: known graph gives {0,1,2} {3,4,5} {6} {7}");

        Graph dag = GraphGenerator.randomDAG(50, 200, 1);
        check(KosarajuSCC.run(dag).count() == 50, "SCC: in a DAG every vertex is its own SCC");
    }

    private static void sccPartitionAndReachability() {
        boolean partition = true;
        boolean reach = true;
        for (int t = 0; t < 50; t++) {
            int v = 2 + t % 25;
            int e = Math.min(v * (v - 1), v + t);
            Graph g = GraphGenerator.randomDirected(v, e, t);
            KosarajuSCC.Result r = KosarajuSCC.run(g);

            // every vertex must be in exactly one component
            int[] seen = new int[v];
            for (List<Integer> c : r.components) {
                for (int x : c) {
                    seen[x]++;
                }
            }
            for (int s : seen) {
                if (s != 1) {
                    partition = false;
                }
            }

            // same SCC only if a and b can reach each other
            boolean[][] closure = transitiveClosure(g);
            for (int a = 0; a < v; a++) {
                for (int b = 0; b < v; b++) {
                    boolean sameScc = r.componentOf[a] == r.componentOf[b];
                    boolean mutual = closure[a][b] && closure[b][a];
                    if (sameScc != mutual) {
                        reach = false;
                    }
                }
            }
        }
        check(partition, "SCC: every vertex belongs to exactly one SCC (50 graphs)");
        check(reach, "SCC: same SCC <=> mutually reachable (brute-force check)");
    }

    // dijkstra tests

    private static void dijkstraKnownGraph() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 10);
        g.addEdge(0, 2, 3);
        g.addEdge(2, 1, 4);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 8);
        g.addEdge(3, 4, 7);
        g.addEdge(2, 4, 20);
        Dijkstra.Result r = Dijkstra.run(g, 0);
        check(Arrays.equals(r.dist, new long[]{0, 7, 3, 9, 16}), "Dijkstra: distances on known graph");
        check(r.pathTo(4).equals(List.of(0, 2, 1, 3, 4)), "Dijkstra: path 0->2->1->3->4");
    }

    private static void dijkstraUnreachable() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 2);
        g.addEdge(1, 2, 2);
        g.addEdge(3, 0, 1);   // 3 can go to 0, but 0 can not go to 3
        Dijkstra.Result r = Dijkstra.run(g, 0);
        check(!r.isReachable(3) && r.dist[3] == Dijkstra.INF, "Dijkstra: unreachable vertex has distance INF");
        check(r.pathTo(3).isEmpty() && r.reachableCount() == 3, "Dijkstra: unreachable vertex has empty path");
    }

    private static void dijkstraMatchesBellmanFord() {
        boolean ok = true;
        for (int t = 0; t < 100; t++) {
            int v = 2 + t % 40;
            int e = Math.min(v * (v - 1), 2 * v + t);
            Graph g = GraphGenerator.randomDirected(v, e, 1000 + t);
            Dijkstra.Result r = Dijkstra.run(g, 0);
            if (!Arrays.equals(r.dist, bellmanFord(g, 0))) {
                ok = false;
            }
        }
        check(ok, "Dijkstra: equals Bellman-Ford on 100 random directed graphs (incl. cycles, unreachable)");
    }

    private static void dijkstraRejectsNegativeWeights() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 3);
        g.addEdge(2, 1, -3);
        boolean thrown = false;
        try {
            Dijkstra.run(g, 0);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        check(thrown, "Dijkstra: negative edge weight is rejected");
    }

    // generator tests

    private static void generatorIsReproducibleAndCorrect() {
        Graph a = GraphGenerator.connectedUndirected(100, 500, 42);
        Graph b = GraphGenerator.connectedUndirected(100, 500, 42);
        boolean same = a.edgeCount() == b.edgeCount();
        for (int i = 0; same && i < a.edgeCount(); i++) {
            Edge x = a.edges().get(i);
            Edge y = b.edges().get(i);
            same = x.from == y.from && x.to == y.to && x.weight == y.weight;
        }
        check(same, "Generator: same seed -> identical graph");
        check(a.edgeCount() == 500 && a.isConnected(), "Generator: connected graph with exactly E edges");

        Set<Long> keys = new HashSet<>();
        boolean simple = true;
        for (Edge e : a.edges()) {
            if (e.from == e.to) {
                simple = false;   // self loop
            } else {
                long key = (long) Math.min(e.from, e.to) * 100 + Math.max(e.from, e.to);
                if (!keys.add(key)) {
                    simple = false;   // duplicate edge
                }
            }
        }
        check(simple, "Generator: no self-loops or duplicate edges");
        check(!TopologicalSort.run(GraphGenerator.randomDirected(100, 500, 42)).isDAG,
                "Generator: random directed graph (100, 500) contains a cycle");
    }

    // helpers

    private static boolean isSpanningTree(Graph g, List<Edge> edges) {
        if (edges.size() != g.vertexCount() - 1) {
            return false;
        }
        DSU d = new DSU(g.vertexCount());
        for (Edge e : edges) {
            if (!d.union(e.from, e.to)) {
                return false;   // cycle
            }
        }
        return d.components() == 1;
    }

    // same graph but weights are only 1, 2 or 3 (many equal weights)
    private static Graph withSmallWeights(Graph g, Random r) {
        Graph copy = new Graph(g.vertexCount(), false);
        for (Edge e : g.edges()) {
            copy.addEdge(e.from, e.to, 1 + r.nextInt(3));
        }
        return copy;
    }

    // tries every set of V-1 edges and returns weight of the lightest spanning tree
    private static long bruteForceMst(Graph g) {
        List<Edge> edges = g.edges();
        int m = edges.size();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != g.vertexCount() - 1) {
                continue;
            }
            DSU d = new DSU(g.vertexCount());
            long w = 0;
            boolean tree = true;
            for (int i = 0; i < m && tree; i++) {
                if ((mask & (1 << i)) != 0) {
                    tree = d.union(edges.get(i).from, edges.get(i).to);
                    w += edges.get(i).weight;
                }
            }
            if (tree) {
                best = Math.min(best, w);
            }
        }
        return best;
    }

    // r[i][j] = true if j can be reached from i (Floyd-Warshall)
    private static boolean[][] transitiveClosure(Graph g) {
        int v = g.vertexCount();
        boolean[][] r = new boolean[v][v];
        for (int i = 0; i < v; i++) {
            r[i][i] = true;
        }
        for (Edge e : g.edges()) {
            r[e.from][e.to] = true;
        }
        for (int k = 0; k < v; k++) {
            for (int i = 0; i < v; i++) {
                for (int j = 0; j < v; j++) {
                    if (r[i][k] && r[k][j]) {
                        r[i][j] = true;
                    }
                }
            }
        }
        return r;
    }

    // simple O(VE) shortest paths, used to check Dijkstra
    private static long[] bellmanFord(Graph g, int s) {
        long[] d = new long[g.vertexCount()];
        Arrays.fill(d, Dijkstra.INF);
        d[s] = 0;
        for (int i = 0; i < g.vertexCount() - 1; i++) {
            for (Edge e : g.edges()) {
                if (d[e.from] != Dijkstra.INF && d[e.from] + e.weight < d[e.to]) {
                    d[e.to] = d[e.from] + e.weight;
                }
            }
        }
        return d;
    }
}
