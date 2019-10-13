import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Linker
{
	public static void main(String[] args) throws IOException
	{	
		//Create input reader
		Scanner sc = new Scanner(System.in);
		File tmpFile = File.createTempFile("test", ".tmp");
	    FileWriter writer = new FileWriter(tmpFile);
	    
        System.out.println("Enter Input Here:");
		
		//Declaring variables
		int numModuals = Integer.parseInt(String.valueOf(sc.next()));	//Number of modules to link
		writer.write(numModuals + " ");
		int numObjRead = 0;		//Number of objects to read on a line
		int[] modOffset = new int[numModuals + 1];		//Array to store the offset of each modal
		modOffset[0] = 0;
		
		//Symbol table variables
		String[] symbolTable = new String[300];		//Names of the symbol
		int[] symbolLoc = new int[301];				//Location of the symbol definition
		symbolLoc[300] = 111;
		int symbolPos = 0;							//The number of symbols stored
		boolean[] mulDef = new boolean[301];		//True if multiply defined
		for(int i = 0; i <= 300; i++){mulDef[i] = false;}
		boolean[] used = new boolean[300];			//True if symbol is used
		for(int i = 0; i < 300; i++){used[i] = false;}
		boolean[] multUsed = new boolean[300];		//True if mult symbols conflict
		
		//External reference variables
		int[] refTable = new int[300];
		
		//First pass through the input for symbols and offsets
		for(int i1 = 0; i1 < numModuals; i1++)
		{
			//Add symbols to symbol array
			numObjRead = Integer.parseInt(String.valueOf(sc.next()));
			writer.write(numObjRead + " ");
			for(int i2 = 0; i2 < numObjRead; i2++)
			{
				String temp = sc.next();
				writer.write(temp + " ");
				int loc = 300;
				//Symbol multiply defined
				for(int i3 = 0; i3 < symbolPos; i3++)
				{
					if(symbolTable[i3].equals(temp))
					{
						mulDef[i3] = true;
						loc = i3;
					}
				}
				
				if(mulDef[loc] == true)
				{
					String temp2 = sc.next();
					symbolLoc[loc] = Integer.parseInt(String.valueOf(temp2))
						+ modOffset[i1];
					writer.write(temp2 + " ");
				}
				else
				{
					symbolTable[symbolPos] = temp;
					String temp2 = sc.next();
					symbolLoc[symbolPos] = Integer.parseInt(String.valueOf(temp2))
						+ modOffset[i1];
					writer.write(temp2 + " ");
					symbolPos++;
				}
			}
			
			//skip external references
			numObjRead = Integer.parseInt(String.valueOf(sc.next()));
			writer.write(numObjRead + " ");
			for(int i2 = 0; i2 < numObjRead; i2++)
			{
				String temp = " ";
				while(!temp.equals("-1"))
				{
					temp = sc.next();
					writer.write(temp + " ");
					temp.replaceAll("\\s+","");
				}
			}
			
			//Add the offsets to the offset array
			numObjRead = Integer.parseInt(String.valueOf(sc.next()));
			writer.write(numObjRead + " ");
			modOffset[i1+1] = numObjRead + modOffset[i1];
			for(int i2 = 0; i2 < numObjRead; i2++)
			{
				writer.write(sc.next() + " ");
			}
		}
		
		//Print the symbol table
		System.out.println("Symbol Table");
		for(int i = 0; i < symbolPos; i++)
		{
			if(symbolLoc[i] >= (modOffset[numModuals]))
			{
				symbolLoc[i] = modOffset[numModuals] - 1;
				System.out.print(symbolTable[i] + "=" + symbolLoc[i] +
						" Error: Definition exceeds module size; last word in module used.");
			}
			else
			{
				System.out.print(symbolTable[i] + "=" + symbolLoc[i]);
			}
			if(mulDef[i] == true)
			{
				System.out.print(" Error: This variable is multiply defined; last value used.");
			}
			System.out.print("\n");
		}
		System.out.println("\nMemory Map");
		
		//Reset the scanner by making a new one
	    writer.close();
		sc.close();
		sc = new Scanner(tmpFile);
		sc.next();
		
		//Second pass to create the memory map
		for(int i1 = 0; i1 < numModuals; i1++)
		{
			//Reset ref and mult use table
			for(int i2 = 0; i2 < 300; i2++)
			{
				refTable[i2] = 999999;
				multUsed[i2] = false;
			}
			
			//Skip symbols
			numObjRead = Integer.parseInt(String.valueOf(sc.next()));
			for(int i2 = 0; i2 < numObjRead; i2++)
			{
				sc.next();
				sc.next();
			}
			
			//Save external references
			numObjRead = Integer.parseInt(String.valueOf(sc.next()));
			for(int i2 = 0; i2 < numObjRead; i2++)
			{
				int loc = 300;
				String temp = sc.next();
				temp.replaceAll("\\s+","");
				
				//Search for the appropriate symbol
				for(int i3 = 0; i3 < symbolPos; i3++)
				{
					if(temp.equals(symbolTable[i3]))
					{
						loc = i3;
						used[loc] = true;
					}
				}
				
				while(!temp.equals("-1"))
				{
					temp = sc.next();
					temp.replaceAll("\\s+","");
					if(!temp.equals("-1"))
					{
						if(refTable[Integer.parseInt(String.valueOf(temp))] != 999999)
						{
							multUsed[Integer.parseInt(String.valueOf(temp))] = true;
						}
						refTable[Integer.parseInt(String.valueOf(temp))] = symbolLoc[loc];
					}
				}
			}
				
			//Complete the memory map
			numObjRead = Integer.parseInt(String.valueOf(sc.next()));
			for(int i2 = 0; i2 < numObjRead; i2++)
			{
				String temp = sc.next();
				//Immediate and absolute are unchanged
				if(temp.substring(4).equals("1") || temp.substring(4).equals("2"))
				{
					if(Integer.parseInt(String.valueOf(temp.substring(1, 4))) >= 300
							&& temp.substring(4).equals("2"))
					{
						if(modOffset[i1] + i2 > 9)
						{
							System.out.println(Integer.toString(modOffset[i1] + i2) + ": "
									+ temp.substring(0, 1) + "299 Error: Absolute address "
											+ "exceeds machine size; largest legal value used.");
						}
						else
						{
							System.out.println(Integer.toString(modOffset[i1] + i2) + ":  "
									+ temp.substring(0, 1) + "299 Error: Absolute address "
											+ "exceeds machine size; largest legal value used.");
						}
					}
					else
					{
						if(modOffset[i1] + i2 > 9)
						{
							System.out.println(Integer.toString(modOffset[i1] + i2) + ": "
									+ temp.substring(0, 4));
						}
						else
						{
							System.out.println(Integer.toString(modOffset[i1] + i2) + ":  "
									+ temp.substring(0, 4));
						}
					}
				}
				
				//Relative is relocated
				if(temp.substring(4).equals("3"))
				{
					if(modOffset[i1] + i2 > 9)
					{
						System.out.println(Integer.toString(modOffset[i1] + i2) + ": "
								+ Integer.toString(Integer.parseInt(String.valueOf(
										temp.substring(0, 4))) + modOffset[i1]));
					}
					else
					{
						System.out.println(Integer.toString(modOffset[i1] + i2) + ":  "
								+ Integer.toString(Integer.parseInt(String.valueOf(
										temp.substring(0, 4))) + modOffset[i1]));
					}
				}
				
				//External is resolved
				if(temp.substring(4).equals("4"))
				{
					if(modOffset[i1] + i2 > 9)
					{
						System.out.print(Integer.toString(modOffset[i1] + i2) + ": ");
					}
					else
					{
						System.out.print(Integer.toString(modOffset[i1] + i2) + ":  ");
					}
					
					if(refTable[i2] < 10)
					{
						System.out.print(temp.substring(0, 1) + "00" + refTable[i2]);
						if(refTable[i2] == 111)
						{
							System.out.print(" Error: X21 is not defined; 111 used.");
						}
						if(multUsed[i2] == true)
						{
							System.out.print(" Error: Multiple variables used in "
									+ "instruction; all but last ignored.");
						}
					}
					if(refTable[i2] >= 10 && refTable[i2] < 100)
					{
						System.out.print(temp.substring(0, 1) + "0" + refTable[i2]);
						if(refTable[i2] == 111)
						{
							System.out.print(" Error: X21 is not defined; 111 used.");
						}
						if(multUsed[i2] == true)
						{
							System.out.print(" Error: Multiple variables used in "
									+ "instruction; all but last ignored.");
						}
					}
					if(refTable[i2] >= 100)
					{
						System.out.print(temp.substring(0, 1) + refTable[i2]);
						if(refTable[i2] == 111)
						{
							System.out.print(" Error: X21 is not defined; 111 used.");
						}
						if(multUsed[i2] == true)
						{
							System.out.print(" Error: Multiple variables used in "
									+ "instruction; all but last ignored.");
						}
					}
					System.out.print("\n");
				}
			}
		}
		
		//Check for unused symbols
		boolean check1 = true;
		for(int i = 0; i < symbolPos; i++)
		{
			if(used[i] == false)
			{
				if(check1 == true)
				{
					System.out.print("\n");
					check1 = false;
				}
				int modual = 999999;
				for(int i2 = 0; i2 <= numModuals; i2++)
				{
					if(symbolLoc[i] >= modOffset[i2])
					{
						modual = i2;
					}
				}
				
				System.out.println("Warning: " + symbolTable[i] + " was defined in module " +
						 modual + " but never used.");
			}
		}
		
		//Close the file reader
		sc.close();
	}
}