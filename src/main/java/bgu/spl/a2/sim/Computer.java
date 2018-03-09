package bgu.spl.a2.sim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Computer {

	String computerType;
	long failSig;
	long successSig;
	protected SuspendingMutex suspendingMutex;
	
	/**
	 * Constructor.
	 *   
	 * @param computerType - String computer type. 
	 */
	public Computer(String computerType) {
		this.computerType = computerType;
		this.suspendingMutex=new SuspendingMutex(this);
	}
	
	/**
	 * this method checks if the courses' grades fulfill the conditions
	 * @param courses
	 * 							courses that should be pass
	 * @param coursesGrades
	 * 							courses' grade
	 * @return a signature if couersesGrades grades meet the conditions
	 */
	public long checkAndSign(List<String> courses, Map<String, Integer> coursesGrades){
		HashMap<String, Integer> coursesGrades1=(HashMap<String, Integer>) coursesGrades;
		for(int i=0;i<courses.size();i++) {
			String courseName=courses.get(i);
			Integer grade=coursesGrades1.get(courseName);
			if(grade==null || grade<56) { // Checking each grade and returning failSig in case one of the grades is below 56 or does not exist.
				return failSig;
			}
		}
		return successSig;
	}
	
	/**
	 * Sets the fail signature according to the given value. 
	 * @param failSig - long fail signature
	 */
	public void setFailSignature(long failSig) {
		this.failSig=failSig;
	}
	
	/**
	 * Sets the success signature according to the given value. 
	 * @param successSig - long success signature
	 */
	public void setSuccessSignature(long successSig) {
		this.successSig=successSig;
	}
	
	/**
	 * @return The suspendingMutex of the computer.
	 */
	public SuspendingMutex getSuspendingMutex() {
		return suspendingMutex;
	}
	
	/**
	 * @return The type of the computer. 
	 */
	public String getComputerType() {
		return computerType;
	}	
	
}
