package com.brentcroft.gtd.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.camera.CameraObjectManager.FactorySpecification;
import com.brentcroft.gtd.camera.model.SwtGuiObjectConsultant;
import com.brentcroft.gtd.camera.model.SwtSnapshotGuiObject;
import com.brentcroft.gtd.camera.model.swt.ListGuiObject;
import com.brentcroft.gtd.camera.model.swt.ShellGuiObject;
import com.brentcroft.gtd.camera.model.swt.TableGuiObject;
import com.brentcroft.gtd.camera.model.swt.TreeGuiObject;
import com.brentcroft.gtd.camera.model.swt.WidgetGuiObject;

/**
 * Created by Alaric on 15/07/2017.
 */
public class SwtCameraObjectService extends FxCameraObjectService
{

	@Override
	protected < C, H extends GuiObject< C > > void addAdapters( List< FactorySpecification< C, H > > adapters, Properties properties )
	{
		super.addAdapters( adapters, properties );

		adapters.addAll( buildSWTAdapters( properties ) );
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< C > > List< FactorySpecification< C, H > > buildSWTAdapters( Properties properties )
	{
		List< FactorySpecification< C, H > > adapters = new ArrayList<>();

		CameraObjectManager gom = getManager();

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Widget.class, WidgetGuiObject.class, new SwtGuiObjectConsultant< Widget >( properties ) ) );

		//adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Item.class, ItemGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Control.class, ControlGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Browser.class, BrowserGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Composite.class, CompositeGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Label.class, LabelGuiObject.class ) );
		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Link.class, LinkGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Menu.class, MenuGuiObject.class ) );
		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( MenuItem.class, MenuItemGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Button.class, ButtonGuiObject.class ) );
		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Text.class, TextGuiObject.class ) );
		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Combo.class, ComboGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( org.eclipse.swt.widgets.List.class, ListGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Shell.class, ShellGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Table.class, TableGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Tree.class, TreeGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( ToolBar.class, ToolBarGuiObject.class ) );
		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( ToolItem.class, ToolItemGuiObject.class ) );

		//adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( TabFolder.class, TabFolderGuiObject.class ) );
		//adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( TabItem.class, TabItemGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( SwtSnapshot.class, SwtSnapshotGuiObject.class ) );

		return adapters;
	}

}
