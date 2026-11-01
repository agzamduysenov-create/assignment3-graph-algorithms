package app;

import connectivity.KosarajuSCC;
import connectivity.TopologicalSort;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.util.List;

// small smart city demo: all algorithms on hand-made graphs with district names
public class Main {
    private static final String[] DISTRICTS = {
            "Downtown", "Harbor", "University", "Airport", "Industrial", "OldTown", "TechPark", "Riverside", "NewSuburb"
    };
    private static final String[] SERVICES = {
            "PowerGrid", "WaterSupply", "Telecom", "TrafficLights", "Hospital", "Metro"
    };

    public static void main(String[] args) {
        System.out.println("=== Smart City Infrastructure Demo ===\n");
        infrastructurePlanning();
        oneWayStreets();
        serviceDependencies();
        routePlanning();
    }

    // task 1: cheapest road network that connects all districts
    private static void infrastructurePlanning() {
        System.out.println("--- Task 1: Minimum Spanning Tree (road construction cost) ---");
        Graph roads = cityRoads(8);
        MSTResult k = Kruskal.run(roads);
        MSTResult p = Prim.run(roads);
        printMst(k);
        printMst(p);
        System.out.println("Same total weight: " + (k.totalWeight == p.totalWeight));

        Graph withSuburb = cityRoads(9);   // NewSuburb has no roads yet
        MSTResult k2 = Kruskal.run(withSuburb);
        System.out.println("After adding NewSuburb without roads -> connected = " + k2.connected
                + ", components = " + k2.components + "\n");
    }

    // task 2a: one-way streets, groups of districts that can reach each other
    private static void oneWayStreets() {
        System.out.println("--- Task 2: Strongly Connected Components (one-way streets) ---");
        Graph g = new Graph(8, true);
        int[][] streets = {{0, 1}, {1, 2}, {2, 0}, {2, 3}, {3, 4}, {4, 5}, {5, 3}, {5, 6}, {6, 7}};
        for (int[] s : streets) {
            g.addEdge(s[0], s[1], 1);
        }
        KosarajuSCC.Result scc = KosarajuSCC.run(g);
        System.out.println("Number of SCCs: " + scc.count());
        for (List<Integer> c : scc.components) {
            System.out.println("  " + set(c, DISTRICTS));
        }
        System.out.println();
    }

    // task 2b: in which order city services must be built
    private static void serviceDependencies() {
        System.out.println("--- Task 2: Topological order (service dependencies) ---");
        Graph deps = new Graph(6, true);
        int[][] d = {{0, 1}, {0, 2}, {0, 3}, {2, 3}, {1, 4}, {2, 4}, {3, 5}, {0, 5}};
        for (int[] e : d) {
            deps.addEdge(e[0], e[1], 1);
        }
        TopologicalSort.Result ok = TopologicalSort.run(deps);
        System.out.println("DAG: " + ok.isDAG + ", build order: " + names(ok.order, SERVICES));

        deps.addEdge(5, 0, 1);   // Metro -> PowerGrid makes a cycle
        TopologicalSort.Result bad = TopologicalSort.run(deps);
        System.out.println("After adding Metro -> PowerGrid: DAG = " + bad.isDAG
                + ", vertices on/behind a cycle: " + set(bad.blockedVertices, SERVICES) + "\n");
    }

    // task 3: shortest distances from Downtown
    private static void routePlanning() {
        System.out.println("--- Task 3: Dijkstra shortest paths from Downtown (travel distance) ---");
        Graph roads = cityRoads(9);
        Dijkstra.Result r = Dijkstra.run(roads, 0);
        for (int v = 0; v < roads.vertexCount(); v++) {
            if (r.isReachable(v)) {
                System.out.printf("  %-11s dist = %3d  path: %s%n", DISTRICTS[v], r.dist[v], names(r.pathTo(v), DISTRICTS));
            } else {
                System.out.printf("  %-11s unreachable%n", DISTRICTS[v]);
            }
        }
    }

    // undirected roads, if vertices = 9 then the last district (NewSuburb) has no roads
    private static Graph cityRoads(int vertices) {
        Graph g = new Graph(vertices, false);
        int[][] roads = {
                {0, 1, 4}, {0, 2, 3}, {1, 2, 5}, {1, 5, 6}, {2, 6, 7}, {2, 5, 2},
                {5, 7, 4}, {6, 7, 5}, {6, 4, 8}, {4, 3, 9}, {3, 7, 10}, {0, 5, 6}
        };
        for (int[] r : roads) {
            g.addEdge(r[0], r[1], r[2]);
        }
        return g;
    }

    private static void printMst(MSTResult r) {
        System.out.println(r);
        for (Edge e : r.edges) {
            System.out.println("    " + DISTRICTS[e.from] + " - " + DISTRICTS[e.to] + " : " + e.weight);
        }
    }

    // [A -> B -> C]
    private static String names(List<Integer> ids, String[] labels) {
        return join(ids, labels, " -> ", "[", "]");
    }

    // {A, B, C}
    private static String set(List<Integer> ids, String[] labels) {
        return join(ids, labels, ", ", "{", "}");
    }

    private static String join(List<Integer> ids, String[] labels, String separator, String start, String end) {
        StringBuilder sb = new StringBuilder(start);
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(labels[ids.get(i)]);
        }
        sb.append(end);
        return sb.toString();
    }
}
