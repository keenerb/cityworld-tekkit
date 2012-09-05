package me.daddychurchill.CityWorld.Plugins;

import java.util.Random;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Plats.PlatLot;
import me.daddychurchill.CityWorld.Support.CachedYs;
import me.daddychurchill.CityWorld.Support.RealChunk;
import org.bukkit.Material;

public class OreProvider_Normal extends OreProvider {

	public OreProvider_Normal(WorldGenerator generator) {
		super(generator);
	}
	
	/**
	 * Populates the world with ores.
	 *
	 * @author Nightgunner5
	 * @author Markus Persson
	 * modified by simplex
	 * wildly modified by daddychurchill
	 */
	
	private static final int[] ore_types = new int[] {Material.WATER.getId(),
													  Material.LAVA.getId(),
		  											  Material.GRAVEL.getId(), 
													  Material.COAL_ORE.getId(),
													  Material.IRON_ORE.getId(), 
													  Material.GOLD_ORE.getId(), 
													  Material.LAPIS_ORE.getId(),
													  Material.REDSTONE_ORE.getId(),
													  Material.DIAMOND_ORE.getId()
													  }; 
	
	//                                                         WATER   LAVA   GRAV   COAL   IRON   GOLD  LAPIS  REDST   DIAM   EMER  
	private static final int[] ore_iterations = new int[]    {     8,     6,    40,    30,    12,     4,     2,     4,     2};
	private static final int[] ore_amountToDo = new int[]    {     1,     1,    12,     8,     8,     3,     3,    10,     3};
	private static final int[] ore_maxY = new int[]          {   128,    32,   111,   128,    61,    29,    25,    16,    15};
	private static final int[] ore_minY = new int[]          {    32,     2,    40,    16,    10,     8,     8,     6,     2};
	private static final boolean[] ore_upper = new boolean[] {  true, false, false,  true,  true,  true,  true,  true, false};
	private static final boolean[] ore_physics = new boolean[] {true,  true, false, false, false, false, false, false, false};
	private static final boolean[] ore_liquid = new boolean[] { true,  true, false, false, false, false, false, false, false};
	
	@Override
	public void sprinkleOres(WorldGenerator generator, PlatLot lot, RealChunk chunk, CachedYs blockYs, Random random, OreLocation location) {
		
		// do it!
		for (int typeNdx = 0; typeNdx < ore_types.length; typeNdx++) {
			sprinkleOre(generator, lot, chunk, blockYs,
					random, ore_types[typeNdx], ore_maxY[typeNdx], 
					ore_minY[typeNdx], ore_iterations[typeNdx], 
					ore_amountToDo[typeNdx], ore_upper[typeNdx], ore_physics[typeNdx], ore_liquid[typeNdx]);
		}
	}
}
