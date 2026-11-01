package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// topological order with Kahn algorithm (bfs with in-degrees)
// if we can not output all vertices, then other vertices are on a cycle or after a cycle,
// so topological order does not exist
public class TopologicalSort {

    public static class Result {
        public boolean isDAG;   // true -> order is a correct topological order
        public List<Integer> order = new ArrayList<>();
        public List<Integer> blockedVertices = new ArrayList<>();   // not empty only if there is a cycle

        public long edgeExaminations;
        public long queueOperations;
    }

    public static Result run(Graph g) {
        if (!g.isDirected()) {
            throw new IllegalArgumentException("topological order needs directed graph");
        }
        Result result = new Result();
        int v = g.vertexCount();

        // count in-degree of every vertex
        int[] indegree = new int[v];
        for (int u = 0; u < v; u++) {
            for (Edge e : g.neighbors(u)) {
                result.edgeExaminations++;
                indegree[e.to]++;
            }
        }

        // start with vertices that have no incoming edges
        Deque<Integer> queue = new ArrayDeque<>();
        for (int u = 0; u < v; u++) {
            if (indegree[u] == 0) {
                queue.add(u);
                result.queueOperations++;
            }
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            result.queueOperations++;
            result.order.add(u);
            for (Edge e : g.neighbors(u)) {
                result.edgeExaminations++;
                indegree[e.to]--;
                if (indegree[e.to] == 0) {
                    queue.add(e.to);
                    result.queueOperations++;
                }
            }
        }

        result.isDAG = result.order.size() == v;
        if (!result.isDAG) {
            for (int u = 0; u < v; u++) {
                if (indegree[u] > 0) {
                    result.blockedVertices.add(u);
                }
            }
            result.order.clear();   // part of the order is not a correct answer
        }
        return result;
    }

    // checks that for every edge u -> v, u is before v in the order
    public static boolean isValidOrder(Graph g, List<Integer> order) {
        int v = g.vertexCount();
        if (order.size() != v) {
            return false;
        }
        int[] position = new int[v];
        boolean[] seen = new boolean[v];
        for (int i = 0; i < v; i++) {
            int u = order.get(i);
            if (u < 0 || u >= v || seen[u]) {
                return false;
            }
            seen[u] = true;
            position[u] = i;
        }
        for (Edge e : g.edges()) {
            if (position[e.from] >= position[e.to]) {
                return false;
            }
        }
        return true;
    }
}
