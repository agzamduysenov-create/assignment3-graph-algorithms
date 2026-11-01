package app;

import connectivity.KosarajuSCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

// runs all experiments, saves tables to results/tables and plots to results/plots
// every measurement: WARMUP runs without timing (for JIT), then RUNS timed runs and we take average
// seed is fixed, so operation counts are always the same (time depends on the computer)
public class Benchmark {
    static final long SEED = 42;
    static final int RUNS = 5;
    static final int WARMUP = 200;

    static final String[] NAMES = {"Small", "Medium", "Large"};
    static final int[] V = {10, 100, 1000};
    static final int[] E = {30, 500, 5000};

    // so java does not delete "unused" results
    static volatile Object sink;

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        File tables = new File("results/tables");
        File plots = new File("results/plots");
        tables.mkdirs();
        plots.mkdirs();

        int n = NAMES.length;
        double[] kruskalMs = new double[n];
        double[] primMs = new double[n];
        double[] dijkstraMs = new double[n];
        double[] sccMs = new double[n];
        double[] topoDagMs = new double[n];
        double[] topoCycMs = new double[n];
        double[] kruskalOps = new double[n];
        double[] primOps = new double[n];

        StringBuilder mst = new StringBuilder();
        StringBuilder mstOps = new StringBuilder();
        StringBuilder conn = new StringBuilder();
        StringBuilder dij = new StringBuilder();
        StringBuilder csv = new StringBuilder("algorithm,graph,V,E,avg_time_ms,operations\n");

        mst.append("| Graph | V | E | Kruskal time (ms) | Prim time (ms) | Kruskal weight | Prim weight | Same weight | Connected |\n");
        mst.append("|---|---|---|---|---|---|---|---|---|\n");
        mstOps.append("| Graph | Kruskal edge exams | Kruskal sort comparisons | Kruskal DSU finds | Kruskal DSU unions | Kruskal total ops | Prim edge exams | Prim PQ inserts | Prim PQ polls | Prim heap comparisons | Prim total ops |\n");
        mstOps.append("|---|---|---|---|---|---|---|---|---|---|---|\n");
        conn.append("| Graph | V | E | Directed graph type | Topo time (ms) | Topo result | Topo edge exams | SCC time (ms) | #SCC | Largest SCC | SCC vertex visits | SCC edge exams |\n");
        conn.append("|---|---|---|---|---|---|---|---|---|---|---|---|\n");
        dij.append("| Graph | V | E | Time (ms) | Reachable | PQ inserts | PQ polls | Edge exams | Relaxations | Heap comparisons | Total ops |\n");
        dij.append("|---|---|---|---|---|---|---|---|---|---|---|\n");

        for (int i = 0; i < n; i++) {
            String name = NAMES[i];
            Graph und = GraphGenerator.connectedUndirected(V[i], E[i], SEED);
            Graph dag = GraphGenerator.randomDAG(V[i], E[i], SEED);
            Graph cyc = GraphGenerator.randomDirected(V[i], E[i], SEED);

            // mst
            kruskalMs[i] = averageMs(() -> Kruskal.run(und));
            primMs[i] = averageMs(() -> Prim.run(und));
            MSTResult k = Kruskal.run(und);
            MSTResult p = Prim.run(und);
            long kTotal = k.edgeExaminations + k.comparisons + k.dsuFinds + k.dsuUnions;
            long pTotal = p.edgeExaminations + p.pqInserts + p.pqPolls + p.comparisons;
            kruskalOps[i] = kTotal;
            primOps[i] = pTotal;
            String sameWeight = k.totalWeight == p.totalWeight ? "yes" : "NO";
            String connected = k.connected && p.connected ? "yes" : "no";
            mst.append(String.format("| %s | %d | %d | %.4f | %.4f | %d | %d | %s | %s |%n", name, V[i], E[i],
                    kruskalMs[i], primMs[i], k.totalWeight, p.totalWeight, sameWeight, connected));
            mstOps.append(String.format("| %s | %d | %d | %d | %d | %d | %d | %d | %d | %d | %d |%n", name,
                    k.edgeExaminations, k.comparisons, k.dsuFinds, k.dsuUnions, kTotal,
                    p.edgeExaminations, p.pqInserts, p.pqPolls, p.comparisons, pTotal));
            csv.append(String.format("Kruskal,%s,%d,%d,%.4f,%d%n", name, V[i], E[i], kruskalMs[i], kTotal));
            csv.append(String.format("Prim,%s,%d,%d,%.4f,%d%n", name, V[i], E[i], primMs[i], pTotal));

            // topological order and scc, on DAG and on graph with cycles
            topoDagMs[i] = averageMs(() -> TopologicalSort.run(dag));
            topoCycMs[i] = averageMs(() -> TopologicalSort.run(cyc));
            double sccDagMs = averageMs(() -> KosarajuSCC.run(dag));
            sccMs[i] = averageMs(() -> KosarajuSCC.run(cyc));
            addConnectivityRow(conn, csv, name, i, "DAG", dag, topoDagMs[i], sccDagMs);
            addConnectivityRow(conn, csv, name, i, "Random (cyclic)", cyc, topoCycMs[i], sccMs[i]);

            // dijkstra
            dijkstraMs[i] = averageMs(() -> Dijkstra.run(und, 0));
            Dijkstra.Result d = Dijkstra.run(und, 0);
            long dTotal = d.pqInserts + d.pqPolls + d.edgeExaminations + d.comparisons;
            dij.append(String.format("| %s | %d | %d | %.4f | %d | %d | %d | %d | %d | %d | %d |%n", name, V[i], E[i],
                    dijkstraMs[i], d.reachableCount(), d.pqInserts, d.pqPolls, d.edgeExaminations, d.relaxations,
                    d.comparisons, dTotal));
            csv.append(String.format("Dijkstra,%s,%d,%d,%.4f,%d%n", name, V[i], E[i], dijkstraMs[i], dTotal));

            System.out.println("Finished " + name + " graph (V=" + V[i] + ", E=" + E[i] + ")");
        }

