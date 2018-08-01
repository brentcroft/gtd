package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TabItem;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 * 
 */
public class TabItemGuiObject< T extends TabItem > extends ItemGuiObject< T > implements GuiObject.Click
{
	public TabItemGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		// the last action in the list is the prime action
		addClickAction( element, options );
		addTabAction( element, options );
	}

	@Override
	public boolean hasChildren()
	{
		return true;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > children = super.loadChildren();

		children.add( getManager().adapt( onDisplayThread( getObject(), go -> go.getControl() ), this ) );

		return children;
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

	enum Attr implements AttrSpec< TabItem >
	{
		INDEX( "index", go -> go.getParent().indexOf( go ) ),
		TEXT( "text", go -> DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getText() ) ),
		TOOLTIP( "tooltip", go -> DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getToolTipText() ) ),
		;

		final String n;
		final Function< TabItem, Object > f;

		Attr( String name, Function< TabItem, Object > f )
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
		public String getAttribute( TabItem go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}

	@Override
	public int[] getLocation()
	{
		Rectangle p = onDisplayThread( getObject(), go -> go.getBounds() );
		return new int[] { p.x, p.y, p.width, p.height };
	}

	@Override
	public int[] getObjectLocationOnScreen()
	{
		int[] r = getLocation();

		int centreX = r[ 0 ] + r[ 2 ] / 2;
		int centreY = r[ 1 ] + r[ 3 ] / 2;

		Point p = onDisplayThread( getObject(), go -> go.getParent().toDisplay( centreX, centreY ) );

		return new int[] { p.x, p.y };
	}

	@Override
	public void click()
	{
		click( 1 );
	}

	@Override
	public void rightClick()
	{
		click( 3 );
	}

	public void click( int mouseButton )
	{
		long delay = 80;

		Display parentDisplay = onDisplayThread( getObject(), go -> go.getParent().getDisplay() );

		int[] locOnScreen = getObjectLocationOnScreen();

		final Point[] pt = { new Point( locOnScreen[ 0 ], locOnScreen[ 1 ] ) };

		processEvents(
				new Event[] {
						new Event()
						{
							{
								type = SWT.MouseMove;
								display = parentDisplay;
								x = pt[ 0 ].x;
								y = pt[ 0 ].y;
							}
						},
						new Event()
						{
							{
								type = SWT.MouseDown;
								display = parentDisplay;
								button = mouseButton;
							}
						},
						new Event()
						{
							{
								type = SWT.MouseUp;
								display = parentDisplay;
								button = mouseButton;
							}
						}

				}, delay );
	}

}
