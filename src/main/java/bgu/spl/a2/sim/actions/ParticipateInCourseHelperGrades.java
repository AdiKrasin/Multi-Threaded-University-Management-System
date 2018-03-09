package bgu.spl.a2.sim.actions;

import java.util.HashMap;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

/**
 *
 *This class represent an action which is being initialized only when the ParticipatingInCourse action is being handled.
 *The purpose of this action is to change the grades sheet which is located within the StudentPrivateState.
 *This action adds the courseName and the grade (if one is provided) to the sheet.
 */

public class ParticipateInCourseHelperGrades extends Action<Boolean> {
	
	protected String courseName="";
	protected int grade;
	protected String myId="";
	protected final String participateInCourseHelperGradesActionName="Participate In Course Helper Grades";
	
	public ParticipateInCourseHelperGrades(String studentId, int grade, String courseName) {
		this.actionName=participateInCourseHelperGradesActionName;
		this.courseName=courseName;
		this.myId=studentId;
		this.grade=grade;
	}
	
	@Override
	protected void start() {
		StudentPrivateState myPrivateState = (StudentPrivateState)this.myPool.getPrivateState(myId);
		HashMap<String, Integer> myGrades= myPrivateState.getGrades();
		if (!myGrades.containsKey(courseName)) {
			myGrades.put(courseName, grade);
		}
		this.complete(new Boolean(true));
	}
}
