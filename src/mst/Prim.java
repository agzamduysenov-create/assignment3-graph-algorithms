package mst;

import graph.Edge;
import graph.Graph;

import java.util.PriorityQueue;

// Prim with priority queue (binary heap), lazy version:
// old edges stay in the queue and we skip them when we poll
// tree grows from vertex 0, if some vertices are not reached we start again from them
// (so for disconnected graph we also get spanning forest)
public class Prim {

    public static MSTResult run(Graph g) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("MST needs undirected graph");
        }
        MSTResult result = new MSTResult("Prim");
        int v = g.vertexCount();
        boolean[] inTree = new boolean[v];
        long[] comparisons = {0};   // array because lambda can not change normal local variable
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        for (int start = 0; start < v; start++) {
            if (inTree[start]) {
                continue;
            }
            result.components++;
            visit(g, start, inTree, pq, result);
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                result.pqPolls++;
                if (inTree[e.to]) {
                    continue;   // both ends are already in tree
                }
                result.edges.add(e);
                result.totalWeight += e.weight;
                visit(g, e.to, inTree, pq, result);
            }
        }

        result.comparisons = comparisons[0];
        result.connected = result.components == 1;
        return result;
    }

    // add u to tree and push its edges to vertices outside the tree
    private static void visit(Graph g, int u, boolean[] inTree, PriorityQueue<Edge> pq, MSTResult result) {
        inTree[u] = true;
        for (Edge e : g.neighbors(u)) {
            result.edgeExaminations++;
            if (!inTree[e.to]) {
                pq.add(e);
                result.pqInserts++;
            }
        }
    }
}
