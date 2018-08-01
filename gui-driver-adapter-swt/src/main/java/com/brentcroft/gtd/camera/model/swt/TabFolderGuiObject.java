package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.TabFolder;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 * 
 */
public class TabFolderGuiObject< T extends TabFolder > extends CompositeGuiObject< T > implements GuiObject.Index
{
	public TabFolderGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}
	

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		// the last action in the list is the prime action
		addIndexAction( element, options );
		addTabsAction( element, options );
	}	
	

	@Override
	public boolean hasChildren()
	{
		//return onDisplayThread( getObject(), go -> go.getItemCount() ) > 0;
		return true;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > items = Arrays
				.asList( onDisplayThread( getObject(), go -> go.getItems() ) )
				.stream()
				.map( child -> {
					return getManager().adapt( child, this );
				} )
				.collect( Collectors.toList() );

		return items;
	}

	@Override
	public Integer getItemCount()
	{
		return onDisplayThread( getObject(), go -> go.getItemCount() );
	}

	@Override
	public Integer getSelectedIndex()
	{
		return onDisplayThread( getObject(), go -> go.getSelectionIndex() );
	}

	@Override
	public void setSelectedIndex( int index )
	{
		onDisplayThread( getObject(), go -> {
			go.setSelection( index );
			return null;
		} );
	}
}
