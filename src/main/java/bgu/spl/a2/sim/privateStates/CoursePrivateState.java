package bgu.spl.a2.sim.privateStates;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.a2.PrivateState;

/**
 * this class describe course's private state
 */
public class CoursePrivateState extends PrivateState{

	private Integer availableSpots;
	private Integer registered;
	private List<String> regStudents;
	private List<String> prequisites;
	
	/**
 	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 */
	public CoursePrivateState() {
		registered = 0;
		regStudents = new ArrayList<String>(); 
		prequisites= new ArrayList<String>();
		
	}

	/**
	* @return The number of available spots.
	*/
	public Integer getAvailableSpots() {
		return availableSpots;
	}
	/**
	* @return The number of registered students.
	*/
	public Integer getRegistered() {
		return registered;
	}
	
	/**
	* @return The list of registered students.
	*/
	public List<String> getRegStudents() {
		return regStudents;
	}

	/**
	@return The Prerequisites' list.
	*/
	public List<String> getPrequisites() {
		return prequisites;
	}
	
	/**
	* Adding the space value to the current availableSpots.
	* 
	* @param space- the space which should be added to the current availableSpots.
	*/
	public void setAdditionalAvailableSpots(int space) {
		availableSpots = availableSpots+space;
	}
	
	/**
	* Set the registered value according to the regStudents's size.
	*/
	public void SetRegistered() { 
		registered=regStudents.size();
	}
	
	/**
	* Initializing the new course properties:
	* 
	* @param prerequisites
	* String list of prerequisites.
	* @param space 
	* int value of the space.
	*/ 
	public void addCourse(List<String> prequisites, int space) { 
		this.prequisites=prequisites;
		this.availableSpots=space;
	}
}
