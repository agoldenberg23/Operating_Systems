This program simulates a first in first out algorithm and a banker’s 
algorithm on a set of mock programs in order to see positive and 
negative aspects of both algorithms.


This is a java file and can be ran with the following commands:
javac Banker.java
java Banker <Input File Name>

This takes an input by a text file specified by the command line.
This outputs, by standard output, the time of running, time spent waiting 
and the % of time spent waiting for all processes and the total run.
This is done for both the FIFO and the Bankers algorithms.

Input format:
#Tasks #ResorceTypes #UnitsInResorce1 #UnitsInResorce2 <...>
<any of 5 commands listed below.>
initiate task-number resource-type initial-claim
request task-number resource-type number-requested
release task-number resource-type number-requested
compute task-number number-of-cycles -1
terminate task-number -1 -1

The -1 is a placeholder for an unused number. This can be any number in
the input but must be some number. An example is provided.
