package me.daddychurchill.CityWorld.Plats;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Context.DataContext;
import me.daddychurchill.CityWorld.Maps.PlatMap;
import me.daddychurchill.CityWorld.Plugins.BalloonProvider;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.RealChunk;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

public class FloatingBlimpLot extends IsolatedLot {

	protected final static byte baseId = (byte) Material.SMOOTH_BRICK.getId();
	protected final static byte pedestalId = (byte) Material.STONE.getId();
	
	boolean manyBalloons;
		
	public FloatingBlimpLot(PlatMap platmap, int chunkX, int chunkZ) {
		super(platmap, chunkX, chunkZ);
		
		manyBalloons = chunkRandom.nextBoolean();
	}
	
	@Override
	public int getBottomY(WorldGenerator generator) {
		return 0;
	}

	@Override
	protected void generateActualChunk(WorldGenerator generator,
			PlatMap platmap, ByteChunk chunk, BiomeGrid biomes,
			DataContext context, int platX, int platZ) {
		
		boolean toNorth = platmap.isStructureLot(platX, platZ - 1);
		boolean toSouth = platmap.isStructureLot(platX, platZ + 1);
		boolean toWest = platmap.isStructureLot(platX - 1, platZ);
		boolean toEast = platmap.isStructureLot(platX + 1, platZ);
		
		chunk.setCircle(8, 8, 6, generator.streetLevel, baseId, true);
		if (toNorth) {
			chunk.setBlocks(0, 16, generator.streetLevel, 0, 7, baseId);
			chunk.setBlocks(7, 9, generator.streetLevel - 2, generator.streetLevel, 0, 13, stoneId);
		}
		if (toSouth) {
			chunk.setBlocks(0, 16, generator.streetLevel, 8, 16, baseId);
			chunk.setBlocks(7, 9, generator.streetLevel - 2, generator.streetLevel, 3, 16, stoneId);
		}
		if (toWest) {
			chunk.setBlocks(0, 7, generator.streetLevel, 0, 16, baseId);
			chunk.setBlocks(0, 13, generator.streetLevel - 2, generator.streetLevel, 7, 9, stoneId);
		}
		if (toEast) {
			chunk.setBlocks(8, 16, generator.streetLevel, 0, 16, baseId);
			chunk.setBlocks(3, 16, generator.streetLevel - 2, generator.streetLevel, 7, 9, stoneId);
		}
		
		// what types of balloon?
		if (manyBalloons) {
			chunk.setBlocks(7, 9, generator.streetLevel + 1, generator.streetLevel + 5, 3, 13, pedestalId);
			chunk.setBlocks(3, 13, generator.streetLevel + 1, generator.streetLevel + 5, 7, 9, pedestalId);
		} else {
			chunk.setBlocks(7, 9, generator.streetLevel + 1, generator.streetLevel + 5, 1, 3, pedestalId);
			chunk.setBlocks(7, 9, generator.streetLevel + 1, generator.streetLevel + 5, 13, 15, pedestalId);
			chunk.setBlocks(1, 3, generator.streetLevel + 1, generator.streetLevel + 5, 7, 9, pedestalId);
			chunk.setBlocks(13, 15, generator.streetLevel + 1, generator.streetLevel + 5, 7, 9, pedestalId);
		}
	}

	@Override
	protected void generateActualBlocks(WorldGenerator generator,
			PlatMap platmap, RealChunk chunk, DataContext context, int platX,
			int platZ) {
		if (generator.settings.includeDecayedBuildings)
			generator.decayBlocks.destroyLot(generator.streetLevel - 1, generator.streetLevel + 3);
		
		// what type of balloon?
		BalloonProvider balloons = generator.balloonProvider;
		if (manyBalloons) {
			balloons.generateBalloon(generator, chunk, context, 7 + chunkRandom.nextInt(2), generator.streetLevel + 5, 3, chunkRandom);
			balloons.generateBalloon(generator, chunk, context, 7 + chunkRandom.nextInt(2), generator.streetLevel + 5, 12, chunkRandom);
			balloons.generateBalloon(generator, chunk, context, 3, generator.streetLevel + 5, 7 + chunkRandom.nextInt(2), chunkRandom);
			balloons.generateBalloon(generator, chunk, context, 12, generator.streetLevel + 5, 7 + chunkRandom.nextInt(2), chunkRandom);
		} else {
			balloons.generateBlimp(generator, chunk, context, generator.streetLevel + 5, chunkRandom);
		}
	}

}
