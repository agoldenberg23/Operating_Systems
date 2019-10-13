import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

public class Scheduler
{
	//Node is an object to hold a process and all of its info
	public static class node
	{
		//Below variables should not be changed b/c used between runs
		int id;						//Process number for id
		final int CPUTime;			//(C) The CPU time needed
		final int maxRunTime;		//(B) The maximum CPU burst
		final int IOMult;			//(M) The multiplier determining the IO burst
		final int arivalTime;		//(A) When the process starts
		//Run specific variables, used during run
		int time;			//Time left till completion, time = 0 means finished
		int runTime = 0;	//The time left in a CPU burst
		int IOTime = 0;		//The time left in a IO burst
		int pre = 0;		//The time left until preempt
		double r = 1;		//The r value for HPRN
		boolean run = false;
		boolean preemp = false;
		//The variables used at the end of the algorithm
		int finishTime = 0;	//The time a node has finished at
		int readyTime = 0;	//Total amount of time in ready
		
		//Create a node and set its values
		public node(int arivalTime, int maxRunTime, int CPUTime, int IOMult)
		{
			this.arivalTime = arivalTime;
			this.maxRunTime = maxRunTime;
			this.CPUTime = CPUTime;
			this.time = CPUTime;
			this.IOMult = IOMult;
		}
		
		//Resets the r value and returns it
		public double rSet(int time)
		{
			if(this.CPUTime - this.time == 0)
			{
				this.r = ((double)(time - this.arivalTime));
			}
			else
			{
				this.r = ((double)(time - this.arivalTime)) 
						/ ((double)this.CPUTime - this.time);
			}
			return this.r;
		}
		
		//Return the IO time waited
		public int IOTimeWait()
		{
			return (this.finishTime - this.arivalTime 
			- this.readyTime - this.CPUTime);
		}
		
		//Return a sting of the final variables
		public String toString()
		{
			return "id = " + id + " CPUTime = " + CPUTime + " maxRunTime = " + maxRunTime + 
					" IOMult = " + IOMult + " arivalTime = " + arivalTime + " Time = " + time + "\n";
		}
		
		//ABCM output
		public String ABCM()
		{
			return "(" + arivalTime + " " + maxRunTime + " " 
					+ CPUTime + " " + IOMult + ")";
		}
		
		//Return the status of the node
		//q is the quantum for RR. 0 Will discount this
		public String status(int q)
		{
			//Error state
			if(this.runTime > 0 && this.IOTime == 0 && this.time != 0)
			{
				return " ERROR - Running and Blocked ";
			}
			//The process has finished
			if(this.time == 0)
			{
				return " terminated  0";
			}
			//The process has not ran any time
			else if(this.CPUTime == this.time && this.readyTime == 0 && this.run == false)
			{
				return "  unstarted  0";
			}
			//The process is not in IO or running
			else if((this.runTime == 0 && this.IOTime == 0) || this.preemp == true)
			{
				return "      ready  0";
			}
			//The process is running but over the q value
			else if(this.run && q > 0)
			{
				if((q) < 10)
				{
					return "    running  " + (q);
				}
				if((q) < 100)
				{
					return "    running " + (q);
				}
				return "    running" + (q);
			}
			//The process is running
			else if(this.run)
			{
				if(this.runTime + 1 < 10)
				{
					return "    running  " + (runTime);
				}
				if(this.runTime < 100)
				{
					return "    running " + (runTime);
				}
				return "    running" + (runTime);
			}
			//The process is in IO
			else if(this.IOTime > 0)
			{
				if(this.IOTime < 10)
				{
					return "    blocked  " + IOTime;
				}
				if(this.IOTime < 100)
				{
					return "    blocked " + IOTime;
				}
				return "    blocked" + IOTime;
			}
			return " ERROR - Unknown status ";
		}
		
		//The process end output
		public void endStatus()
		{
			System.out.println("Process " + this.id + ":");
			System.out.println("	(A,B,C,M) = " + this.ABCM());
			System.out.println("	Finishing time: " + this.finishTime);
			System.out.println("	Turnaround time: " + (this.finishTime - this.arivalTime));
			System.out.println("	I/O time: " + this.IOTimeWait());
			System.out.println("	Waiting time: " + this.readyTime + "\n");
		}
	}
	
