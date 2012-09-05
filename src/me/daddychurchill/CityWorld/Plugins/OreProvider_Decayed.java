package me.daddychurchill.CityWorld.Plugins;

import me.daddychurchill.CityWorld.WorldGenerator;

public class OreProvider_Decayed extends OreProvider_Tekkit {

	public OreProvider_Decayed(WorldGenerator generator) {
		super(generator);

		fluidId = stillLavaId;
		surfaceId = sandId;
		subsurfaceId = sandstoneId;
	}

}
