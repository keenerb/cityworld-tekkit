package me.daddychurchill.CityWorld.Plugins;

import java.util.Random;

import org.bukkit.util.noise.NoiseGenerator;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Plats.PlatLot;
import me.daddychurchill.CityWorld.Plugins.FoliageProvider.HerbaceousType;
import me.daddychurchill.CityWorld.Plugins.FoliageProvider.LigneousType;
import me.daddychurchill.CityWorld.Support.CachedYs;
import me.daddychurchill.CityWorld.Support.RealChunk;

public class SurfaceProvider_Floating extends SurfaceProvider {

	public SurfaceProvider_Floating(Random random) {
		super(random);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void generateSurface(WorldGenerator generator, PlatLot lot, RealChunk chunk, CachedYs blockYs, boolean includeTrees) {
		ShapeProvider shape = generator.shapeProvider;
		FoliageProvider foliage = generator.foliageProvider;
		for (int x = 0; x < chunk.width; x++) {
			for (int z = 0; z < chunk.width; z++) {
				generateSurfacePoint(generator, lot, chunk, foliage, x, shape.findGroundY(generator, chunk.getBlockX(x), chunk.getBlockZ(z)), z, includeTrees);
			}
		}
	}

	@Override
	public void generateSurfacePoint(WorldGenerator generator, PlatLot lot, RealChunk chunk, FoliageProvider foliage, int x, double perciseY, int z, boolean includeTrees) {
		int y = NoiseGenerator.floor(perciseY);
		
		// roll the dice
		double primary = random.nextDouble();
		double secondary = random.nextDouble();
		
		// are on a plantable spot?
		if (foliage.isPlantable(generator, chunk, x, y, z)) {
			
			// trees? but only if we are not too close to the edge
			if (includeTrees && primary > treeOdds && x > 0 && x < 15 && z > 0 && z < 15 && x % 2 == 0 && z % 2 != 0) {
				if (secondary > 0.75)
					foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.SHORT_PINE);
				else if (secondary > 0.50)
					foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.SHORT_BIRCH);
				else 
					foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.SHORT_OAK);
			
			// foliage?
			} else if (primary < foliageOdds && y <= ShapeProvider_Floating.snowPoint) {
				
				// what to pepper about
				if (secondary > 0.90)
					foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.FLOWER_RED);
				else if (secondary > 0.80)
					foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.FLOWER_YELLOW);
				else 
					foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.GRASS);
			}
		}
		
		// snow?
		if (y > ShapeProvider_Floating.snowPoint)
			foliage.generateFlora(generator, chunk, x, y, z, HerbaceousType.COVER);
	}

}
