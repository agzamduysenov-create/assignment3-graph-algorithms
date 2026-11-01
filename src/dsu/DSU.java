package dsu;

// disjoint set union (union-find)
// by default uses path compression and union by rank
// optimizations can be turned off to see how much they help (see Benchmark)
public class DSU {
    private int[] parent;
    private int[] rank;
    private boolean pathCompression;
    private boolean unionByRank;
    private int components;

    // counters
    private long findCalls;
    private long unionCalls;
    private long pointerSteps;   // how many parent links we followed in find

    public DSU(int n) {
        this(n, true, true);
    }

    public DSU(int n, boolean pathCompression, boolean unionByRank) {
        this.parent = new int[n];
        this.rank = new int[n];
        this.pathCompression = pathCompression;
        this.unionByRank = unionByRank;
        this.components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    // returns root of the set where x is
    public int find(int x) {
        findCalls++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
            pointerSteps++;
        }
        if (pathCompression) {
            // second loop: every vertex on the path now points to root
            while (parent[x] != root) {
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
        }
        return root;
    }

    // joins sets of a and b, returns false if they are already in one set
    public boolean union(int a, int b) {
        unionCalls++;
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) {
            return false;
        }
        if (unionByRank) {
            // smaller tree goes under bigger tree
            if (rank[rootA] < rank[rootB]) {
                int temp = rootA;
                rootA = rootB;
                rootB = temp;
            }
            parent[rootB] = rootA;
            if (rank[rootA] == rank[rootB]) {
                rank[rootA]++;
            }
        } else {
            parent[rootA] = rootB;   // simple linking without rank
        }
        components--;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    public int components() {
        return components;
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getUnionCalls() {
        return unionCalls;
    }

    public long getPointerSteps() {
        return pointerSteps;
    }
}
