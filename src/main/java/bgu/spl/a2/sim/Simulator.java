/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import bgu.spl.a2.Action;
import bgu.spl.a2.ActorThreadPool;
import bgu.spl.a2.PrivateState;
import bgu.spl.a2.sim.actions.*;
import bgu.spl.a2.sim.privateStates.*;
import com.google.gson.*;
/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	
	public static ActorThreadPool actorThreadPool;
	private static JsonObject simulatorJsonData;
	protected static CountDownLatch phase1Counter;
	protected static CountDownLatch phase2Counter;
	protected static CountDownLatch phase3Counter;
	protected static Warehouse warehouse=Warehouse.getInstance();
	
	/**
	* Begin the simulation, should not be called before attachActorThreadPool()
	*/
    public static void start(){
    	JsonArray computersArray=getJsonArray(simulatorJsonData,"Computers");
    	AddingComputersFromJsonArray(computersArray); // Once extracting the parameters from the JsonArray, adding the computers to the WareHouse. 
    	JsonArray actionsPhase1=getJsonArray(simulatorJsonData,"Phase 1");
    	phase1Counter=new CountDownLatch(actionsPhase1.size());
    	Simulator.actorThreadPool.start();
    	SubmitActionsPerPhase(actionsPhase1, phase1Counter); // Submitting the actions on phase 1.
    	try { // Will wait until the actions from phase 1 have been completed.
    		phase1Counter.await();
		} catch (InterruptedException e) {}
    	JsonArray actionsPhase2=getJsonArray(simulatorJsonData,"Phase 2");
    	phase2Counter=new CountDownLatch(actionsPhase2.size());
    	SubmitActionsPerPhase(actionsPhase2, phase2Counter); // Submitting the actions on phase 2.
    	try { // Will wait until the actions from phase 2 have been completed.
    		phase2Counter.await();
		} catch (InterruptedException e) {}
    	JsonArray actionsPhase3=getJsonArray(simulatorJsonData,"Phase 3");
    	phase3Counter=new CountDownLatch(actionsPhase3.size());
    	SubmitActionsPerPhase(actionsPhase3, phase3Counter); // Submitting the actions on phase 3.
    	try { // Will wait until the actions from phase 3 have been completed.
    		phase3Counter.await();
		} catch (InterruptedException e) {}
    	return;
    }
    
    /**
     * Extracting the relevant parameters from the JSON in order to create the computers, creating the computers and adding them to the Warehouse.
     * @param computersArray -  A JsonArray to parse.
     */
    private static void AddingComputersFromJsonArray(JsonArray computersArray) {
    	 for(int i=0;i<computersArray.size();i++) { // Iterating the computers:
    		 JsonObject jsonComputer=computersArray.get(i).getAsJsonObject();
    		 String ComputerType=getStringValue(jsonComputer,"Type");
    		 long sigSuccess=getLongValue(jsonComputer,"Sig Success");
    		 long sigFail=getLongValue(jsonComputer,"Sig Fail");
    		 Computer computerToAdd=new Computer(ComputerType);
    		 computerToAdd.setSuccessSignature(sigSuccess);
    		 computerToAdd.setFailSignature(sigFail);
    		 warehouse.addComputer(computerToAdd); // Adding the computer to the warehouse. 
    	 }
    }
    /**
     * Extracting the relevant parameters from the JSON and submitting the actions within it to the thread pool.
     * @param actionsPhase - A JsonArray to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitActionsPerPhase(JsonArray actionsPhase, CountDownLatch latch) {
	    for(int i=0;i<actionsPhase.size();i++) { // Iterating the actions:
			JsonObject jsonAction=actionsPhase.get(i).getAsJsonObject(); // Extracting the JsonObject from the JsonArray.
			String actionName=getStringValue(jsonAction,"Action");
			switch(actionName) { // Will submit the proper action according to its name:
				case "Open Course": SubmitOpenCourseAction(jsonAction, latch);break;
				case "Add Student": SubmitAddStudentAction(jsonAction, latch);break;
				case "Participate In Course": SubmitParticipateInCourse(jsonAction, latch);break;
				case "Unregister": SubmitUnregisterAction(jsonAction, latch);break;
				case "Close Course": SubmitCloseCourseAction(jsonAction, latch);break;
				case "Add Spaces": SubmitAddSpacesAction (jsonAction, latch);break; 
				case "Administrative Check": SubmitAdministrativeCheck(jsonAction, latch);break;
				case "Register With Preferences": SubmitRegisterWithPreferences(jsonAction, latch);break;
			}
	    }
    }
    
    /**
     * Extracting the relevant parameters from the JSON and submitting the OpenCourse action to the thread pool.
     * @param jsonAction -  A JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitOpenCourseAction(JsonObject jsonAction, CountDownLatch latch) {
    	String departmentName=getStringValue(jsonAction,"Department");
		String courseName=getStringValue(jsonAction,"Course");
		int space=getIntValue(jsonAction,"Space");
		JsonArray prerequisitesJsonArray=getJsonArray(jsonAction,"Prerequisites");
		ArrayList<String> prerequisitesArray=JsonToStringArray(prerequisitesJsonArray);
		OpenCourse openCourseAction=new OpenCourse(courseName,space,prerequisitesArray);
		CountDownLatchHelper(openCourseAction, latch);
		Simulator.actorThreadPool.submit(openCourseAction, departmentName, new DepartmentPrivateState());
		
    }
    
    /**
     * Extracting the relevant parameters from the JSON and submitting the AddStudent action to the thread pool.
     * @param jsonAction -  A JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitAddStudentAction(JsonObject jsonAction, CountDownLatch latch) {
    	String departmentName=getStringValue(jsonAction,"Department");
		String studentID=getStringValue(jsonAction,"Student");
		AddStudent addStudentAction=new AddStudent(studentID);
		CountDownLatchHelper(addStudentAction, latch);
		Simulator.actorThreadPool.submit(addStudentAction, departmentName, new DepartmentPrivateState());		
    }
    /**
     * Extracting the relevant parameters from the JSON and submitting the ParticipatingInCourse action to the thread pool.
     * @param jsonAction -  A JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitParticipateInCourse(JsonObject jsonAction, CountDownLatch latch) {
		String courseName=getStringValue(jsonAction,"Course");
		String studentID=getStringValue(jsonAction,"Student");
		JsonArray gradesArray=getJsonArray(jsonAction,"Grade");
		int grade=GradeFromJsonArray(gradesArray);
		ParticipateInCourse participateInCourseAction=new ParticipateInCourse(studentID, grade);
		CountDownLatchHelper(participateInCourseAction, latch);
		Simulator.actorThreadPool.submit(participateInCourseAction, courseName, new CoursePrivateState());		
    }
    
    /**
     * Extracting the relevant parameters from the JSON and submitting the Unregister action to the thread pool.
     * @param jsonAction -  A JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitUnregisterAction(JsonObject jsonAction, CountDownLatch latch) {
		String courseName=getStringValue(jsonAction,"Course");
		String studentID=getStringValue(jsonAction,"Student");
		Unregister unregisterAction=new Unregister(studentID);
		CountDownLatchHelper(unregisterAction, latch);
		Simulator.actorThreadPool.submit(unregisterAction, courseName, new CoursePrivateState());		
    }
    
    /**
     * Extracting the relevant parameters from the JSON and submitting the CloseCourse action to the thread pool.
     * @param jsonAction -  A JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitCloseCourseAction(JsonObject jsonAction, CountDownLatch latch) {
    	String departmentName=getStringValue(jsonAction,"Department");
		String courseName=getStringValue(jsonAction,"Course");
		CloseCourse closeCourseAction=new CloseCourse(courseName);
		CountDownLatchHelper(closeCourseAction, latch);
		Simulator.actorThreadPool.submit(closeCourseAction, departmentName, new DepartmentPrivateState());		
    }
    
    /**
     * Extracting the relevant parameters from the JSON and submitting the AddSpaces action to the thread pool.
     * @param jsonAction -  a JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitAddSpacesAction (JsonObject jsonAction, CountDownLatch latch) {
		String courseName=getStringValue(jsonAction,"Course");
		int space=getIntValue(jsonAction,"Number");
		AddSpaces addSpacesAction=new AddSpaces(space);
		CountDownLatchHelper(addSpacesAction, latch);
		Simulator.actorThreadPool.submit(addSpacesAction, courseName, new CoursePrivateState());		
    }
    
    /**
     * Extracting the relevant parameters from the JSON and submitting the AdministrativeCheck action to the thread pool.
     * @param jsonAction -  a JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitAdministrativeCheck(JsonObject jsonAction, CountDownLatch latch) {
    	String departmentName=getStringValue(jsonAction,"Department");
		JsonArray studentsJsonArray=getJsonArray(jsonAction, "Students");
		ArrayList<String> studentsArray=JsonToStringArray(studentsJsonArray);
		JsonArray conditionsJsonArray=getJsonArray(jsonAction, "Conditions");
		ArrayList<String> conditionsArray=JsonToStringArray(conditionsJsonArray);
		String computerType=getStringValue(jsonAction,"Computer");
		Computer computer=warehouse.getComputer(computerType);
		AdministrativeCheck administrativeCheckAction=new AdministrativeCheck(studentsArray, conditionsArray,computer);// CHECK
		CountDownLatchHelper(administrativeCheckAction, latch);
		Simulator.actorThreadPool.submit(administrativeCheckAction, departmentName, new DepartmentPrivateState());		
    }
    
    /**
     * Extracting the relevant parameters from the JSON and submitting the RegisterWithPreferences action to the thread pool.
     * @param jsonAction -  A JsonObject to parse.
     * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
     */
    private static void SubmitRegisterWithPreferences(JsonObject jsonAction, CountDownLatch latch) {
		String studentID=getStringValue(jsonAction,"Student");
		JsonArray preferencesJsonArray=getJsonArray(jsonAction, "Preferences");
		ArrayList<String> preferencesArray=JsonToStringArray(preferencesJsonArray);
		JsonArray gradesJsonArray=getJsonArray(jsonAction, "Grade");
		ArrayList<Integer> gradesArray=JsonToIntegerArray(gradesJsonArray);
		RegisterWithPreferences registerWithPreferencesAction=new RegisterWithPreferences(preferencesArray,gradesArray, studentID);
		CountDownLatchHelper(registerWithPreferencesAction, latch);
		Simulator.actorThreadPool.submit(registerWithPreferencesAction, preferencesArray.get(0) , new CoursePrivateState());
    }
    
    
	/**
	* attach an ActorThreadPool to the Simulator, this ActorThreadPool will be used to run the simulation
	* 
	* @param myActorThreadPool - the ActorThreadPool which will be used by the simulator
	*/
	public static void attachActorThreadPool(ActorThreadPool myActorThreadPool) {
		actorThreadPool=myActorThreadPool;
	}
	
	/**
	* shut down the simulation
	* returns list of private states
	*/
	public static HashMap<String,PrivateState> end(){
		try {
			actorThreadPool.shutdown();
		} catch (InterruptedException e) {}
		HashMap<String,PrivateState> endHashMap=new HashMap<>();
		endHashMap.putAll(actorThreadPool.getActors());
		return endHashMap;
	}
	
	
	public static void main(String [] args){ 
		Simulator.simulatorJsonData=stringToJsonObject(args[0]);
		int nthreads = getIntValue(Simulator.simulatorJsonData, "threads");
		ActorThreadPool actorThreadPool=new ActorThreadPool(nthreads);
		Simulator.attachActorThreadPool(actorThreadPool);
		Simulator.start();
		HashMap<String,PrivateState> endHashMap=Simulator.end();
		Simulator.serializeHashMap(endHashMap);
	}
	
	
	/**
	 * 
	 * @param name - String name to parse
	 * @return Parsed JSON Object.
	 */
	private static JsonObject stringToJsonObject(String name) {
		JsonObject returnObject = null;
		try {
			FileReader jsonFileReader;
			jsonFileReader= new FileReader(name);
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(jsonFileReader);
			if (element.isJsonObject())
				returnObject = element.getAsJsonObject(); 
			return returnObject; 
		} 
		catch (FileNotFoundException e) { 
			return returnObject;}
	}
	
	/**
	 * @param jsonObject - An object to parse from.
	 * @param stringToSearch - String name to be extracted.
	 * @return JSON Array.
	 */
	private static JsonArray getJsonArray(JsonObject jsonObject, String stringToSearch) {
		JsonArray returnValue = jsonObject.get(stringToSearch).getAsJsonArray();
		return returnValue;
	}

	/**
	 * 
	 * @param jsonObject -  An object to parse from.
	 * @param stringToSearch -  String name to be extracted.
	 * @return The int value of the key stringToSearch.
	 */
	private static int getIntValue(JsonObject jsonObject, String stringToSearch) {
		int returnValue = jsonObject.get(stringToSearch).getAsInt();
		return returnValue;
	}
	
	/**
	 * 
	 * @param jsonObject -  An object to parse from.
	 * @param stringToSearch -  String name to be extracted.
	 * @return The String value of the key stringToSearch.
	 */
	private static String getStringValue(JsonObject jsonObject, String stringToSearch) {
		String returnValue = jsonObject.get(stringToSearch).getAsString();
		return returnValue;
	}
	
	/**
	 * 
	 * @param jsonObject -  An object to parse from.
	 * @param stringToSearch -  String name to be extracted.
	 * @return The long value of the key stringToSearch.
	 */
	private static long getLongValue(JsonObject jsonObject, String stringToSearch) {
		long returnValue = jsonObject.get(stringToSearch).getAsLong();
		return returnValue;
	}
	
	/**
	 * 
	 * @param jsonArr -  A JsonArray to parse from.
	 * @return ArrayList<String> of the values within the JsonArr. 
	 */
	private static ArrayList<String> JsonToStringArray(JsonArray jsonArr){
		ArrayList<String> arr=new ArrayList<>(jsonArr.size());
		for(int j=0;j<jsonArr.size();j++) {
			arr.add(j, jsonArr.get(j).getAsString());
		}
		return arr;
	}
	
	/**
	 * 
	 * @param jsonArr -  A JsonArray to parse from.
	 * @return ArrayList<Integer> of the values within the JsonArr. 
	 */
	private static ArrayList<Integer> JsonToIntegerArray(JsonArray jsonArr){
		ArrayList<Integer> arr=new ArrayList<>(jsonArr.size());
		for(int j=0;j<jsonArr.size();j++) {
			String stringGrade=jsonArr.get(j).getAsString(); // Verifying that the grade is not -, in that case returning -1.   
			int grade;
			if(stringGrade.equals("-")) {
				grade=-1;
			}
			else {
				grade=jsonArr.get(j).getAsInt();
			}
			arr.add(j, new Integer(grade));
		}
		return arr;
	}
	
	
	/**
	 * @param gradesArray - A JsonArray to parse from.
	* @return If the grade is provided, returning the grade as int, Otherwise, grade is (-), thus returning (-1).
	*/			
	private static int GradeFromJsonArray(JsonArray gradesArray) {
		if(gradesArray.getAsString().equals("-")) {
			return (-1);
		}
		else {
			return gradesArray.getAsInt();
		}
    }
	
	/**
	 * Serializes the HashMap and generates a Java Serialized file "result.ser"
	 * @param endHashMap - The HashMap which will be serialized.
	 */
	private static void serializeHashMap(HashMap<String,PrivateState> endHashMap) {
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream("result.ser");
			ObjectOutputStream objectOutputStream;
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(endHashMap);
			objectOutputStream.close();		
		} 
		catch (IOException e) {}
	}
	
	
	/**
	 * Adding a call to the action's promise in order to reduce the CountDownLatch of the phase once the action is completed. 
	 * @param action - The action which the subscription of the countDown will be added to its promise. 
	 * @param latch - A countDownLatch which is used in order to count the completed actions per phase.
	 */
	private static void CountDownLatchHelper(Action<?> action, CountDownLatch latch) {
		action.getResult().subscribe(()->{latch.countDown();});
	}	
}
