# Network Flow (Ford Fulkerson Algorithm)

Java implementation of Ford-Fulkerson Algorithm. The search for augmented path uses BFS which finds the path with smallest number of edges and thus gives a total time complexity of O(mn).

Takes input information through standard input and output the maximum flow as an integer.

**Input format:**
4	5   	# 4 nodes, 5 edges
1 3 20 		# edge from 1 to 3 with capacity 20
1 4 10 		# edge from 1 to 4 with capacity 10
3 4 20 		# edge from 3 to 4 with capacity 20
3 2 10 		# edge from 3 to 2 with capacity 10
4 2 20 		# edge from 4 to 2 with capacity 20

Note*: By default, 1 is source node and 2 is sink node here
.
**Output format:**
30			# value of max flow
