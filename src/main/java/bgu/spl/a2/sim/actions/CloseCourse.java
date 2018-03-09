package bgu.spl.a2.sim.actions;

import java.util.ArrayList;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

public class CloseCourse extends Action<Boolean> {
	
	protected String courseName;
	protected final String closeCourseActionName="Close Course";
	/**
	 * 
	 * @param courseName -  The course's actor key
	 */
	public CloseCourse(String courseName) {
		this.actionName=closeCourseActionName;
		this.courseName=courseName;
	}
	/**
	 * 
	 * The start method closes the required course.
	 */
	@Override
	protected void start() {
		DepartmentPrivateState myDepartmentPrivateState = (DepartmentPrivateState)myPool.getPrivateState(myId);
		CoursePrivateState myCoursePrivateState = (CoursePrivateState)myPool.getPrivateState(courseName);
		ArrayList<String> courseList = (ArrayList<String>)myDepartmentPrivateState.getCourseList();
		if(courseList.contains(courseName)) { // Making sure the course exist
			courseList.remove(courseName); // Removing the course from the course list
			CloseCourseHelper helper = new CloseCourseHelper(); // Creating the action which will un-register all of the students that are registered to this course and change the available spots number to the required value of -1
			ArrayList<Action<?>> actions = new ArrayList<>();
			actions.add(helper);
			this.then(actions, ()->{ this.complete(new Boolean(true));} ); // subscribing for a callback from the new action and creating a callback which will complete the action
			this.sendMessage(helper, courseName, myCoursePrivateState); // Submitting the new action into the actorThreadPool
		}
				else { // In case the course does not exist
					this.complete(new Boolean(true));
				}
		}
}
