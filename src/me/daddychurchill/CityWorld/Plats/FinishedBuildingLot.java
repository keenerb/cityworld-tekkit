package me.daddychurchill.CityWorld.Plats;

import java.util.Random;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Context.DataContext;
import me.daddychurchill.CityWorld.Maps.PlatMap;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.Direction.StairWell;
import me.daddychurchill.CityWorld.Support.RealChunk;
import me.daddychurchill.CityWorld.Support.SurroundingFloors;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

public abstract class FinishedBuildingLot extends BuildingLot {

	protected Material wallMaterial;
	protected Material ceilingMaterial;
	protected Material glassMaterial;
	protected Material stairMaterial;
	protected Material stairWallMaterial;
	protected Material doorMaterial;
	protected Material roofMaterial;
	
	//TODO columns height
	protected int insetWallEW;
	protected int insetWallNS;
	protected int insetCeilingEW;
	protected int insetCeilingNS;
	protected boolean insetInsetted;
	protected int insetInsetMidAt;
	protected int insetInsetHighAt;

	public FinishedBuildingLot(PlatMap platmap, int chunkX, int chunkZ) {
		super(platmap, chunkX, chunkZ);
		
		DataContext context = platmap.context;

		// how do the walls inset?
		insetWallEW = chunkRandom.nextInt(context.rangeOfWallInset) + 1; // 1 or 2
		insetWallNS = chunkRandom.nextInt(context.rangeOfWallInset) + 1;
		
		// what about the ceiling?
		if (chunkRandom.nextInt(context.oddsOfFlatWalledBuildings) == 0) {
			insetCeilingEW = insetWallEW;
			insetCeilingNS = insetWallNS;
		} else {
			insetCeilingEW = insetWallEW + chunkRandom.nextInt(3) - 1; // -1, 0 or 1 -> 0, 1, 2
			insetCeilingNS = insetWallNS + chunkRandom.nextInt(3) - 1;
		}
		
		// make the buildings have a better chance at being round
		if (chunkRandom.nextInt(context.oddsOfSimilarInsetBuildings) == 0) {
			insetWallNS = insetWallEW;
			insetCeilingNS = insetCeilingEW;
		}
		
		// nudge in a bit more as we go up
		insetInsetMidAt = 1;
		insetInsetHighAt = 1;
		insetInsetted = height >= context.buildingWallInsettedMinLowPoint && chunkRandom.nextInt(context.oddsOfBuildingWallInset) == 0;
		if (insetInsetted) {
			insetInsetMidAt = Math.max(context.buildingWallInsettedMinMidPoint, 
					chunkRandom.nextInt(context.buildingWallInsettedMinLowPoint));
			insetInsetHighAt = Math.max(insetInsetMidAt + 1, chunkRandom.nextInt(context.buildingWallInsettedMinLowPoint));
		}
		
		// what is it made of?
		wallMaterial = pickWallMaterial(chunkRandom);
		ceilingMaterial = pickCeilingMaterial(chunkRandom);
		glassMaterial = pickGlassMaterial(chunkRandom);
		stairMaterial = pickStairMaterial(wallMaterial);
		doorMaterial = Material.WOOD_DOOR;
		roofMaterial = pickRoofMaterial(chunkRandom);
		
		// what are the walls of the stairs made of?
		if (chunkRandom.nextInt(context.oddsOfStairWallMaterialIsWallMaterial) == 0)
			stairWallMaterial = wallMaterial;
		else
			stairWallMaterial = pickStairWallMaterial(wallMaterial);

		// Fix up any material issues
		// thin glass should not be used with ceiling inset, it looks goofy
		// thin glass should not be used with double-step walls, the glass does not align correctly
		if (glassMaterial == Material.THIN_GLASS) {
			insetCeilingEW = Math.min(insetCeilingEW, insetWallEW);
			insetCeilingNS = Math.min(insetCeilingNS, insetWallNS);
			if (wallMaterial == Material.DOUBLE_STEP)
				glassMaterial = Material.GLASS;
		}
	}

	public boolean makeConnected(PlatLot relative) {
		boolean result = super.makeConnected(relative);

		// other bits
		if (result && relative instanceof FinishedBuildingLot) {
			FinishedBuildingLot relativebuilding = (FinishedBuildingLot) relative;

			// nudge in a bit
			insetWallEW = relativebuilding.insetWallEW;
			insetWallNS = relativebuilding.insetWallNS;
			insetCeilingEW = relativebuilding.insetCeilingEW;
			insetCeilingNS = relativebuilding.insetCeilingNS;
			
			// nudge in a bit more as we go up
			insetInsetted = relativebuilding.insetInsetted;
			insetInsetMidAt = relativebuilding.insetInsetMidAt;
			insetInsetHighAt = relativebuilding.insetInsetHighAt;
			
			// what is it made of?
			wallMaterial = relativebuilding.wallMaterial;
			ceilingMaterial = relativebuilding.ceilingMaterial;
			glassMaterial = relativebuilding.glassMaterial;
			stairMaterial = relativebuilding.stairMaterial;
			stairWallMaterial = relativebuilding.stairWallMaterial;
			doorMaterial = relativebuilding.doorMaterial;
			roofMaterial = relativebuilding.roofMaterial;
		}
		return result;
	}

