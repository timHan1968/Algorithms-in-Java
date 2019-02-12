import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class Framework
{
	int n; // number of applicants (employers)

	int APrefs[][]; // preference list of applicants (n*n)
	int EPrefs[][]; // preference list of employers (n*n)

	ArrayList<MatchedPair> MatchedPairsList; // arraylist to store final pairs

	public class MatchedPair
	{
		int appl; // applicant's number
		int empl; // employer's number

		public MatchedPair(int Appl,int Empl)
		{
			appl=Appl;
			empl=Empl;
		}

		public MatchedPair()
		{
		}
	}

	// reading the input
	void input(String input_name)
	{
		File file = new File(input_name);
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new FileReader(file));

			String text = reader.readLine();

			String [] parts = text.split(" ");
			n=Integer.parseInt(parts[0]);

			APrefs=new int[n][n];
			EPrefs=new int[n][n];

			for (int i=0;i<n;i++)
			{
				text=reader.readLine();
				String [] aList=text.split(" ");
				for (int j=0;j<n;j++)
				{
					APrefs[i][j]=Integer.parseInt(aList[j]);
				}
			}

			for (int i=0;i<n;i++)
			{
				text=reader.readLine();
				String [] eList=text.split(" ");
				for(int j=0;j<n;j++)
				{
					EPrefs[i][j]=Integer.parseInt(eList[j]);
				}
			}

			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// writing the output
	void output(String output_name)
	{
		try
		{
			PrintWriter writer = new PrintWriter(output_name, "UTF-8");

			for(int i=0;i<MatchedPairsList.size();i++)
			{
				writer.println(MatchedPairsList.get(i).empl+" "+MatchedPairsList.get(i).appl);
			}

			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Framework(String []Args)
	{
		input(Args[0]);

		MatchedPairsList=new ArrayList<MatchedPair>(); // the final stable matching will be in this array list

		LinkedList<Integer> freeE = new LinkedList<Integer>();
		int [] next = new int[n];
		int [] current = new int[n];
		int [][] ARanks = new int[n][n];
		for(int i=0; i<n; i++){
			freeE.add(i);
			next[i] = 0;
			current[i] = -1;
			for(int j=0; j<n; j++){
				//ARanks[i][j] returns Ai's preference rank for Ej
 				ARanks[i][APrefs[i][j]] = j;
			}
		}
		
		int e; //Employer proposing
		int a; //Applicant proposing
		int c; //Current employer who hires [a]
		while(!freeE.isEmpty()){
			e = freeE.remove();
			a = EPrefs[e][next[e]];
			c = current[a];
			if (c == -1){
				// [a] is not hired and accepts [e]'s offer
				current[a] = e;
			} else if (ARanks[a][e] < ARanks[a][c]) {
				// [a] is hired but prefers [e], [c] returns to freeList
				current[a] = e;
				freeE.addFirst(c);
			} else {
				// [a] is hired and prefers [c], [e] returns to freeList
				freeE.addFirst(e);
			}
			next[e] = next[e] + 1;

		}
		
		for (int i=0; i<n; i++){
			MatchedPair pair= new MatchedPair(i, current[i]);
			MatchedPairsList.add(pair);
		}
		

		output(Args[1]);
	}

	public static void main(String [] Args) // Strings in Args are the name of the input file followed by the name of the output file
	{
		new Framework(Args);
	}
}