	//Print the end status
	public static void endStatus(node[] pros, int prosNum, int time, int IOTime)
	{
		double CPUUtil = 0;
		double aveTurn = 0;
		double aveWait = 0;
		for(int i = 0; i < prosNum; i++)
		{
			pros[i].endStatus();
			
			CPUUtil = CPUUtil + ((double)pros[i].CPUTime);
			
			aveTurn = (((double)pros[i].finishTime - pros[i].arivalTime)) + aveTurn;
			
			aveWait = pros[i].readyTime + aveWait;
		}
		
		CPUUtil = CPUUtil / time;
		aveTurn = aveTurn / ((double)prosNum);
		aveWait = aveWait / ((double)prosNum);
		
		System.out.println("Summary Data:");
		System.out.println("	Finishing time: " + time);
		System.out.println("	CPU Utilization: " + round(CPUUtil, 6));
		System.out.println("	I/O Utilization: " + round((((double)IOTime) / ((double)time)), 6));
		System.out.println("	Throughput: " + round((((double)prosNum) / ((double)time)) * 100, 6)
				+ " processes per hundred cycles");
		System.out.println("	Average turnaround time: " + round(aveTurn, 6));
		System.out.println("	Average waiting time: " + round(aveWait, 6));
	}
	
	//Prints the status lines
	public static void statusLine(node[] pros, int prosNum, int time)
	{
		if(time < 10)
		{
			System.out.print("Before cycle    " + time + ": ");
		}
		else if(time < 100)
		{
			System.out.print("Before cycle   " + time + ": ");
		}
		else if(time < 1000)
		{
			System.out.print("Before cycle  " + time + ": ");
		}
		else
		{
			System.out.print("Before cycle " + time + ": ");
		}
		
		for(int i = 0; i < prosNum; i++)
		{
			System.out.print(pros[i].status(0));
		}
		System.out.println(".");
	}
	
	//Prints the status lines
	public static void RRstatusLine(node[] pros, int prosNum, int time, int q)
	{
		if(time < 10)
		{
			System.out.print("Before cycle    " + time + ": ");
		}
		else if(time < 100)
		{
			System.out.print("Before cycle   " + time + ": ");
		}
		else if(time < 1000)
		{
			System.out.print("Before cycle  " + time + ": ");
		}
		else
		{
			System.out.print("Before cycle " + time + ": ");
		}
		
		int quant = 0;
		
		for(int i = 0; i < prosNum; i++)
		{
			quant = 0;
			if(pros[i].pre == 1 || pros[i].runTime == 1)
			{
				quant = 1;
			}
			else if(pros[i].pre == 0)
			{
				quant = 2;
			}
			System.out.print(pros[i].status(quant));
		}
		System.out.println(".");
	}
	
	//Resets the nodes for the next run
	public static void reset(node[] pros, int prosNum)
	{
		for(int i = 0; i < prosNum; i++)
		{
			//All non final values set to default
			pros[i].IOTime = 0;
			pros[i].runTime = 0;
			pros[i].time = pros[i].CPUTime;
			pros[i].finishTime = 0;
			pros[i].readyTime = 0;
		}
	}
	
	//Creates a random number from the next number from file: maximum U, minimum 1
	public static int random(int random, int U)
	{
		return 1 + (random % U);
	}
	
