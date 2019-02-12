import java.util.*;

class Edge {
    public int id; // number
    public int x; // first vertex
    public int y; // second vertex
    public int c; // cost
}

class Main {

    public static class PEdge implements Comparable<PEdge> {
        int cost; //Cost to connect to this node
        int eid; //Id of this edge
        int target; //New node added by this edge

        public PEdge(int c, int e, int t) {
            cost = c;
            eid = e;
            target = t;
        }

        @Override
        public int compareTo(PEdge pEdge) {
            if (this.cost > pEdge.cost) {
                return 1;
            }
            else if (this.cost < pEdge.cost) {
                return -1;
            }
            else {
                return this.eid-pEdge.eid;
            }
        }
    }


    public static class UnionFind {
        int set[];
        int size[];
        Map<Integer, ArrayList<Integer>> map;

        public UnionFind(int n) {
            set = new int[n];
            size = new int[n];
            map = new HashMap<Integer, ArrayList<Integer>>();
            for(int i=0;i<n;i++) {
                set[i] = i;
                size[i] = 1;
                ArrayList<Integer> elements = new ArrayList<Integer>();
                elements.add(i);
                //System.out.println(i);
                //System.out.println(Arrays.toString(elements.toArray()));
                map.put(i, elements);
            }
        }

        public int find(int u) {
            return set[u-1];
        }

        public void union(int a, int b) {
            if (size[a] < size[b]) {
                //set a is smaller:
                //move all elements in a to set b
                ArrayList<Integer> updateList = map.get(a);
                for(int i=0;i<updateList.size();i++) {
                    int e = updateList.get(i);
                    set[e] = b;
                    map.get(b).add(e);
                }
                size[b] += size[a];
            } 
            else {
                //set b is smaller:
                //move all elements in b to set a
                ArrayList<Integer> updateList = map.get(b);
                for(int i=0;i<updateList.size();i++) {
                    int e = updateList.get(i);
                    set[e] = a;
                    map.get(a).add(e);
                }
                size[a] += size[b];
            }
        }
    }


	public static Map<Integer, ArrayList<Integer>> nodeMap(Vector<Edge> edges, int n, int m) {
		Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
		for(int i=1;i<=n;i++) {
			ArrayList<Integer> hasEdges = new ArrayList<Integer>();
			map.put(i, hasEdges);
		}
		for(int i=0;i<m;i++) {
			Edge e = edges.get(i);
			map.get(e.x).add(e.id);
			map.get(e.y).add(e.id);
		}
		return map;
	}


    public static ArrayList<Integer> runPrim(Vector<Edge> edges, int n, int m) {
        Map<Integer, ArrayList<Integer>> map = nodeMap(edges, n, m);
        ArrayList<Integer> tree = new ArrayList<Integer>();
        PriorityQueue<PEdge> queue = new PriorityQueue<PEdge>();
        int [] nodesAdded = new int[n]; //if nodesAdded[i] == Null, then node i+1 has not been added
        int curNode = 1; //Id of most recently added node
        nodesAdded[0] = 1;
        int hasAdded = 1;   //Counter for how many nodes have been added

        while(hasAdded != n) {
            ArrayList<Integer> curEdges = map.get(curNode);
            for(int i=0;i<curEdges.size();i++) {
                Edge e = edges.get(curEdges.get(i)-1); //-1 because edge id starts with 1
                int t = e.y; //[t] is id of the node connected by edge [e]
                if (t == curNode) {
                    t = e.x;
                }
                PEdge p = new PEdge(e.c, e.id, t);
                queue.add(p);
            }
            PEdge newEdge = queue.poll();
            while(nodesAdded[newEdge.target-1]==1) {
                newEdge = queue.poll();
            }
            curNode = newEdge.target;
            nodesAdded[curNode-1] = 1;
            tree.add(newEdge.eid);
            hasAdded++;
        }

        return tree;
    }


    public static ArrayList<Integer> runKruskal(Vector<Edge> edges, int n, int m) {
        UnionFind u = new UnionFind(n);
        PriorityQueue<PEdge> queue = new PriorityQueue<PEdge>();
        ArrayList<Integer> tree = new ArrayList<Integer>();

        for(int i=0;i<m;i++) {
            Edge e = edges.get(i);
            PEdge p = new PEdge(e.c, e.id, 0); //3rd argument, 'target node', does not matter here
            queue.add(p);
        }

        while(!queue.isEmpty()) {
            int candidateId = queue.poll().eid; //id of the candidate edge
            Edge candidate = edges.get(candidateId-1); //The actual edge

            while((u.find(candidate.x) == u.find(candidate.y)) 
                && !queue.isEmpty()) {
                candidateId = queue.poll().eid;
                candidate = edges.get(candidateId-1);
            }

            if (u.find(candidate.x) != u.find(candidate.y)) {
                //if the above if condition is not met, 
                //then [queue] run out of valid candidates in the 
                //las while-loop iteration.
                u.union(u.find(candidate.x), u.find(candidate.y));
                tree.add(candidateId);
            }
        }

        return tree;
    }


    public static void main(String[] args) {
        /* To read the input */
        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();//Number of nodes
        int m = sc.nextInt();//Number of edges
        int p = sc.nextInt();//Kruskal or Prim

        Vector<Edge> edges = new Vector<Edge>();

        for(int i=1;i<=m;++i) {
            Edge e = new Edge();
            e.id=i;
            e.x = sc.nextInt();
            e.y = sc.nextInt();
            e.c = sc.nextInt();

            edges.addElement(e);
        }

        ArrayList<Integer> tree = new ArrayList<Integer>();
        if (p==0) {
            tree = runKruskal(edges, n, m);
        } else {
            tree = runPrim(edges, n, m);
        }

        /* To output the first N-1 edges; note this is probably not actually a spanning tree */
        for(int i=0;i<tree.size();i++) {
            System.out.println(tree.get(i));

        }
    }   
}