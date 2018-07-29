package com.brentcroft.gtd.camera;

public class SwtCamera extends Camera
{
	@Override
	protected SwtCameraObjectService createService()
	{
		return new SwtCameraObjectService();
	}

	@Override
	public Object getOrigin()
	{
		return new SwtSnapshot();
	}
}
