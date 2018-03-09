package bgu.spl.a2.sim.actions;

import java.util.HashMap;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

/**
 *
 *This class represent an action which is being initialized only when the Unregister action is being handled.
 *The purpose of this action is to change the grades sheet which is located within the StudentPrivateState.
 *This action removes the courseName from the mentioned sheet.
 */

public class UnregisterHelper extends Action<Boolean> {
	
	protected String courseName="";
	protected String myId="";
	protected final String UnregisterHelperActionName="Unregister Helper";
	
	public UnregisterHelper(String studentId, String courseName) {
		this.actionName=UnregisterHelperActionName;
		this.courseName=courseName;
		this.myId=studentId;
	}
	
	@Override
	protected void start() {
		StudentPrivateState myPrivateState = (StudentPrivateState)this.myPool.getPrivateState(myId);
		HashMap<String, Integer> myGrades= myPrivateState.getGrades();
		if (myGrades.containsKey(courseName)) {
			myGrades.remove(courseName);
		}
		this.complete(new Boolean(true));
	}
}