        String dsu = dsuExperiment();

        write(new File(tables, "mst.md"), "## Kruskal vs Prim - time and MST weight\n\n" + mst
                + "\n## Kruskal vs Prim - operation counts\n\n" + mstOps);
        write(new File(tables, "connectivity.md"), "## Topological order and SCC\n\n" + conn);
        write(new File(tables, "dijkstra.md"), "## Dijkstra\n\n" + dij);
        write(new File(tables, "dsu.md"), dsu);
        write(new File(tables, "all_results.csv"), csv.toString());

        // plots
        double[] vs = {V[0], V[1], V[2]};
        double[] es = {E[0], E[1], E[2]};
        Map<String, double[]> times = new LinkedHashMap<>();
        times.put("Kruskal", kruskalMs);
        times.put("Prim", primMs);
        times.put("Dijkstra", dijkstraMs);
        times.put("SCC (Kosaraju, cyclic graph)", sccMs);
        times.put("Topological sort (DAG)", topoDagMs);
        PlotGenerator.logLogChart("Execution time vs number of vertices", "Vertices V (log scale)",
                "Average time, ms (log scale)", vs, times, new File(plots, "time_vs_vertices.png"));
        PlotGenerator.logLogChart("Execution time vs number of edges", "Edges E (log scale)",
                "Average time, ms (log scale)", es, times, new File(plots, "time_vs_edges.png"));

        Map<String, double[]> mstCompare = new LinkedHashMap<>();
        mstCompare.put("Kruskal total operations", kruskalOps);
        mstCompare.put("Prim total operations", primOps);
        PlotGenerator.logLogChart("Kruskal vs Prim: operation count", "Edges E (log scale)",
                "Operations (log scale)", es, mstCompare, new File(plots, "kruskal_vs_prim_operations.png"));

        Map<String, double[]> mstTime = new LinkedHashMap<>();
        mstTime.put("Kruskal", kruskalMs);
        mstTime.put("Prim", primMs);
        PlotGenerator.logLogChart("Kruskal vs Prim: execution time", "Edges E (log scale)",
                "Average time, ms (log scale)", es, mstTime, new File(plots, "kruskal_vs_prim_time.png"));

