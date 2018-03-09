package bgu.spl.a2.sim.actions;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.*;

public class AddSpaces extends Action<Boolean> {
	
	protected int additionalSpace;
	protected final String addSpacesActionName="Add Spaces";
	
	/**
	 * 
	 * @param additionalSpace -  the amount of additional spaces to be added
	 */
	
	public AddSpaces(int additionalSpace) {
		this.actionName=addSpacesActionName;
		this.additionalSpace=additionalSpace;
	}
	/**
	 * 
	 * The start method adds the additional spaces to the course's privateState.
	 */
	@Override
	protected void start() {
			CoursePrivateState myCoursePrivateState = (CoursePrivateState)myPool.getPrivateState(myId);
			if(myCoursePrivateState.getAvailableSpots()!= (-1)) { // In case the course is not closed:
			myCoursePrivateState.setAdditionalAvailableSpots(additionalSpace); //Adding the additional empty spots.
			}
			this.complete(new Boolean(true)); //Resolving the action
	}
}