package mst;

import graph.Edge;

import java.util.ArrayList;
import java.util.List;

// result of Kruskal or Prim + counters for experiments
public class MSTResult {
    public String algorithm;
    public List<Edge> edges = new ArrayList<>();
    public long totalWeight;
    public boolean connected;   // false -> graph is disconnected, result is spanning forest
    public int components;

    // counters
    public long edgeExaminations;
    public long comparisons;
    public long dsuFinds;
    public long dsuUnions;
    public long pqInserts;
    public long pqPolls;

    public MSTResult(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String toString() {
        return algorithm + ": weight=" + totalWeight + ", edges=" + edges.size()
                + ", connected=" + connected + ", components=" + components;
    }
}
