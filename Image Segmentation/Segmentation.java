import java.util.*;
import java.io.*;

class FlowGraph{

	int n; //Number of nodes
	ArrayList<LinkedList<Integer>> adj; //Adjcency list of the Residual Graph
	Map<String, Integer> map; //Capacity map for each edge in the Residual Graph
	Map<String, Integer> maxCapMap; //Capacity map for each edge in the original Graph

	int s; //Source node
	int t; //Sink node

	public FlowGraph(int nodeNum, int source, int sink,
		ArrayList<LinkedList<Integer>> graph, 
		Map<String, Integer> rMap, 
		Map<String, Integer> maxMap) {

		n = nodeNum; 
		adj = graph;
		map = rMap;
		maxCapMap = maxMap;
		s = source;
		t = sink;
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
	public ArrayList<Integer> findSTpath(){
		Boolean [] visited = new Boolean[n];
		for (int i=0;i<n;i++){
			visited[i] = false;
		}
		Queue<ArrayList<Integer>> q = new LinkedList<ArrayList<Integer>>();

		ArrayList<Integer> path = new ArrayList<Integer>();
		path.add(s);
		q.add(path);
		visited[s] = true;

		while(!q.isEmpty()) {
			path = q.poll();
			int last = path.get(path.size()-1);

			if (last == t) return path;

			ListIterator<Integer> li = adj.get(last).listIterator();

			while(li.hasNext())	{
				int head = li.next();
				if (!visited[head]) {
					ArrayList<Integer> newPath = new ArrayList<Integer>(path);
					newPath.add(head);
					q.add(newPath);
					visited[head] = true;
				}
			}
		}

		ArrayList<Integer> empty = new ArrayList<Integer>();
		return empty;
	}

	//The augment function of Max-Flow algorithm
	public void augment(ArrayList<Integer> path){
		int b = bottleNeck(path);

	 	for(int i=1;i<path.size();i++) {
			int head = path.get(i);
			int tail = path.get(i-1);

	    	String key = Integer.toString(tail) + " " + Integer.toString(head);
	 		int cap = map.get(key);
	 		int newCap = cap-b;

	 		//Modifing the edge whose flow is decreased
	 		if (newCap == 0) {
	 			//The edge should be removed
	 			adj.get(tail).remove((Integer)head);
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
	 			adj.get(head).add(tail);
	 			map.put(reverseKey, b);
	 		}
	 		else {
	 			//Reverse edge already exists. Just needs to modify the value.
	 			map.put(reverseKey, map.get(reverseKey)+b);
	 		}
		}
	}

	// Finding minimum cut
	public ArrayList<Integer> findA(){
		//Running Max-Flow Algorithm
		ArrayList<Integer> path = findSTpath();
		while(!path.isEmpty()) {
			augment(path);
			path = findSTpath();
		}

		//Retrieving minimum cut set A
		ArrayList<Integer> setA = new ArrayList<Integer>();
		Boolean [] visited = new Boolean[n];
		for (int i=0;i<n;i++){
			visited[i] = false;
		}

		Queue<Integer> q = new LinkedList<Integer>();
		q.add(s);
		visited[s] = true;

		int node;
		while(!q.isEmpty()){
			node = q.poll();
			if (node != s) setA.add(node);

			ListIterator<Integer> li = adj.get(node).listIterator();
			while(li.hasNext()){
				int next = li.next();
				if (!visited[next]){
					q.add(next);
					visited[next] = true;
				}
			}
		}
		return setA;
	}
}


class Main {
    /* To be loaded from StdIn using input(). */
    int height; // height of image
    int width; // width of image
    int blockHeight; // height of a tiling block
    int blockWidth; // width of a tiling block
    int[][] fReward; // reward for putting pixel in foreground
    int[][] bReward; // reward for putting pixel in background
    int[][] pBtwCols;   // pBtwCols[i][j] is separation penalty between pixel (i,j), (i,j+1)
                        // dimensions: height x (width - 1) 
    int[][] pBtwRows;   // pBtwRows[i][j] is separation penalty between pixel (i,j), (i+1,j)
                        // dimensions: (height-1) x (width)

