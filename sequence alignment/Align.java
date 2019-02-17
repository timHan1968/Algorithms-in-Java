import java.util.*;
import java.io.*;

class Main{

	int lenA; //Length of the first string
	int lenB; //Length of the second string

	String wordA; //First word
	String wordB; //Second word

	int g; //Gap penalty
	Map<String, Integer> penaltyMap; //Hashmap for penalty costs

	int optArray[][]; //Array that stores the optimum penalty cost using each match
	String optPairs[]; //List that stores pairs in the optimal match
	int pairNum; //Number of mathced pairs in the optimal match


	// Reading Standard Inputs
	void readInput() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			
			String line = input.readLine();
			String [] numbers = line.split(" ");

			lenA = Integer.parseInt(numbers[0]);
			lenB = Integer.parseInt(numbers[1]); 

			wordA = input.readLine().toLowerCase(); 
			wordB = input.readLine().toLowerCase();

			g = Integer.parseInt(input.readLine());

			String key; 
			String [] chars;
			penaltyMap = new HashMap<String, Integer>();

			for(int i=0;i<676;i++) {
				line = input.readLine();
				chars = line.split(" ");
				key = chars[0] + " " + chars[1];
				penaltyMap.put(key, Integer.parseInt(chars[2]));

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
			
			output.write(Integer.toString(optArray[lenA][lenB]));
			output.newLine();

			output.write(Integer.toString(pairNum));
			output.newLine();

			for(int i=pairNum-1;i>=0;i--) {
				output.write(optPairs[i]);
				output.newLine();
			}

			output.close();

		} catch (IOException io) {
			io.printStackTrace();
		}
	}


	//Simple method for 3-element min
	public int min(int a, int b, int c) {
		if (a<=b && a<=c) {
			return a; 
		}
		else if (b<=a && b<=c) {
			return b;
		}
		else {
			return c;
		}
	}


	//Finding all Opt(i,j) and store them in [OptArray]
	public void alignment() {
		optArray = new int[lenA+1][lenB+1];

		//Initializing the array:
		optArray[0][0] = 0;
		for(int i=1;i<=lenA;i++) {
			optArray[i][0] = i*g;
		}
		for(int j=1;j<=lenB;j++) {
			optArray[0][j] = j*g;
		}

		//Starting the 'recurrsion':
		int p;
		String pair;
		for(int i=1;i<=lenA;i++) {
			for(int j=1;j<=lenB;j++) {
				pair = wordA.charAt(i-1) + " " + wordB.charAt(j-1);
				p = penaltyMap.get(pair);
				optArray[i][j] = min(p+optArray[i-1][j-1], g+optArray[i-1][j], g+optArray[i][j-1]);
			}
		}
	}


	//Trace back [optArray] to find optiaml matches
	public void findSolution() {
		if (lenA > lenB) {
			optPairs = new String[lenB];
		} else {
			optPairs = new String[lenA];
		}

		int counter = 0;
		int i = lenA;
		int j = lenB;

		while(i>0 && j>0) {
			if (optArray[i][j] == g+optArray[i-1][j]) {
				i--;
			}
			else if (optArray[i][j] == g+optArray[i][j-1]) {
				j--;
			}
			else {
				optPairs[counter] = i + " " + j;
				counter++;
				i--;
				j--;
			}
		}

		pairNum = counter;

	}

	public Main() {
		readInput();
		alignment();
		findSolution();
		writeOutput();
	}

	public static void main(String[] args) {

		new Main();
		
	}
}