package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.model.GuiObjectFactory;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class TableGuiObject< T extends org.eclipse.swt.widgets.Table > extends WidgetGuiObject< T >
		implements GuiObject.Table
{
	public TableGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	public TableGuiObject(
			T go, Gob parent,
			GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager,
			Map< String, Object > methods,
			List< AttrSpec< T > > attr )
	{
		super( go, parent, guiObjectConsultant, objectManager, methods, attr );
	}

	@SuppressWarnings( "unchecked" )
	public static < T extends Control > GuiObjectFactory< T > getSpecialist( T go, Gob parent, GuiObjectConsultant< T > consultant,
			CameraObjectManager objectManager )
	{
		GuiObjectFactory< T > specialist = null;

		if ( consultant.specialise( go ) )
		{
			specialist = objectManager.newSoftFactory(
					( Class< T > ) go.getClass(),
					TableGuiObject.class,
					consultant,
					wrapAvailableFunctions(
							WidgetGuiObject.getAvailableFunctions( go, ( haplotype, availableFunctions ) -> {
								installFunctions( haplotype, parent, availableFunctions );
								return null;
							} )
					),
					wrapAvailableAttributes( Attributes.getAttributes( go ) )
			);
		}

		return specialist;
	}
	
	@Override
	public boolean hasChildren()
	{
		return false;
	}	

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		appendTableModel(
				element,
				options,
				getRowCount(),
				getColumnCount(),
				Arrays
						.asList( onDisplayThread( getObject(), go -> go.getColumns() ) )
						.stream()
						.map( column -> onDisplayThread( ( TableColumn ) column, go -> column.getText() ) )
						.toArray( String[]::new ),
				onDisplayThread( getObject(), go -> go.getItems() ),
				( item, index ) -> onDisplayThread( ( TableItem ) item, go -> go.getText( index ) ) );

		addTableAction( element, options );
	}

	@Override
	public Integer getColumnCount()
	{
		return onDisplayThread( getObject(), go -> go.getColumnCount() );
	}

	@Override
	public Integer getRowCount()
	{
		return onDisplayThread( getObject(), go -> go.getItemCount() );
	}

	@Override
	public Integer getSelectionIndex()
	{
		return onDisplayThread( getObject(), go -> go.getSelectionIndex() );
	}

	@Override
	public void selectRow( int row )
	{
		onDisplayThread( getObject(), go -> {
			go.select( row );
			return null;
		} );
	}

	@Override
	public void selectColumn( final int column )
	{
		onDisplayThread( getObject(), go -> {
			go.select( column );
			return null;
		} );
	}

	@Override
	public void selectCell( int row, int column )
	{

	}
}
