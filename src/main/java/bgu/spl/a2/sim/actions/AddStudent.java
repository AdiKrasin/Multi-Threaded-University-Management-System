package bgu.spl.a2.sim.actions;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

public class AddStudent extends Action<Boolean> {
	
	protected String studentId;
	protected final String addStudentActionName="Add Student";
	/**
	 * 
	 * @param studentId -  The student's actor key
	 */
	public AddStudent(String studentId) {
		this.actionName=addStudentActionName;
		this.studentId=studentId;
	}
	/**
	 * 
	 * The start method registers the student to the assigned department.
	 */
	@Override
	protected void start() {
		boolean exist=myPool.getActors().containsKey(studentId);
		if(!exist) { //In case this student does not exist:
			DepartmentPrivateState MyDepartmentPrivateState = (DepartmentPrivateState)myPool.getPrivateState(myId);
			MyDepartmentPrivateState.getStudentList().add(studentId); //Adding the student to the department's student list (within the department's privateState).
			StudentPrivateState myPrivateState=new StudentPrivateState();
			myPool.submit(null, studentId, myPrivateState); //Initializing the new actor - the student.
			this.complete(new Boolean(true)); //Resolving the action
		}
		else {
			this.complete(new Boolean(true)); //Resolving the action
		}
	}
}
