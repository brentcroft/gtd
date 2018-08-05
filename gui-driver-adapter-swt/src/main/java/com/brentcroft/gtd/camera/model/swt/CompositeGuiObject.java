package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectAdapter;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class CompositeGuiObject< T extends Composite > extends ControlGuiObject< T >
{
	private static Logger logger = Logger.getLogger( CompositeGuiObject.class );

	public CompositeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return onDisplayThread( getObject(), go -> go.getChildren().length > 0 );
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		return Arrays
				.asList( onDisplayThread( getObject(), go -> go.getChildren() ) )
				.stream()
				.map( child -> getManager().adapt( child, this ) )
				.collect( Collectors.toList() );
	}

	/**
	 * This is called by reflection by the CameraObjectManager allowing an
	 * alternative adapter to be provided.
	 * <p/>
	 * 
	 * 
	 * @param go
	 * @param parent
	 * @param consultant
	 * @param objectManager
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static < T extends Composite > GuiObjectAdapter< T > getSpecialist( T go, Gob parent, GuiObjectConsultant< T > consultant,
			CameraObjectManager objectManager )
	{
		Map< SpecialistMethod, Object > chosenOne = GeneralCompositeGuiObject.Functions.getSpecialistFunctions( go );

		return objectManager.newAdapter( ( Class< T > ) go.getClass(), GeneralCompositeGuiObject.class, consultant, chosenOne );
	}
}

