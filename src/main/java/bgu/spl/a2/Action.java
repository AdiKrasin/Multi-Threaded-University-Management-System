package bgu.spl.a2;

import java.util.Collection;
import java.util.concurrent.atomic.*;

/**
 * an abstract class that represents an action that may be executed using the
 * {@link ActorThreadPool}
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 * @param <R> the action result type
 */
public abstract class Action<R> {
	
	protected callback callback= null;
	protected String actionName= "";
	protected Promise<R> promise= new Promise<R>();
	protected AtomicInteger counter = new AtomicInteger(0); 
	protected ActorThreadPool myPool;
	protected String myId;
	protected PrivateState myState;

	/**
     * start handling the action - note that this method is protected, a thread
     * cannot call it directly.
     */
    protected abstract void start();
    

    /**
    *
    * start/continue handling the action
    *
    * this method should be called in order to start this action
    * or continue its execution in the case where it has been already started.
    *
    * IMPORTANT: this method is package protected, i.e., only classes inside
    * the same package can access it - you should *not* change it to
    * public/private/protected
    *
    */
   /*package*/ final void handle(ActorThreadPool pool, String actorId, PrivateState actorState) {
	   myPool=pool;
	   myId=actorId;
	   myState=actorState;
	   if(callback==null) { // In case this action is being handled for the first time:
		   myPool.getActors().get(actorId).addRecord(this.getActionName());
		   start();
	   }
	   else { // In case this action was already handled once:
		   callback.call();
	   }
   } 
    
   
    /**
     * add a callback to be executed once *all* the given actions results are
     * resolved
     * 
     * Implementors note: make sure that the callback is running only once when
     * all the given actions completed.
     *
     * @param actions
     * @param callback the callback to execute once all the results are resolved
     */
    protected final void then(Collection<? extends Action<?>> actions, callback callback) {
    	counter.set(actions.size()); //Initializing the atomic counter
    	for(Action<?> action:actions) { //Subscribing for the promises of each action this action is relying on:
    	action.promise.subscribe(()-> { //Once the promise is resolved the action shall decrease the atomic counter by one.
    		counter.compareAndSet(counter.get(), counter.get()-1);
    			if(counter.compareAndSet(0, -1)) { // In case all of the actions this action was relying on are resolved, the "last" action shall enqueue the action back to it's original queue as requested.
    				this.callback=callback;
    				myPool.submit(Action.this, myId, myState);
    			}
    		});
    	}
    }

    /**
     * resolve the internal result - should be called by the action derivative
     * once it is done.
     *
     * @param result - the action calculated result
     */
    protected final void complete(R result) {
       	promise.resolve(result);
    }
    
    /**
     * @return action's promise (result)
     */
    public final Promise<R> getResult() {
    	return promise;
    }
    
    /**
     * send an action to an other actor
     * 
     * @param action
     * 				the action
     * @param actorId
     * 				actor's id
     * @param actorState
	 * 				actor's private state (actor's information)
	 *    
     * @return promise that will hold the result of the sent action
     */
	public Promise<?> sendMessage(Action<?> action, String actorId, PrivateState actorState){
		myPool.submit(action, actorId, actorState);
		return action.promise;
	}
	
	/**
	 * set action's name
	 * @param actionName
	 */
	public void setActionName(String actionName){
       this.actionName=actionName;
	}
	
	/**
	 * @return action's name
	 */
	public String getActionName(){
		return actionName;
	}
}