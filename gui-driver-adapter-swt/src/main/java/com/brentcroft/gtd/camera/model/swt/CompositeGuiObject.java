package com.brentcroft.gtd.camera.model.swt;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectFactory;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.model.SpecialistGuiObject;
import com.brentcroft.gtd.adapter.model.SpecialistGuiObject.SpecialistFunctions;
import com.brentcroft.gtd.adapter.utils.ReflectionUtils;
import com.brentcroft.gtd.adapter.utils.Specialist;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.TriFunction;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class CompositeGuiObject< T extends Composite > extends ControlGuiObject< T > implements GuiObject.Indexed
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
		return getChildren().isEmpty();

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

	@Override
	public Integer getItemCount()
	{
		return hasChildren() ? getChildren().size() : 0;
	}

}