    /* To be printed to StdOut using output() */
    boolean[][] foreground; // selects the pixels that will go in the foreground

    // Load input from StdIn.
    void input() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String[] hw = in.readLine().split("\\s+");
            height = Integer.parseInt(hw[0]);
            width = Integer.parseInt(hw[1]);
            String[] bhw = in.readLine().split("\\s+");
            blockHeight = Integer.parseInt(bhw[0]);
            blockWidth = Integer.parseInt(bhw[1]);
            fReward = new int[height][width];
            bReward = new int[height][width];
            pBtwCols = new int[height][width-1];
            pBtwRows = new int[height-1][width];
            // populate fReward
            for (int i = 0; i < height; i++) {
                String[] rewards = in.readLine().split("\\s+");
                for (int j = 0; j < width; j++) {
                    fReward[i][j] = Integer.parseInt(rewards[j]);
                }
            }
            // populate bReward
            for (int i = 0; i < height; i++) {
                String[] rewards = in.readLine().split("\\s+");
                for (int j = 0; j < width; j++) {
                    bReward[i][j] = Integer.parseInt(rewards[j]);
                }
            }
            // populate pBtwColsA
            for (int i = 0; i < height; i++) {
                String[] penalties = in.readLine().split("\\s+");
                for (int j = 0; j < width-1; j++) {
                    pBtwCols[i][j] = Integer.parseInt(penalties[j]);
                }
            }
            // populate pBtwRows
            for (int i = 0; i < height-1; i++) {
                String[] penalties = in.readLine().split("\\s+");
                for (int j = 0; j < width; j++) {
                    pBtwRows[i][j] = Integer.parseInt(penalties[j]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String arrToString(boolean[][] arr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                builder.append(arr[i][j]);
                if (j < arr[i].length-1) {
                    builder.append(" ");
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    // Print output to StdOut.
    void output() {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
            out.write(arrToString(foreground).replace("true","1").replace("false","0"));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Main() {
        input();
        // Transforming Pixel parameters to Block ones
        int heightB = height/blockHeight;
        int widthB = width/blockWidth;
        int[][] blockFR = new int[heightB][widthB]; //reward for putting block in foreground
        int[][] blockBR = new int[heightB][widthB]; //reward for putting block in background
        int[][] blockPCols = new int[heightB][widthB-1]; //penalty for between columns
        int[][] blockPRows = new int[heightB-1][widthB]; //penalty for between rows

        //Populating [blockFR] and [blockBR]
        for (int i=0; i<heightB; i++) {
        	for (int j=0; j<widthB; j++) {
        		int sumFReward = 0;
        		int sumBReward = 0;
        		for (int pi=blockHeight*i; pi<blockHeight*(i+1); pi++) {
        			for (int pj=blockWidth*j; pj<blockWidth*(j+1); pj++) {
        				sumFReward += fReward[pi][pj];
        				sumBReward += bReward[pi][pj];
        			}
        		}
        		blockFR[i][j] = sumFReward/(blockHeight*blockWidth);
        		blockBR[i][j] = sumBReward/(blockHeight*blockWidth);
        	}
        }

        //Populating [blockPCols]
        for (int i=0; i<heightB; i++) {
        	for (int j=0; j<widthB-1; j++) {
        		int sumPenalty = 0;
        		int pj = blockWidth*(j+1)-1; //Last pixel column in block[i][j]
        		for (int pi=blockHeight*i; pi<blockHeight*(i+1); pi++) {
        			sumPenalty += pBtwCols[pi][pj];
        		}
        		blockPCols[i][j] = sumPenalty/blockHeight;
        	}
        }

        //Populating [blockPRows]
        for (int i=0; i<heightB-1; i++) {
        	for (int j=0; j<widthB; j++) {
        		int sumPenalty = 0;
        		int pi = blockHeight*(i+1)-1; //Last pixel row in block[i][j]
        		for (int pj=blockWidth*j; pj<blockWidth*(j+1); pj++) {
        			sumPenalty += pBtwRows[pi][pj];
        		}
        		blockPRows[i][j] = sumPenalty/blockWidth;
        	}
        }

        // Setting up the graph for Ford-Fulkson Algorithm
        int n = heightB*widthB+2; //number of nodes for the flow graph
        int s = heightB*widthB; //source node
        int t = heightB*widthB+1; //sink node

        ArrayList<LinkedList<Integer>> adj = new ArrayList<LinkedList<Integer>>(); //adjacency list of neighboring nodes
		HashMap<String, Integer> rMap = new HashMap<String, Integer>();	//edge capacities in initial residual graph
		HashMap<String, Integer> maxCapMap = new HashMap<String, Integer>(); //maximum capacity for edges in residual graph

		for (int i=0; i<n; i++) {
		    adj.add(new LinkedList<Integer>());
		}

        for (int v=0; v<n-2; v++) {
        	int bi = v / widthB; //row index of the block corresponding to [v]
        	int bj = v % widthB; //column index of the block corresponding to [v]

        	//Addidng reward edges for foreground
        	int rewardf = blockFR[bi][bj];
        	String key = Integer.toString(s) + " " + Integer.toString(v);
			String reverseKey = Integer.toString(v) + " " + Integer.toString(s);

			adj.get(s).add(v);
			rMap.put(key, rewardf);
			maxCapMap.put(key, rewardf);
			maxCapMap.put(reverseKey, rewardf);


			//Adding reward edges for background
			int rewardb = blockBR[bi][bj];
        	key = Integer.toString(v) + " " + Integer.toString(t);
			reverseKey = Integer.toString(t) + " " + Integer.toString(v);

			adj.get(v).add(t);
			rMap.put(key, rewardb);
			maxCapMap.put(key, rewardb);
			maxCapMap.put(reverseKey, rewardb);


			//Adding the penalty edges between row blocks
        	if (bi < heightB-1) {
				int aboveV = v + widthB;
				adj.get(v).add(aboveV);
				adj.get(aboveV).add(v);

				key = Integer.toString(v) + " " + Integer.toString(aboveV);
				reverseKey = Integer.toString(aboveV) + " " + Integer.toString(v);
				int rCapacity = blockPRows[bi][bj];
				int maxRCapacity = 2*rCapacity;

				rMap.put(key, rCapacity);
				rMap.put(reverseKey, rCapacity);
				maxCapMap.put(key, maxRCapacity);
				maxCapMap.put(reverseKey, maxRCapacity);
        	}


        	//Adding the penalty edges between column blocks
        	if (bj < widthB-1) {
				int rightV = v+1;
				adj.get(v).add(rightV);
				adj.get(rightV).add(v);

				key = Integer.toString(v) + " " + Integer.toString(rightV);
				reverseKey = Integer.toString(rightV) + " " + Integer.toString(v);
				int rCapacity = blockPCols[bi][bj];
				int maxRCapacity = 2*rCapacity;

				rMap.put(key, rCapacity);
				rMap.put(reverseKey, rCapacity);
				maxCapMap.put(key, maxRCapacity);
				maxCapMap.put(reverseKey, maxRCapacity);
        	}

        }

        // Running the Ford-Fulkson Algorithm
        FlowGraph ford = new FlowGraph(n, s, t, adj, rMap, maxCapMap);
        ArrayList<Integer> setA = ford.findA();

        // Populating [foreground]
        foreground = new boolean[height][width];
        for (int i=0; i<height; i++) {
        	for (int j=0; j<width; j++) {
        		foreground[i][j] = false;
        	}
        }

        for (int a=0; a<setA.size(); a++) {
        	int v = setA.get(a);
        	int bi = v / widthB;
        	int bj = v % widthB;

        	for (int pi=bi*blockHeight; pi<(bi+1)*blockHeight; pi++) {
        		for (int pj=bj*blockWidth; pj<(bj+1)*blockWidth; pj++) {
        			foreground[pi][pj] = true;
        		}
        	}
        }

        output();
    }

    public static void main(String[] args) {
        new Main();
    }
}
