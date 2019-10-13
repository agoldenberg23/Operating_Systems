import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Paging
{
	static boolean debug = false;	//Set true for debug output
	
	//Holds information for each possess and is in the process list
	public static class pros
	{
		int id;			//The pros number (should = the location in the pros list)
		int time = 0;	//The time the pros has been running
		int nextReff;	//The next reference this process will make
		int faultNum = 0;	//Number of page faults
		int evictionNum = 0; //The number of evictions
		int residency = 0;	//The residence updated when a page is evicted
		
		//Initialize the pros
		public pros(int id, int processSize)
		{
			this.id = id;
			this.nextReff = (111 * (this.id + 1) + processSize) % processSize;
		}
		
		//Update the residency
		public void resUpdate(int timeEvict, int timeLoaded)
		{
			this.evictionNum++;
			this.residency = this.residency + (timeEvict - timeLoaded);
		}
		
		//String representation
		public String toString()
		{
			return "Pros id " + this.id + "; next reff " + this.nextReff;
		}
		
		//Get the ave residency
		public double getAveRes()
		{
			if(this.evictionNum == 0)
			{
				//Indicate that the aveRes is undefined
				return -1;
			}
			
			return ((double)this.residency) / ((double)this.evictionNum);
		}
		
		//Print a line of the end statement
		public void printEnd()
		{
			double aveRes = this.getAveRes();
			
			System.out.print("Process " + (this.id+1) + " had " + this.faultNum + " faults");
			
			if(aveRes != -1)
			{
				System.out.println(" and " + aveRes + " average residency.");
			}
			else	//Undefined ave residency
			{
				System.out.println(".\n     With no evictions, the average residence is undefined.");
			}	
		}
	}
	
	//Holds information on a page placed in the frame table
	public static class page
	{
		int id;			//The id saying which process owns this
		int pageNum;	//What page this is
		int refStart;	//The first reference in this page
		int refEnd;		//The last reference in this page
		int timeAdd;	//The time this page was added
		int lastUsed;	//The time step this was last used
		
		//Initialize the page
		public page(int pageNum, int id, int pageSize, int time)
		{
			this.pageNum = pageNum;
			this.id = id;
			this.refStart = pageNum * pageSize;
			this.refEnd = this.refStart + pageSize - 1;
			this.timeAdd = time;
		}
		
		//String representation of this page
		public String toString()
		{
			return "Page " + this.pageNum + " owned by " + this.id 
					+ "; start " + this.refStart + ", end " + this.refEnd;
		}
	}
	
	//Prints the end statement
	public static void printEnd(pros[] prosList)
	{
		int totFault = 0;	//Total faults
		int totRes = 0;		//Total residency
		int totEvict = 0;	//Total evictions
		
		for(int i = 0; i < prosList.length; i++)
		{
			totFault = totFault + prosList[i].faultNum;
			totRes = totRes + prosList[i].residency;
			totEvict = totEvict + prosList[i].evictionNum;
			prosList[i].printEnd();
		}
		
		double aveRes = 0;
		if(totEvict != 0)
		{
			aveRes = ((double)totRes) / ((double)totEvict);
		}
		
		System.out.println();
		System.out.print("The total number of faults is " + totFault); 
		
		if(aveRes < 0.00000001)
		{
			System.out.println(".\n     With no evictions, the overall average residence is undefined.");
		}
		else
		{
			System.out.println(" and the overall average residency is " + aveRes + ".");
		}
	}
	
	//Given a random number and the job mix, will give the next reference
	public static int getNextReff(int prev, int randNum, int processSize, int prosNumber, int jobMix)
	{	
		//J = 1; One process with A=1 and B=C=0, the simplest (fully sequential) case.
		//J = 2; Four processes, each with A=1 and B=C=0.
		//(both have A=1 so this would return the same regardless of which process)
		if(jobMix == 1 || jobMix == 2)
		{
			return (prev + 1 + processSize) % processSize;
		}
		
		//J = 3; Four processes, each with A=B=C=0 (fully random references)
		if(jobMix == 3)
		{
			//Return need to get another random number
			return -100;
		}
		
		//J = 4; 
		//One process with  A=.75,  B=.25  and C=0
		//one process with  A=.75,  B=0    and C=.25
		//one process with  A=.75,  B=.125 and C=.125
		//one process with  A=.5,   B=.125 and C=.125
		if(jobMix == 4)
		{
			//Get the operation
			double y = randNum / (Integer.MAX_VALUE + 1d);
			
			if(prosNumber == 0)
			{
				if(y < 0.75)	//A
				{
					return (prev + 1 + processSize) % processSize;
				}
				else	//B
				{
					return (prev - 5 + processSize) % processSize;
				}
			}
			else if(prosNumber == 1)
			{
				if(y < 0.75)	//A
				{
					return (prev + 1 + processSize) % processSize;
				}
				else	//C
				{
					return (prev + 4 + processSize) % processSize;
				}
			}
			else if(prosNumber == 2)
			{
				if(y < 0.75)	//A
				{
					return (prev + 1 + processSize) % processSize;
				}
				else if(y < 0.875)	//B
				{
					return (prev - 5 + processSize) % processSize;
				}
				else //C
				{
					return (prev + 4 + processSize) % processSize;
				}
			}
			else //prosNumber == 3
			{
				if(y < 0.5)	//A
				{
					return (prev + 1 + processSize) % processSize;
				}
				else if(y < 0.625)	//B
				{
					return (prev - 5 + processSize) % processSize;
				}
				else if(y < 0.75)	//C
				{
					return (prev + 4 + processSize) % processSize;
				}
				else	//D
				{
					return -100;
				}
			}
		}
		
		System.out.println("Error - Failed to reconize the job mix");
		return -1;
	}
	
	//Given that we need to make a random reference, find the next reference
	public static int getNextReffRand(int randNum, int processSize)
	{
		return (randNum + processSize) % processSize;
	}
	
	//Returns where the frame is in the frame table or -1 if not there
	public static int findFrame(page[] frameTable, int id, int toFind)
	{
		int length = frameTable.length - 1;
		
		for(int i = length; i >= 0; i--)
		{
			//Hit the start of free frames
			if(frameTable[i] == null)
			{
				return -1;
			}
			
			if(frameTable[i].pageNum == toFind && frameTable[i].id == id)
			{
				//Found the page
				return i;
			}
		}
		
		//Page fault
		return -1;
	}
	
	//The lifo replacement algorithm (will replace the actual)
	public static page[] lifo(page[] frameTable, page toAdd, int time, pros[] prosList)
	{
		if(debug)
		{
			System.out.println("Removing frame " + frameTable[0].toString());
		}
		
		//Update the residency
		prosList[frameTable[0].id].resUpdate(time, frameTable[0].timeAdd);
		
		//0 is the spot which is always changed last
		frameTable[0] = toAdd;
		
		return frameTable;
	}
	
	//The random replacement algorithm (will replace the actual)
	public static page[] randomReplace(page[] frameTable, page toAdd, int randNum, int time, pros[] prosList)
	{
		if(debug)
		{
			System.out.println("Random num for replace = " + randNum);
		}
		
		randNum = (randNum + frameTable.length) % frameTable.length;
		
		if(debug)
		{
			System.out.println("Removing frame " + frameTable[randNum].toString() + " from frame " + randNum);
		}
		
		//Update the residency
		prosList[frameTable[randNum].id].resUpdate(time, frameTable[randNum].timeAdd);
		
		frameTable[randNum] = toAdd;
		
		return frameTable;
	}
	
	//Finds the loc of the last used page (checks all pages)
	public static int lastUsed(page[] frameTable)
	{
		//Put in the first value to start
		int loc = 0;
		int lastUse = frameTable[0].lastUsed;
		
		if(debug)
		{
			System.out.println("Looking for last used:");
			System.out.println("Start with " + frameTable[0].lastUsed);
		}
			
		for(int i = 1; i < frameTable.length; i++)
		{
			if(frameTable[i].lastUsed < lastUse)
			{
				if(debug)
				{
					System.out.println("Found better " + frameTable[i].lastUsed + " in loc " + i);
				}
				
				loc = i;
				lastUse = frameTable[i].lastUsed;
			}
		}
		
		return loc;
	}
	
	//The lru replacement algorithm (will replace the actual)
	public static page[] lru(page[] frameTable, page toAdd, int time, pros[] prosList)
	{
		int lastUsed = lastUsed(frameTable);
		
		if(debug)
		{
			System.out.println("Removing frame " + frameTable[lastUsed].toString() 
					+ " from frame " + lastUsed);
		}
		
		//Update the residency
		prosList[frameTable[lastUsed].id].resUpdate(time, frameTable[lastUsed].timeAdd);
		
		frameTable[lastUsed] = toAdd;
		frameTable[lastUsed].lastUsed = time;
				
		return frameTable;
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		//Need to change to concle input

		//The input parameters
		final int machineSize = Integer.parseInt(String.valueOf(args[0]));	//M, the machine size in words
		final int pageSize = Integer.parseInt(String.valueOf(args[1]));		//P, the page size in words
		final int processSize = Integer.parseInt(String.valueOf(args[2]));	//S, the size of each process
		final int jobMix = Integer.parseInt(String.valueOf(args[3]));		//J, the "job mix" for getNextReff
		final int reffNum = Integer.parseInt(String.valueOf(args[4]));		//N, the number of references each process will make
		final String replace = args[5];										//R, the replacement algorithm, LIFO, RANDOM, or LRU
		
		pros[] prosList;
		//The process list (create and initialize)
		if(jobMix == 1)	//Only one pros in this run
		{
			prosList = new pros[1];
			prosList[0] = new pros(0, processSize);
		}
		else	//4 pros in these runs
		{
			prosList = new pros[4];
			prosList[0] = new pros(0, processSize);
			prosList[1] = new pros(1, processSize);
			prosList[2] = new pros(2, processSize);
			prosList[3] = new pros(3, processSize);
		}
		
		//The frame table
		page[] frameTable = new page[machineSize / pageSize];
		
		//Print starting conditions
		System.out.println("The machine size is " + machineSize + ".");
		System.out.println("The page size is " + pageSize + ".");
		System.out.println("The process size is " + processSize + ".");
		System.out.println("The job mix number is " + jobMix + ".");
		System.out.println("The number of references per process is " + reffNum + ".");
		System.out.println("The replacement algorithm is " + replace + ".\n");
	
		//Debug to check replace
		if(!(replace.equals("lru") || replace.equals("random") || replace.equals("lifo")
				|| replace.equals("LRU") || replace.equals("RANDOM") || replace.equals("LIFO")))
		{
			System.out.println("Error - the replacement algorithm is not reconized");
			return;
		}
		
		if(debug)
		{
			System.out.println("Number of pros = " + prosList.length
					+ " Number of frames = " + frameTable.length + "\n");
		}
	
		//File reader to read random numbers
		File file = new File("Random_Numbers");
		Scanner sc = new Scanner(file);
		int time = 0;
		
		int randNum;	//A random number
		int freeFrameLeft = frameTable.length - 1;	//The next free frame to take
		
		int timeMax;
		if(jobMix > 1)	//4 processes
		{
			timeMax = reffNum*4;
		}
		else	//Only one process
		{
			timeMax = reffNum;
		}
		
		if(debug)
		{
			System.out.println("time max = " + timeMax + "\n");
		}
		
		while(time < timeMax)
		{
			for(int pros = 0; pros < 4 && (jobMix > 1 || pros == 0); pros++)	//Swap between the pros
			{
				for(int quant = 0; quant < 3; quant++)	//Run 3 times per pros
				{
					time++;
					prosList[pros].time++;
					
					if(debug)
					{
						System.out.println("Time = " + time + ", pros = " + pros 
								+ ", pros time = " + prosList[pros].time);
						System.out.println("pros=" + pros + " quant=" + quant);
					}
						
					int nextPage = prosList[pros].nextReff / pageSize;
					int loc = findFrame(frameTable, pros, nextPage);
			
					//Frame not in the page table
					if(loc == -1)
					{
						if(debug)
							System.out.println("Page fault for page " + nextPage);
						
						//Increase the number of faults
						prosList[pros].faultNum++;
						
						//Empty frames left
						if(freeFrameLeft != -1)
						{
							if(debug)
								System.out.println("Useing a free frame at loc " + freeFrameLeft);
					
							frameTable[freeFrameLeft] = new page(nextPage, pros, pageSize, time);
							
							//Update last used
							frameTable[freeFrameLeft].lastUsed = time;
							
							freeFrameLeft--;
						}
						else //No empty frames left
						{
							if(debug)
								System.out.println("No frames left");
							
							if(replace.equals("lifo") || replace.equals("LIFO"))
							{
								page toAdd = new page(nextPage, pros, pageSize, time);
								frameTable = lifo(frameTable, toAdd, time, prosList);
							}
							else if(replace.equals("random") || replace.equals("RANDOM"))
							{
								page toAdd = new page(nextPage, pros, pageSize, time);
								randNum = sc.nextInt();
								frameTable = randomReplace(frameTable, toAdd, randNum, time, prosList);
							}
							else if(replace.equals("lru") || replace.equals("LRU"))
							{
								page toAdd = new page(nextPage, pros, pageSize, time);
								frameTable = lru(frameTable, toAdd, time, prosList);
							}
						}
					}
					else 
					{
						if(debug)
							System.out.println("Page found in frame " + loc);
						
						//Change the time of usage
						frameTable[loc].lastUsed = time;
					}
			
					if(debug)
						System.out.println("1 references word " + prosList[pros].nextReff + " (page " + nextPage + ")");
			
					randNum = sc.nextInt();
			
					if(debug)
						System.out.println("Random number is " + randNum);
			
					prosList[pros].nextReff = getNextReff(prosList[pros].nextReff, randNum, processSize, pros, jobMix);
			
					//Need to get a random reference
					if(prosList[pros].nextReff == -100)
					{
						randNum = sc.nextInt();
						if(debug)
							System.out.println("Next random number (for rand reff) = " + randNum); 
					
						prosList[pros].nextReff = getNextReffRand(randNum, processSize);
					}
			
					if(debug)
						System.out.println("Next ref is " + prosList[pros].nextReff + "\n");
				
					//The process quantum ended early
					if(prosList[pros].time >= reffNum)
					{
						break;
					}
				
				}
			}
		}
		
		//Stop reading in random numbers
		sc.close();
		
		//Print the end statement
		printEnd(prosList);
	}
}