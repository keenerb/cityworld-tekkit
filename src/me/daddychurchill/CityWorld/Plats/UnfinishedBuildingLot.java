package me.daddychurchill.CityWorld.Plats;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Context.DataContext;
import me.daddychurchill.CityWorld.Maps.PlatMap;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.RealChunk;
import me.daddychurchill.CityWorld.Support.SurroundingFloors;
import me.daddychurchill.CityWorld.Support.WorldBlocks;
import me.daddychurchill.CityWorld.Support.Direction.StairWell;

public class UnfinishedBuildingLot extends BuildingLot {

	private final static int FloorHeight = DataContext.FloorHeight;
	
	private final static byte girderId = (byte) Material.CLAY.getId();
	
	private final static Material dirtMaterial = Material.DIRT;
	private final static Material fenceMaterial = Material.IRON_FENCE;
	private final static Material stairMaterial = Material.WOOD_STAIRS;
	private final static Material wallMaterial = Material.SMOOTH_BRICK;
	private final static Material ceilingMaterial = Material.STONE;
	private final static Material sandMaterial = Material.SAND;
	private final static int fenceHeight = 3;
	private final static int inset = 2;
	
	// our special bits
	protected boolean unfinishedBasementOnly;
	protected int floorsBuilt;
	protected int lastHorizontalGirder;
	
	public UnfinishedBuildingLot(PlatMap platmap, int chunkX, int chunkZ) {
		super(platmap, chunkX, chunkZ);
		DataContext context = platmap.context;
		
		// basement only?
		unfinishedBasementOnly = chunkRandom.nextInt(context.oddsOfOnlyUnfinishedBasements) == 0;
		
		// how many floors are finished?
		floorsBuilt = chunkRandom.nextInt(height);
	}

	@Override
	public int getBottomY(WorldGenerator generator) {
		return generator.streetLevel - FloorHeight * (depth - 1) - 3;
	}
	
	@Override
	protected void generateActualChunk(WorldGenerator generator, PlatMap platmap, ByteChunk chunk, BiomeGrid biomes, DataContext context, int platX, int platZ) {

		// check out the neighbors
		SurroundingFloors neighborBasements = getNeighboringBasementCounts(platmap, platX, platZ);
		SurroundingFloors neighborFloors = getNeighboringFloorCounts(platmap, platX, platZ);

		// starting with the bottom
		int lowestY = getBottomY(generator);
		
		// bottom most floor
		drawCeilings(chunk, context, lowestY, 1, 0, 0, false, ceilingMaterial, neighborBasements);
		
		// below ground
		for (int floor = 0; floor < depth; floor++) {
			int floorAt = generator.streetLevel - FloorHeight * floor - 2;
			
			// clear it out
			chunk.setLayer(floorAt, FloorHeight, airId);
			
			// at the first floor add a fence to prevent folks from falling in
			if (floor == 0) {
				drawWalls(chunk, context, generator.streetLevel + 2, fenceHeight, 0, 0, false,
						fenceMaterial, fenceMaterial, neighborBasements);
				holeFence(chunk, generator.streetLevel + 2, neighborBasements);
			}
			
			// one floor please
			if(generator.settings.includeDecayedNature) {
			drawWalls(chunk, context, floorAt, FloorHeight, 0, 0, false, sandMaterial, sandMaterial, neighborBasements);
			}
			else {
			drawWalls(chunk, context, floorAt, FloorHeight, 0, 0, false, dirtMaterial, dirtMaterial, neighborBasements);
			}
			drawWalls(chunk, context, floorAt, FloorHeight, 1, 1, false, wallMaterial, wallMaterial, neighborBasements);
			
			// ceilings if needed
			if (!unfinishedBasementOnly) {
				drawCeilings(chunk, context, floorAt + FloorHeight - 1, 1, 1, 1, false,
						ceilingMaterial, neighborBasements);
			} else {
				drawHorizontalGirders(chunk, floorAt + FloorHeight - 1, neighborBasements);
			}
	
			// hold up the bit we just drew
			drawVerticalGirders(chunk, floorAt, FloorHeight);
			
			// one down, more to go
			neighborBasements.decrement();
		}
		
		// do more?
		if (!unfinishedBasementOnly) {
			lastHorizontalGirder = 0;

			// above ground
			for (int floor = 0; floor < height; floor++) {
				int floorAt = generator.streetLevel + FloorHeight * floor + 2;
				
				// floor built yet?
				if (floor <= floorsBuilt) {
					
					// the floor of the next floor
					drawCeilings(chunk, context, floorAt + FloorHeight - 1, 1, 1, 1, false,
							ceilingMaterial, neighborFloors);
				} else {
					
					// sometimes the top most girders aren't there quite yet
					if (floor < height - 1 || chunkRandom.nextBoolean()) {
						drawHorizontalGirders(chunk, floorAt + FloorHeight - 1, neighborFloors);
						lastHorizontalGirder = floorAt + FloorHeight - 1;
					}
				}
	
				// hold up the bit we just drew
				drawVerticalGirders(chunk, floorAt, FloorHeight);
				
				// one down, more to go
				neighborFloors.decrement();
			}
		}
	}

