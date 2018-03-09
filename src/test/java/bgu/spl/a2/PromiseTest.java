package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class PromiseTest {
	Promise<Integer> testedPromise;	
	
	@Before
	public void setUp() throws Exception {
		testedPromise = new Promise<>();
		
	}

	@After
	public void tearDown() throws Exception {
		testedPromise=null;
	}

	@Test
	/**
	 * Testing Get():
	 * The test makes sure the function returns the proper resolved value, if such exists. 
	 * If the promise has not been resolved yet, the test will verify that the function throws an IllegalStateException. 
	 */
	public void testGet() {
		boolean testException=false;
		try{testedPromise.get(); //Trying to use the get() method when the testedPromise is yet to be resolved.
		}
		catch (IllegalStateException e) { //In the case described above, the following exception should be thrown.
			testException=true;
		}
		if(testException==false) { //In case the exception was not thrown as expected:
			fail("The initial value is not as expected");
		}
		testedPromise.resolve(5);
		int x=testedPromise.get();
		assertEquals("Get() does not return the desired value",x, 5);
	}

	@Test
	/**
	 * Testing IsResolved():
	 * The test verifies that the initial state is false and once the promise has been resolved, 
	 * the test check that the resolve state was changed to true. 
	 */
	public void testIsResolved() {
		assertEquals("isResolved() should be initialized with false",false, testedPromise.isResolved());
		testedPromise.resolve(5);
		assertEquals("The promise should be resolved immediately",true, testedPromise.isResolved());
	}

	@Test
	/**
	 * Testing Resolve():
	 * The test verifies that once the promise has been resolved, the subscribed call backs were triggered, as well as the resolve state.
	 * In case the promise was already resolved, the test verifies that the proper exception was thrown.   
	 */
	public void testResolve() {
		Integer testInteger=new Integer (5);
		boolean testException=false;  
		testedPromise.subscribe(()->{ modifyInteger(testInteger);});
		testedPromise.resolve(testInteger);
		assertEquals("A callback was not triggered",testInteger.intValue(),6);
		assertEquals("The promise should be resolved immediately",true, testedPromise.isResolved());
		
		try{
			testedPromise.resolve(testInteger);
		}
		catch(IllegalStateException e){ // Checks that the exception was thrown in the right case.
			testException=true;
		}
		if(testException==false) {
			fail("This Promise should already been resolved");
		}
	}

	@Test
	/**
	 * Testing Subscribe():
	 * The test verifies that once the promise has been resolved, the subscribed call backs were triggered.
	 * In case the promise was already resolved, the test verifies that the call back was triggered immediately.    
	 */
	public void testSubscribe() {
		Integer testInteger=new Integer (10);
		testedPromise.subscribe(()->{ modifyInteger(testInteger);});
		testedPromise.resolve(testInteger);
		assertEquals("A callback was not triggered",testInteger.intValue(),11);
		testedPromise.subscribe(()->{ modifyInteger(testInteger);});
		assertEquals("The promise should already been resolved",testInteger.intValue(),12);
	}
	
	private void modifyInteger(Integer n) {
		n=n+1;
	}
}
