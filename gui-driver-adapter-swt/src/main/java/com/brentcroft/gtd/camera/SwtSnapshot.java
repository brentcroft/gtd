package com.brentcroft.gtd.camera;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

import com.brentcroft.util.Waiter8;


/**
 * A synthetic top-level parent for all SWT, Swing, FX, HTML gui components.
 *
 * Created by Alaric on 14/07/2017.
 */
public class SwtSnapshot
{
	private Snapshot snapshot = new Snapshot();
	
    @SuppressWarnings("unchecked")
	public < T extends Object > List< T > getChildren()
    {
        List<T> children = snapshot.getChildren();

        getDisplay()
	        .syncExec(()->{
	        	children.addAll( (Collection<? extends T>) Arrays.asList( getDisplay().getShells() ) );
	        } );
        
        return children;
    }
        
    
    public static Display getDisplay()
    {
    	return Optional
    		.ofNullable(Display.getCurrent())
    		.orElse(Display.getDefault());
    }
    
    
    private static void processEvents(Event[] events, long delay )
    {
    	Arrays
    		.asList(events)
    		.stream()
    		.forEach(event->{
    		    event.display.post(event);
    	    	
    	    	Waiter8.delay(delay);
    		});
    }
    
    public static void click( Widget sourceWidget)
    {
    	long delay = 80;
    	processEvents(
    			new Event[] {
    					new Event() {
    						{
    					    	type = SWT.MouseDown;
    					    	widget = sourceWidget;
    					    	display = sourceWidget.getDisplay();
    					    	button = 1;
    						}
    					},
    					new Event() {
    						{
    					    	type = SWT.MouseUp;
    					    	widget = sourceWidget;
    					    	display = sourceWidget.getDisplay();
    					    	button = 1;
    						}
    					}
    					
    			}, delay );
    }
}
