package org.erikaredmark.util;

import java.beans.PropertyChangeListener;

/*************************************************************************************************************************
 * 
 * Interface for corresponding class {@link ObservableModel}. Java does not allow multiple inheritence, even though in this
 * case it would be very useful because the underlying boilerplate functionality is the same for every class. Nevertheless,
 * in the event a class already has an inheritence relationship, or the underlying functionality needs to be changed,
 * a class can implement this interface to achieve the same effects.
 * <p/>
 * Implementors of this class are typically the 'Model' part of the Observer pattern. The interface takes advantage of
 * Property Change Events via {@link PropertyChangeListener}.
 * 
 * @author TJS
 *
 ************************************************************************************************************************/
public interface IObservableModel {
    /*********************************************************************************************************************
     * 
     * To be used by clients: Allows them to register a property change listener
     * 
     * @param listener
     *      the listener implements {@link PropertyChangeListener}. Listener will receive all events from this observable
     * 
     ********************************************************************************************************************/
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /*********************************************************************************************************************
     * 
     * To be used by clients: Allows them to remove a property change listener
     * 
     * @param listener
     *      the listener to remove. This comparison is done via == reference equality, so the actual reference must be
     *      passed in
     * 
     ********************************************************************************************************************/
    public void removePropertyChangeListener(PropertyChangeListener listener);

    
    /*********************************************************************************************************************
     * 
     * Adds a listener for a specific property
     * 
     * @param propertyName
     *      the property for the listener to listen for
     * 
     * @param listener
     *      the listener that will receive events only for this property on this observable
     * 
     ********************************************************************************************************************/
    public void addPropertyChangeListener(String                 propertyName,
                                          PropertyChangeListener listener);

    /*********************************************************************************************************************
     * 
     * Removes a listener for a specific property. If the listener is added via 
     * {@link #addPropertyChangeListener(PropertyChangeListener)}, then it is impossible to do the inverse and tell it
     * what not to listen to; it must have been added from {@link #addPropertyChangeListener(String, PropertyChangeListener)}
     * 
     * @param propertyName
     *      the property to remove the listener from listening from
     * 
     * @param listener
     *      the listener to remove. This comparison is done via == reference equality, so the actual reference must be
     *      passed in
     * 
     ********************************************************************************************************************/
    public void removePropertyChangeListener(String                 propertyName,
                                             PropertyChangeListener listener);

    /*********************************************************************************************************************
     * 
     * To be used by subclass only (or in case of Decorator, container object). Alerts all listeners that the state of 
     * a variable has been changed.
     * <p/>
     * This method is <strong> not </strong> asynchronous. As such, when this called, code that runs directly after
     * can be guaranteed that all listeners have been notified. Whether or not the listener itself is asynchronous is not
     * under control of this method.
     * 
     * @param propertyName
     *      Name of the property, which roughly maps to the name of the variable.
     * 
     * @param oldValue
     *      Original value
     * 
     * @param newValue
     *      Newly set value
     * 
     ********************************************************************************************************************/
    public void firePropertyChange(String propertyName,
                                   Object oldVal,
                                   Object newVal);
    
    /*********************************************************************************************************************
     * 
     * Tells the observable to suspend all property change events. This is typically used to reload or re-initialise an
     * object where it would be undesirable to fire a property change for every change. Suspending the property events
     * does not prevent objects from registering as listeners. They just will not receive any events until the observable
     * is resumed.
     * <p/>
     * This method <strong>must</strong> be paired with {@link #resumePropertyChangeEvents()} or the observable will
     * become useless. It may <strong>not </strong> be called twice in a row.
     * 
     * @throws
     *      IllegalStateException
     *          if called again without first being closed by {@link #resumePropertyChangeEvents()}
     * 
     ********************************************************************************************************************/
    public void suspendPropertyChangeEvents();
    
    /*********************************************************************************************************************
     * 
     * Tells the obseverable to resume property change operations. This method may only be called once after the method
     * {@link #suspendPropertyChangeEvents()} is executed, and may not be called again unless closing out another
     * call to that method. Otherwise this will throw an exception.
     * 
     * @throws
     *      IllegalStateException
     *          if called when the observable is not suspended
     * 
     ********************************************************************************************************************/
    public void resumePropertyChangeEvents();
}
