package org.erikaredmark.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/*********************************************************************************************************************
 * 
 * Generic class designed to be subclassed by clients. This class will impose the Java bean 'property change' model onto
 * the child object thus allowing it to be easily used with JFace data binding. Any java objects, such as the
 * <code>ProjectTaskManager</code>, whose state is to be represented visually to the user should subclass this class.
 * <p/>
 * The class is not abstract, in the event the client would rather use the Decorator pattern instead of subclassing.
 * @author TJS
 * <p/>
 * The interface is intended to make it easy to clarify to clients that an object responds to the property change
 * protocol, although generally it is recommended to subclass or decorate <code>ObersvableModel</code> as it implements
 * the core features.
 *
 ********************************************************************************************************************/
public class ObservableModel implements IObservableModel {
    private PropertyChangeSupport changeSupport = 
        new PropertyChangeSupport(this);
    
    boolean suspended = false;

    /*********************************************************************************************************************
     * 
     * To be used by clients: Allows them to register a property change listener
     * 
     * {@inheritDoc}
     * 
     * @param listener
     * 
     ********************************************************************************************************************/
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        
        changeSupport.addPropertyChangeListener(listener);
        
    }

    /*********************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     * To be used by clients: Allows them to remove a property change listener
     * 
     * @param listener
     * 
     ********************************************************************************************************************/
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        
        changeSupport.removePropertyChangeListener(listener);
        
    }

    /*********************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ********************************************************************************************************************/
    @Override
    public void addPropertyChangeListener(String                 propertyName,
                                          PropertyChangeListener listener) {
    
        changeSupport.addPropertyChangeListener(propertyName, listener);
        
    }

    /*********************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ********************************************************************************************************************/
    @Override
    public void removePropertyChangeListener(String                 propertyName,
                                             PropertyChangeListener listener) {
        
        changeSupport.removePropertyChangeListener(propertyName, listener);
        
    }

    
    /*********************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ********************************************************************************************************************/
    @Override
    public void firePropertyChange(String propertyName, 
                                   Object oldValue,
                                   Object newValue) {
        
        if (suspended == true) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
      
    }

    /*********************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ********************************************************************************************************************/
    @Override
    public void suspendPropertyChangeEvents() {
        if (suspended == true) {
            throw new IllegalStateException("suspend already called");
        }
        
        this.suspended = true;
        
    }
    
    /*********************************************************************************************************************
     * 
     * {@inheritDoc}
     * 
     ********************************************************************************************************************/
    @Override
    public void resumePropertyChangeEvents() {
        if (suspended == false) {
            throw new IllegalStateException("observable not suspended");
        }
        
        this.suspended = false;
        
    }
}
