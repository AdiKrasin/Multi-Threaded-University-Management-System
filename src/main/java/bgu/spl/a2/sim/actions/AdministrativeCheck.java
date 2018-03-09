package bgu.spl.a2.sim.actions;

import java.util.ArrayList;

import bgu.spl.a2.Action;
import bgu.spl.a2.Promise;
import bgu.spl.a2.sim.Computer;
import bgu.spl.a2.sim.privateStates.*;

public class AdministrativeCheck extends Action<Boolean> {
	
	protected ArrayList<String> studentList;
	protected ArrayList<String> conditionList;
	protected Computer computer; 
	protected final String administrativeCheckActionName="Administrative Check";
	/**
	 * 
	 * @param studentList -  The list of the students the department would check.
	 * @param conditionList - The list of the courses the department would check.
	 * @param computer - The computer in which the department would like to perform the checks on.
	 */
	public AdministrativeCheck(ArrayList<String> studentList, ArrayList<String> conditionList, Computer computer) {
		this.actionName=administrativeCheckActionName;
		this.studentList=studentList;
		this.conditionList=conditionList;
		this.computer=computer;
	}
	/**
	 * 
	 * The start method perform the administrative check.
	 */
	@Override
	protected void start() {
		Promise<Computer> myPromise = computer.getSuspendingMutex().down(); // The department tries to use the computer:
		if (myPromise.isResolved()) { // In case the department succeeds to use to use the computer:
			for (int i=0; i<studentList.size(); i++) { // The department check and signs for each student from the student list:
				StudentPrivateState myStudentPrivateState = (StudentPrivateState) myPool.getPrivateState(studentList.get(i));
				long signature = computer.checkAndSign(conditionList, myStudentPrivateState.getGrades());
				myStudentPrivateState.SetSignature(signature);// Changing the signature according to the check and sign.
			}
			computer.getSuspendingMutex().up(); // Releasing the used computer.
			this.complete(new Boolean(true)); // Resolving the action.
		}
		else { // In case the department did not succeed to use the required computer, subscribing for a callback which will enqueue the action back to it's queue.
			myPromise.subscribe(()->{myPool.submit(AdministrativeCheck.this, myId, myState);});
			}
	}
}