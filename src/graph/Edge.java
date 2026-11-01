package graph;

// weighted edge (road) from one district to another
// in undirected graph the same road is stored in both adjacency lists
public class Edge {
    public int from;
    public int to;
    public int weight;

    public Edge(int from, int to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return from + " - " + to + " (" + weight + ")";
    }
}
