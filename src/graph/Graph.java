package graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

// weighted graph, stored as adjacency list
// vertices are districts 0..V-1, edges are roads with weight (cost or distance)
public class Graph {
    private int vertexCount;
    private boolean directed;
    private List<List<Edge>> adj;
    private List<Edge> edges = new ArrayList<>();

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("graph must have at least one vertex");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        this.adj = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            adj.add(new ArrayList<>());
        }
    }

    // adds road u -> v (and v -> u if graph is undirected)
    public void addEdge(int u, int v, int weight) {
        checkVertex(u);
        checkVertex(v);
        Edge e = new Edge(u, v, weight);
        edges.add(e);
        adj.get(u).add(e);
        if (!directed) {
            adj.get(v).add(new Edge(v, u, weight));
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    // undirected road is counted only one time
    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> neighbors(int u) {
        return adj.get(u);
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    // same graph but all edges are reversed (for Kosaraju)
    public Graph reversed() {
        Graph r = new Graph(vertexCount, directed);
        for (Edge e : edges) {
            r.addEdge(e.to, e.from, e.weight);
        }
        return r;
    }

    // bfs from vertex 0, directions are ignored
    // for undirected graph it shows if all districts are connected
    public boolean isConnected() {
        List<List<Integer>> both = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            both.add(new ArrayList<>());
        }
        for (Edge e : edges) {
            both.get(e.from).add(e.to);
            both.get(e.to).add(e.from);
        }
        boolean[] seen = new boolean[vertexCount];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        seen[0] = true;
        int count = 1;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : both.get(u)) {
                if (!seen[v]) {
                    seen[v] = true;
                    count++;
                    queue.add(v);
                }
            }
        }
        return count == vertexCount;
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= vertexCount) {
            throw new IllegalArgumentException("wrong vertex: " + v);
        }
    }
}