	@Override
	public int getBottomY(WorldGenerator generator) {
		int result = generator.streetLevel;
		if (depth > 0)
			result -= basementFloorHeight * (depth - 1) + 3;
		return result;
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
		//chunk.setBlocks(0, chunk.width, lowestY, lowestY + 1, 0, chunk.width, (byte) ceilingMaterial.getId());
		
		// below ground
		if (depth > 0) {
			for (int floor = 0; floor < depth; floor++) {
				int floorAt = generator.streetLevel - basementFloorHeight * floor - 2;
	
				// clear it out
				chunk.setLayer(floorAt, basementFloorHeight, airId);
	
				// one floor please
				drawWalls(chunk, context, floorAt, basementFloorHeight - 1, 0, 0, false,
						wallMaterial, wallMaterial, neighborBasements);
				drawCeilings(chunk, context, floorAt + basementFloorHeight - 1, 1, 0, 0, false,
						ceilingMaterial, neighborBasements);
				
				// one down, more to go
				neighborBasements.decrement();
			}
		} else {
			chunk.setLayer(lowestY + 1, (byte) ceilingMaterial.getId());
		}

		// is rounding allowed?
		boolean allowRounded = rounded && 
				insetWallEW == insetWallNS && insetCeilingEW == insetCeilingNS;
		
		// insetting the inset
		int localInsetWallEW = insetWallEW;
		int localInsetWallNS = insetWallNS;
		int localInsetCeilingEW = insetCeilingEW;
		int localInsetCeilingNS = insetCeilingNS;

		// above ground
		for (int floor = 0; floor < height; floor++) {
			int floorAt = generator.streetLevel + aboveFloorHeight * floor + 2;
			allowRounded = allowRounded && neighborFloors.isRoundable();

			// breath in?
			if (insetInsetted) {
				if (floor == insetInsetMidAt || floor == insetInsetHighAt) {
					localInsetWallEW++;
					localInsetWallNS++;
					localInsetCeilingEW++;
					localInsetCeilingNS++;
				}
			}
			
			// one floor please
			drawWalls(chunk, context, floorAt, aboveFloorHeight - 1, 
					localInsetWallEW, localInsetWallNS, 
					allowRounded, wallMaterial, glassMaterial, neighborFloors);
			drawCeilings(chunk, context, floorAt + aboveFloorHeight - 1, 1, 
					localInsetCeilingEW, localInsetCeilingNS, 
					allowRounded, ceilingMaterial, neighborFloors);
			
			// final floor is done... how about a roof then?
			if (floor == height - 1)
				drawRoof(chunk, context, generator.streetLevel + aboveFloorHeight * (floor + 1) + 2, localInsetWallEW, localInsetWallNS, allowRounded, roofMaterial, neighborFloors);

			// one down, more to go
			neighborFloors.decrement();
		}
	}
	
	@Override
	protected void generateActualBlocks(WorldGenerator generator, PlatMap platmap, RealChunk chunk, DataContext context, int platX, int platZ) {

		// check out the neighbors
		//SurroundingFloors neighborBasements = getNeighboringBasementCounts(platmap, platX, platZ);
		SurroundingFloors neighborFloors = getNeighboringFloorCounts(platmap, platX, platZ);
		
		// is rounding allowed and where are the stairs
		boolean allowRounded = rounded && 
				insetWallEW == insetWallNS && insetCeilingEW == insetCeilingNS && 
				neighborFloors.isRoundable();
		StairWell stairLocation = getStairWellLocation(allowRounded, neighborFloors);
		
		// bottom floor? 
		drawDoors(chunk, generator.streetLevel + 2, aboveFloorHeight, insetWallEW, insetWallNS, 
				stairLocation, neighborFloors, wallMaterial);
		
		// work on the basement stairs first
		for (int floor = 0; floor < depth; floor++) {
			int y = generator.streetLevel - basementFloorHeight * floor - 2;
			
			// stairs?
			if (needStairsDown) {
				
				// top is special... but only if there are no stairs up
				if (floor == 0 && !needStairsUp) {
					drawStairsWalls(chunk, y, basementFloorHeight, insetWallEW, insetWallNS, 
							stairLocation, stairWallMaterial, true, false);
				
				// all the rest of those lovely stairs
				} else {
					// place the stairs and such
					drawStairs(chunk, y, basementFloorHeight, insetWallEW, insetWallNS, 
							stairLocation, stairMaterial);
						
					// plain walls please
					drawStairsWalls(chunk, y, basementFloorHeight, insetWallEW, insetWallNS, 
							stairLocation, wallMaterial, false, floor == depth - 1);

					// pillars if no stairs here
					drawOtherPillars(chunk, y, basementFloorHeight, 
							stairLocation, wallMaterial);
				}
			
			// if no stairs then
			} else {
				drawOtherPillars(chunk, y, basementFloorHeight, 
						StairWell.CENTER, wallMaterial);
			}
		}
		
		// now the above ground floors
		if (needStairsUp) {
			for (int floor = 0; floor < height; floor++) {
				int y = generator.streetLevel + aboveFloorHeight * floor + 2;
				
				// more stairs and such
				if (floor < height - 1)
					drawStairs(chunk, y, aboveFloorHeight, insetWallEW, insetWallNS, 
							stairLocation, stairMaterial);
				
				// fancy walls... maybe
				if (floor > 0 || (floor == 0 && (depth > 0 || height > 1)))
					drawStairsWalls(chunk, y, aboveFloorHeight, insetWallEW, insetWallNS, 
							stairLocation, stairWallMaterial, floor == height - 1, floor == 0 && depth == 0);
			}
		}
		
		// happy place?
		if (!generator.settings.includeDecayedBuildings) {
		
			// maybe draw a navlight?
			drawNavLight(chunk, context);
			
		// nope, let's destroy our work!
		} else {
			int y1 = generator.streetLevel + 2;
			int y2 = y1 + aboveFloorHeight * height;
			switch (roofStyle) {
			case EDGED:
			case FLATTOP:
				if (roofFeature == RoofFeature.ANTENNAS)
					y2 -= aboveFloorHeight;
				break;
			case PEAK:
			case TENTEW:
			case TENTNS:
				y2 += aboveFloorHeight;
				break;
			}
			generator.decayBlocks.destroyLot(y1, y2);
		}
	}
	
