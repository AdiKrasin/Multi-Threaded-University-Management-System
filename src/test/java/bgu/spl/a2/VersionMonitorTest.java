package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionMonitorTest {
	VersionMonitor testedVersionMonitor; 
	@Before
	public void setUp() throws Exception {
		testedVersionMonitor=new VersionMonitor();
	}

	@After
	public void tearDown() throws Exception {
		testedVersionMonitor=null;
	}

	@Test
	/**
	 * Testing getVersion():
	 * The test makes sure the function returns the current valid version number.
	 * After initialization, the version number is 0 and after increasing the version by 1, 
	 * the getVersion() provides the new version accordingly. 
	 */
	public void testGetVersion() {
	int version1=testedVersionMonitor.getVersion();
	assertEquals("Initial version is not 0",0, testedVersionMonitor.getVersion());
	testedVersionMonitor.inc();
	assertEquals("failure - ints are not equal" ,version1+1, testedVersionMonitor.getVersion());
	assertNotSame("should not be same numerical value", version1, testedVersionMonitor.getVersion());
	}

	@Test
	/**
	 * Testing inc():
	 * The test is first extracting the current version.
	 * then calls inc() to increment it by 1 and finally,
	 * checks the version number again to see that it was changed to version+1
	 */
	public void testInc() {
		int version2=testedVersionMonitor.getVersion();
		testedVersionMonitor.inc();
		assertEquals("failure - ints are not equal" ,version2+1, testedVersionMonitor.getVersion());
		assertNotSame("should not be same numerical value", version2, testedVersionMonitor.getVersion());
	}

	@Test
	/**
	 * Testing await():
	 * The test starts a thread that calls the testedVersionMonitor's await function, 
	 * making sure the thread is WAITING afterwards.
	 * Then it uses inc() to increment the version by 1, 
	 * making sure the thread was notified and not WAITING any longer. In addition, the test verifies
	 * that old version will be initiated immediately.
	 */
	public void testAwait() { 
		int version=testedVersionMonitor.getVersion();
		Integer number = 0;
		Runnable testTask = ()-> {
			try {
				testedVersionMonitor.await(version);
			} catch (InterruptedException e1) {	}
			modifyInteger(number);
		};
		Thread testThread = new Thread(testTask);
		testThread.start();
		assertEquals("Failure - ints are not equal" ,number.intValue(), 0);
		testedVersionMonitor.inc();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		assertEquals("Failure - ints are not equal" ,number.intValue(), 1);
		
		testTask = ()-> {
			try {
				testedVersionMonitor.await(version);
			} catch (InterruptedException e1) {	}
			modifyInteger(number);
		};
		testThread = new Thread(testTask);
		testThread.start();
		assertEquals("Old version was not initiated immediately" ,number.intValue(), 2);  
	}
	private void modifyInteger(Integer n) {
		n=n+1;
	}
}