	//Rounds a double
	public static double round(double value, int places)
	{
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	//The first come first served algorithm
	public static void FCFS(node[] pros, int prosNum, boolean debug, boolean Hdebug) throws FileNotFoundException
	{
		//Create the random number generator (read numbers in order)
				File randFile = new File("Random_Numbers");
				Scanner rand = new Scanner(randFile);
				
				int time = 0;			//The time the program has been running
				int IOTime = 0;			//The time in which IO is being used
				Queue<node> active = new LinkedList<>();	//The pros with active use
				List<node> inactive = new ArrayList<>();	//The pros not started
				List<node> block = new ArrayList<>();		//All blocked pros
				List<node> tie = new ArrayList<node>();		//Handles ties
				Iterator<node> iter;	//Iterator to go through lists
				node use;	//node to hold a node when transferring
				node running = null; //the active node
				
				if(debug == true)
				{
					System.out.println("This detailed printout gives the state "
							+ "and remaining burst for each process\n");
				}
				
				//All pros start as inactive
				for(int i = 0; i < prosNum; i++)
				{
					inactive.add(pros[i]);
				}
				
				//Run the scheduling algorithm
				while(active.isEmpty() != true || inactive.isEmpty() != true 
						|| block.isEmpty() != true || running != null)
				{	
					if(debug == true)
					{
						statusLine(pros, prosNum, time);
					}
					
					//Increment IOTime
					if(block.isEmpty() == false)
					{
						IOTime = IOTime + 1;
					}
					
					//Go through blocked nodes
					iter = block.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						use.IOTime = use.IOTime - 1;
						
						//Finished blocking
						if(use.IOTime == 0)
						{
							if(Hdebug == true)
							{
								System.out.println("Stoped blocking " + use.id);
							}
							tie.add(use);
						}
						//Needs to continue blocking
						else if(Hdebug == true)
						{
							System.out.println("Continue blocking " + use.id);
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id < use.id)
							{
								if(Hdebug == true)
								{
									System.out.println("Tie beaker used, " 
											+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						block.remove(use);
						tie.remove(use);
						active.add(use);
					}
						
					//Add all active elements from the inactive
					iter = inactive.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						//Set the node to active
						if(use.arivalTime <= time)
						{
							if(Hdebug == true)
							{
								System.out.println("Adding " + use.id + " to active");
							}
							tie.add(use);
						}
						//Keep the node in inactive
						else if(Hdebug)
						{
							System.out.println("Keeping " + use.id + " inactive");
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id < use.id)
							{
								if(Hdebug)
								{
									System.out.println("Tie beaker used, " 
										+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						inactive.remove(use);
						tie.remove(use);
						active.add(use);
					}
					
					//No active node, trying to get one
					if(running == null)
					{
						running = active.poll();
						
						if(running != null)
						{
							if(Hdebug)
							{
								System.out.println("Was not running, now is " + running.id);
							}
							running.runTime = random(rand.nextInt(), running.maxRunTime);
							running.IOTime = running.runTime * running.IOMult;
							running.run = true;
						}
						else if(Hdebug)
						{
							System.out.println("Was not running, still nothing to run");
						}
					}
					//Active node finished running, switching to a different node
					else if(running.runTime <= 1 || running.time <= 1)	//1 = finish this cycle
					{	
						//Complete cycle of running
						if((running.runTime == 1 && running.time != 0) || running.time == 1)
						{
							running.time = running.time - 1;
						}
						running.runTime = 0;
						running.run = false;
						
						if(Hdebug)
						{
							System.out.println("Pros " + running.id + " has finished, time left = " + running.time);
						}
						
						//Terminate if finished, block if not
						if(running.time == 0)
						{
							if(Hdebug)
							{
								System.out.println("Terminating process");
							}
							running.finishTime = time;
						}
						else
						{
							if(Hdebug)
							{
								System.out.println("Adding to blocked list");
							}
							
							if(running.IOTime == 0)
							{
								System.out.println("Error - not IO time after running & no termination");
							}
							block.add(running);
						}
						
						//Next running pros
						running = active.poll();
						
						if(running != null)
						{
							if(Hdebug)
							{
								System.out.println("Now running " + running.id);
							}
							running.run = true;
							running.runTime = random(rand.nextInt(), running.maxRunTime);
							running.IOTime = running.runTime * running.IOMult;
						}
					}
					//The running CPU burst has not ended
					else if(running.runTime > 1)
					{
						if(Hdebug)
						{
							System.out.println("Continueing to run " + running.id);
						}
						running.runTime = running.runTime - 1;
						running.time = running.time - 1;
					}
					
					//Go through active nodes and change ready time
					int size = active.size();
					for(int i = 0; i < size && active.isEmpty() == false; i++)
					{
						use = active.poll();
						//Use is ready but not being run
						if(use.runTime == 0 && use.IOTime == 0)
						{
							if(Hdebug)
							{
								System.out.println("Ready " + use.id);
							}
							use.readyTime = use.readyTime + 1;
						}
						
						//Put element back in
						active.add(use);
					}
					
					time++;
				}
				
				time--;
				rand.close();
				
				System.out.println("The scheduling algorithm used was First Come First Served\n");
				endStatus(pros, prosNum, time, IOTime);
				System.out.println("\n");
	}
	
	//Round robin algorithm
	public static void RR(node[] pros, int prosNum, boolean debug, int q, boolean Hdebug) throws FileNotFoundException
	{
		//Create the random number generator (read numbers in order)
				File randFile = new File("Random_Numbers");
				Scanner rand = new Scanner(randFile);
				
				int time = 0;			//The time the program has been running
				int IOTime = 0;			//The time spent with some IO
				Queue<node> active = new LinkedList<>();	//The pros with active use
				List<node> inactive = new ArrayList<>();	//The pros not started
				List<node> block = new ArrayList<>();		//All blocked pros
				List<node> tie = new ArrayList<node>();		//Handles ties
				Iterator<node> iter;	//Iterator to go through lists
				node use;	//node to hold a node when transferring
				node running = null; //the active node
				
				if(debug == true)
				{
					System.out.println("This detailed printout gives the state "
							+ "and remaining burst for each process\n");
				}
				
				//All pros start as inactive
				for(int i = 0; i < prosNum; i++)
				{
					inactive.add(pros[i]);
				}
				
				//Run the scheduling algorithm
				while(active.isEmpty() != true || inactive.isEmpty() != true 
						|| block.isEmpty() != true || running != null)
				{	
					if(debug == true)
					{
						RRstatusLine(pros, prosNum, time, q);
					}
					
					//Check for preempt
					if(running != null)
					{
						if(running.runTime > 1 && running.time > 1 && running.pre >= q-1)
						{
							//Complete cycle of running
							running.time = running.time - 1;
							running.pre = running.pre + 1;
							running.runTime = running.runTime - 1;
							running.run = false;
							
							if(Hdebug)
							{
								System.out.println("Prempting pros, adding to tie list");
							}
							running.preemp = true;
							tie.add(running);
							
							running = null;
						}
					}
					
					//Increment IOTime
					if(block.isEmpty() == false)
					{
						IOTime = IOTime + 1;
					}
					
					//Go through blocked nodes
					iter = block.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						use.IOTime = use.IOTime - 1;
						
						//Finished blocking
						if(use.IOTime == 0)
						{
							if(Hdebug)
							{
								System.out.println("Stoped blocking " + use.id);
							}
							tie.add(use);
						}
						//Needs to continue blocking
						else if(Hdebug)
						{
							System.out.println("Continue blocking " + use.id);
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id < use.id)
							{
								if(Hdebug)
								{
									System.out.println("Tie beaker used, " 
										+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						block.remove(use);
						tie.remove(use);
						active.add(use);
					}
					
					//Add all active elements from the inactive
					iter = inactive.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						//Set the node to active
						if(use.arivalTime <= time)
						{
							if(Hdebug)
							{
								System.out.println("Adding " + use.id + " to active");
							}
							tie.add(use);
						}
						//Keep the node in inactive
						else if(Hdebug)
						{
							System.out.println("Keeping " + use.id + " inactive");
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id < use.id)
							{
								if(Hdebug)
								{
									System.out.println("Tie beaker used, " 
										+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						inactive.remove(use);
						tie.remove(use);
						active.add(use);
					}
					
					//No active node, trying to get one
					if(running == null)
					{
						running = active.poll();
						
						if(running != null)
						{
							if(running.runTime != 0)
							{
								if(Hdebug)
								{
									System.out.println("Now running after preempt " + running.id);
								}
							}
							else
							{
								if(Hdebug)
								{
									System.out.println("Was not running, now is " + running.id);
								}
								running.runTime = random(rand.nextInt(), running.maxRunTime);
								running.IOTime = running.runTime * running.IOMult;
							}
							running.pre = 0;
							running.preemp = false;
							running.run = true;
						}
						else if(Hdebug)
						{
							System.out.println("Was not running, still nothing to run");
						}
					}
					//Active node finished running, switching to a different node
					else if(running.runTime <= 1 || running.time <= 1)
					{	
						//Complete cycle of running
						if((running.runTime == 1 && running.time != 0) || running.time == 1)
						{
							running.time = running.time - 1;
							running.pre = running.pre + 1;
							running.runTime = running.runTime - 1;
						}
						running.run = false;
						
						if(Hdebug)
						{
							System.out.println("Pros " + running.id + " has finished, time left = " + running.time
								+ " preempt time = " + running.pre);
						}
							
						//Terminate if finished, block if not
						if(running.time == 0)
						{
							if(Hdebug)
							{
								System.out.println("Terminating process");
							}
							running.finishTime = time;
						}
						else if(running.runTime == 0)
						{
							if(Hdebug)
							{
								System.out.println("Adding to blocked list");
							}
							
							if(running.IOTime == 0)
							{
								System.out.println("Error - not IO time after "
										+ "running & no termination or pre");
							}
							if(running.pre >= q && Hdebug)
							{
								System.out.println("Would have preempted but run time = " + running.runTime);
							}
							block.add(running);
						}
						
						//Next running pros
						running = active.poll();
						
						if(running != null)
						{
							if(running.runTime != 0)
							{
								if(Hdebug)
								{
									System.out.println("Now running after preempt " + running.id);
								}
							}
							else
							{
								if(Hdebug)
								{
									System.out.println("Was not running, now is " + running.id);
								}
								running.runTime = random(rand.nextInt(), running.maxRunTime);
								running.IOTime = running.runTime * running.IOMult;
							}
							running.pre = 0;
							running.preemp = false;
							running.run = true;
						}
					}
					//The running CPU burst has not ended
					else if(running.runTime > 1)
					{
						if(Hdebug)
						{
							System.out.println("Continueing to run " + running.id);
						}
						running.runTime = running.runTime - 1;
						running.time = running.time - 1;
						running.pre = running.pre + 1;
					}
					
					//Go through active nodes and change ready time
					int size = active.size();
					for(int i = 0; i < size && active.isEmpty() == false; i++)
					{
						use = active.poll();
						//Use is ready but not being run
						if(use.IOTime == 0 || use.preemp == true)
						{
							if(Hdebug)
							{
								System.out.println("Ready " + use.id);
							}
							use.readyTime = use.readyTime + 1;
						}
						
						//Put element back in
						active.add(use);
					}
					
					time++;
				}
				
				time--;
				rand.close();
				
				System.out.println("The scheduling algorithm used was Round Robbin\n");
				endStatus(pros, prosNum, time, IOTime);
				System.out.println("\n");
	}

	//The last come first served algorithm
	public static void LCFS(node[] pros, int prosNum, boolean debug, boolean Hdebug) throws FileNotFoundException
	{
		//Create the random number generator (read numbers in order)
				File randFile = new File("Random_Numbers");
				Scanner rand = new Scanner(randFile);
				
				int time = 0;			//The time the program has been running
				int IOTime = 0;			//The time spent with IO
				Stack<node> active = new Stack<node>();	//The pros with active use
				List<node> inactive = new ArrayList<>();	//The pros not started
				List<node> block = new ArrayList<>();		//All blocked pros
				List<node> tie = new ArrayList<node>();		//Handles ties
				Iterator<node> iter;	//Iterator to go through lists
				node use;	//node to hold a node when transferring
				node running = null; //the active node
				
				if(debug == true)
				{
					System.out.println("This detailed printout gives the state "
							+ "and remaining burst for each process\n");
				}
				
				//All pros start as inactive
				for(int i = 0; i < prosNum; i++)
				{
					inactive.add(pros[i]);
				}
				
				//Run the scheduling algorithm
				while(active.isEmpty() != true || inactive.isEmpty() != true 
						|| block.isEmpty() != true || running != null)
				{	
					if(debug == true)
					{
						statusLine(pros, prosNum, time);
					}
						
					//Add all active elements from the inactive
					iter = inactive.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						//Set the node to active
						if(use.arivalTime <= time)
						{
							if(Hdebug)
							{
								System.out.println("Adding " + use.id + " to active");
							}
							tie.add(use);
						}
						//Keep the node in inactive
						else if(Hdebug)
						{
							System.out.println("Keeping " + use.id + " inactive");
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id > use.id)
							{
								if(Hdebug)
								{
									System.out.println("Tie beaker used, " 
										+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						inactive.remove(use);
						tie.remove(use);
						active.push(use);
					}
					
					//Increment IOTime
					if(block.isEmpty() == false)
					{
						IOTime = IOTime + 1;
					}
					
					//Go through blocked nodes
					iter = block.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						use.IOTime = use.IOTime - 1;
						
						//Finished blocking
						if(use.IOTime == 0)
						{
							if(Hdebug)
							{
								System.out.println("Stoped blocking " + use.id);
							}
							tie.add(use);
						}
						//Needs to continue blocking
						else if(Hdebug)
						{
							System.out.println("Continue blocking " + use.id);
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id > use.id)
							{
								if(Hdebug)
								{
									System.out.println("Tie beaker used, " 
										+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						block.remove(use);
						tie.remove(use);
						active.push(use);
					}
					
					//No active node, trying to get one
					if(running == null)
					{	
						if(active.isEmpty() == true)
						{
							running = null;
						}
						else
						{
							running = active.pop();
						}
						
						if(running != null)
						{
							if(Hdebug)
							{
								System.out.println("Was not running, now is " + running.id);
							}
							running.runTime = random(rand.nextInt(), running.maxRunTime);
							running.IOTime = running.runTime * running.IOMult;
							running.run = true;
						}
						else if(Hdebug)
						{
							System.out.println("Was not running, still nothing to run");
						}
					}
					//Active node finished running, switching to a different node
					else if(running.runTime <= 1 || running.time <= 1)	//1 = finish this cycle
					{	
						//Complete cycle of running
						if((running.runTime == 1 && running.time != 0) || running.time == 1)
						{
							running.time = running.time - 1;
						}
						running.runTime = 0;
						running.run = false;
						
						if(Hdebug)
						{
							System.out.println("Pros " + running.id + " has finished, time left = " + running.time);
						}
						
						//Terminate if finished, block if not
						if(running.time == 0)
						{
							if(Hdebug)
							{
								System.out.println("Terminating process");
							}
							running.finishTime = time;
						}
						else
						{
							if(Hdebug)
							{
								System.out.println("Adding to blocked list");
							}
							
							if(running.IOTime == 0)
							{
								System.out.println("Error - not IO time after running & no termination");
							}
							block.add(running);
						}
						
						//Next running pros
						if(active.isEmpty() == true)
						{
							running = null;
						}
						else
						{
							running = active.pop();
						}
						
						if(running != null)
						{
							if(Hdebug)
							{
								System.out.println("Now running " + running.id);
							}
							running.run = true;
							running.runTime = random(rand.nextInt(), running.maxRunTime);
							running.IOTime = running.runTime * running.IOMult;
						}
					}
					//The running CPU burst has not ended
					else if(running.runTime > 1)
					{
						if(Hdebug)
						{
							System.out.println("Continueing to run " + running.id);
						}
						running.runTime = running.runTime - 1;
						running.time = running.time - 1;
					}
					
					//Go through active nodes and change ready time
					iter = active.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						
						//Use is ready but not being run
						if(use.runTime == 0 && use.IOTime == 0)
						{
							if(Hdebug)
							{
								System.out.println("Ready " + use.id);
							}
							use.readyTime = use.readyTime + 1;
						}
					}
					
					time++;
				}
				
				time--;
				rand.close();
				
				System.out.println("The scheduling algorithm used was Last Come First Served\n");
				endStatus(pros, prosNum, time, IOTime);
				System.out.println("\n");
	}
	
	//Highest penalty ratio 
	public static void HPRN(node[] pros, int prosNum, boolean debug, boolean Hdebug) throws FileNotFoundException
	{
		//Create the random number generator (read numbers in order)
				File randFile = new File("Random_Numbers");
				Scanner rand = new Scanner(randFile);
				
				int time = 0;			//The time the program has been running
				int IOTime = 0;			//The time spent with IO
				List<node> active = new ArrayList<>();	//The pros with active use
				List<node> inactive = new ArrayList<>();	//The pros not started
				List<node> block = new ArrayList<>();		//All blocked pros
				List<node> tie = new ArrayList<node>();		//Handles ties
				Iterator<node> iter;	//Iterator to go through lists
				node use;	//node to hold a node when transferring
				node running = null; //the active node
				
				if(debug == true)
				{
					System.out.println("This detailed printout gives the state "
							+ "and remaining burst for each process\n");
				}
				
				//All pros start as inactive
				for(int i = 0; i < prosNum; i++)
				{
					inactive.add(pros[i]);
				}
				
				//Run the scheduling algorithm
				while(active.isEmpty() != true || inactive.isEmpty() != true 
						|| block.isEmpty() != true || running != null)
				{	
					if(debug == true)
					{
						statusLine(pros, prosNum, time);
					}
					
					//Increment IOTime
					if(block.isEmpty() == false)
					{
						IOTime = IOTime + 1;
					}
					
					//Go through blocked nodes
					iter = block.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						use.IOTime = use.IOTime - 1;
						
						//Finished blocking
						if(use.IOTime == 0)
						{
							if(Hdebug)
							{
								System.out.println("Stoped blocking " + use.id);
							}
							tie.add(use);
						}
						//Needs to continue blocking
						else if(Hdebug)
						{
							System.out.println("Continue blocking " + use.id);
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id < use.id)
							{
								if(Hdebug)
								{
									System.out.println("Tie beaker used, " 
										+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						block.remove(use);
						tie.remove(use);
						active.add(use);
					}
						
					//Add all active elements from the inactive
					iter = inactive.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						//Set the node to active
						if(use.arivalTime <= time)
						{
							if(Hdebug)
							{
								System.out.println("Adding " + use.id + " to active");
							}
							tie.add(use);
						}
						//Keep the node in inactive
						else if(Hdebug)
						{
							System.out.println("Keeping " + use.id + " inactive");
						}
					}
					//Go through tie to add/remove in order
					while(tie.isEmpty() == false)
					{
						node temp = null;
						use = tie.get(0);
						
						//Check for tie breaker case
						for(int c = 1; c < tie.size(); c++)
						{
							temp = tie.get(c);
							if(temp.id < use.id)
							{
								if(Hdebug)
								{
									System.out.println("Tie beaker used, " 
										+ temp.id + " < " + use.id);
								}
								use = temp;
							}
						}
						
						inactive.remove(use);
						tie.remove(use);
						active.add(use);
					}
					
					//No active node, trying to get one
					if(running == null)
					{
						//Next running pros
						if(active.isEmpty() == false)
						{
							iter = active.listIterator();
							running = iter.next();
							while(iter.hasNext())
							{
								use = iter.next();
								
								//Found a pros with a better priority
								if(running.rSet(time) < use.rSet(time))
								{
									if(Hdebug)
									{
										System.out.println("Priority swap: " + running.id + " to " + use.id);
										System.out.println("r pre: " + running.r + " r next: " + use.r);
									}
									running = use;
								}
								//Tie found
								else if(running.rSet(time) == use.rSet(time))
								{
									if(Hdebug)
									{
										System.out.println("Tie found in r values");
									}
									if(running.id > use.id)
									{
										running = use;
									}
								}
							}
						}
						else
						{
							running = null;
						}
						
						if(running != null)
						{
							if(Hdebug)
							{
								System.out.println("Was not running, now is " + running.id);
							}
							running.runTime = random(rand.nextInt(), running.maxRunTime);
							running.IOTime = running.runTime * running.IOMult;
							running.run = true;
						}
						else if(Hdebug)
						{
							System.out.println("Was not running, still nothing to run");
						}
					}
					//Active node finished running, switching to a different node
					else if(running.runTime <= 1 || running.time <= 1)	//1 = finish this cycle
					{	
						//Complete cycle of running
						if((running.runTime == 1 && running.time != 0) || running.time == 1)
						{
							running.time = running.time - 1;
						}
						running.runTime = 0;
						running.run = false;
						
						if(Hdebug)
						{
							System.out.println("Pros " + running.id + " has finished, time left = " + running.time);
						}
						
						//Terminate if finished, block if not
						if(running.time == 0)
						{
							if(Hdebug)
							{
								System.out.println("Terminating process");
							}
							running.finishTime = time;
							active.remove(running);
						}
						else
						{
							if(Hdebug)
							{
								System.out.println("Adding to blocked list");
							}
							
							if(running.IOTime == 0)
							{
								System.out.println("Error - not IO time after running & no termination");
							}
							block.add(running);
							active.remove(running);
						}
						
						//Next running pros
						if(active.isEmpty() == false)
						{
							iter = active.listIterator();
							running = iter.next();
							if(Hdebug)
							{
								System.out.println(running.id + " has r = " + running.rSet(time));
							}
							while(iter.hasNext())
							{
								use = iter.next();
								if(Hdebug)
								{
									System.out.println(use.id + " has r = " + use.rSet(time));
								}
								
								//Found a pros with a better priority
								if(running.rSet(time) < use.rSet(time))
								{
									if(Hdebug)
									{
										System.out.println("Priority swap: " + running.id + " to " + use.id);
										System.out.println("r pre: " + running.r + " r next: " + use.r);
									}
									running = use;
								}
								//Tie found
								else if(running.rSet(time) == use.rSet(time))
								{
									if(Hdebug)
									{
										System.out.println("Tie found in r values");
									}
									if(running.id > use.id)
									{
										running = use;
									}
								}
							}
						}
						else
						{
							running = null;
						}
						
						if(running != null)
						{
							if(Hdebug)
							{
								System.out.println("Now running " + running.id + " r = " + running.r);
							}
							running.run = true;
							running.runTime = random(rand.nextInt(), running.maxRunTime);
							running.IOTime = running.runTime * running.IOMult;
						}
					}
					//The running CPU burst has not ended
					else if(running.runTime > 1)
					{
						if(Hdebug)
						{
							System.out.println("Continueing to run " + running.id);
						}
						running.runTime = running.runTime - 1;
						running.time = running.time - 1;
					}
					
					//Go through active nodes and change ready time
					iter = active.listIterator();
					while(iter.hasNext())
					{
						use = iter.next();
						//Use is ready but not being run
						if(use.runTime == 0 && use.IOTime == 0)
						{
							if(Hdebug)
							{
								System.out.println("Ready " + use.id);
							}
							use.readyTime = use.readyTime + 1;
						}
					}
					
					time++;
				}
				
				time--;
				rand.close();
				
				System.out.println("The scheduling algorithm used was Highest Penalty Ratio Next\n");
				endStatus(pros, prosNum, time, IOTime);
	}
	
	//Slow sort for the input before being ran
	public static void inputSort(node[] pros, int prosNum)
	{
		for(int i = 0; i < prosNum-1; i++)
		{
			for(int j = 0; j < prosNum - i - 1; j++)
			{
				if(pros[j].arivalTime > pros[j+1].arivalTime)
				{
					node temp = pros[j];
					pros[j] = pros[j+1];
					pros[j+1] = temp;
				}
			}
		}
		
		//Give the id values
		for(int i = 0; i < prosNum; i++)
		{
			pros[i].id = i;
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		String fileName = "Processes";
		boolean debug = false;
		boolean Hdebug = false;
		
		if(args[0].equals("--verbose"))
		{
			debug = true;
			fileName = args[1];
		}
		else if(args[0].equals("--Hverbose"))
		{
			Hdebug = true;
			debug = true;
			fileName = args[1];
		}
		else
		{
			fileName = args[0];
		}
		
		//Reading in the file and filling the knapsack variables
		//Use a file to input the knapsack values
		File file = new File(fileName);
		Scanner sc = new Scanner(file);

		int prosNum = sc.nextInt();
	
		node[] pros = new node[prosNum];	//An array of processes
		
		for(int i = 0; i < prosNum; i++)
		{
			int a = Integer.parseInt(String.valueOf(sc.next().substring(1)));
			int b = sc.nextInt();
			int c = sc.nextInt();
			String temp = sc.next();
			int d = Integer.parseInt(String.valueOf(temp.substring(0, temp.length()-1)));
			pros[i] = new node(a, b, c, d);
		}
		
		//Stop reading in processes
		sc.close();
		
		System.out.print("The original input was: " + prosNum + " ");
		for(int i = 0; i < prosNum; i++)
		{
			System.out.print(pros[i].ABCM() + " ");
		}
		System.out.println();
			
		System.out.print("The (sorted) input is:  " + prosNum + " ");
		inputSort(pros, prosNum);
		for(int i = 0; i < prosNum; i++)
		{
			System.out.print(pros[i].ABCM() + " ");
		}
		System.out.println("\n");
		
		FCFS(pros, prosNum, debug, Hdebug);
		reset(pros, prosNum);
		
		RR(pros, prosNum, debug, 2, Hdebug);
		reset(pros, prosNum);
		
		LCFS(pros, prosNum, debug, Hdebug);
		reset(pros, prosNum);
		
		HPRN(pros, prosNum, debug, Hdebug);
	}
}