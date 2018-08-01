package com.brentcroft.gtd.camera;

public class FxCamera extends Camera
{
	@Override
	protected FxCameraObjectService createService()
	{
		return new FxCameraObjectService();
	}

	@Override
	public Object getOrigin()
	{
		return new FxSnapshot();
	}
}
