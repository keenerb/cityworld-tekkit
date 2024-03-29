package me.daddychurchill.CityWorld;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import me.daddychurchill.CityWorld.Maps.PlatMap;
import me.daddychurchill.CityWorld.Plugins.BalloonProvider;
import me.daddychurchill.CityWorld.Plugins.FoliageProvider;
import me.daddychurchill.CityWorld.Plugins.HouseProvider;
import me.daddychurchill.CityWorld.Plugins.LootProvider;
import me.daddychurchill.CityWorld.Plugins.OdonymProvider;
import me.daddychurchill.CityWorld.Plugins.OreProvider;
import me.daddychurchill.CityWorld.Plugins.ShapeProvider;
import me.daddychurchill.CityWorld.Plugins.SpawnProvider;
import me.daddychurchill.CityWorld.Plugins.SurfaceProvider;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.RealChunk;
import me.daddychurchill.CityWorld.Support.SupportChunk;
import me.daddychurchill.CityWorld.Support.WorldBlocks;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class WorldGenerator extends ChunkGenerator {

	private CityWorld plugin;
	private World world;
	private Long worldSeed;
	private Random connectionKeyGen;
	
	public String worldName;
	public WorldStyle worldStyle;
	public Environment worldEnvironment;
	
	public ShapeProvider shapeProvider;
	public LootProvider lootProvider;
	public SpawnProvider spawnProvider;
	public OreProvider oreProvider;
	public SurfaceProvider surfaceProvider;
	public FoliageProvider foliageProvider;
	public OdonymProvider odonymProvider;
	public BalloonProvider balloonProvider;
	public HouseProvider houseProvider;
	
	public WorldBlocks decayBlocks;

	public CityWorldSettings settings;

	public int streetLevel;
	
	public int deepseaLevel;
	public int seaLevel;
	public int structureLevel;
	public int treeLevel;
	public int evergreenLevel;
	public int deciduousRange;
	public int evergreenRange;
	public int height;
	public int snowLevel;
	public int landRange;
	public int seaRange;
	
	public long connectedKeyForPavedRoads;
	public long connectedKeyForParks;
	
	public enum WorldStyle {
		FLOATING,		// very low terrain with floating houses and cities
		//LUNAR,		// lunar landscape with lunar bases
		//FLOODED,		// traditional terrain and cities but with raised sea level
		//UNDERWATER,	// traditional terrain with raised sea level with under water cities
		//WESTERN,		// desert landscape with sparse western styled towns and ranches
		//UNDERGROUND,	// elevated terrain with underground cities
		NORMAL};   		// traditional terrain and cities
	
	public WorldGenerator(CityWorld plugin, String worldName, String worldStyle) {
		this.plugin = plugin;
		this.worldName = worldName;
		this.worldStyle = WorldStyle.NORMAL;
		
		// parse the style string
		if (worldStyle != null) {
			try {
				this.worldStyle = WorldStyle.valueOf(worldStyle.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				CityWorld.log.info(plugin.getName() + ": Unknown world style [" + worldStyle + "], switching to NORMAL");
				this.worldStyle = WorldStyle.NORMAL;
			}
		}
	}

	public CityWorld getPlugin() {
		return plugin;
	}

	public World getWorld() {
		return world;
	}

	public Long getWorldSeed() {
		return worldSeed;
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return Arrays.asList((BlockPopulator) new CityWorldBlockPopulator(this));
	}
	
	private void initializeWorldInfo(World aWorld) {
		
		// initialize the shaping logic
		if (world == null) {
			world = aWorld;
			settings = new CityWorldSettings(this);
			worldSeed = world.getSeed();
			connectionKeyGen = new Random(worldSeed + 1);
			shapeProvider = ShapeProvider.loadProvider(this, new Random(worldSeed + 2));
			lootProvider = LootProvider.loadProvider(this);
			spawnProvider = SpawnProvider.loadProvider(this);
			oreProvider = OreProvider.loadProvider(this);
			foliageProvider = FoliageProvider.loadProvider(this, new Random(worldSeed + 3));
			odonymProvider = OdonymProvider.loadProvider(this, new Random(worldSeed + 4));
			surfaceProvider = SurfaceProvider.loadProvider(this, new Random(worldSeed + 5));
			balloonProvider = BalloonProvider.loadProvider(this);
			houseProvider = HouseProvider.loadProvider(this);
			decayBlocks = new WorldBlocks(this, new Random(worldSeed + 6));
			
			// get ranges
			height = shapeProvider.getWorldHeight();
			seaLevel = shapeProvider.getSeaLevel();
			landRange = shapeProvider.getLandRange();
			seaRange = shapeProvider.getSeaRange();
			structureLevel = shapeProvider.getStructureLevel();
			streetLevel = shapeProvider.getStreetLevel();

			// now the other vertical points
			deepseaLevel = seaLevel - seaRange / 3;
			snowLevel = seaLevel + (landRange / 4 * 3);
			evergreenLevel = seaLevel + (landRange / 4 * 2);
			treeLevel = seaLevel + (landRange / 4);
			deciduousRange = evergreenLevel - treeLevel;
			evergreenRange = snowLevel - evergreenLevel;
			
//			// seabed = 35 deepsea = 50 sea = 64 sidewalk = 65 tree = 110 evergreen = 156 snow = 202 top = 249
//			CityWorld.log.info("seabed = " + (seaLevel - seaRange) + 
//							   " deepsea = " + deepseaLevel + 
//							   " sea = " + seaLevel + 
//							   " sidewalk = " + sidewalkLevel + 
//							   " tree = " + treeLevel + 
//							   " evergreen = " + evergreenLevel + 
//							   " snow = " + snowLevel + 
//							   " top = " + (seaLevel + landRange));
			
			// get the connectionKeys
			connectedKeyForPavedRoads = connectionKeyGen.nextLong();
			connectedKeyForParks = connectionKeyGen.nextLong();
		}
	}
	
	@Override
	public byte[][] generateBlockSections(World aWorld, Random random, int chunkX, int chunkZ, BiomeGrid biomes) {
		initializeWorldInfo(aWorld);
		
		// place to work
		ByteChunk byteChunk = new ByteChunk(this, chunkX, chunkZ);
		
		// figure out what everything looks like
		PlatMap platmap = getPlatMap(byteChunk, chunkX, chunkZ);
		if (platmap != null) {
			//CityWorld.log.info("generate X,Z = " + chunkX + "," + chunkZ);
			platmap.generateChunk(byteChunk, biomes);
		}

		return byteChunk.blocks;
	}
	
	public long getConnectionKey() {
		return connectionKeyGen.nextLong();
	}
	
	public int getFarBlockY(int blockX, int blockZ) {
		return shapeProvider.findBlockY(this, blockX, blockZ);
	}
	
	private final static int spawnRadius = 100;
	
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		int spawnX = random.nextInt(spawnRadius * 2) - spawnRadius;
		int spawnZ = random.nextInt(spawnRadius * 2) - spawnRadius;
		
		// find the first non empty spot;
		int spawnY = world.getMaxHeight();
		while ((spawnY > 0) && world.getBlockAt(spawnX, spawnY - 1, spawnZ).isEmpty()) {
			spawnY--;
		}
		
		// return the location
		return new Location(world, spawnX, spawnY, spawnZ);
	}

	// manager for handling the city plat maps collection
	private Hashtable<Long, PlatMap> platmaps;
	private PlatMap getPlatMap(SupportChunk cornerChunk, int chunkX, int chunkZ) {

		// get the plat map collection
		if (platmaps == null)
			platmaps = new Hashtable<Long, PlatMap>();

		// find the origin for the plat
		int platX = calcOrigin(chunkX);
		int platZ = calcOrigin(chunkZ);

		// calculate the plat's key
		Long platkey = Long.valueOf(((long) platX * (long) Integer.MAX_VALUE + (long) platZ));

		// get the right plat
		PlatMap platmap = platmaps.get(platkey);
		
		// doesn't exist? then make it!
		if (platmap == null) {
			
			// what is the context for this one?
			platmap = shapeProvider.createPlatMap(this, cornerChunk, platX, platZ);
			
			// remember it for quicker look up
			platmaps.put(platkey, platmap);
		}

		// finally return the plat
		return platmap;
	}

	// Supporting code used by getPlatMap
	private int calcOrigin(int i) {
		if (i >= 0) {
			return i / PlatMap.Width * PlatMap.Width;
		} else {
			return -((Math.abs(i + 1) / PlatMap.Width * PlatMap.Width) + PlatMap.Width);
		}
	}

	private class CityWorldBlockPopulator extends BlockPopulator {

		private WorldGenerator chunkGen;

		public CityWorldBlockPopulator(WorldGenerator chunkGen) {
			this.chunkGen = chunkGen;
		}

		@Override
		public void populate(World aWorld, Random random, Chunk chunk) {
			chunkGen.initializeWorldInfo(aWorld);
			
			// where are we?
			int chunkX = chunk.getX();
			int chunkZ = chunk.getZ();
			
			// place to work
			RealChunk realChunk = new RealChunk(chunkGen, chunk);

			// figure out what everything looks like
			PlatMap platmap = chunkGen.getPlatMap(realChunk, chunkX, chunkZ);
			if (platmap != null) {
				//CityWorld.log.info("populate X,Z = " + chunkX + "," + chunkZ);
				platmap.generateBlocks(realChunk);
			}
		}
	}
}
