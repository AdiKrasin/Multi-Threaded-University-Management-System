package bgu.spl.a2.sim.privateStates;

import java.util.HashMap;

import bgu.spl.a2.PrivateState;

/**
 * this class describe student private state
 */
public class StudentPrivateState extends PrivateState{

	private HashMap<String, Integer> grades;
	private long signature;
	
	/**
 	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 */
	public StudentPrivateState() {
		grades = new HashMap<String, Integer>();
	}

	/**
	* @return The grades' HashMap.
	*/
	public HashMap<String, Integer> getGrades() {
		return grades;
	}

	/**
	* @return The signature.
	*/
	public long getSignature() {
		return signature;
	}
	
	/**
	* Setting the student's signature according to the given value. 
	* 
	* @param signature- long signature which should be set as the student's signature.
	*/
	public void SetSignature(long signature) { 
		this.signature=signature;
	}
}