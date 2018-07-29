package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Created by Alaric on 14/07/2017.
 */
public class TableGuiObject< T extends org.eclipse.swt.widgets.Table > extends CompositeGuiObject< T >
		implements GuiObject.Table
{
	public TableGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
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

	@SuppressWarnings( "unchecked" )
	@Override
	public List< AttrSpec< T > > loadAttrSpec()
	{
		if ( attrSpec == null )
		{
			attrSpec = super.loadAttrSpec();
			attrSpec.addAll( Arrays.asList( ( AttrSpec< T >[] ) Attr.values() ) );
		}

		return attrSpec;
	}

	static enum Attr implements AttrSpec< org.eclipse.swt.widgets.Table >
	{
		SIZE( "size", go -> "" + go.getItemCount() ),
		SELECTED_INDEX( "selected-index", go -> "" + go.getSelectionIndex() );

		final String n;
		final Function< org.eclipse.swt.widgets.Table, Object > f;

		Attr( String name, Function< org.eclipse.swt.widgets.Table, Object > f )
		{
			this.n = name;
			this.f = f;
		}

		@Override
		public String getName()
		{
			return n;
		}

		@Override
		public String getAttribute( org.eclipse.swt.widgets.Table go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
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