	protected static Material pickWallMaterial(Random rand) {
		switch (rand.nextInt(15)) {
		case 1:
			return Material.COBBLESTONE;
		case 2:
			return Material.SAND;
		case 3:
			return Material.GRAVEL;
		case 4:
			return Material.WOOD;
		case 5:
			return Material.SANDSTONE;
		case 6:
			return Material.WOOL;
		case 7:
			return Material.DOUBLE_STEP;
		case 8:
			return Material.BRICK;
		case 9:
			return Material.MOSSY_COBBLESTONE;
		case 10:
			return Material.CLAY;
		case 11:
			return Material.NETHERRACK;
		case 12:
			return Material.SOUL_SAND;
		case 13:
			return Material.SMOOTH_BRICK;
		case 14:
			return Material.NETHER_BRICK;
//			case 15:
//			return Material.IRON_BLOCK;
		default:
			return Material.STONE;
		}
	}

	static protected Material pickRoofMaterial(Random rand) {
		switch (rand.nextInt(12)) {
		case 1:
			return Material.COBBLESTONE;
		case 2:
			return Material.WOOD;
		case 3:
			return Material.SANDSTONE;
		case 4:
			return Material.WOOL;
		case 5:
			return Material.DOUBLE_STEP;
		case 6:
			return Material.BRICK;
		case 7:
			return Material.MOSSY_COBBLESTONE;
		case 8:
			return Material.CLAY;
		case 9:
			return Material.NETHERRACK;
		case 10:
			return Material.SMOOTH_BRICK;
		case 11:
			return Material.NETHER_BRICK;
//			case 15:
//			return Material.IRON_BLOCK;
		default:
			return Material.STONE;
		}
	}

	static protected Material pickStairMaterial(Material wall) {
		switch (wall) {
		case COBBLESTONE:
		case MOSSY_COBBLESTONE:
			return Material.COBBLESTONE_STAIRS;

		case STONE:
		case SMOOTH_BRICK:
		case CLAY:
//		case IRON_BLOCK:
		case DOUBLE_STEP:
			return Material.SMOOTH_STAIRS;

		case WOOL:
		case BRICK:
			return Material.BRICK_STAIRS;
		
		case NETHERRACK:
		case NETHER_BRICK:
			return Material.NETHER_BRICK_STAIRS;
		
		default: // SANDSTONE, WOOD, SAND
			return Material.WOOD_STAIRS;
		}
	}

	static protected Material pickStairWallMaterial(Material wall) {
		switch (wall) {
		case COBBLESTONE:
		case MOSSY_COBBLESTONE:
		case STONE:
		case SMOOTH_BRICK:
			return Material.IRON_FENCE;

//		case IRON_BLOCK:
		case DOUBLE_STEP:
		case WOOL:
		case BRICK:
			return Material.THIN_GLASS;
		
		case NETHERRACK:
		case NETHER_BRICK:
			return Material.NETHER_FENCE;
		
		default: // SANDSTONE, WOOD, SAND, CLAY
			return Material.FENCE;
		}
	}

	static protected Material pickCeilingMaterial(Random rand) {
		switch (rand.nextInt(12)) {
		case 1:
			return Material.COBBLESTONE;
		case 2:
			return Material.WOOD;
		case 3:
			return Material.SANDSTONE;
		case 4:
			return Material.WOOL;
		case 5:
			return Material.DOUBLE_STEP;
		case 6:
			return Material.BRICK;
		case 7:
			return Material.MOSSY_COBBLESTONE;
		case 8:
			return Material.CLAY;
		case 9:
			return Material.NETHERRACK;
		case 10:
			return Material.SMOOTH_BRICK;
		case 11:
			return Material.NETHER_BRICK;
//			case 15:
//			return Material.IRON_BLOCK;
		default:
			return Material.STONE;
		}
	}
}
