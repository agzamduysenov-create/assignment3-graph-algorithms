package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

// Kruskal: sort edges by weight, take edge if it connects two different components (DSU)
// if graph is disconnected we get minimum spanning forest
public class Kruskal {

    public static MSTResult run(Graph g) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("MST needs undirected graph");
        }
        MSTResult result = new MSTResult("Kruskal");
        int v = g.vertexCount();

        // copy so we dont change the graph
        List<Edge> sorted = new ArrayList<>(g.edges());
        long[] comparisons = {0};   // array because lambda can not change normal local variable
        sorted.sort((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        DSU dsu = new DSU(v);
        for (Edge e : sorted) {
            if (result.edges.size() == v - 1) {
                break;   // tree is ready, other edges are not needed
            }
            result.edgeExaminations++;
            if (dsu.union(e.from, e.to)) {
                result.edges.add(e);
                result.totalWeight += e.weight;
            }
        }

        result.comparisons = comparisons[0];
        result.dsuFinds = dsu.getFindCalls();
        result.dsuUnions = dsu.getUnionCalls();
        result.components = dsu.components();
        result.connected = dsu.components() == 1;
        return result;
    }
}
