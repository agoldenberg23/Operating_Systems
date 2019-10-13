import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Banker_Old
{
	static boolean debug = true;
	
	public static class task
	{	
		//General variables
		int id;			//The id number
		
		int resorceNum;	//The number of resources in the system
		int[] have;		//The amount of each resource owned
		int[] claim;	//The initial claims
		
		//Variables for the optimistic run
		int OtimeStart = -1;
		int OtimeTaken = -1;
		int OwaitTime = 0;
		//How the process ends: 0 = continuing, -1 = aborted, 1 = ended as expected
		int Oterm = 0;
		
		//Variables for the bankers run
		int BtimeStart = -1;
		int BtimeTaken = -1;
		int BwaitTime = 0;
		//How the process ends: 0 = continuing, -1 = aborted, 1 = ended as expected
		int Bterm = 0;
		
		//Create a task
		public task(int id, int resorceNum)
		{
			this.id = id;
			this.resorceNum = resorceNum;
			this.have = new int[resorceNum];
			this.claim = new int[resorceNum];
			this.init();
		}
		
		//Initializes the variables claim and have
		public void init()
		{
			for(int i = 0; i < this.resorceNum; i++)
			{
				this.claim[i] = 0;
				this.have[i] = 0;
			}
		}
		
		//Gives the task the resources
		public void OtakeResorce(int[] sysResorce, int resorce, int takeNum)
		{
			sysResorce[resorce] = sysResorce[resorce] - takeNum;
			this.have[resorce] = takeNum + this.have[resorce];
		}
		
		//Releases resources
		public void OreleaseResorce(int[] sysResorce, int resorce, int giveNum)
		{
			sysResorce[resorce] = sysResorce[resorce] + giveNum;
			this.have[resorce] = this.have[resorce] - giveNum;
		}
		
		//Terminates and releases the resources
		public void terminate(int time, int[] sysResorce, boolean banker)
		{
			if(banker)
			{
				this.Bterm = 1;
				this.BtimeTaken = time - this.BtimeStart;
			}
			else
			{
				this.Oterm = 1;
				this.OtimeTaken = time - this.OtimeStart;
			}
			
			for(int i = 0; i < this.resorceNum; i++)
			{
				sysResorce[i] = sysResorce[i] + this.have[i];
				this.have[i] = 0;
			}
		}
		
		//Changes the claim array and returns if valid
		public boolean claimResorce(int resorce, int claimNum)
		{
			if(this.claim[resorce] != 0)
			{
				System.out.println("Trying to claim a recorce that has been claimed previously");
				return false;
			}
			
			this.claim[resorce] = claimNum;
			return true;
		}
		
		//Checks if a resource request is good by claims
		public boolean checkRequest(int resorce, int wanted)
		{
			if(wanted > this.claim[resorce])
			{
				return false;
			}
			return true;
		}
		
		//Aborts the program and gives the resources they had
		public int[] abort(int[] sysResorce, boolean banker)
		{
			if(banker)
				this.Bterm = -1;
			else
				this.Oterm = -1;
			
			for(int i = 0; i < this.resorceNum; i++)
			{
				sysResorce[i] = sysResorce[i] + this.have[i];
				this.have[i] = 0;
			}
			
			return this.have;
		}
		
		//Wipes the have variables for next run
		public void wipeHave()
		{
			for(int i = 0; i < this.resorceNum; i++)
			{
				this.have[i] = 0;
			}
		}
		
		//Gives the % of time spent waiting for optimistic
		public double OPerTimeWait()
		{
			return ((((double)this.OwaitTime) / ((double)this.OtimeTaken)) * 100);
		}
		
		//Gives the % of time spent waiting for banker
		public double BPerTimeWait()
		{
			return ((((double)this.BwaitTime) / ((double)this.BtimeTaken)) * 100);
		}
		
		//Prints the optimistic print statement
		public void print(boolean banker)
		{
			if(banker)
				System.out.println("Task #: " + this.id + " term: " + this.Bterm);
			else
				System.out.println("Task #: " + this.id + " term: " + this.Oterm);
			
			for(int i = 0; i < this.resorceNum; i++)
			{
				System.out.println("Resorce #: " + i + " Has: " + this.have[i] 
						+ " claim: " + this.claim[i]);
			}
			System.out.println();
		}
	
		//Prints the line at the end output
		public void printEnd()
		{
			System.out.print("     Task " + (this.id+1) + "      ");
			System.out.print(this.OtimeTaken + "   " + this.OwaitTime + "   " + this.OPerTimeWait() + "%");
			System.out.print("           Task " + (this.id+1) + "        ");
			System.out.println(this.BtimeTaken + "   " + this.BwaitTime + "   " + this.BPerTimeWait() + "%");
		}
	}
	
	//Prints the end output
	public static void printEnd(task[] task, int taskNum)
	{
		System.out.println("              FIFO                             BANKER'S");
		
		int Owait = 0;
		int Bwait = 0;
		int Otime = 0;
		int Btime = 0;
		
		for(int i = 0; i < taskNum; i++)
		{
			task[i].printEnd();
			Owait = Owait + task[i].OwaitTime;
			Bwait = Bwait + task[i].BwaitTime;
			Otime = Otime + task[i].OtimeTaken;
			Btime = Btime + task[i].BtimeTaken;
		}
		
		System.out.print("     total       " + Otime + "   " + Owait + "   "
				+ ((((double)Owait) / ((double)Otime)) * 100));
		System.out.print("%           total         " + Btime + "   " + Bwait + "   "
				+ ((((double)Bwait) / ((double)Btime)) * 100) + "%");
	}
	
	//Prints all of the tasks
	public static void printTasks(task[] taskList, int taskNum, boolean banker)
	{
		for(int i = 0; i < taskNum; i++)
		{
			taskList[i].print(banker);
		}
	}
	
	//Prints the resources in the system
	public static void printSys(int[] sysResorceCur, int resorceNum)
	{
		for(int i = 0; i < resorceNum; i++)
		{
			System.out.println("Resorce: " + i + " cur: " + sysResorceCur[i]);
		}
	}
	
	//Runs the optimistic algorithm and returns the time ended
	public static void optimistic(File file, task[] task, int resorceNum, int taskNum, int[] sysResorce) throws FileNotFoundException
	{
		//Read the lines of the file
		Scanner sc = new Scanner(file);
		sc.nextLine();	//Skip the resource numbers
		
		int time = 0;
		String line;
		int id;
		
		while(sc.hasNextLine())
		{
			line = sc.next();
			id = sc.nextInt() - 1;
			
			if(debug)
				System.out.println("Time: " + time);
			
			if(line.equals("initiate"))
			{
				//Skips the line in optimistic
				sc.nextLine();
				
				if(debug)
					System.out.println("Initiate id " + id + " skiped");
				
				if(task[id].OtimeStart == -1)
				{
					task[id].OtimeStart = time;
				}
			}
			else if(line.equals("request"))
			{
				int resorce = sc.nextInt() - 1;
				int num = sc.nextInt();
				
				if(debug)
					System.out.println("Request id " + id + " resorce: " + resorce + " num: " + num);
				
				//Grant the resource request
				if(sysResorce[resorce] >= num)
				{
					if(debug)
						System.out.println("Request Granted, sys resorce = " + sysResorce[resorce]);
					
					task[id].OtakeResorce(sysResorce, resorce, num);
				}
				else	//Deny the request
				{
					if(debug)
						System.out.println("Request denied, sys resorce = " + sysResorce[resorce]);
				}
			}
			else if(line.equals("release"))
			{
				int resorce = sc.nextInt() - 1;
				int num = sc.nextInt();
				
				if(debug)
					System.out.println("Release id " + id + " resorce: " + resorce + " num: " + num);
				
				task[id].OreleaseResorce(sysResorce, resorce, num);
			}
			else if(line.equals("terminate"))
			{
				sc.nextLine();	//Skip rest of unused line
				
				if(debug)
					System.out.println("Terminating " + id);
				
				task[id].terminate(time, sysResorce, false);
			}
			else if(line.equals("compute"))
			{
				int num = sc.nextInt();
				sc.next();	//Skip unused number
				
				if(debug)
					System.out.println("compute id " + id + " for " + num);
			}
			else
				System.out.println("ERROR - Unknown comand " + line);
			
			time++;
			
			if(debug)
			{
				System.out.println("System:");
				printSys(sysResorce, resorceNum);
				System.out.print("\nTasks");
				printTasks(task, taskNum, false);
			}
		}
		
		//Stop reading the file
		sc.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		String fileName = "input.txt";
		//Use a file to input
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		
		int taskNum = sc.nextInt();		//The number of tasks in the system
		int resorceNum = sc.nextInt();	//The number of resources in the system
		
		//Declare variables used
		task[] taskList = new task[taskNum];	//An array of tasks
		int[] sysResorce = new int[resorceNum];//The number of resources currently held in the system
		
		//Initialize the task list
		for(int i = 0; i < taskNum; i++)
		{
			taskList[i] = new task(i, resorceNum);
		}
		
		//Read in the max resources
		for(int i = 0; i < resorceNum; i++)
		{
			int num = sc.nextInt();
			sysResorce[i] = num;
		}
		
		if(false)
		{
			System.out.println("Start resorces");
			printSys(sysResorce, resorceNum);
			System.out.println();
			printTasks(taskList, taskNum, false);
		}
		
		//Stop reading
		sc.close();
		
		optimistic(file, taskList, resorceNum, taskNum, sysResorce);
		
		printEnd(taskList, taskNum);
	}
}
