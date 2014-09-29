package TBC.ZoneGeneration;

import java.util.HashMap;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.biome.BiomeGenBase;

public class ZoneHandler
{
	public static ZoneHandler ServerInstance = new ZoneHandler();
	public static ZoneHandler ClientInstance = new ZoneHandler();

	private HashMap<ChunkCoordIntPair, HashMap<Integer, ZoneChunkData>> LoadedData = new HashMap<ChunkCoordIntPair, HashMap<Integer, ZoneChunkData>>();

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

	public void SetRegionData(ChunkCoordIntPair chunkCoordinates, BiomeGenBase biome, ZoneChunkData chunkData)
	{
		ChunkCoordIntPair regionPair = GetRegionCoordinates(chunkCoordinates);
		HashMap<Integer, ZoneChunkData> perBiome = LoadedData.get(regionPair);
		if(perBiome == null)
		{
			perBiome = new HashMap<Integer, ZoneChunkData>();
			LoadedData.put(regionPair, perBiome);
		}

		perBiome.put(biome.biomeID, chunkData);
	}

	public void SetRegionDataForAllBiomes(ChunkCoordIntPair chunkCoordinates, HashMap<Integer, ZoneChunkData> data)
	{
		ChunkCoordIntPair regionPair = GetRegionCoordinates(chunkCoordinates);
		HashMap<Integer, ZoneChunkData> perBiome = LoadedData.get(regionPair);
		if(perBiome == null)
		{
			perBiome = data;
			LoadedData.put(regionPair, perBiome);
		}
		else
		{
			for(Integer key : data.keySet())
			{
				perBiome.put(key, data.get(key));
			}
		}
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
		if(chunkCoord < 0)
		{
			return (chunkCoord / 8) - 1;
		}

		return chunkCoord / 8;
	}

	public static int HydrateRegionCoord(int regionCoord)
	{
		int chunkCoord = regionCoord * 8;
		if(chunkCoord < 0)
		{
			chunkCoord = chunkCoord + 8;
		}

		return chunkCoord;
	}
}
