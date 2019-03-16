import java.util.*;
import java.io.*;

class Main{

	int n; //Number of nodes
	ArrayList<LinkedList<Integer>> adj; //Adjcency list of the Residual Graph
	Map<String, Integer> map; //Capacity map for each edge in the Residual Graph
	Map<String, Integer> maxCapMap; //Capacity map for each edge in the original Graph

	int s = 1; //Default source node
	int t = 2; //Default sink node


	int maxFlow; //Maximum flow of the graph

	// Reading Standard Inputs
	void readInput() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			
			String line = input.readLine();
			String [] numbers = line.split(" ");

			n = Integer.parseInt(numbers[0]);
			int m = Integer.parseInt(numbers[1]); //Number of edges

			adj = new ArrayList<LinkedList<Integer>>();
			for(int i=0;i<n;i++) {
				adj.add(new LinkedList<Integer>());
			}

			map = new HashMap<String, Integer>();
			maxCapMap = new HashMap<String, Integer>();

			for(int i=0;i<m;i++) {
				line = input.readLine();
				numbers = line.split(" ");

				int tail = Integer.parseInt(numbers[0]);
				int head = Integer.parseInt(numbers[1]);
				int c = Integer.parseInt(numbers[2]);

				adj.get(tail-1).add(head);

				String key = numbers[0] + " " + numbers[1];
				String reverseKey = numbers[1] + " " + numbers[0];
				//REdge r = new REdge(c, true);
				map.put(key, c);
				maxCapMap.put(key, c);
				maxCapMap.put(reverseKey, c);
			}

			input.close();

		} catch (IOException io) {
			io.printStackTrace();
		}
	}


	//Writing Standard Outputs
	void writeOutput() {
		try {
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(System.out));

			output.write(Integer.toString(maxFlow));
			output.newLine();

			output.close();

		} catch (IOException io) {
			io.printStackTrace();
		}
	}


	//Function to find the bottle-neck of a path; return -1 if path is empty
	public int bottleNeck(ArrayList<Integer> path){
		int min = -1;
		for (int i=1; i<path.size(); i++){
			int tail = path.get(i-1);
			int head = path.get(i);

			String key = Integer.toString(tail) + " " + Integer.toString(head);
			int c = map.get(key);
			if (min == -1) {
				min = c;
			}
			else if (c < min) {
				min = c;
			}
		}
		return min;
	}


	//BFS to find a "s-t" path in the residual graph
	//Current version returns a random "s-t" path
	public ArrayList<Integer> bfs(){
		Boolean [] visited = new Boolean[n];
		for (int i=1;i<n;i++){
			visited[i] = false;
		}
		Queue<ArrayList<Integer>> q = new LinkedList<ArrayList<Integer>>();

		ArrayList<Integer> path = new ArrayList<Integer>();
		path.add(s);
		q.add(path);
		visited[s-1] = true;

		while(!q.isEmpty()) {
			path = q.poll();
			int last = path.get(path.size()-1);

			if (last == t) return path;

			ListIterator<Integer> li = adj.get(last-1).listIterator();

			while(li.hasNext())	{
				int head = li.next();
				if (!visited[head-1]) {
					ArrayList<Integer> newPath = new ArrayList<Integer>(path);
					newPath.add(head);
					q.add(newPath);
					visited[head-1] = true;
				}
			}
		}

		ArrayList<Integer> empty = new ArrayList<Integer>();
		return empty;
	}

	//The augment function of Max-Flow algorithm
	public void augment(ArrayList<Integer> path){
		int b = bottleNeck(path);
		// System.out.println("Augment by " + Integer.toString(b));
	 	maxFlow += b;

	 	for(int i=1;i<path.size();i++) {
			int head = path.get(i);
			int tail = path.get(i-1);

	    	String key = Integer.toString(tail) + " " + Integer.toString(head);
	 		int cap = map.get(key);
	 		int newCap = cap-b;

	 		//Modifing the edge whose flow is decreased
	 		if (newCap == 0) {
	 			//The edge should be removed
	 			adj.get(tail-1).remove((Integer)head);
	 			map.remove(key);
	 		} 
	 		else {
	 			//Capacity decreases, but the edge remains
	 			map.put(key, newCap);
	 		}

	 		//Modifying the edge whose flow is increased
	 		String reverseKey = Integer.toString(head) + " " + Integer.toString(tail);

	 		if (cap == maxCapMap.get(key)) {
	 			//Reverse edge not existant yet. Needs to add a new edge.
	 			adj.get(head-1).add(tail);
	 			map.put(reverseKey, b);
	 		}
	 		else {
	 			//Reverse edge already exists. Just needs to modify the value.
	 			map.put(reverseKey, map.get(reverseKey)+b);
	 		}
		}
	}


	//Ford-Fulkerson's Max-Flow algorithm
	public void maxFlow(){
		ArrayList<Integer> path = bfs();
		while(!path.isEmpty()) {
			augment(path);
			// for (String key : map.keySet()) {
			// 	System.out.println(key+": " + Integer.toString(map.get(key)));
			// }
			path = bfs();
		}

	}


	public Main() {
		readInput();
		maxFlow();
		writeOutput();
	}

	public static void main(String[] args) {

		new Main();
		
	}
}