package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.HashMap;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

public class ParticipateInCourse extends Action<Boolean> {
	
	protected String studentId;
	protected final String participateInCourseActionName="Participate In Course";
	protected int grade;
	
	/**
	 * 
	 * @param studentId -  The student's actor key
	 * @param grade - The grade that is assigned for this student, for this particular course (in case there's one) or -1 (in case
	 * no grade is assigned).
	 */
	
	
	public ParticipateInCourse(String studentId, int grade) {
		this.actionName=participateInCourseActionName;
		this.studentId=studentId;
		this.grade=grade;
	}
	/**
	 * 
	 * This start method registers the required student to the assigned course.
	 */
	
	@Override
	protected void start() {
		CoursePrivateState myCoursePrivateState = (CoursePrivateState)myPool.getPrivateState(myId);
		StudentPrivateState myStudentPrivateState = (StudentPrivateState)myPool.getPrivateState(studentId);
		ArrayList<String> myRegStudents=(ArrayList<String>)myCoursePrivateState.getRegStudents();
		ArrayList<String> myPrequisites=(ArrayList<String>)myCoursePrivateState.getPrequisites();
		HashMap<String, Integer> myGrades= myStudentPrivateState.getGrades();
		if((!myRegStudents.contains(studentId)) && myCoursePrivateState.getAvailableSpots()>0) { // Making sure the student is not already registered to the course, and that there are available spots for the course
				boolean prequisites=true;
				for(int i=0; i<myPrequisites.size() && prequisites; i++) { //Checking that the student was registered to all of the courses he should be registered to prior his registration to this one
					if(!myGrades.containsKey(myPrequisites.get(i))) {
						prequisites=false;	
					}
				}
				if(prequisites) { // In case the student can register to this course:
			myRegStudents.add(studentId); // Adding the student to the course's registered students list
			myCoursePrivateState.SetRegistered(); // Changing the number of registered students within the course's privateState
			myCoursePrivateState.setAdditionalAvailableSpots(-1); // Changing the number of the available spots within the course's privateState
			ParticipateInCourseHelperGrades helper = new ParticipateInCourseHelperGrades(studentId, grade, myId); // Creating the action which will change the studen's grades sheet
			ArrayList<Action<?>> actions = new ArrayList<>();
			actions.add(helper);
			this.then(actions, ()->{this.complete(new Boolean(true));} ); // subscribing for a callback from the new action. Creating a callback which will complete the action
			this.sendMessage(helper, studentId, myStudentPrivateState); // Submitting the new action into the actorThreadPool
				}
				else { // In case the student can not register to this course: 
					this.complete(new Boolean(true));
				}
		}
		else { // In case the student can not register to this course: 
			this.complete(new Boolean(true));
		}}}