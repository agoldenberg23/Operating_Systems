import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Banker
{
	static boolean inputDebug = false;	//Enable debug output for the input
	static boolean Odebug = false;		//Enable debug output for optimistic
	static boolean timeLim = false;		//Enable time limit of 30
	static boolean Bdebug = false;		//Enable debug output for banker
	
	//Class to hold the operations the tasks complete
	//The operations the tasks carry out
	public static class operation
	{
		String oper;			//The operation to be carried out
		int resorceType = -1;	//The resource type by number the operation effects
		int number = -1;		//Any other number the operation needs
		
		//Create for request and release and initiate
		public operation(String oper, int resorceType, int number)
		{
			this.oper = oper;
			this.resorceType = resorceType;
			this.number = number;
		}
		
		//Create for compute
		public operation(String oper, int number)
		{
			this.oper = oper;
			this.number = number;
		}
		
		//Create for terminate
		public operation(String oper)
		{
			this.oper = oper;
		}
		
		public String toString()
		{
			return(this.oper + " " + this.resorceType + " " + this.number);
		}
	}
	
	//Class to hold a specific task and all variables associated with it
	//The tasks that can request and use resources
	public static class task
	{	
		//General variables
		int id;			//The id number
		int[] have;		//The amount of each resource owned
		int[] claim;	//The initial claims
		List<operation> oper = new ArrayList<operation>();	//Lists the operations
		int instNum = 0;	//The instruction number
		int computeTime = 0;//Time left in a compute
		boolean blocked = false;	//If the task is blocked
		boolean skip = false;		//If the task is skipped (newly unblocked)
		
		//Variables for the optimistic run
		int OtimeTaken = 0;	//The amount of time taken
		int OwaitTime = 0;	//The time spent waiting
		//How the process ends: 0 = continuing, -1 = aborted, 1 = ended as expected
		int Oterm = 0;
		
		//Variables for the bankers run
		int BtimeTaken = 0;	//The amount of time taken
		int BwaitTime = 0;	//The time spent waiting
		//How the process ends: 0 = continuing, -1 = aborted, 1 = ended as expected
		int Bterm = 0;
		
		//Create a task
		public task(int id, int resorceNum)
		{
			this.id = id;
			this.have = new int[resorceNum];
			this.claim = new int[resorceNum];
			this.init();
		}
		
		//Initializes the variables claim and have
		public void init()
		{
			for(int i = 0; i < this.claim.length; i++)
			{
				this.claim[i] = 0;
				this.have[i] = 0;
			}
		}
		
		//Gives the task the resources or returns false for optimistic
		public boolean OtakeResorce(int[] sysResorce, int[] resorceAdd, int resorce, int takeNum)
		{
			if(takeNum > (sysResorce[resorce] - resorceAdd[resorce]))
			{
				//Not enough resources
				return false;
			}
			
			sysResorce[resorce] = sysResorce[resorce] - takeNum;
			this.have[resorce] = takeNum + this.have[resorce];
			return true;
		}
		
		//Gives the task the resources or returns false for the bankers run
		public boolean BtakeResorce(int[] sysResorce, int[] resorceAdd, int resorce, int takeNum, 
				task[] task, int resorceNum, int taskNum)
		{
			if(Bdebug)
			{
				System.out.println("Checking if system is safe to take resorce " + 
						resorce + " number " + takeNum);
				System.out.print("Original system ");
				printSys(sysResorce, resorceNum);
			}
				
			int[] change = sysResorce.clone();	//the change in the sysResorce
			change[resorce] = sysResorce[resorce] - takeNum - resorceAdd[resorce];
			
			//Give the remorse to test it
			this.have[resorce] = this.have[resorce] + takeNum;
			
			boolean safe = isSafe(task, resorceNum, taskNum, change);
			
			if(safe == false)
			{
				this.have[resorce] = this.have[resorce] - takeNum;
				//System not safe so false
				return false;
			}
			
			sysResorce[resorce] = sysResorce[resorce] - takeNum;
			return true;
		}
		
		//Releases resources
		public void OreleaseResorce(int[] sysResorce, int resorce, int giveNum)
		{
			sysResorce[resorce] = sysResorce[resorce] + giveNum;
			this.have[resorce] = this.have[resorce] - giveNum;
		}
		
		//Terminates and releases the resources, returns what it had
		public int[] terminate(int time, int[] sysResorce, boolean banker)
		{
			if(banker == true)
			{
				this.Bterm = 1;
				this.BtimeTaken = time;
			}
			else
			{
				this.Oterm = 1;
				this.OtimeTaken = time;
			}
			
			int[] ret = this.have;
			
			for(int i = 0; i < this.have.length; i++)
			{
				sysResorce[i] = sysResorce[i] + this.have[i];
				this.have[i] = 0;
			}
			
			return ret;
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
		
		//Checks if a resource request is good by claims, will give -1 
		//if good and the resource that causes it to fail otherwise
		public int BcheckClaims(int[] sysResorce, int resorceNum)
		{
			for(int i = 0; i < resorceNum; i++)
			{
				if(this.claim[i] > sysResorce[i])
				{
					if(Bdebug)
						System.out.println("Resorece " + i 
								+ " claim exceeds systems in " + this.id);
					return i;
				}
			}
				
			//No problem with the claim
			return -1;
		}
		
		//Aborts the program and gives the resources they had
		public int[] abort(int[] sysResorce, boolean banker)
		{
			if(banker)
				this.Bterm = -1;
			else
				this.Oterm = -1;
			
			for(int i = 0; i < this.have.length; i++)
			{
				sysResorce[i] = sysResorce[i] + this.have[i];
				this.have[i] = 0;
			}
			
			return this.have;
		}
		
		//Wipes the have variables and instNum for next run
		public void reset()
		{
			this.instNum  = 0;
			this.computeTime = 0;
			this.blocked = false;
			
			for(int i = 0; i < this.have.length; i++)
			{
				this.have[i] = 0;
			}
		}
		
		//Gives the % of time spent waiting for optimistic
		public int OPerTimeWait()
		{
			return (int)round(((((double)this.OwaitTime) / ((double)this.OtimeTaken)) * 100), 0);
		}
		
		//Gives the % of time spent waiting for banker
		public int BPerTimeWait()
		{
			return (int)round(((((double)this.BwaitTime) / ((double)this.BtimeTaken)) * 100), 0);
		}
		
		//Prints the optimistic print statement
		public void print(boolean banker)
		{
			if(banker)
				System.out.println("Task #: " + this.id + " term: " + this.Bterm);
			else
				System.out.println("Task #: " + this.id + " term: " + this.Oterm);
			
			for(int i = 0; i < this.claim.length; i++)
			{
				System.out.println("Resorce #: " + i + " Has: " + this.have[i] 
						+ " claim: " + this.claim[i]);
			}
			System.out.println();
		}
	
		//Prints the line at the end output
		public void printEnd()
		{
			System.out.print("       Task " + (this.id+1) + "     ");
			
			if(this.Oterm == -1)
			{
				System.out.print("  aborted            ");
			}
			else
			{
				if(this.OtimeTaken < 10)
				{
					System.out.print("  ");
				}
				else if(this.OtimeTaken < 100)
				{
					System.out.print(" ");
				}
				System.out.print(this.OtimeTaken);
				
				if(this.OwaitTime < 10)
				{
					System.out.print("  ");
				}
				else if(this.OwaitTime < 100)
				{
					System.out.print(" ");
				}
				System.out.print(" " + this.OwaitTime);
				
				int temp = this.OPerTimeWait();
				if(temp < 10)
				{
					System.out.print("    ");
				}
				else if(temp < 100)
				{
					System.out.print("   ");
				}
				else
				{
					System.out.print("  ");
				}
				System.out.print(temp + "%        ");
			}
			
			System.out.print("Task " + (this.id+1) + "     ");
			
			if(this.Bterm == -1)
			{
				System.out.println("  aborted            ");
			}
			else
			{
				if(this.BtimeTaken < 10)
				{
					System.out.print("  ");
				}
				else if(this.BtimeTaken < 100)
				{
					System.out.print(" ");
				}
				System.out.print(this.BtimeTaken);
				
				if(this.BwaitTime < 10)
				{
					System.out.print("  ");
				}
				else if(this.BwaitTime < 100)
				{
					System.out.print(" ");
				}
				System.out.print(" " + this.BwaitTime);
				
				int temp = this.BPerTimeWait();
				if(temp < 10)
				{
					System.out.print("    ");
				}
				else if(temp < 100)
				{
					System.out.print("   ");
				}
				else
				{
					System.out.print("  ");
				}
				System.out.println(temp + "%");
			}
		}
	}
	
	//Prints the end output
	public static void printEnd(task[] task, int taskNum)
	{
		System.out.println("                       FIFO                    BANKER'S");
		
		//Total times for wait and time spent
		int Owait = 0;
		int Bwait = 0;
		int Otime = 0;
		int Btime = 0;
		
		for(int i = 0; i < taskNum; i++)
		{
			task[i].printEnd();
			
			if(task[i].Oterm == 1)
			{
				Owait = Owait + task[i].OwaitTime;
				Otime = Otime + task[i].OtimeTaken;
			}
			if(task[i].Bterm == 1)
			{
				Bwait = Bwait + task[i].BwaitTime;
				Btime = Btime + task[i].BtimeTaken;
			}
			//Do not include aborted
		}
		
		//For the optimistic end total
		System.out.print("       total     ");
		
		if(Otime < 10)
		{
			System.out.print("  ");
		}
		if(Otime < 100)
		{
			System.out.print(" ");
		}
		System.out.print(Otime + "");
		
		if(Owait < 10)
		{
			System.out.print("  ");
		}
		if(Owait < 100)
		{
			System.out.print(" ");
		}
		System.out.print(Owait + "  ");
		
		int temp = (int)round(((((double)Owait) / ((double)Otime)) * 100), 0);
		if(temp < 10)
		{
			System.out.print("  ");
		}
		if(temp < 100)
		{
			System.out.print(" ");
		}
		System.out.print(temp + "%        ");
		
		
		//For the banker end output
		System.out.print("total      ");
		
		if(Btime < 10)
		{
			System.out.print("  ");
		}
		if(Btime < 100)
		{
			System.out.print(" ");
		}
		System.out.print(Btime + "");
		
		if(Bwait < 10)
		{
			System.out.print("  ");
		}
		if(Bwait < 100)
		{
			System.out.print(" ");
		}
		System.out.print(Bwait + "  ");
		
		temp = (int)round(((((double)Bwait) / ((double)Btime)) * 100), 0);
		if(temp < 10)
		{
			System.out.print("  ");
		}
		if(temp < 100)
		{
			System.out.print(" ");
		}
		System.out.println(temp + "%");
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
	
	//Sets an array given to 0 and returns it
	public static int[] wipeArray(int[] arr)
	{
		for(int i = 0; i < arr.length; i++)
		{
			arr[i] = 0;
		}
		
		return arr;
	}
	
	//Rounds a decimal
	private static double round(double value, int places)
	{
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	//Runs the optimistic algorithm and returns the time ended
	public static void optimistic(task[] task, int resorceNum, int taskNum, int[] sysResorce)
	{
		int time = 0;				//The time counter in cycles
		int taskLeft = taskNum;		//The number of tasks not terminated (run until finished)
		String line;				//The instruction the task is to do
		int failedTake; 			//Number of process trying to get a resource and failing
		int[] resorceAdded = new int[resorceNum];	//The number of resources added in an iteration
		List<Integer> blocked = new ArrayList<Integer>();	//The list of blocked
		
		while(taskLeft > 0)
		{
			if(Odebug)
			{
				System.out.println("Time Step: " + time);
			}
				
			failedTake = 0;
			resorceAdded = wipeArray(resorceAdded);
			
			//Handle the blocked tasks before the rest
			if(blocked.isEmpty() == false)
			{
				Iterator itr = blocked.iterator();
				while(itr.hasNext())
				{	
					int t = (int) itr.next();
				
					//The task has already been completed/aborted elsewhere
					if(task[t].Oterm != 0)
					{
						task[t].skip = true;
						continue;
					}
					
					if(Odebug)
					{
						System.out.println("Handleing task " + t + " from blocked");
					}
						
					int resorce = task[t].oper.get(task[t].instNum).resorceType;
					int takeNum = task[t].oper.get(task[t].instNum).number;
					
					boolean sucsses = task[t].OtakeResorce(sysResorce, resorceAdded, resorce, takeNum);
					
					if(Odebug)
					{
						System.out.println("task " + t + " reuesting resorce type " + resorce + " number " + takeNum);
					}
						
					if(sucsses == false)
					{
						//Request not granted, block operation
						failedTake++;
						task[t].OwaitTime++;
						
						if(Odebug)
						{
							System.out.println("Request denied, blocking: ");
							printSys(sysResorce, resorceNum);
							System.out.println();
						}
					}
					else
					{
						//Request granted
						task[t].blocked = false;
						task[t].skip = true;
						//Remove latter
						
						//Go to next operation
						task[t].instNum++;
						
						if(Odebug)
						{
							System.out.print("Request granted: ");
							printSys(sysResorce, resorceNum);
							System.out.println();
						}
					}
				}
			}
			
			//Handle standard operations
			for(int t = 0; t < taskNum; t++)	//t = the task acting
			{
				//Skip newly unblocked tasks
				if(task[t].skip == true)
				{
					task[t].skip = false;
					blocked.remove((Integer) t);
					continue;
				}
				
				//Skip if task is complete or aborted
				if(task[t].Oterm != 0)
				{
					continue;
				}
				
				//Skip blocked tasks (handled previously)
				if(task[t].blocked == true)
				{
					continue;
				}
				
				if(task[t].computeTime == 0)
				{
					if(Odebug)
					{
						System.out.println(task[t].oper.get(task[t].instNum).toString());
					}
						
					line = task[t].oper.get(task[t].instNum).oper;
					
					if(line.equals("initiate"))
					{
						//Ignore initiate but account for it here
						if(Odebug)
						{
							System.out.println("task " + t + " initiate ignored");
						}
					}
					else if(line.equals("request"))
					{
						int resorce = task[t].oper.get(task[t].instNum).resorceType;
						int takeNum = task[t].oper.get(task[t].instNum).number;
						
						boolean sucsses = task[t].OtakeResorce(sysResorce, resorceAdded, resorce, takeNum);
						
						if(Odebug)
						{
							System.out.println("task " + t + " reuesting resorce type " + resorce + " number " + takeNum);
						}
							
						if(sucsses == false)
						{
							//Request not granted, block operation
							task[t].instNum--;	//Task does not go to the next operation
							
							failedTake++;
							task[t].OwaitTime++;
							
							blocked.add(t);
							task[t].blocked = true;
							
							if(Odebug)
							{
								System.out.println("Request denied, blocking: ");
								printSys(sysResorce, resorceNum);
								System.out.println();
							}
						}
						else if(Odebug)
						{
							System.out.print("Request granted: ");
							printSys(sysResorce, resorceNum);
							System.out.println();
						}
					}
					else if(line.equals("release"))
					{
						int resorce = task[t].oper.get(task[t].instNum).resorceType;
						int giveNum = task[t].oper.get(task[t].instNum).number;
						
						if(Odebug)
						{
							System.out.println("task " + t + " releasing resorce type " + resorce + " number " + giveNum);
						}
							
						if(giveNum > task[t].have[resorce])
						{
							System.out.println("Error - trying to release more then had, releasing what had");
							giveNum = task[t].have[resorce];
						}
						
						task[t].OreleaseResorce(sysResorce, resorce, giveNum);
						resorceAdded[resorce] = resorceAdded[resorce] + giveNum;
						
						if(Odebug)
						{
							printSys(sysResorce, resorceNum);
						}
					}
					else if(line.equals("terminate"))
					{
						taskLeft--;
						int[] had = task[t].terminate(time, sysResorce, false);
						
						//Add the resources gained to the number added this iteration
						for(int i = 0; i < resorceNum; i++)
						{
							resorceAdded[i] = resorceAdded[i] + had[i];
						}
						
						if(Odebug)
						{
							System.out.println("Task " + t + " terminateing");
						}
					}
					else if(line.equals("compute"))
					{
						task[t].computeTime = task[t].oper.get(task[t].instNum).number - 1;
						
						if(Odebug)
						{
							System.out.println("task " + t + " compute for " + task[t].computeTime);
						}
					}
					else
						System.out.println("ERROR - Unknown comand - cannot run - " + line);
					
					//Go to the next task
					task[t].instNum++;
				}
				else	//Handle the compute time
				{
					task[t].computeTime--;
					
					if(Odebug)
					{
						System.out.println("Task " + t + " compute time left = " + task[t].computeTime);
					}
				}
			}
			
			if(Odebug)
			{
				System.out.println("Failed take = " + failedTake + " task left = " + taskLeft);
			}
			
			//Deadlock occurred, aborting processes
			if(failedTake >= taskLeft && taskLeft != 0)
			{
				if(Odebug)
				{
					System.out.println("Deadlock occured - aborting prossesses");
				}
					
				for(int t = 0; t < taskNum; t++)
				{
					
					//Skip if task is complete or aborted
					if(task[t].Oterm != 0)
					{
						continue;
					}
					
					int resorce = task[t].oper.get(task[t].instNum).resorceType;
					int takeNum = task[t].oper.get(task[t].instNum).number;
					
					if(takeNum <= sysResorce[resorce])	//The deadlock ended
					{
						if(Odebug)
						{
							System.out.println("Deadlock broke for " + t + " wanting " + resorce + " number " + takeNum);
							printSys(sysResorce, resorceNum);
						}
						
						failedTake = 0;
						break;
					}
					else
					{
						if(Odebug)
						{
							System.out.println("Aborting process " + t + ", which wanted resorce " + resorce + " number " + takeNum);
						}
							
						task[t].abort(sysResorce, false);
						taskLeft--;
						
						if(Odebug)
						{
							printSys(sysResorce, resorceNum);
						}
					}
				}
			}
			
			time++;
			
			if(time > 30 && timeLim)
			{
				return;
			}
			
			if(Odebug)
			{
				System.out.println("\n");
				//printTasks(task, taskNum, false);
			}
		}
	}
	
	//Checks if the system is safe, should be handed a copy of sysResorce
	public static boolean isSafe(task[] task, int resorceNum, int taskNum, int[] sysResorce)
	{
		boolean[] finish = new boolean[taskNum];	//If the task is finished
		int finished = 0;	//The number of tasks finished
		boolean sucsess;	//If a task was finished in an iteration
		
		if(Bdebug)
		{
			System.out.print("After alter system: ");
			printSys(sysResorce, resorceNum);
		}
		
		//fill the finish array
		for(int i = 0; i < taskNum; i++)
		{
			//Finished
			if(task[i].Bterm != 0)
			{
				finish[i] = true;
				finished++;
			}
			else
			{
				finish[i] = false;
			}
		}
		
		//Stop when there are more more tasks left
		while(finished < taskNum)
		{
			//check all possesses for a possible one to finish
			sucsess = false;
			label: for(int t = 0; t < taskNum; t++)
			{
				if(finish[t] == false)
				{
					//check each resource
					for(int r = 0; r < resorceNum; r++)
					{
						if(task[t].have[r] + sysResorce[r] < task[t].claim[r])
						{
							//Failed to satisfy a need, try next task
							if(Bdebug)
								System.out.println("Task " + t + " claims " + task[t].claim[r]
										+ " tot posible " + (task[t].have[r] + sysResorce[r]) + " - fail");
								
							continue label;
						}
					}
					//Process can be finished
					sucsess = true;
					finished++;
					finish[t] = true;
					
					if(Bdebug)
						System.out.print(t + " fin, ");
					
					//Add resources to the system resources
					for(int i = 0; i < resorceNum; i++)
					{
						sysResorce[i] = sysResorce[i] + task[t].have[i];
					}
				}
				//Skip finished tasks
			}
			
			if(sucsess == false)
			{
				if(Bdebug)
					System.out.println("System not in safe state");
				
				return false;
			}
		}
		
		if(Bdebug)
			System.out.println("System in safe state after change");
		
		return true;
	}
	
	//Runs the optimistic algorithm and returns the time ended
	public static void banker(task[] task, int resorceNum, int taskNum, int[] sysResorce)
	{
			int time = 0;				//The time counter in cycles
			int taskLeft = taskNum;		//The number of tasks not terminated (run until finished)
			String line;				//The instruction the task is to do
			int failedTake; 			//Number of process trying to get a resource and failing
			int[] resorceAdded = new int[resorceNum];	//The number of resources added in an iteration
			List<Integer> blocked = new ArrayList<Integer>();	//The list of blocked
			
			//Check the claims
			for(int t = 0; t < taskNum; t++)
			{
				int check = task[t].BcheckClaims(sysResorce, resorceNum);
				
				//Check = true is good - ignore
				
				if(check != -1)
				{
					System.out.println("  Banker aborts task " + (t+1) + " before run begins:");
					System.out.println("       claim for resourse " + (check + 1) + " (" + task[t].claim[check]
							+ ") exceeds number of units present (" + sysResorce[check] + ")");
					
					task[t].abort(sysResorce, true);
					taskLeft--;
				}
			}
			
			while(taskLeft > 0)
			{
				if(Bdebug)
					System.out.println("Time Step: " + time);
				
				failedTake = 0;
				resorceAdded = wipeArray(resorceAdded);
				
				//Handle the blocked tasks before the rest
				if(blocked.isEmpty() == false)
				{
					Iterator itr = blocked.iterator();
					while(itr.hasNext())
					{	
						int t = (int) itr.next();
					
						//The task has already been completed/aborted elsewhere
						if(task[t].Bterm != 0)
						{
							task[t].skip = true;
							continue;
						}
						
						if(Bdebug)
						{
							System.out.println("task " + t + " instruction num = " + task[t].instNum);
							System.out.println("Handleing task " + t + " from blocked");
						}
							
						int resorce = task[t].oper.get(task[t].instNum).resorceType;
						int takeNum = task[t].oper.get(task[t].instNum).number;
						
						boolean sucsses = task[t].BtakeResorce(sysResorce, resorceAdded, resorce, takeNum, task, resorceNum, taskNum);
						
						if(Bdebug)
							System.out.println("task " + t + " reuesting resorce type " + resorce + " number " + takeNum);
						
						if(sucsses == false)
						{
							//Request not granted, block operation
							failedTake++;
							task[t].BwaitTime++;
							
							if(Bdebug)
							{
								System.out.println("Request denied, blocking: ");
								printSys(sysResorce, resorceNum);
								System.out.println();
							}
						}
						else
						{
							//Request granted
							task[t].blocked = false;
							task[t].skip = true;
							//Remove latter
							
							//Go to next operation
							task[t].instNum++;
							
							if(Bdebug)
							{
								System.out.print("Request granted: ");
								printSys(sysResorce, resorceNum);
								System.out.println();
							}
						}
					}
				}
				
				//Handle standard operations
				for(int t = 0; t < taskNum; t++)	//t = the task acting
				{
					//Skip newly unblocked tasks
					if(task[t].skip == true)
					{
						task[t].skip = false;
						blocked.remove((Integer) t);
						continue;
					}
					
					//Skip if task is complete or aborted
					if(task[t].Bterm != 0)
					{
						continue;
					}
					
					//Skip blocked tasks (handled previously)
					if(task[t].blocked == true)
					{
						continue;
					}
					
					if(task[t].computeTime == 0)
					{
						if(Bdebug)
						{	
							System.out.println("task " + t + " instruction num = " + task[t].instNum);
							System.out.println(task[t].oper.get(task[t].instNum).toString());
						}
							
						line = task[t].oper.get(task[t].instNum).oper;
						
						if(line.equals("initiate"))
						{
							//Checked for claim exceeding total system resources previously
							if(Bdebug)
								System.out.println("task " + t + " initiate already checked");
						}
						else if(line.equals("request"))
						{
							int resorce = task[t].oper.get(task[t].instNum).resorceType;
							int takeNum = task[t].oper.get(task[t].instNum).number;
							
							//Exceeded the claim
							if(takeNum + task[t].have[resorce] > task[t].claim[resorce])
							{
								System.out.println("During cycle " + time + "-" + (time+1) 
										+ " of Banker's algorithms");
								System.out.println("   Task " + (t+1) + "'s request exceeds its claim; aborted");
								task[t].abort(sysResorce, true);
								taskLeft--;
								failedTake--;	//Account for not necessarily deadlocked (aborted resources added)
								continue;
							}
							
							boolean sucsses = task[t].BtakeResorce(sysResorce, resorceAdded, resorce, takeNum, task, resorceNum, taskNum);
							
							if(Bdebug)
								System.out.println("task " + t + " reuesting resorce type " + resorce + " number " + takeNum);
							
							if(sucsses == false)
							{
								//Request not granted, block operation
								task[t].instNum--;	//Task does not go to the next operation
								
								failedTake++;
								task[t].BwaitTime++;
								
								blocked.add(t);
								task[t].blocked = true;
								
								if(Bdebug)
								{
									System.out.println("Request denied, blocking: ");
									printSys(sysResorce, resorceNum);
									System.out.println();
								}
							}
							else if(Bdebug)
							{
								System.out.print("Request granted: ");
								printSys(sysResorce, resorceNum);
								System.out.println();
							}
						}
						else if(line.equals("release"))
						{
							int resorce = task[t].oper.get(task[t].instNum).resorceType;
							int giveNum = task[t].oper.get(task[t].instNum).number;
							
							if(Bdebug)
								System.out.println("task " + t + " releasing resorce type " + resorce + " number " + giveNum);
							
							if(giveNum > task[t].have[resorce])
							{
								System.out.println("Error - trying to release more then had, releasing what had");
								giveNum = task[t].have[resorce];
							}
							
							task[t].OreleaseResorce(sysResorce, resorce, giveNum);
							resorceAdded[resorce] = resorceAdded[resorce] + giveNum;
							
							if(Bdebug)
								printSys(sysResorce, resorceNum);
						}
						else if(line.equals("terminate"))
						{
							taskLeft--;
							int[] had = task[t].terminate(time, sysResorce, true);
							
							//Add the resources gained to the number added this iteration
							for(int i = 0; i < resorceNum; i++)
							{
								resorceAdded[i] = resorceAdded[i] + had[i];
							}
							
							if(Bdebug)
								System.out.println("Task " + t + " terminateing");
						}
						else if(line.equals("compute"))
						{
							task[t].computeTime = task[t].oper.get(task[t].instNum).number - 1;
							
							if(Bdebug)
								System.out.println("task " + t + " compute for " + task[t].computeTime);
						}
						else
						{
							System.out.println("ERROR - Unknown comand - cannot run - " + line + " !!!!!!!!!!!!!!!!!!!!!!!!!");
							return;
						}
							
						//Go to the next task
						task[t].instNum++;
					}
					else	//Handle the compute time
					{
						task[t].computeTime--;
						
						if(Bdebug)
							System.out.println("Task " + t + " compute time left = " + task[t].computeTime);
					}
				}
				
				if(Bdebug)
				{
					System.out.println("Failed take = " + failedTake + " task left = " + taskLeft);
				}
				
				//Deadlock occurred, error
				if(failedTake >= taskLeft && taskLeft != 0)
				{
					System.out.println("Error - Deadlock Occured!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("System -");
					printSys(sysResorce, resorceNum);
					System.out.println("\nTasks-");
					printTasks(task, taskNum, true);
					return;
				}
				
				time++;
				
				if(Bdebug)
				{
					System.out.println("\n");
					//printTasks(task, taskNum, false);
				}
				
				if(time > 30 && timeLim)
				{
					return;
				}
				
			}
	}
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		String fileName = args[0];
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
		
		if(inputDebug)
		{
			System.out.println("Start resorces");
			printSys(sysResorce, resorceNum);
			System.out.println();
			printTasks(taskList, taskNum, false);
		}
		
		//Start reading in the operations the tasks complete
		String line;
		int id;
		
		while(sc.hasNext())
		{
			line = sc.next();
			id = sc.nextInt() - 1;
			
			if(line.equals("initiate"))
			{
				int resorceType = sc.nextInt() - 1;
				int claim = sc.nextInt();
				
				if(inputDebug)
					System.out.println("Initiate id: " + id + " resorce: " + resorceType + " claim: " + claim);
				
				//Add to the initialization to the tasks claim
				taskList[id].claimResorce(resorceType, claim);
				
				//Add to the tasks operation list
				operation op = new operation(line, resorceType, claim);
				taskList[id].oper.add(op);
			}
			else if(line.equals("request"))
			{
				int resorceType = sc.nextInt() - 1;
				int num = sc.nextInt();
				
				if(inputDebug)
					System.out.println("Request id " + id + " resorce: " + resorceType + " num: " + num);
				
				//Add to the tasks operation list
				operation op = new operation(line, resorceType, num);
				taskList[id].oper.add(op);
			}
			else if(line.equals("release"))
			{
				int resorceType = sc.nextInt() - 1;
				int num = sc.nextInt();
				
				if(inputDebug)
					System.out.println("Release id " + id + " resorce: " + resorceType + " num: " + num);
				
				//Add to the tasks operation list
				operation op = new operation(line, resorceType, num);
				taskList[id].oper.add(op);
			}
			else if(line.equals("terminate"))
			{
				sc.nextLine();
				
				if(inputDebug)
					System.out.println("Terminate " + id);
				
				//Add to the tasks operation list
				operation op = new operation(line);
				taskList[id].oper.add(op);
			}
			else if(line.equals("compute"))
			{
				int num = sc.nextInt();
				sc.next();	//Skip unused number
				
				if(inputDebug)
					System.out.println("compute id " + id + " for " + num);
				
				//Add to the tasks operation list
				operation op = new operation(line, num);
				taskList[id].oper.add(op);
			}
			else
				System.out.println("ERROR - Unknown comand " + line);
		}
		
		if(inputDebug)
		{
			printTasks(taskList, taskNum, false);
			System.out.println("~~~~~~~~~~~~~~~~End input~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		}
		
		//Stop reading
		sc.close();
		
		optimistic(taskList, resorceNum, taskNum, sysResorce);
		
		//Need to reset all of the tasks
		for(int i = 0; i < taskNum; i++)
		{
			taskList[i].reset();
		}
		
		if(Odebug)
		{
			System.out.println("~~~~~~~~~~~~~~~~~End Optimistic~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
		}
			
		banker(taskList, resorceNum, taskNum, sysResorce);
		
		if(Bdebug)
		{
			System.out.println("~~~~~~~~~~~~~~~~~End Banker~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
		}
			
		printEnd(taskList, taskNum);
	}
}
