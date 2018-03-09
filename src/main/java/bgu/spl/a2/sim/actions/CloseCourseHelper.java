package bgu.spl.a2.sim.actions;

import java.util.ArrayList;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

/**
 *
 *This class represent an action which is being initialized only when the CloseACourse action is being handled.
 *The purpose of this action is to un-register each student that is already registered to this course.
 *In addition, this action change the number of available spaces to the required value of -1.
 */

public class CloseCourseHelper extends Action<Boolean> {
	
	protected final String closeCourseHelperActionName="Close Course Helper";
	
	public CloseCourseHelper() {
		this.actionName=closeCourseHelperActionName;
	}
	
	@Override
	protected void start() {
		CoursePrivateState myPrivateState = (CoursePrivateState)this.myPool.getPrivateState(myId);
		myPrivateState.setAdditionalAvailableSpots(-(myPrivateState.getAvailableSpots()+1)); // Changing the number of available spots to be -1 as required
		int nActions = myPrivateState.getRegistered();
		ArrayList<String> myRegStudents=(ArrayList<String>)myPrivateState.getRegStudents();
		ArrayList<Action<?>> actions = new ArrayList<>();
		if (nActions!=0) { // In case there are students registered to this course:
		for (int i=0; i<nActions; i++) { // Creating n actions (n is the amount of students registered to this course), each action un-register a student
			Unregister helper = new Unregister(myRegStudents.get(i));
			actions.add(helper);
			}
		this.then(actions, ()->{ this.complete(new Boolean(true));} ); // subscribing for a callback from the new action and creating a callback which will complete the action
		for (int i=0; i<nActions; i++) { // Submitting all of the actions which will un-register the students
			this.sendMessage(actions.get(i), myId , myPrivateState); // Submitting the new action into the actorThreadPool
			}
		}
		else { // In case there are no students registered to this course:
		this.complete(new Boolean(true));
		}
	}
}
