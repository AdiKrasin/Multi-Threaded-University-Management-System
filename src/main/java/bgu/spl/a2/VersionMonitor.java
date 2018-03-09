package bgu.spl.a2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 *
 * you can also increment the version number by one using the {@link #inc()}
 * method.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class VersionMonitor {
	
	protected AtomicInteger version=new AtomicInteger (0); 
	
	/**
	 *
	 * @return the current version of the VersionMonitor.
	 */
    public int getVersion() {
        return version.get();
    }
    
    /**
	 * Increasing the version of the VersionMonitor by 1. 
	 */
    public void inc() {
        version.compareAndSet(this.getVersion(), (this.getVersion()+1)); // Comparing the current version and increasing it by one.
        synchronized (this) { // Implementing notifyAll in order to awake the thread.
        	notifyAll();
        }
    }

    /**
	 *
	 * Causing the current thread to await.
	 */
    public void await(int version) throws InterruptedException {
       while(this.getVersion()<=version) { // The thread will wait until the current version has been increased.
    	   try {
    	   synchronized (this) {  
    	  wait();
    	}
    		   }
    	   catch (InterruptedException e) {
    		   throw new InterruptedException();
    		   }
    	   }
       } 
    
}
