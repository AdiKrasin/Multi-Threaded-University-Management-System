package bgu.spl.a2;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * represents an actor thread pool - to understand what this class does please
 * refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class ActorThreadPool {
	
	protected Thread[] threads;
	protected Map<String,PrivateState> privateStateHashMap;
	protected Map<String,ConcurrentLinkedQueue<Action<?>>> QueueHashMap;
	protected Map<String,AtomicBoolean> booleanHashMap;
	protected VersionMonitor monitor;
	protected AtomicInteger numberOfEmptyQueues=new AtomicInteger(0);
	protected AtomicInteger numberOfBusyQueues=new AtomicInteger(0);
	protected Object toLock = new Integer(0);
	/**
	 * creates a {@link ActorThreadPool} which has nthreads. Note, threads
	 * should not get started until calling to the {@link #start()} method.
	 *
	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param nthreads
	 *            the number of threads that should be started by this thread
	 *            pool
	 */
	public ActorThreadPool(int nthreads) {
		privateStateHashMap=new ConcurrentHashMap<String,PrivateState>();
		QueueHashMap=new ConcurrentHashMap<String,ConcurrentLinkedQueue<Action<?>>>();
		booleanHashMap=new ConcurrentHashMap<String,AtomicBoolean>();
		monitor=new VersionMonitor();
		threads=new Thread [nthreads];
		for(int i=0; i<nthreads;i++) {
			threads[i]=new Thread (()->LocateActorAndInitializingAction());
		}
	}
	
	/**
	 *
	 *Helper function in order to locate the relevant actor and initialize the actions.
	 */
	public void LocateActorAndInitializingAction() {
		while(!(Thread.currentThread().isInterrupted())) { // Checking whether a thread has to fetch an action.
			while(Precondition()) {
				try {
					monitor.await(monitor.getVersion());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
			}
			if(!(Thread.currentThread().isInterrupted())) {
				for (Map.Entry<String,AtomicBoolean> entry : booleanHashMap.entrySet()) {
					String actorID=entry.getKey();
					ConcurrentLinkedQueue<Action<?>> queue=QueueHashMap.get(actorID);
					if(!queue.isEmpty() && entry.getValue().compareAndSet(false, true)) { // Preventing additional access to this queue.
						if(!(queue.isEmpty())) {
							synchronized (toLock) {
							numberOfBusyQueues.compareAndSet(numberOfBusyQueues.get(), (numberOfBusyQueues.get()+1)); // Increasing the number of busy queues until the action is handled.
							}
							Action<?> action=queue.poll();
							action.handle(ActorThreadPool.this, actorID, privateStateHashMap.get(actorID));
							synchronized (toLock) {
							numberOfBusyQueues.compareAndSet(numberOfBusyQueues.get(), (numberOfBusyQueues.get()-1)); // Decreasing the number of busy queues once the action was handled. 
							}
							if(queue.isEmpty()) { //Increasing the number of empty queues in case the queue is now empty due to this handle function. 
								synchronized (toLock) {
								numberOfEmptyQueues.compareAndSet(numberOfEmptyQueues.get(), (numberOfEmptyQueues.get()+1));
								}}
							else {
								monitor.inc(); // In case the queue is not empty, increasing the version in order that the action will be able to be fetched.
							}
						}
						entry.getValue().compareAndSet(true, false); // Providing the possibility to access this queue.
					}
					if(Thread.currentThread().isInterrupted()) // Preventing starvation/executing additional actions by this thread. 
						break;
				}
			}	
		}		
	}
	
	/**
	 * getter for actors
	 * @return actors
	 */
	public Map<String, PrivateState> getActors(){
		return privateStateHashMap;
	}
	
	/**
	 * getter for actor's private state
	 * @param actorId actor's id
	 * @return actor's private state
	 */
	public PrivateState getPrivateState(String actorId){
		return (privateStateHashMap.get(actorId));
	}


	/**
	 * submits an action into an actor to be executed by a thread belongs to
	 * this thread pool
	 *
	 * @param action
	 *            the action to execute
	 * @param actorId
	 *            corresponding actor's id
	 * @param actorState
	 *            actor's private state (actor's information)
	 */
	public void submit(Action<?> action, String actorId, PrivateState actorState) {
		Semaphore sem = new Semaphore(1);
		try {
			sem.acquire();

		} catch (InterruptedException e) {}
		try {
			ConcurrentLinkedQueue<Action<?>> queue=new ConcurrentLinkedQueue<Action<?>>();
			if(QueueHashMap.containsKey(actorId)) {
				queue=QueueHashMap.get(actorId);
				if(queue.isEmpty()) {// Since the action will be enqueued, decreasing the number of empty queues.
					if(action!=null) {
						synchronized (toLock) {
							numberOfEmptyQueues.compareAndSet(numberOfEmptyQueues.get(), (numberOfEmptyQueues.get()-1));
						}
					monitor.inc(); // Increasing the version of the monitor.
					}
				}
				if(action!=null) {
				queue.add(action); // Enqueues the action
				}
			}
			else { // In case the actorId's queue has not been created, creating it and the correspond booleanHashMap and privateStateHashMap: 
				QueueHashMap.put(actorId, new ConcurrentLinkedQueue<Action<?>>());
				privateStateHashMap.put(actorId, actorState);
				booleanHashMap.put(actorId, new AtomicBoolean(false));
				queue=QueueHashMap.get(actorId);
				if(action!=null) {
					queue.add(action); // Enqueues the action and increasing the version of the monitor. 
					monitor.inc();
				}
				else { // In case the action is null - therefore there's a new queue and it's empty
					synchronized (toLock) {
					numberOfEmptyQueues.compareAndSet(numberOfEmptyQueues.get(), (numberOfEmptyQueues.get()+1)); // Increasing the number of empty queues
					}}
			}
		}
		finally {
			sem.release();	
		}
	}

	/**
	 * closes the thread pool - this method interrupts all the threads and waits
	 * for them to stop - it is returns *only* when there are no live threads in
	 * the queue.
	 *
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException
	 *             if the thread that shut down the threads is interrupted
	 */
	public void shutdown() throws InterruptedException {
		for(int i=0;i<threads.length;i++) { //Interrupting all the threads.
			threads[i].interrupt();
		}
		return;
	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {
		for(int i=0;i<threads.length;i++) { //Initiating all the threads.
			threads[i].start(); 
		}
	}
	
	private boolean Precondition() {
		synchronized (toLock) {
		return (numberOfBusyQueues.get()+numberOfEmptyQueues.get()==QueueHashMap.size());
		}
	}
	
}
