package bgu.spl.a2.sim;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import bgu.spl.a2.Promise;

/**
 * 
 * this class is related to {@link Computer}
 * it indicates if a computer is free or not
 * 
 * Note: this class can be implemented without any synchronization. 
 * However, using synchronization will be accepted as long as the implementation is blocking free.
 *
 */
public class SuspendingMutex {
	
	protected AtomicBoolean isComputerFree;
	protected Computer computerOfSuspendingMutex;
	protected List<Promise<Computer>> listOfPromises;
	
	/**
	 * Constructor
	 * @param computer - Computer which should be added to the SuspendingMutex.
	 */
	public SuspendingMutex(Computer computer){
		isComputerFree=new AtomicBoolean (true);
		computerOfSuspendingMutex=computer;
		listOfPromises=new ArrayList<Promise<Computer>>();
	}
	
	/**
	 * Computer acquisition procedure
	 * Note that this procedure is non-blocking and should return immediately
	 * 
	 * @return A promise for the requested computer.
	 */
	public Promise<Computer> down(){
		Promise<Computer> promiseComputer=new Promise<Computer>();
		if(isComputerFree.compareAndSet(true, false)) { // Verifying that the computer is free and setting its value to false. 
			promiseComputer.resolve(computerOfSuspendingMutex); // Returning a promise of the corresponding computer of the suspending mutex.  
		}
		else {
			listOfPromises.add(promiseComputer);
		}
		return promiseComputer;
	}
	/**
	 * Computer return procedure
	 * releases a computer which becomes available in the warehouse upon completion
	 */
	public void up(){
		isComputerFree.compareAndSet(false, true); // Setting the suspending mutex as free by changing the atomic boolean to true.  
		if(listOfPromises.size()!=0) { // In case the promise is not empty:
			listOfPromises.get(0).resolve(computerOfSuspendingMutex); // Resolving the promise with the corresponding computer of the suspending mutex.
			listOfPromises.remove(0); // Removing the promise from the list. 
		}
	}
}
