package bgu.spl.a2.sim.actions;

import java.util.ArrayList;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

public class Unregister extends Action<Boolean> {
	
	protected String studentId;
	protected final String UnregisterActionName="Unregister";
	/**
	 * 
	 * @param studentId -  The student's actor key
	 */
	
	public Unregister(String studentId) {
		this.actionName=UnregisterActionName;
		this.studentId=studentId;
	}
	/**
	 * 
	 * This start method removes the required student from the assigned course.
	 */
	
	@Override
	protected void start() {
		CoursePrivateState myCoursePrivateState = (CoursePrivateState)myPool.getPrivateState(myId);
		StudentPrivateState myStudentPrivateState = (StudentPrivateState)myPool.getPrivateState(studentId);
		ArrayList<String> myRegStudents=(ArrayList<String>)myCoursePrivateState.getRegStudents();
		if(myRegStudents.contains(studentId)) { // Making sure the student is already registered to the course
			myRegStudents.remove(studentId); // Removing the student from the course's registered students list
			myCoursePrivateState.SetRegistered(); // Changing the number of registered students within the course's privateState
			if(myCoursePrivateState.getAvailableSpots()!=(-1)) { //In case the course is not being closed:
			myCoursePrivateState.setAdditionalAvailableSpots(1); // Changing the number of the available spots within the course's privateState
			}
			UnregisterHelper helper = new UnregisterHelper(studentId, myId); // Creating the action which will change the student's grades sheet
			ArrayList<Action<?>> actions = new ArrayList<>();
			actions.add(helper);
			this.then(actions, ()->{this.complete(new Boolean(true));}); // subscribing for a callback from the new action. Creating a callback which will complete the action
			this.sendMessage(helper, studentId, myStudentPrivateState); // Submitting the new action into the actorThreadPool
		}
				else { // In case the student can not un-register to this course: (because he is not registered to it)
					this.complete(new Boolean(true));
				}
		}
}
