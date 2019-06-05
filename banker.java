/* 
 * Operating Systems Banker
 *
 * Description: This algorithm takes in command line arguments when calling the program. This banker features 2
 * algorithms as specified in the specification. There is an added --force feature. (See README for more details).
 * This program works with the two REQUIRED invocations of <program> <file> and <program> <file> --force.
 *
 * FILE NAME MUST BE THE **SECOND** ARGUMENT IN INVOCATION.
 * If the force flag is used, the flag must be last. The file name has to be BEFORE the flag.
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.Scanner;

public class nanker {
    // MAIN OF PROGRAM IS HERE (Class Declaration Above)
    public static void main(String[] args) throws FileNotFoundException {
        // Force Top and Bottom FIFO/Banker Output (no side by side) Option
        boolean wantSxS = true;

        // Check amount of command line arguments
        // Too many args
        if (args.length > 2) {
            System.out.println("Error: There is an invalid number of arguments. (too many)");
            System.out.println("Please check the README file, for details about this error, and how to resolve this error.");
            System.out.println("ERROR CODE: 1");
            System.exit(1);
        }
        // Too few args
        else if (args.length < 1) {
            System.out.println("Error: There is an invalid number of arguments. (too few)");
            System.out.println("Please check the README file, for details about this error, and how to resolve this error.");
            System.out.println("ERROR CODE: 2");
            System.exit(2);
        }
        // Allowable amount of args
        else if (args.length == 2) {
            // Check flag usage
            if (args[1].equals("--force")) {
                wantSxS = false;
            }
            // Wrong usage (error)
            else {
                System.out.println("Error: There is an invalid argument passed in. (wrong flag)");
                System.out.println("Please check the README file, for details about this error, and how to resolve this error.");
                System.out.println("ERROR CODE: 3");
                System.exit(3);
            }
        }

        // Get file name, and Verify existence
        File fileName = new File(args[0]);
        if (!fileName.exists()) {
            System.out.println("Error: The file does not exist in the directory.");
            System.out.println("Please check the README file, for details about this error, and how to resolve this error.");
            System.out.println("ERROR CODE: 4");
            System.exit(4);
        }

        // Scanner (read) the input file
        Scanner input = new Scanner(fileName);

        // First Token is number of tasks
        int numTasks = Integer.parseInt(input.next());
        // Second Token is number of resources
        int numResources = Integer.parseInt(input.next());

        // Initialize the algorithm objects
        fifo fifoObj = new fifo(numTasks, numResources);
        banker bankerObj = new banker(numTasks, numResources);


        // Additional Tokens are units per resource
        for (int i = 0; i < numResources; i++) {
            int resourceUnits = Integer.parseInt(input.next());
            fifoObj.free[i] = resourceUnits;
            bankerObj.free[i] = resourceUnits;
            bankerObj.sys[i] = resourceUnits;
        }

        // Initialize Task Arrays
        // Use two arrays for simplicity's sake
        Task[] tasks1 = new Task[numTasks];
        Task[] tasks2 = new Task[numTasks];
        for (int i = 0; i < numTasks; i++) {
            tasks1[i] = new Task(i);
            tasks2[i] = new Task(i);
        }

        // Input Parser
        // Which Adds the Activities into each Task
        // Activities is kept in a CLEANED String, in the arrayList in each task.
        // Just clean once, and deal with the other activities as we need. Don't need to care about
        // all the activities at once. No real parsing here. Just check structure, and fix and "gaps or holes"
        String activity = "";
        String taskNum = "";
        String arg3 = "";
        String arg4 = "";

        // Cleaned Arg to add to activities list of tasks, and safeRead tracker (did we over-read?)
        // Cleaned Arg to add to activities list of tasks, and safeRead tracker (did we over-read?)
        String cleanArg = "";
        Boolean safeRead = true;

        // Do while we have input
        while (input.hasNext()) {
            // Normal Operation Conditions
            if (safeRead) {
                activity = input.next();
            }
            // We over-read by a chunk (in compute and terminate)
            else {
                // DO NOT read next.
                // Reset safeRead state
                safeRead = true;
            }
            // What was the activity choice?
            switch (activity) {
                // These use all 4 args
                case "initiate":
                    // Same as below
                case "request":
                    // Same as below
                case "release":
                    taskNum = input.next();
                    // Resource Type
                    arg3 = input.next();
                    // Resource Amount
                    arg4 = input.next();
                    // Create Clean Arg
                    cleanArg = activity + ' ' + taskNum + ' ' + arg3 + ' ' + arg4;
                    break;
                // Compute only NEEDS 3 args, deal with what if 1 arg was dropped?
                case "compute":
                    taskNum = input.next();
                    // Cycles
                    arg3 = input.next();
                    // Create Clean Arg
                    cleanArg = activity + ' ' + taskNum + ' ' + arg3 + " 0";
                    if (input.hasNext()) {
                        arg4 = input.next();
                        // Read "arg4", is next activity?
                        switch (arg4) {
                            // If any case was true we over read, we have entered an unsafe read state
                            // And overwrite activity to prevent data loss
                            case "initiate":
                            case "request":
                            case "release":
                            case "compute":
                            case "terminate":
                                activity = arg4;
                                safeRead = false;
                                break;
                            // We did NOT overread, and arg4 was truely arg4
                            default:
                                safeRead = true;
                        }
                    }
                    break;
                // Terminate only NEEDS 2 args, deal with what if 2 args were dropped?
                case "terminate":
                    taskNum = input.next();
                    // Create Clean Arg
                    cleanArg = activity + ' ' + taskNum + " 0 0";
                    if (input.hasNext()) {
                        arg3 = input.next();
                        // Read "arg3", is it next activity?
                        switch (arg3) {
                            // If any case was true we over read, we have entered an unsafe read state
                            // Need to add to activities list, and overwrite activity to prevent data loss
                            case "initiate":
                            case "request":
                            case "release":
                            case "compute":
                            case "terminate":
                                activity = arg3;
                                safeRead = false;
                                break;
                            // We did NOT overread, and arg3 was truely arg3
                            default:
                                // We DID NOT enter unsafe state, may need to clear out another bit to be safe for next activity read
                                if (input.hasNext()) {
                                    arg4 = input.next();
                                    switch (arg4) {
                                        // If any case was true we over read, we have entered an unsafe read state
                                        // Need to add to activities list, and overwrite activity to prevent data loss
                                        case "initiate":
                                        case "request":
                                        case "release":
                                        case "compute":
                                        case "terminate":
                                            activity = arg4;
                                            safeRead = false;
                                            break;
                                        // We did NOT overread, and arg4 was truely arg4
                                        default:
                                            safeRead = true;
                                    }
                                }
                        }
                    }
                    break;
            }
            // Get the task number from above, then add a copy into each task's array.
            int taskID = Integer.parseInt(taskNum) - 1;
            tasks1[taskID].Activities.add(cleanArg);
            tasks2[taskID].Activities.add(cleanArg);
        }

        // Arraylists to Store Output
        ArrayList<String> fifoOutput= new ArrayList<String>();
        ArrayList<String> bankerOutput= new ArrayList<String>();

        // Run the algorithms
        fifoObj.runFifo(tasks1, fifoOutput);
        bankerObj.runBanker(tasks2, bankerOutput);

        // Default Make Side by side output, by combining the two output arraylists.
        if (fifoOutput.size() == bankerOutput.size() && wantSxS){
            // Same lengths, we can do SxS
            while(!fifoOutput.isEmpty()){
                String thisLine = String.format("%-32s",fifoOutput.remove(0));
                thisLine = thisLine + String.format("%s",bankerOutput.remove(0));
                System.out.println(thisLine);
            }
        }
        else {
            // Either Forced Top/Bottom or Uneven Outputs?
            while (!fifoOutput.isEmpty()) {
                System.out.println(fifoOutput.remove(0));
            }
            while (!bankerOutput.isEmpty()) {
                System.out.println(bankerOutput.remove(0));
            }
        }
    }
}

class Task {
    // Passed in mostly constant attributes for tasks
    int taskId;
    int computeCycle;
    ArrayList<String> Activities;

    // Pointers and Running Statistics
    int activityPtr;
    int blockedCycle;
    int finishCycle;
    Boolean aborted;
    Boolean finished;

    // Constructor
    public Task(int i) {
        // Mostly INPUT items
        taskId = i+1;
        activityPtr = 0;
        Activities = new ArrayList<String>();

        // Pointer and running statistics
        computeCycle = 0;
        blockedCycle = 0;
        aborted = false;
        finished = false;
    }

    // Check to see if any activities are left for task
    public Boolean hasNextActivity() {
        if(activityPtr == Activities.size()-1) {
            return false;
        }
        else {
            return true;
        }
    }

    // Move the pointer of the activity list of the task.
    public void movePtr() {
        activityPtr++;
    }

    // Get the next activity for the task
    public String getActivity() {
        if (activityPtr > Activities.size() - 1) {
            // Another corner case
            return "terminate";
        }
        else {
            return Activities.get(activityPtr);
        }
    }

    // Deal with blocked tasks for the current cycle
    public void block() {
        blockedCycle++;
    }

    // Deal with computing tasks for the current cycle
    public void compute() {
        computeCycle--;
    }

    // Finish tasks (checks done/finish-ness with isFinished in algos above)
    public void finishTask(int finish) {
        finishCycle = finish;
        finished = true;
    }

    // Abort tasks (when we decide to abort tasks, note the aborted state, and perform a soft reset.
    public void abortTask() {
        aborted = true;
        blockedCycle = 0;
        activityPtr = Activities.size()-1;
    }

    // See if the task terminated. Terminate and no longer computing (e.g. using the resource).
    // Since terminated is never used as a case in the algorithms, the task deals with teminate,
    // and makes sure that task has terminated.
    // Use of the finished boolean allows for faster lookups after the task first finishes.
    public Boolean isFinished() {
        if (finished) {
            return true;
        }
        else if (getActivity().contains("terminate") && computeCycle == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    // See if the task was aborted.
    public Boolean isAborted() {
        if(aborted) {
            return true;
        }
        else {
            return false;
        }
    }
}

class fifo {
    // Quantities (Tasks and Resources)
    int numTasks;
    int numResources;

    // Arrays of Free, and Alloc(ated)
    int[] free;
    int[][] alloc;

    // Constructor
    public fifo(int Tasks, int Resources) {
        // Quantities (Tasks and Resources)
        numTasks = Tasks;
        numResources = Resources;
        // Arrays of Free, and Alloc(ated)
        free = new int[numResources];
        alloc = new int[numTasks][numResources];
    }

    // Calling Method to run the FIFO algorithm. Weill call other functions for deadlock "detection", and allocation
    // Calls isDeadlocked to see if the state is now deadlocked. The Method (here) will abort to clear the deadlock
    // Calls to fifoAlloc to allocate and block if unable to allocate.
    // Cyclewise it deals with previously blocked processes to see if any unblock (can get resources).
    // Then deals with the activities for each task. In the order Initiate > Request > Release > Compute
    // Then does any computes, checks for deadlocks, updates frees, and checks for completion.
    // Once complete it will write output to an array used in main method for printing.
    public void runFifo(Task[] tasks, ArrayList<String>  output){
        // Initialize Running Algo Items
        int cycle = 1;
        // List (Queues) for Blocked Tasks
        Queue<Task> blocked = new LinkedList<Task>();
        // List for Ready Tasks - Add and Clear-out per cycle
        ArrayList<Task> ready = new ArrayList<Task>();
        // List for Waiting Tasks (resources not ready) - Add and Clear-out per cycle
        ArrayList<Task> wait = new ArrayList<Task>();
        // If there was a deadlock possible from previous cycle
        Boolean isDanger = false;

        // Keep running (forever loop) until all tasks finish or abort
        while(true) {
            // Temporary Object Stores the current task to process
            Task task;
            // Pruge (clear) the wait list (from last iteration/cycle)
            wait.clear();

            // Track the amount of activites and blockedRequests this cycle. If all tasks block (fail allocation) we have a deadlock
            int activities = 0;
            int blockedReq = 0;

            // Array to track released resources in this cycle
            int[] released = new int[numResources];
            for(int i = 0; i < numResources; i++) {
                released[i] = 0;
            }

            // If we had a deadlock last cycle, try aborting to clear deadlock (FIFO's Aborter)
            if(isDanger) {
                for(int i = 0; i < numTasks; i++) {
                    // Based on unfinished/non aborted taskss
                    if(!tasks[i].isFinished() && !tasks[i].isAborted()) {
                        // Abort the lowest number (earliest) task
                        tasks[i].abortTask();
                        // Release it's resources
                        for(int j = 0; j < numResources; j++) {
                            free[j] += alloc[i][j];
                        }
                        // Remove task from blocked list
                        blocked.remove(tasks[i]);

                        // See if we are still deadlocked. If we are *NO* longer deadlocked, we no longer keep aborting.
                        if(!isDeadlock(tasks)) {
                            // Update Status, Leave Aborter Loop
                            isDanger = false;
                            break;
                        }
                    }
                }
            }
            // See if any blocked become ready/wait.
            while(!blocked.isEmpty()) {
                // Based on each task in blocked, we have an activity trying to request
                task = blocked.poll();
                activities++;

                // Try to allocate, if we can, the task is ready
                if(fifoAlloc(task)) {
                    ready.add(task);
                    // Shift to next activity for the task.
                    task.movePtr();
                }
                // Otherwise, the task is now forced to wait. This is a blocked request.
                else{
                    blockedReq++;
                    wait.add(task);
                }
            }
            // All waiting tasks are blocked.
            blocked.addAll(wait);

            // Work on all tasks in order of input
            for(int i = 0; i < numTasks; i++) {
                String curActivity = new String();
                // Work on non-previously blocked (current non-blocked and non-ready tasks)
                if(!blocked.contains(tasks[i]) && !ready.contains(tasks[i])) {
                    // Work on non-computing tasks
                    if(tasks[i].computeCycle == 0) {
                        if(tasks[i].hasNextActivity()) {
                            curActivity = tasks[i].getActivity();
                        }
                        // Use the cleaned input (curActivity) String to Case the input, and parse within the algorithm
                        // Do string.contain matching so find a match.

                        // Initiate Tasks Case
                        if(curActivity.contains("initiate")) {
                            // This is an activity, so increment counter
                            activities++;
                            // We don't care about resources and claims and stuff for optimistic FIFO manager.
                            // Shift to next activity for the task.
                            tasks[i].movePtr();
                        }

                        // Request Resources Case
                        else if(curActivity.contains("request")) {
                            // This is an activity, so increment counter
                            activities++;
                            // Try to allocate
                            if(fifoAlloc(tasks[i])) {
                                // Success!
                                // Shift to next activity for the task.
                                tasks[i].movePtr();
                            }
                            else{
                                // Failure - the task is now forced to wait. This is a blocked request.
                                blockedReq++;
                                blocked.add(tasks[i]);
                            }
                        }

                        // Release Resources Case
                        else if(curActivity.contains("release")) {
                            // This is an activity, so increment counter
                            activities++;

                            // Split/Parse the Activity String
                            String splitRelease[] = curActivity.split("\\s+");
                            int resType = Integer.parseInt(splitRelease[2]) - 1;
                            int numRel = Integer.parseInt(splitRelease[3]);

                            // Note the releases/changes in alloc, only update available resources at the end of cycle
                            released[resType]+=numRel;
                            alloc[i][resType] -= numRel;

                            // Shift to next activity for the task.
                            tasks[i].movePtr();
                            // See if we are now finished (terminate the task)
                            if(tasks[i].isFinished()) {
                                tasks[i].finishTask(cycle);
                            }
                        }

                        // Compute (Task) Case
                        else if(curActivity.contains("compute")) {
                            // This is an activity, so increment counter
                            activities++;

                            // Split/Parse the Activity String
                            String splitCompute[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitCompute[1]) - 1;
                            int computeArg = Integer.parseInt(splitCompute[2]) - 1;

                            // Do the compute time
                            tasks[taskNum].computeCycle = computeArg;

                            // Shift to next activity for the task
                            tasks[taskNum].movePtr();
                            // See if we are now finished (terminate the task)
                            if(tasks[i].isFinished() && tasks[i].computeCycle == 0) {
                                tasks[i].finishTask(cycle);
                            }
                        }
                        // All cases dealt with.
                    }
                    // Task is IN computing state
                    else{
                        // This is an activity, so increment counter
                        activities++;
                        tasks[i].compute();
                        if(tasks[i].computeCycle == 0 && tasks[i].isFinished()) {
                            tasks[i].finishTask(cycle);
                        }
                    }
                }
                // Finished Operations on all tasks, this cycle
            }

            // Update the free resources this cycle from releases.
            for(int i = 0; i < numResources; i++) {
                free[i]+=released[i];
            }

            // Remove remaining tasks that are in the ready list
            Task[] remTask = ready.toArray(new Task[0]);
            for(int i = 0; i < remTask.length; i++) {
                ready.remove(remTask[i]);
            }

            // Did we have a deadlock this cycle? If so, note the danger (for next cycle), otherwise set to false.
            if(activities == blockedReq) {
                isDanger = true;
            }
            else{
                isDanger = false;
            }

            // If all done then stop loop
            if(done(tasks)) {
                break;
            }

            // Increment and start next cycle
            cycle++;
        }
        // Store Printout in Array
        arrayOut(tasks, output);
    }

    // See if the state remains deadlocked. Based on the current tasks, see if there are enough frees.
    public boolean isDeadlock(Task[] task) {
        for(int i = 0; i < numTasks; i++) {
            // Based on unfinished/non aborted taskss
            if(!task[i].isFinished() && !task[i].isAborted()) {
                // Split/Parse the Activity String
                String curActivity = task[i].getActivity();
                String splitRequest[] = curActivity.split("\\s+");
                int resType = Integer.parseInt(splitRequest[2]) - 1;
                int numReq = Integer.parseInt(splitRequest[3]);

                // If there are enough frees now to allocate any resource there is NO deadlock!
                if(free[resType] >= numReq) {
                    return false;
                }
            }
        }
        // If we exit the loop, and never have enough frees to allocate anything, we have deadlock!
        return true;
    }

    // Method for FIFO algorithm (allocate optimistically). If there are enough (free), allocate, otherwise block.
    public Boolean fifoAlloc(Task task) {
        // Split/Parse the Activity String
        String curActivity = task.getActivity();
        String splitRequest[] = curActivity.split("\\s+");
        int resType = Integer.parseInt(splitRequest[2]) - 1;
        int numReq = Integer.parseInt(splitRequest[3]);
        // See if have enough resources
        if(free[resType] - numReq >= 0) {
            // There are enough free!
            // Allocate and update
            alloc[task.taskId-1][resType]+=numReq;
            free[resType] -= numReq;
            return true;
        }
        else{
            // NOT enough free, the task is blocked.
            task.block();
            return false;
        }
    }

    // Check if all tasks are done
    public Boolean done(Task[] task) {
        for(int i = 0; i < numTasks; i++) {
            // Any non finished task will cause a return false
            if(!task[i].isFinished()) {
                return false;
            }
        }
        // Otherwise, all tasks are finished!
        return true;
    }

    // Print out to arraylist
    public void arrayOut(Task[] task, ArrayList<String>  output) {
        // Algo Message
        String currentLine = "              FIFO              ";
        output.add(currentLine);

        // Ints to get total stats
        int totalF = 0;
        int totalB = 0;

        // Task Wise Message
        for(int i = 0; i < numTasks; i++) {
            // Clear Old Line
            currentLine = "";
            currentLine = currentLine + String.format("    Task %-4d", task[i].taskId);
            // Aborted Case
            if (task[i].isAborted()) {
                currentLine = currentLine + "  aborted";
            }
            // Normal Case
            else {
                // Create the Line
                currentLine = currentLine + String.format("%3d", task[i].finishCycle);
                currentLine = currentLine + String.format("%5d", task[i].blockedCycle);
                // Get Float for Percent
                float finishC = (float) task[i].finishCycle;
                float blockC = (float) task[i].blockedCycle;
                float percent = 100 * (blockC / finishC);
                // Finish Line
                currentLine = currentLine + String.format("%5.0f%%", percent);
                // Total Stats
                totalF += task[i].finishCycle;
                totalB += task[i].blockedCycle;
            }
            // Add to output array
            output.add(currentLine);
        }
        // All Tasks Messages Complete

        // Total Stat Message
        // Clear Old Line
        currentLine = "";
        currentLine = currentLine + String.format("    total    ");
        // Get Float for Percent
        float floatF = (float) totalF;
        float floatB = (float) totalB;
        float floatT = 100 * floatB/floatF;
        // Create the Line
        currentLine = currentLine + String.format("%3d",totalF);
        currentLine = currentLine + String.format("%5d",totalB);
        currentLine = currentLine + String.format("%5.0f%%", floatT);
        // Add to output array
        output.add(currentLine);
    }
}

class banker {
    // Quantities (Tasks and Resources)
    int numTasks;
    int numResources;

    // Requester task, and array of requested resources from all tasks
    int reqT;
    int[][] reqR;

    // Arrays of Free, Claims, Alloc(ated), Needs, and System
    int[] free;
    int[][] claim;
    int[][] alloc;
    int[][] need;
    int[] sys;

    // Constructor
    public banker(int Tasks,int Resources) {
        // Quantities (Tasks and Resources)
        numTasks = Tasks;
        numResources = Resources;
        // Arrays of Free, Claims, Alloc(ated), Needs, and System
        free = new int[numResources];
        claim = new int[numTasks][numResources];
        alloc = new int[numTasks][numResources];
        need = new int[numTasks][numResources];
        sys = new int[numResources];
        // Requested Resources Array
        reqR = new int[numTasks][numResources];
    }

    // Calling Method to run the banker's algorithm. Uses submethods to allocate, update, and determine safeness
    // Calls to safeBanker to allocate and block if unable to allocate in a safe manner.
    // Cyclewise it deals with previously blocked processes to see if any unblock (allowed to get resources).
    // Then deals with the activities for each task. In the order Initiate > Request > Release > Compute
    // Then does any computes, updates frees, and checks for completion.
    // Once complete it will write output to an array used in main method for printing.
    public void runBanker(Task[] tasks, ArrayList<String>  output) {
        // Initialize Running Algo Items
        int cycle = 1;
        // List (Queues) for Blocked Tasks
        Queue<Task> blocked = new LinkedList<Task>();
        // List for Ready Tasks - Add and Clear-out per cycle
        ArrayList<Task> ready = new ArrayList<Task>();
        // List for Waiting Tasks (resources not ready) - Add and Clear-out per cycle
        ArrayList<Task> wait = new ArrayList<Task>();

        // Keep running (forever loop) until all tasks finish or abort
        while(true) {
            // Temporary Object Stores the current task to process
            Task task;
            // Pruge (clear) the wait list (from last iteration/cycle)
            wait.clear();

            // Array to track, released resources, new needs, and new allocations in this cycle
            int[] released = new int[numResources];
            int[][] newNeed = new int[numTasks][numResources];
            int[][] newAlloc = new int[numTasks][numResources];
            for(int i = 0; i < numResources; i++) {
                released[i] = 0;
                for(int j = 0; j < numTasks; j++) {
                    newNeed[j][i] = 0;
                    newAlloc[j][i] = 0;

                }
            }

            // See if any blocked become ready/wait.
            while(!blocked.isEmpty()) {
                task = blocked.poll();
                if(safeBanker(tasks,task, cycle)) {
                    // Banker successfully allocated task. (It was allowed, stayed in safe state).
                    ready.add(task);
                    // Shift to next activity for the task.
                    task.movePtr();
                }
                else{
                    // Banker rejected right now, make the task wait.
                    wait.add(task);
                }
            }

            // All remaining blocked tasks will have to WAIT, so make them wait.
            blocked.addAll(wait);

            // Work on all tasks in order of input
            for(int i = 0; i < numTasks; i++) {
                String curActivity = new String();
                // Work on non-previously blocked (current non-blocked and non-ready tasks)
                if(!blocked.contains(tasks[i]) && !ready.contains(tasks[i])) {
                    // Work on non-computing tasks
                    if(tasks[i].computeCycle == 0) {
                        // Get a task to work on
                        if(tasks[i].hasNextActivity()) {
                            curActivity = tasks[i].getActivity();
                        }
                        // Use the cleaned input (curActivity) String to Case the input, and parse within the algorithm
                        // Do string.contain matching so find a match.

                        // Initiate Tasks Case
                        // Checking if claims are valid (for the system) is done in THIS segment.
                        if(curActivity.contains("initiate")) {
                        	// Split/Parse the Activity String
                            String splitInitiate[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitInitiate[1])-1;
                            int resType = Integer.parseInt(splitInitiate[2])-1;
                            int initClaim = Integer.parseInt(splitInitiate[3]);

                            // If claims exceeds the free resources present, we abort
                            if(initClaim>free[resType]) {
                                tasks[taskNum].abortTask();
                                // If claim exceeds system, print error message
                                if (initClaim > sys[resType]) {
                                    int realRes = resType + 1;
                                    // The algorithm simply does not initalize. My banker doesn't do a pre-check.
                                    // Copied Message. Accurate message would be - Aborts during init
                                    System.out.printf("Banker aborts task %d before run begins:\n", tasks[taskNum].taskId);
                                    System.out.printf("    claim for resource %d (%d) exceeds number of units present (%d)\n",realRes, initClaim, sys[resType]);
                                }
                            }
                            // Otherwise we try to allocate
                            else{
                                claim[taskNum][resType] = initClaim;
                                need[taskNum][resType] = initClaim;
                                // Shift to next activity for the task.
                                tasks[i].movePtr();
                            }
                        }

                        // Request Resources Case
                        else if(curActivity.contains("request")) {
                            // Try to allocate by banker
                            if(safeBanker(tasks,tasks[i], cycle)) {
                                // Banker successfully allocated!
                            	// Shift to next activity for the task.
                                tasks[i].movePtr();
                            }
                            else { 
                                // Otherwise, banker did not allow, and this task is blocked.
                                blocked.add(tasks[i]);
                            }
                        }

                        // Release Resources Case
                        else if(curActivity.contains("release")) {
                        	// Split/Parse the Activity String
                            String splitRelease[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitRelease[1])-1;
                            int resType = Integer.parseInt(splitRelease[2])-1;
                            int numRel = Integer.parseInt(splitRelease[3]);

                            // Note the relases/changed alloc and need, only update available resources at end of cycle
                            released[resType] += numRel;
                            newAlloc [taskNum][resType] = numRel;
                            newNeed  [taskNum][resType] = numRel;

                            // Shift to next activity for the task.
                            tasks[i].movePtr();
                            // See if we are now finished (terminate the task)
                            if(tasks[i].isFinished()) {
                                tasks[i].finishTask(cycle);
                            }
                            
                        }

                        // Compute (Task) Case
                        else if(curActivity.contains("compute")) {
                        	// Split/Parse the Activity String
                            String splitCompute[] = curActivity.split("\\s+");
                            int taskNum = Integer.parseInt(splitCompute[1])-1;
                            int computeArg = Integer.parseInt(splitCompute[2])-1;

                            // Do the compute time
                            tasks[taskNum].computeCycle = computeArg;
                            // Shift to next activity for the task.
                            tasks[i].movePtr();
                            
                            // See if we are now finished (terminate the task)
                            if(tasks[i].isFinished() && tasks[i].computeCycle == 0) {
                                tasks[i].finishTask(cycle);
                            }
                        }
                        // All cases dealt with.
                    }
                    // Task is IN computing state.
                    else{
                        tasks[i].compute();
                        if(tasks[i].computeCycle == 0 && tasks[i].isFinished()) {
                            tasks[i].finishTask(cycle);
                        }
                    }
                }
                // Finished operations on all tasks, this cycle
            }

            // Update the status, based on what was released, and what was allod'ed (and now needs)
            update(released, newAlloc, newNeed);

            // Remove remaining tasks that are in the ready list
            Task[] remTask = ready.toArray(new Task[0]);
            for(int i = 0; i < remTask.length; i++) {
                ready.remove(remTask[i]);
            }

            // If all done then stop loop
            if(done(tasks)) {
                break;
            }

            // Increment and start next cycle
            cycle++;
        }
        // Store printout in array
        arrayOut(tasks, output);
    }

    // Update resources, allocation, and needs of tasks, due to last cycle.
    public void update(int[] released,int[][] newAlloc,int[][] newNeed) {
        // Deal with released resources
        for(int i = 0; i < numResources; i++) {
            free[i]+=released[i];
        }
        // Deal with new alloc and needs
        for(int i = 0; i < numTasks; i++) {
            for(int j = 0; j < numResources; j++) {
                alloc[i][j] -= newAlloc[i][j];
                need[i][j] +=newNeed[i][j];
            }
        }
    }

    // Method for the banker algorithm (allocate safely), use the activity requesting, and first parse
    // Then see if system can provide. If so, cosider if there is a safe path? If yes, we can allocate
    // requested resources. If request is too big to fit in claim (with already alloced), reject from
    // banker and abort+error. If no safe way to complete, have activity/task wait (block).
    public Boolean safeBanker(Task[] tasks, Task task, int cycle) {
        // Get the next activity from the task and split into pieces
        String curActivity = task.getActivity();
        String splitA[] = curActivity.split("\\s+");
        int taskNum = Integer.parseInt(splitA[1]) - 1;
        int resType = Integer.parseInt(splitA[2]) - 1;
        int numReq = Integer.parseInt(splitA[3]);

        // Deal with cases when an terminate is passed in
        // DUE to claims exceeded system resources
        if (resType < 0) {
        	resType = resType + 1;
        }
        
        // Initalize requested resources
        for(int k = 0; k < numTasks; k++) {
            for(int l = 0; l < numResources; l++) {
                reqR[k][l] = 0;
            }
        }
        // Track what is the requesting task, and the requested resources
        reqT = taskNum;
        reqR[reqT][resType] = numReq;

        // Try to allocate the requested resources
        for(int k = 0; k < numResources; k++) {
            // If requested resource exceeds remaining need (claim) - Banker's Aborter
            if(need[reqT][k] - reqR[reqT][k] < 0) {
                // Print out error message
                // My cycles start at 1, so I count end of cycles. (so I need to subtract to get the cycle period.)
                System.out.printf("During cycle %d-%d of Banker's algorithm\n", cycle-1, cycle);
                System.out.printf("    Task %d's request exceeded its claim; aborted; %d units available next cycle\n", tasks[taskNum].taskId, alloc[reqT][k]);
                // Abort (not valid) task, and release resources
                free[k] = free[k] + reqR[reqT][k];
                alloc[reqT][k] = alloc[reqT][k] - reqR[reqT][k];
                need[reqT][k] = need[reqT][k] + reqR[reqT][k];
                task.abortTask();
            }
            // If requested resource does NOT exceed remaining need (claim)
            else {
                // Allocate, and note changes
                free[k] = free[k] - reqR[reqT][k];
                alloc[reqT][k] = alloc[reqT][k] + reqR[reqT][k];
                need[reqT][k] = need[reqT][k] - reqR[reqT][k];

            }
        }

        // Test to see if we stay safe
        if(isSafe(tasks)) {
            // Stay in Safe State
            for(int k = 0; k < numResources; k++) {
                reqR[reqT][k] = 0;
            }
            return true;
        }

        else {
            // This allocation moves into an unsafe state.
            // Take back (we never TRUELY allocated yet)
            // And update, free/alloc/need due to non-allocation
            // Due to unsafe state.
            for(int k = 0; k < numResources; k++)
            {
                free[k] = free[k] + reqR[reqT][k];
                alloc[reqT][k] = alloc[reqT][k] - reqR[reqT][k];
                need[reqT][k] = need[reqT][k] + reqR[reqT][k];
            }
            // Reset the tasks's resource requests this cycle
            for(int k = 0; k < numResources; k++) {
                reqR[reqT][k] = 0;
            }
            // Block task, due to allocation fail, due to unsafe state now
            task.block();
            return false;
        }
    }

    // Method to determine safe-ness. Use banker's algoirthm to see if there are enough free to
    // fulfill outstanding needs, and if so, based on terminations in the program determine if the
    // state will be safe of unsafe, now.
    public boolean isSafe(Task[] tasks) {
    	// Array to hold our working space, (state of free resources as jobs "finish")
        // Initialize the workingSpace (by clone), based on the current free resources.
        int[] workingSpace = free.clone();
        // Array to track which tasks so far are "finished"
        boolean[] Finish = new boolean[numTasks];


        // Initalize the FINISHED array (of tasks)
        // Already complete or aborted tasks are considered finished
        for(int i = 0; i < numTasks; i++) {
            if(tasks[i].isFinished()||tasks[i].isAborted()) {
                Finish[i] = true;
            }
            // Otherwise the task is NOT yet finished.
            else{
                Finish[i] = false;
            }
        }

        // Look for task who's entire need can be satisfied by the current working space.
        for(int i = 0; i < numTasks; i++) {
            // Every task, we "assume" their entire need can be satisfied
            boolean satisfied = true;
            
            // Look at every resource of the task.
            for (int j = 0; j < numResources; j++) {
            	// If ANY case of the need exceeds the working space, we enter UNSAFE state (if we were to allocate).
                if (need[i][j] > workingSpace[j]) {
                	// Therefore not satisfied, and give up consideration on this task.
                    satisfied = false;
                    break;
                }
            }

            // Based on above filtering, assume it *now* completes.
            // Was NOT yet finished, and was able to be satisfied on the current workingSpace
            if (!Finish[i] && satisfied) {
            	// Assume task completes, and release all the resources allocated to the task
            	// to the working space. 
                for (int j = 0; j < numResources; j++) {
                    workingSpace[j] = workingSpace[j] + alloc[i][j];
                }
                // And we assume the task now finishes
                Finish[i] = true;
                // And we reset the task iterator (i), to start at the beginning again.
                i = -1;
            }
        }

        // After pretend allocating, freeing and finishing
        // See if ALL tasks finish
        for (int i = 0; i < numTasks; i++) {
        	// UNSAFE state, because some task did not finish
            if(!Finish[i]) {
                return false;
            }
        }
        // All finishes were true, meaning there was some way for
        // all tasks to finish, so SAFE state.
        return true;
    }

    // Check if all tasks are done
    public Boolean done(Task[] task) {
        for(int i = 0; i < numTasks; i++) {
            // Any non finished task will cause a return false
            if(!task[i].isFinished()) {
                return false;
            }
        }
        // Otherwise, all tasks are finished!
        return true;
    }

    // Prepare Printout as an Array of Strings
    public void arrayOut(Task[] task, ArrayList<String>  output) {
        // Algo Message
        String currentLine = "            BANKER'S            ";
        output.add(currentLine);

        // Ints to get total stats
        int totalF = 0;
        int totalB = 0;

        // Task Wise Message
        for(int i = 0; i < numTasks; i++) {
            // Clear Old Line
            currentLine = "";
            currentLine = currentLine + String.format("    Task %-4d", task[i].taskId);
            // Aborted Message
            if (task[i].isAborted()) {
                currentLine = currentLine + "  aborted";
            }
            // Normal Case
            else {
                // Create the Line
                currentLine = currentLine + String.format("%3d", task[i].finishCycle);
                currentLine = currentLine + String.format("%5d", task[i].blockedCycle);
                // Get Float for Percent
                float finishC = (float) task[i].finishCycle;
                float blockC = (float) task[i].blockedCycle;
                float percent = 100 * (blockC / finishC);
                // Finish the Line
                currentLine = currentLine + String.format("%5.0f%%", percent);
                // Total Stats
                totalF += task[i].finishCycle;
                totalB += task[i].blockedCycle;
            }
            // Add to output array
            output.add(currentLine);
        }
        // All Tasks Messages Complete

        // Total Stat Message
        // Clear Old Line
        currentLine = "";
        currentLine = currentLine + String.format("    total    ");
        // Get Float for Percent
        float floatF = (float) totalF;
        float floatB = (float) totalB;
        float floatT = 100 * floatB/floatF;
        // Create the Line
        currentLine = currentLine + String.format("%3d",totalF);
        currentLine = currentLine + String.format("%5d",totalB);
        currentLine = currentLine + String.format("%5.0f%%", floatT);
        // Add to output array
        output.add(currentLine);
    }
}