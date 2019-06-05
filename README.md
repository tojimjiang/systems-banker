# Banker

## What is this?

The program reads input, and keeps all activites as a string, and does any string correction necessary before storing in memory. Once stored in memory, the tasks complete their activites as allowed by the algorithm.

The algorithm works on FIFO resource allocation first, then works on Banker's resource allocation.

## How to Compile

### On  Windows and NYU Compute Servers
To compile and run the program, load the file banker.java to your working directory.  
Next, using a terminal window with the javac and java commands, compile the java program by using the command `javac banker.java`.  
a) To run the program after compilation with default behavior use the command `java banker 'file' `.  
b) To run the program after compilation withOUT side by side printout use the command `java banker 'file' --force`. See below for details.  

### FreeStyle Compiling
Open the jj1922.java file and copy and paste the source code into wherever you want to compile. 
The class declaration (line 21) may need modifications for freestyle compiling.  
The program must be compiled ito a java unit using the Java compiler using at a minimum Java 7.  
a) To run the program use the java command, call the program name, and the file.
    Example is: `java programName 'file' `
b) To add the force tag, add it after the file.
    Example is: `java programName 'file' --froce`

### Notes about Compiling and Running
Quotation marks (and appostrophes) above are for differantion purposes, DO NOT use them when running commands in the terminal.  
The (input) file MUST be in the same directory as the java/class file (pwd) and must be in a similar layout to that of those given.  
Default printing behaviour is side by side output like given output. Use the --force flag to get top/bottom output. (See more details below)  
MAX 2 TOTAL ARGUMENTS at innvoation (file plus MAX 1 flag). ProgramName is NOT an argument. Calling Java is NOT an argument.  
All input must still follow the specifications indicated. (All tasks must terminate.)  

## Flags
--force 	
This forces the output to be top/bottom, with FIFO on top, and Banker's on bottom. This deviates from default behavior which will print side by side.

## Error Coding
ERROR CODE 1: There are too many arguments. This program only needs at most two arguments. If you only have the file, you only have one argument. Make sure you have no odd spaces in your file name or argument that Java is parsing. If you want to use the --force flag, make sure to not add any spaces, --force flag is only 7 characters long.

ERROR CODE 2: There are too few argmeents. This program only needs at least one and at most two arguments. If you only have the file, you only have one argument. Make sure you have no odd spaces in your file name or argument that Java is parsing. If you want to use the --force flag, make sure to not add any spaces, --force flag is only 7 characters long.

ERROR CODE 3: You passed in an invalid flag, or your input was in the wrong order. If you want to use the --force flag, make sure it is SECOND, and the file name is the FIRST argument. Please make sure you type --force correctly.

ERROR CODE 4: You passed in a invalid file name, or the file name was wrong, or the file does not exist in the directory. Please make sure your input file is in the correct directory (see notes above). Please make sure you type in your file name correctly.

## Other Related Repositories:
* [Linker](https://github.com/tojimjiang/systems-linker)  
* [Scheduler](https://github.com/tojimjiang/systems-scheduler)  
* [Demand Pager](https://github.com/tojimjiang/systems-pager)  