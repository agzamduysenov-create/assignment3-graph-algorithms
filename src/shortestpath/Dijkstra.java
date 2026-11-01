package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

// Dijkstra shortest paths from one source, priority queue (binary heap)
// lazy deletion: old entries in the queue are skipped
// works for directed and undirected graphs, but only with weights >= 0
public class Dijkstra {
    public static final long INF = Long.MAX_VALUE;

    public static class Result {
        public int source;
        public long[] dist;
        public int[] prev;   // previous vertex on the shortest path

        public long edgeExaminations;
        public long relaxations;
        public long pqInserts;
        public long pqPolls;
        public long comparisons;

        Result(int source, int v) {
            this.source = source;
            this.dist = new long[v];
            this.prev = new int[v];
            Arrays.fill(dist, INF);
            Arrays.fill(prev, -1);
        }

        public boolean isReachable(int target) {
            return dist[target] != INF;
        }

        // path source -> target, empty list if target is unreachable
        public List<Integer> pathTo(int target) {
            List<Integer> path = new ArrayList<>();
            if (!isReachable(target)) {
                return path;
            }
            int at = target;
            while (at != -1) {
                path.add(at);
                at = prev[at];
            }
            Collections.reverse(path);
            return path;
        }

        public int reachableCount() {
            int count = 0;
            for (long d : dist) {
                if (d != INF) {
                    count++;
                }
            }
            return count;
        }
    }

    public static Result run(Graph g, int source) {
        for (Edge e : g.edges()) {
            if (e.weight < 0) {
                throw new IllegalArgumentException("Dijkstra needs weights >= 0, found " + e);
            }
        }
        int v = g.vertexCount();
        Result result = new Result(source, v);
        boolean[] done = new boolean[v];
        // element of the queue = {distance, vertex}
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> {
            result.comparisons++;
            return Long.compare(a[0], b[0]);
        });

        result.dist[source] = 0;
        pq.add(new long[]{0, source});
        result.pqInserts++;
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            result.pqPolls++;
            int u = (int) top[1];
            if (done[u]) {
                continue;   // old entry, u is already done
            }
            done[u] = true;
            for (Edge e : g.neighbors(u)) {
                result.edgeExaminations++;
                long newDist = result.dist[u] + e.weight;
                if (newDist < result.dist[e.to]) {
                    result.dist[e.to] = newDist;
                    result.prev[e.to] = u;
                    result.relaxations++;
                    pq.add(new long[]{newDist, e.to});
                    result.pqInserts++;
                }
            }
        }
        return result;
    }
}
