package com.brentcroft.gtd.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.camera.CameraObjectManager.FactorySpecification;
import com.brentcroft.gtd.camera.model.SwtGuiObjectConsultant;
import com.brentcroft.gtd.camera.model.SwtSnapshotGuiObject;
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

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Table.class, TableGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Tree.class, TreeGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( SwtSnapshot.class, SwtSnapshotGuiObject.class ) );

		return adapters;
	}
}
