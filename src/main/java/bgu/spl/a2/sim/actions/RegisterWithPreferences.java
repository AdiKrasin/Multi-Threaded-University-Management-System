package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.HashMap;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

public class RegisterWithPreferences extends Action<Boolean> {
	
	protected final String RegisterWithPreferencesActionName="Register With Preferences";
	protected ArrayList<String> courseList;
	protected ArrayList<Integer> gradeList;
	protected String studentId;
	/**
	 * 
	 * @param courseList -  The list of the courses this student would like to register to.
	 * @param gradeList - The list of the grades that will be assigned to this student in each course (in case there's a garde),
	 * or -1 (in case there's no grade)/
	 * @param studentId - The student's actor key.
	 */
	
	
	public RegisterWithPreferences(ArrayList<String> courseList, ArrayList<Integer> gradeList, String studentId) {
		this.actionName=RegisterWithPreferencesActionName;
		this.courseList=courseList;
		this.gradeList=gradeList;
		this.studentId=studentId;
	}
	/**
	 * 
	 * This start method tries to register the student to one course, starting from the beginning of the list.
	 */
	
	@Override
	protected void start() { // In case the student could not register to any of the courses in his course list: (Or, there are no courses in his course list from the beginning)
		if(courseList.size()==0) {
			this.complete(new Boolean(true));
			myPool.getActors().get(myId).addRecord(this.getActionName());
		}
		else {
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
					}	}
				if(prequisites) { // In case the student can register to this course:
			myRegStudents.add(studentId); // Adding the student to the course's registered students list
			myCoursePrivateState.SetRegistered(); // Changing the number of registered students within the course's privateState
			myCoursePrivateState.setAdditionalAvailableSpots(-1); // Changing the number of the available spots within the course's privateState
			ParticipateInCourseHelperGrades helper = new ParticipateInCourseHelperGrades(studentId, gradeList.get(0).intValue() , myId); // Creating the action which will change the studen's grades sheet
			ArrayList<Action<?>> actions = new ArrayList<>();
			actions.add(helper);
			this.then(actions, ()->{this.complete(new Boolean(true));} ); // subscribing for a callback from the new action and creating a callback which will complete the action
			this.sendMessage(helper, studentId, myStudentPrivateState); // Submitting the new action into the actorThreadPool
				}
				else { // In case the student can not register to this course: Recursively creating a register with helper action which will do the same thing for the next course in the preference list
					creatingNewAction();	
				}	}
		else {  // In case the student can not register to this course: Recursively creating a register with helper action which will do the same thing for the next course in the preference list
			creatingNewAction();
			}}}

	protected void creatingNewAction() {
		courseList.remove(0); // Removing the current course from the course list
		gradeList.remove(0); // Removing the grade of the current course from the grade list
		if(courseList.size()==0) {
			this.complete(new Boolean(true));
		}
		else {
		RegisterWithPreferences helper2 = new RegisterWithPreferences(courseList, gradeList, studentId); // Creating the helper action described above
		CoursePrivateState helperCoursePrivateState = (CoursePrivateState)myPool.getPrivateState(courseList.get(0));
		ArrayList<Action<?>> actions2 = new ArrayList<>();
		actions2.add(helper2);
		this.then(actions2, ()->{this.complete(new Boolean(true));} ); // subscribing for a callback from the new action and creating a callback which will complete the action
		this.sendMessage(helper2, courseList.get(0), helperCoursePrivateState); // Submitting the new action into the actorThreadPool
		}
	}
}