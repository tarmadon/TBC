package TBC.ZoneGeneration;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.biome.BiomeGenBase;

public class ZoneHandler
{
	public static Object lockObj = new Object();
	public static ZoneHandler Instance = new ZoneHandler();
	
	private ConcurrentHashMap<ChunkCoordIntPair, HashMap<Integer, ZoneChunkData>> LoadedData = new ConcurrentHashMap<ChunkCoordIntPair, HashMap<Integer, ZoneChunkData>>();

	public ZoneChunkData GetRegionData(ChunkCoordIntPair chunkCoordinates, BiomeGenBase biome)
	{
		ChunkCoordIntPair regionPair = GetRegionCoordinates(chunkCoordinates);
		HashMap<Integer, ZoneChunkData> perBiome = LoadedData.get(regionPair);
		if(perBiome != null && biome != null)
		{
			return perBiome.get(biome.biomeID);
		}

		return null;
	}

	public ZoneChunkData GetRegionDataByRegionCoordinates(ChunkCoordIntPair regionPair, BiomeGenBase biome)
	{
		HashMap<Integer, ZoneChunkData> perBiome = LoadedData.get(regionPair);
		if(perBiome != null)
		{
			return perBiome.get(biome.biomeID);
		}

		return null;
	}

	public HashMap<Integer, ZoneChunkData> GetRegionDataForAllBiomes(ChunkCoordIntPair chunkCoordinates)
	{
		ChunkCoordIntPair regionPair = GetRegionCoordinates(chunkCoordinates);
		return LoadedData.get(regionPair);
	}

	public void SetRegionDataForAllBiomes(ChunkCoordIntPair chunkCoordinates, HashMap<Integer, ZoneChunkData> data)
	{
		int x = chunkCoordinates.chunkXPos;
		int z = chunkCoordinates.chunkZPos;
		ChunkCoordIntPair regionPair = GetRegionCoordinates(chunkCoordinates);
		LoadedData.putIfAbsent(regionPair, new HashMap<Integer, ZoneChunkData>());
		HashMap<Integer, ZoneChunkData> perBiome = LoadedData.get(regionPair);
		for(Integer key : data.keySet())
		{
			perBiome.put(key, data.get(key));
		}
	}

	public void ClearData()
	{
		LoadedData.clear();
	}

	public void ClearDataForRegion(ChunkCoordIntPair regionCoordinates)
	{
		int x = regionCoordinates.chunkXPos;
		int z = regionCoordinates.chunkZPos;
		LoadedData.remove(regionCoordinates);
	}
	
	public static ChunkCoordIntPair GetRegionCoordinates(ChunkCoordIntPair chunkCoordinates)
	{
		return new ChunkCoordIntPair(FlattenChunkCoord(chunkCoordinates.chunkXPos), FlattenChunkCoord(chunkCoordinates.chunkZPos));
	}

	public static ChunkCoordIntPair GetChunkCoordFromRegion(ChunkCoordIntPair regionCoordinates)
	{
		return new ChunkCoordIntPair(HydrateRegionCoord(regionCoordinates.chunkXPos), HydrateRegionCoord(regionCoordinates.chunkZPos));
	}

	public static int FlattenChunkCoord(int chunkCoord)
	{
		return chunkCoord;
	}
	
	public static int HydrateRegionCoord(int regionCoord)
	{
		return regionCoord;
	}
}
