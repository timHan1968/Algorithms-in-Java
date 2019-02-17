# Sequence Alignment

Java implementation of Sequence Alignment through Dynamic Programming.
Takes input information through standard input and output the optimal alignment through standard output.

**Input format:**

4	4	0	# 4 symbols in the first word, 4 in second
pear 		# First word
bear		# Second word
3			# Gap penalty (for unmatched symbol)
a   a   0   # 0 penalty for matching 'a' with itself
a   b   7   # penalty of 7 for mismatching 'a' with 'b'
...
b   a   7   # penalty of 7 for mismatching 'b' with 'a'
b   b   2   # penalty of 2 for matching 'b' with itself
...
z   z   0 

**Output format:**

10			# Cost of optimal alignment
3			# Number of matches in optimal alignment
1   1       # 'p' matched to 'b'
2   2       # 'e' matched to 'e'
4   3       # 'r' matched to 'a'