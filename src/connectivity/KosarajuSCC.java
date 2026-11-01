package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

// strongly connected components, Kosaraju algorithm
// 1. dfs on the graph, save vertices by finish time
// 2. dfs on reversed graph from the last finished vertex, every dfs tree is one SCC
// dfs uses my own stack (not recursion), so big graphs dont give StackOverflowError
public class KosarajuSCC {

    public static class Result {
        public List<List<Integer>> components = new ArrayList<>();
        public int[] componentOf;   // componentOf[v] = number of SCC where v is

        public long vertexVisits;
        public long edgeExaminations;

        public int count() {
            return components.size();
        }

        public int largestSize() {
            int max = 0;
            for (List<Integer> c : components) {
                max = Math.max(max, c.size());
            }
            return max;
        }
    }

    public static Result run(Graph g) {
        if (!g.isDirected()) {
            throw new IllegalArgumentException("SCC needs directed graph");
        }
        Result result = new Result();
        int v = g.vertexCount();

        // pass 1: finish order on the normal graph
        boolean[] visited = new boolean[v];
        Deque<Integer> finishOrder = new ArrayDeque<>();
        int[] nextEdge = new int[v];   // which edge of the vertex we check next
        for (int s = 0; s < v; s++) {
            if (visited[s]) {
                continue;
            }
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(s);
            visited[s] = true;
            result.vertexVisits++;
            while (!stack.isEmpty()) {
                int u = stack.peek();
                List<Edge> out = g.neighbors(u);
                if (nextEdge[u] < out.size()) {
                    Edge e = out.get(nextEdge[u]);
                    nextEdge[u]++;
                    result.edgeExaminations++;
                    if (!visited[e.to]) {
                        visited[e.to] = true;
                        result.vertexVisits++;
                        stack.push(e.to);
                    }
                } else {
                    // all edges are checked -> vertex is finished
                    stack.pop();
                    finishOrder.push(u);   // last finished will be on top
                }
            }
        }

        // pass 2: dfs on reversed graph
        Graph reversed = g.reversed();
        result.componentOf = new int[v];
        Arrays.fill(result.componentOf, -1);
        while (!finishOrder.isEmpty()) {
            int s = finishOrder.pop();
            if (result.componentOf[s] != -1) {
                continue;
            }
            int id = result.components.size();
            List<Integer> component = new ArrayList<>();
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(s);
            result.componentOf[s] = id;
            result.vertexVisits++;
            while (!stack.isEmpty()) {
                int u = stack.pop();
                component.add(u);
                for (Edge e : reversed.neighbors(u)) {
                    result.edgeExaminations++;
                    if (result.componentOf[e.to] == -1) {
                        result.componentOf[e.to] = id;
                        result.vertexVisits++;
                        stack.push(e.to);
                    }
                }
            }
            result.components.add(component);
        }
        return result;
    }
}
