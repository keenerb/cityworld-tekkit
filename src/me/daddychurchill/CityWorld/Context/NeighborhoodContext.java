package me.daddychurchill.CityWorld.Context;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Maps.PlatMap;
import me.daddychurchill.CityWorld.Plats.HouseLot;
import me.daddychurchill.CityWorld.Plats.PlatLot;

public class NeighborhoodContext extends RuralContext {

	public NeighborhoodContext(WorldGenerator generator, PlatMap platmap) {
		super(generator, platmap);

		//TODO anything else? 
	}

	@Override
	public void populateMap(WorldGenerator generator, PlatMap platmap) {
		
		// do we check for roads?
		boolean checkForRoads = platmap.getNumberOfRoads() > 0;
		
		// backfill with buildings and parks
		for (int x = 0; x < PlatMap.Width; x++) {
			for (int z = 0; z < PlatMap.Width; z++) {
				PlatLot current = platmap.getLot(x, z);
				if (current == null) {
					
					// make houses?
					if (generator.settings.includeHouses) {
						
						// check for roads?
						if (checkForRoads) {
							if (platmap.isExistingRoad(x - 1, z) || platmap.isExistingRoad(x + 1, z) || 
								platmap.isExistingRoad(x, z - 1) || platmap.isExistingRoad(x, z + 1))
								placeHouse(platmap, x, z);
							
						// just do it then
						} else
							placeHouse(platmap, x, z);
					}
				}
			}
		}
	}
	
	private void placeHouse(PlatMap platmap, int x, int z) {
		platmap.setLot(x, z, new HouseLot(platmap, platmap.originX + x, platmap.originZ + z));
	}
}