	@Override
	protected void generateActualBlocks(WorldGenerator generator, PlatMap platmap, RealChunk chunk, DataContext context, int platX, int platZ) {
		
		// work on the basement stairs first
		if (!unfinishedBasementOnly) {
			
			if (needStairsDown) {
				for (int floor = 0; floor < depth; floor++) {
					int y = generator.streetLevel - FloorHeight * floor - 2;
					
					// place the stairs and such
					drawStairs(chunk, y, FloorHeight, inset, inset, StairWell.CENTER, stairMaterial);
				}
			}
			
			if (needStairsUp) {
				for (int floor = 0; floor < height; floor++) {
					int y = generator.streetLevel + FloorHeight * floor + 2;
					
					// floor built yet?
					if (floor <= floorsBuilt) {
						
						// more stairs and such
						if (floor < height - 1)
							drawStairs(chunk, y, FloorHeight, inset, inset, StairWell.CENTER, stairMaterial);
					}
				}
			}
			
			// plop a crane on top?
			boolean craned = false;
			if (lastHorizontalGirder > 0 && chunkRandom.nextInt(context.oddsOfCranes) == 0) {
				if (chunkRandom.nextBoolean())
					chunk.drawCrane(context, chunkRandom, inset + 2, lastHorizontalGirder + 1, inset);
				else
					chunk.drawCrane(context, chunkRandom, inset + 2, lastHorizontalGirder + 1, chunk.width - inset - 1);
				craned = true;
			}
			
			// it looked so nice for a moment... but the moment has passed
			if (generator.settings.includeDecayedBuildings) {

				// world centric view of blocks
				WorldBlocks blocks = new WorldBlocks(generator);
				
				// what is the top floor?
				int floors = height;
				if (craned)
					floors--;
				
				// work our way up
				for (int floor = 1; floor < floors; floor++) {
					
					// do only floors that aren't top one or do the top one if there isn't a crane
					int y = generator.streetLevel + FloorHeight * floor + 1;
						
					// do we take out a bit of it?
					decayEdge(blocks, chunk.getBlockX(7) + chunkRandom.nextInt(3) - 1, y, chunk.getBlockZ(inset));
					decayEdge(blocks, chunk.getBlockX(8) + chunkRandom.nextInt(3) - 1, y, chunk.getBlockZ(chunk.width - inset - 1));
					decayEdge(blocks, chunk.getBlockX(inset), y, chunk.getBlockZ(7) + chunkRandom.nextInt(3) - 1);
					decayEdge(blocks, chunk.getBlockX(chunk.width - inset - 1), y, chunk.getBlockZ(8) + chunkRandom.nextInt(3) - 1);
				}
			}
		}
	}
	
	private final static double decayedEdgeOdds = 0.20;
	
	private void decayEdge(WorldBlocks blocks, int x, int y, int z) {
		if (chunkRandom.nextDouble() < decayedEdgeOdds) {
			
			// make it go away
			blocks.desperseArea(chunkRandom, x, y, z, chunkRandom.nextInt(2) + 2);
		}	
	}
	
	private void drawVerticalGirders(ByteChunk chunk, int y1, int floorHeight) {
		int y2 = y1 + floorHeight;
		chunk.setBlocks(inset, y1, y2, inset, girderId);
		chunk.setBlocks(inset, y1, y2, chunk.width - inset - 1, girderId);
		chunk.setBlocks(chunk.width - inset - 1, y1, y2, inset, girderId);
		chunk.setBlocks(chunk.width - inset - 1, y1, y2, chunk.width - inset - 1, girderId);
	}

	private void drawHorizontalGirders(ByteChunk chunk, int y1, SurroundingFloors neighbors) {
		int x1 = neighbors.toWest() ? 0 : inset;
		int x2 = neighbors.toEast() ? chunk.width - 1 : chunk.width - inset - 1;
		int z1 = neighbors.toNorth() ? 0 : inset;
		int z2 = neighbors.toSouth() ? chunk.width - 1 : chunk.width - inset - 1;
		int i1 = inset;
		int i2 = chunk.width - inset - 1;
		
		chunk.setBlocks(x1, x2 + 1, y1, y1 + 1, i1, i1 + 1, girderId);
		chunk.setBlocks(x1, x2 + 1, y1, y1 + 1, i2, i2 + 1, girderId);
		chunk.setBlocks(i1, i1 + 1, y1, y1 + 1, z1, z2 + 1, girderId);
		chunk.setBlocks(i2, i2 + 1, y1, y1 + 1, z1, z2 + 1, girderId);
	}
	
	private void holeFence(ByteChunk chunk, int y1, SurroundingFloors neighbors) {
		
		int i = chunkRandom.nextInt(chunk.width / 2) + 4;
		int y2 = y1 + 2;
		if (chunkRandom.nextBoolean() && !neighbors.toWest())
			chunk.setBlocks(0, y1, y2, i, airId);
		if (chunkRandom.nextBoolean() && !neighbors.toEast())
			chunk.setBlocks(chunk.width - 1, y1, y2, i, airId);
		if (chunkRandom.nextBoolean() && !neighbors.toNorth())
			chunk.setBlocks(i, y1, y2, 0, airId);
		if (chunkRandom.nextBoolean() && !neighbors.toSouth())
			chunk.setBlocks(i, y1, y2, chunk.width - 1, airId);
	}
}