        System.out.println("Tables written to results/tables, plots written to results/plots");
    }

    private static void addConnectivityRow(StringBuilder conn, StringBuilder csv, String name, int i, String type,
                                           Graph g, double topoMs, double sccMs) {
        TopologicalSort.Result t = TopologicalSort.run(g);
        KosarajuSCC.Result s = KosarajuSCC.run(g);
        String topoResult;
        if (t.isDAG) {
            topoResult = "valid order";
        } else {
            topoResult = "cycle detected (" + t.blockedVertices.size() + " blocked)";
        }
        conn.append(String.format("| %s | %d | %d | %s | %.4f | %s | %d | %.4f | %d | %d | %d | %d |%n", name, V[i], E[i],
                type, topoMs, topoResult, t.edgeExaminations, sccMs, s.count(), s.largestSize(),
                s.vertexVisits, s.edgeExaminations));
        String tag = type.startsWith("DAG") ? "DAG" : "Cyclic";
        csv.append(String.format("Topo-%s,%s,%d,%d,%.4f,%d%n", tag, name, V[i], E[i], topoMs, t.edgeExaminations + t.queueOperations));
        csv.append(String.format("SCC-%s,%s,%d,%d,%.4f,%d%n", tag, name, V[i], E[i], sccMs, s.vertexVisits + s.edgeExaminations));
    }

    // dsu experiment: 4 variants of path compression (PC) and union by rank (UR)
    // worst case: union(i, i+1) for all i (long chain without rank), then find for every element
    // random case: n random unions and then n random finds
    private static String dsuExperiment() {
        int[] sizes = {1000, 5000, 10000};
        boolean[][] variants = {{false, false}, {true, false}, {false, true}, {true, true}};
        String[] labels = {"No optimizations", "Path compression only", "Union by rank only", "PC + union by rank"};
        StringBuilder sb = new StringBuilder("## DSU - worst case (chain of unions, then find on every element)\n\n");
        sb.append("| n | Variant | Avg time (ms) | Find calls | Union calls | Parent pointer steps |\n|---|---|---|---|---|---|\n");
        for (int s = 0; s < sizes.length; s++) {
            int n = sizes[s];
            for (int v = 0; v < variants.length; v++) {
                boolean pc = variants[v][0];
                boolean ur = variants[v][1];
                double ms = averageMs(() -> chainWorkload(n, pc, ur), 2);
                DSU d = chainWorkload(n, pc, ur);
                sb.append(String.format("| %d | %s | %.4f | %d | %d | %d |%n", n, labels[v], ms,
                        d.getFindCalls(), d.getUnionCalls(), d.getPointerSteps()));
            }
        }

        sb.append("\n## DSU - random case (n random unions, then n random finds)\n\n");
        sb.append("| n | Variant | Avg time (ms) | Find calls | Union calls | Parent pointer steps |\n|---|---|---|---|---|---|\n");
        int[] randomSizes = {1000, 10000, 100000};
        Map<String, double[]> stepsSeries = new LinkedHashMap<>();
        for (String label : labels) {
            stepsSeries.put(label, new double[randomSizes.length]);
        }
        for (int s = 0; s < randomSizes.length; s++) {
            int n = randomSizes[s];
            for (int v = 0; v < variants.length; v++) {
                boolean pc = variants[v][0];
                boolean ur = variants[v][1];
                double ms = averageMs(() -> randomWorkload(n, pc, ur), 1);
                DSU d = randomWorkload(n, pc, ur);
                stepsSeries.get(labels[v])[s] = d.getPointerSteps();
                sb.append(String.format("| %d | %s | %.4f | %d | %d | %d |%n", n, labels[v], ms,
                        d.getFindCalls(), d.getUnionCalls(), d.getPointerSteps()));
            }
        }

        double[] xs = new double[randomSizes.length];
        for (int i = 0; i < randomSizes.length; i++) {
            xs[i] = randomSizes[i];
        }
        try {
            PlotGenerator.logLogChart("DSU (random unions + finds): parent pointer steps", "Number of elements n (log scale)",
                    "Pointer steps (log scale)", xs, stepsSeries, new File("results/plots/dsu_optimizations.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private static DSU chainWorkload(int n, boolean pc, boolean ur) {
        DSU d = new DSU(n, pc, ur);
        for (int i = 0; i + 1 < n; i++) {
            d.union(i, i + 1);
        }
        for (int i = 0; i < n; i++) {
            d.find(i);
        }
        return d;
    }

    private static DSU randomWorkload(int n, boolean pc, boolean ur) {
        DSU d = new DSU(n, pc, ur);
        Random r = new Random(SEED);
        for (int i = 0; i < n; i++) {
            d.union(r.nextInt(n), r.nextInt(n));
        }
        for (int i = 0; i < n; i++) {
            d.find(r.nextInt(n));
        }
        return d;
    }

    private static double averageMs(Supplier<Object> task) {
        return averageMs(task, WARMUP);
    }

    private static double averageMs(Supplier<Object> task, int warmup) {
        for (int i = 0; i < warmup; i++) {
            sink = task.get();
        }
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            System.gc();   // clean memory before timing, not during
            long start = System.nanoTime();
            sink = task.get();
            total += System.nanoTime() - start;
        }
        return total / (double) RUNS / 1000000.0;
    }

    private static void write(File f, String content) throws IOException {
        try (PrintWriter w = new PrintWriter(f, "UTF-8")) {
            w.print(content);
        }
    }
}
