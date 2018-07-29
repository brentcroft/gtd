package com.brentcroft.gtd.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.camera.CameraObjectManager.AdapterSpecification;
import com.brentcroft.gtd.camera.model.swt.ButtonGuiObject;
import com.brentcroft.gtd.camera.model.swt.ComboGuiObject;
import com.brentcroft.gtd.camera.model.swt.CompositeGuiObject;
import com.brentcroft.gtd.camera.model.swt.ControlGuiObject;
import com.brentcroft.gtd.camera.model.swt.ItemGuiObject;
import com.brentcroft.gtd.camera.model.swt.LabelGuiObject;
import com.brentcroft.gtd.camera.model.swt.ListGuiObject;
import com.brentcroft.gtd.camera.model.swt.MenuGuiObject;
import com.brentcroft.gtd.camera.model.swt.MenuItemGuiObject;
import com.brentcroft.gtd.camera.model.swt.SwtGuiObjectConsultant;
import com.brentcroft.gtd.camera.model.swt.SwtSnapshotGuiObject;
import com.brentcroft.gtd.camera.model.swt.TableGuiObject;
import com.brentcroft.gtd.camera.model.swt.TextGuiObject;
import com.brentcroft.gtd.camera.model.swt.TreeGuiObject;
import com.brentcroft.gtd.camera.model.swt.WidgetGuiObject;

/**
 * Created by Alaric on 15/07/2017.
 */
public class SwtCameraObjectService extends CameraObjectService
{

	@Override
	protected < C, H extends GuiObject< ? super C > > void addAdapters( List< AdapterSpecification< C, H > > adapters, Properties properties )
	{
		super.addAdapters( adapters, properties );

		adapters.addAll( buildSWTAdapters( properties ) );
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< AdapterSpecification< C, H > > buildSWTAdapters( Properties properties )
	{
		List< AdapterSpecification< C, H > > adapters = new ArrayList<>();

		CameraObjectManager gom = getManager();

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Widget.class, WidgetGuiObject.class, new SwtGuiObjectConsultant< Widget >( properties ) ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Item.class, ItemGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Control.class, ControlGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Composite.class, CompositeGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Label.class, LabelGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Menu.class, MenuGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( MenuItem.class, MenuItemGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Button.class, ButtonGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Text.class, TextGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Combo.class, ComboGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( org.eclipse.swt.widgets.List.class, ListGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Table.class, TableGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Tree.class, TreeGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( SwtSnapshot.class, SwtSnapshotGuiObject.class ) );

		return adapters;
	}

}
