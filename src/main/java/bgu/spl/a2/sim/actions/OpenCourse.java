 package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

public class OpenCourse extends Action<Boolean> {
	
	protected String courseName="";
	protected int space;
	protected ArrayList<String> prequisites;
	protected final String openCourseActionName="Open Course";
	
	/**
	 * 
	 * @param courseName -  The course's actor key
	 * @param space - The amount of spaces that should be assigned to this course.
	 * @param prequisites - The list of the prequisites courses to this course.
	 */
	
	public OpenCourse(String courseName, int space, ArrayList<String> prequisites) {
		this.actionName=openCourseActionName;
		this.courseName=courseName;
		this.space=space;
		this.prequisites=prequisites;
	}
	/**
	 * 
	 * This start method opens the required course.
	 */
	
	@Override
	protected void start() {
		boolean exist=myPool.getActors().containsKey(courseName);
		if(!exist) { //In case this course does not exist:
			DepartmentPrivateState myDepartmentPrivateState = (DepartmentPrivateState)myPool.getPrivateState(myId);
			myDepartmentPrivateState.getCourseList().add(courseName); //Adding the student to the department's course list (within the department's privateState).
			CoursePrivateState myPrivateState=new CoursePrivateState();
			myPrivateState.addCourse(prequisites, space); // Initializing the required properties for the course's PrivateState.
			myPool.submit(null, courseName, myPrivateState); //Initializing the new actor - the course.
			this.complete(new Boolean(true)); //Resolving the action
		}
		else {
			this.complete(new Boolean(true)); //Resolving the action
		}
	}
}
