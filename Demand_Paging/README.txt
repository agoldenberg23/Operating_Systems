This is a java file and can be ran with the following commands:
javac Paging.java
java Paging <Machine Size> <Page Size> <Size of Each Process> <Job Mix> <Number of References for Each Process> <Replacement Algorithm>
(The <> are included to denote a command line argument)

The command line arguments are integers, with the exception of the replacement algorithm, which is a string. Sizes are in words. The 
replacement algorithm must be all lowercase or all uppercase characters. Valid options are listed below.

lru
Chooses to evict the last recently used page.

random
Chooses to evict a randomly chosen page.

lifo
Chooses to evict the last page that entered the frame table.

The job mix can be any of the following:
Job Mix = 1; One process with fully sequential references
Job Mix = 2; 4 processes with fully sequential references
Job Mix = 3; 4 processes with fully random references
Job Mix = 4; 4 processes with the following aspects:
	One process with  Sequential = 0.75,  Back 5 references = 0.25,  Forward 4 references = 0     and Random = 0
	one process with  Sequential = 0.75,  Back 5 references = 0,     Forward 4 references = 0.25  and Random = 0
	one process with  Sequential = 0.75,  Back 5 references = 0.125, Forward 4 references = 0.125 and Random = 0
	one process with  Sequential = 0.50,  Back 5 references = 0.125, Forward 4 references = 0.125 and Random = 0.25
	
This requires a random numbers file named Random_Numbers, which is included. This should be placed in the same 
relative directory as the program.
