package me.daddychurchill.CityWorld.Plats;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Context.DataContext;
import me.daddychurchill.CityWorld.Maps.PlatMap;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.RealChunk;
import me.daddychurchill.CityWorld.Support.SurroundingLots;

import org.bukkit.generator.ChunkGenerator.BiomeGrid;

public class StorageLot extends BuildingLot {

	public StorageLot(PlatMap platmap, int chunkX, int chunkZ) {
		super(platmap, chunkX, chunkZ);

		height = 1;
		depth = 0;
	}
	
	@Override
	protected void generateActualChunk(WorldGenerator generator,
			PlatMap platmap, ByteChunk chunk, BiomeGrid biomes,
			DataContext context, int platX, int platZ) {
		int groundY = getBottomY(generator);

		// look around
		SurroundingLots neighbors = new SurroundingLots(platmap, platX, platZ);
		
		// top it off
		chunk.setLayer(groundY, generator.oreProvider.subsurfaceId);
		chunk.setLayer(groundY + 1, RoadLot.pavementId);
		
		// fence please
		drawFence(generator, chunk, context, 1, groundY + 2, neighbors);
	}

	@Override
	protected void generateActualBlocks(WorldGenerator generator,
			PlatMap platmap, RealChunk chunk, DataContext context, int platX,
			int platZ) {
		int groundY = getBottomY(generator);
		
		// look around
		SurroundingLots neighbors = new SurroundingLots(platmap, platX, platZ);
		
		// shed please
		if (chunkRandom.nextInt(neighbors.getNeighborCount() + 2) == 0)
			generator.houseProvider.generateShed(generator, chunk, context, chunkRandom, 7, groundY + 2, 7, chunkRandom.nextInt(2) + 2);

		// it looked so nice for a moment... but the moment has passed
		if (generator.settings.includeDecayedBuildings)
			generator.decayBlocks.destroyLot(groundY, groundY + 4);
	}

	@Override
	public int getBottomY(WorldGenerator generator) {
		return generator.streetLevel;
	}

}
