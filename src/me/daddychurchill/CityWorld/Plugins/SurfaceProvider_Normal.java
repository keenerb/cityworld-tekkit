package me.daddychurchill.CityWorld.Plugins;

import java.util.Random;

import org.bukkit.util.noise.NoiseGenerator;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Plats.PlatLot;
import me.daddychurchill.CityWorld.Plugins.FoliageProvider.HerbaceousType;
import me.daddychurchill.CityWorld.Plugins.FoliageProvider.LigneousType;
import me.daddychurchill.CityWorld.Support.RealChunk;

public class SurfaceProvider_Normal extends SurfaceProvider {

	public SurfaceProvider_Normal(Random random) {
		super(random);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generateSurfacePoint(WorldGenerator generator, PlatLot lot, RealChunk chunk, FoliageProvider foliage, 
			int x, double perciseY, int z, boolean includeTrees) {
		OreProvider ores = generator.oreProvider;
		int y = NoiseGenerator.floor(perciseY);
		
		// roll the dice
		double primary = random.nextDouble();
		double secondary = random.nextDouble();
		
		// top of the world?
		if (y >= generator.snowLevel) {
			ores.dropSnow(generator, chunk, x, y, z, (byte) NoiseGenerator.floor((perciseY - Math.floor(perciseY)) * 8.0));
		
		// are on a plantable spot?
		} else if (foliage.isPlantable(generator, chunk, x, y, z)) {
			
			// below sea level and plantable.. then cactus?
			if (y <= generator.seaLevel) {
				
				// trees? but only if we are not too close to the edge
				if (!generator.settings.includeAbovegroundFluids && includeTrees && primary > 0.95 && x % 2 == 0 && z % 2 != 0) {
					if (chunk.isSurroundedByEmpty(x, y + 1, z))
						foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.CACTUS);
				}
				
			// regular trees, grass and flowers only
			} else if (y < generator.treeLevel) {

				// trees? but only if we are not too close to the edge
				if (includeTrees && primary > treeOdds && x > 0 && x < 15 && z > 0 && z < 15 && x % 2 == 0 && z % 2 != 0) {
					if (secondary > 0.90 && x > 5 && x < 11 && z > 5 && z < 11)
						foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.TALL_OAK);
					else if (secondary > 0.50)
						foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.BIRCH);
					else 
						foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.OAK);
				
				// foliage?
				} else if (primary < foliageOdds) {
					
					// what to pepper about
					if (secondary > 0.90)
						foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.FLOWER_RED);
					else if (secondary > 0.80)
						foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.FLOWER_YELLOW);
					else 
						foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.GRASS);
				}
				
			// regular trees, grass and some evergreen trees... no flowers
			} else if (y < generator.evergreenLevel) {

				// trees? but only if we are not too close to the edge
				if (includeTrees && primary > treeOdds && x % 2 == 0 && z % 2 != 0) {
					
					// range change?
					if (secondary > ((double) (y - generator.treeLevel) / (double) generator.deciduousRange))
						foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.OAK);
					else
						foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.PINE);
				
				// foliage?
				} else if (primary < foliageOdds) {

					// range change?
					if (secondary > ((double) (y - generator.treeLevel) / (double) generator.deciduousRange))
						foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.GRASS);
					else
						foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.FERN);
				}
				
			// evergreen and some grass and fallen snow, no regular trees or flowers
			} else if (y < generator.snowLevel) {
				
				// trees? but only if we are not too close to the edge
				if (includeTrees && primary > treeOdds && x % 2 == 0 && z % 2 != 0) {
					if (secondary > 0.50)
						foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.PINE);
					else
						foliage.generateTree(generator, chunk, x, y + 1, z, LigneousType.TALL_PINE);
				
				// foliage?
				} else if (primary < foliageOdds) {
					
					// range change?
					if (secondary > ((double) (y - generator.evergreenLevel) / (double) generator.evergreenRange)) {
						if (random.nextDouble() < 0.40)
							foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.FERN);
					} else {
						foliage.generateFlora(generator, chunk, x, y, z, HerbaceousType.COVER);
					}
				}
			}
		
		// can't plant, maybe there is something else I can do
		} else {
			
			// regular trees, grass and flowers only
			if (y < generator.treeLevel) {

			// regular trees, grass and some evergreen trees... no flowers
			} else if (y < generator.evergreenLevel) {

			// evergreen and some grass and fallen snow, no regular trees or flowers
			} else if (y < generator.snowLevel) {
				
				if (primary < foliageOdds) {
					
					// range change?
					if (secondary > ((double) (y - generator.evergreenLevel) / (double) generator.evergreenRange)) {
						if (random.nextDouble() < 0.10 && foliage.isPlantable(generator, chunk, x, y, z))
							foliage.generateFlora(generator, chunk, x, y + 1, z, HerbaceousType.FERN);
					} else {
						ores.dropSnow(generator, chunk, x, y, z);
					}
				}
			}
		}
		
	}

}
