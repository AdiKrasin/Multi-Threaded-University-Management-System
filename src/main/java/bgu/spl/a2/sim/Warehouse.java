package bgu.spl.a2.sim;

import java.util.HashMap;
import java.util.Map;

/**
 * represents a warehouse that holds a finite amount of computers
 *  and their suspended mutexes.
 * 
 */
public class Warehouse {
	
	/**
	 * A singleton class which represents the Warehouse which will be used in the Simulator.
	 */
	private static class SingletonHolder{
		private static Warehouse instance=new Warehouse();
	}
	
	private Map<String,Computer> hashMapOfComputers; 
	private Map<Computer,SuspendingMutex> hashMapOfComputersAndSuspendingMutex; 
	
	
	/**
	 * Constructor - Initializing the HashMaps.  
	 * 
	 */
	public Warehouse() {
		hashMapOfComputers=new HashMap<String,Computer>();
		hashMapOfComputersAndSuspendingMutex=new HashMap<Computer,SuspendingMutex>();
	}
	
	/**
	 * @return The Singleton Warehouse. 
	 * 
	 */
	public static Warehouse getInstance() {
		return SingletonHolder.instance;
	}
	
	
	/**
	 * Adding a computer to the Warehouse by adding it to the HashMaps. 
	 *
	 * @param computer
	 *            The Computer which should be added to the Warehouse.
	 */
	public void addComputer(Computer computer) {
		hashMapOfComputers.put(computer.getComputerType(), computer);
		hashMapOfComputersAndSuspendingMutex.put(computer, computer.getSuspendingMutex());
	}
	
	/**
	 * Returning the computer according to its type. 
	 *
	 * @param computerType
	 *            A String Computer type which should be returned from the Warehouse.
	 * @return The computer which corresponds to the computerType. 
	 */
	public Computer getComputer(String computerType) {
		return (hashMapOfComputers.get(computerType));
	}

	
	
}
