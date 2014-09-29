package TBC.ZoneGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import TBC.StringMessage;
import TBC.TBCMod;
import TBC.MainMod;
import TBC.EnemyLabels.EntityDataRequestHandler;
import TBC.EnemyLabels.EntityDataResponseHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class ZoneGenerationMod
{
	public static SimpleNetworkWrapper zoneDataHandler;
	
	public void Init(FMLInitializationEvent evt)
	{
		zoneDataHandler = new SimpleNetworkWrapper("TBCZoneData");
		zoneDataHandler.registerMessage(ZoneDataRequestHandler.class, StringMessage.class, 0, Side.SERVER);
		zoneDataHandler.registerMessage(ZoneDataResponseHandler.class, ZoneDataMessage.class, 1, Side.CLIENT);
	}

	public void onChunkLoad(ChunkEvent.Load evt)
	{
		World world = evt.world;
		byte[] allBiomes = GetBiomesForChunk(world, evt.getChunk());
		HashSet<BiomeGenBase> biomes = new HashSet<BiomeGenBase>();
		for(byte oneofAll : allBiomes)
		{
			if(oneofAll >= 0)
			{
				biomes.add(BiomeGenBase.getBiome(oneofAll));
			}
		}

		ChunkCoordIntPair p = evt.getChunk().getChunkCoordIntPair();
		ChunkCoordIntPair regionCoords = ZoneHandler.GetRegionCoordinates(p);
		ChunkCoordIntPair roundedCoords = ZoneHandler.GetChunkCoordFromRegion(regionCoords);

		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && Minecraft.getMinecraft().thePlayer != null)
		{
			boolean hasUpToDateZone = true;
			for(BiomeGenBase b : biomes)
			{
				if(ZoneHandler.ClientInstance.GetRegionData(p, b) == null)
				{
					hasUpToDateZone = false;
					break;
				}
			}

			if(!hasUpToDateZone)
			{
				StringMessage message = new StringMessage();
				message.Data = p.chunkXPos + "," + p.chunkZPos;
				zoneDataHandler.sendToServer(message);
			}

			return;
		}

		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			ArrayList<BiomeGenBase> biomesNotFound = new ArrayList<BiomeGenBase>();
			for(BiomeGenBase b : biomes)
			{
				if(ZoneHandler.ServerInstance.GetRegionData(p, b) == null)
				{
					biomesNotFound.add(b);
				}
			}
	
			if(biomesNotFound.size() == 0)
			{
				return;
			}
	
			for(BiomeGenBase biome : biomesNotFound)
			{
				Integer areaLevel = null;
				String areaName = null;
				ArrayList<Chunk> surroundingRegionChunks = new ArrayList<Chunk>();
				int maxSearch = 3;
				for(int i = 0; i < maxSearch; i++)
				{
					for(int j = 0; j < maxSearch; j++)
					{
						if(i == 0 && j == 0)
						{
							continue;
						}
	
						int posX = regionCoords.chunkXPos + (i - maxSearch/2);
						int posZ = regionCoords.chunkZPos + (j - maxSearch/2);
						ZoneChunkData regionValue = ZoneHandler.ServerInstance.GetRegionDataByRegionCoordinates(new ChunkCoordIntPair(posX,  posZ), biome);
						if(regionValue != null)
						{
							areaName = regionValue.AreaName;
							areaLevel = regionValue.AreaLevel;
							break;
						}
					}
				}
	
				if(areaLevel == null)
				{
					areaLevel = TBCMod.MainModInstance.distanceScaling.GetCurrentLevel(p.getCenterXPos(), p.getCenterZPosition());
					areaName = biome.biomeName + ": " + p.getCenterXPos() + "," + p.getCenterZPosition();
				}
	
				ZoneChunkData createdData = new ZoneChunkData();
				createdData.AreaLevel = areaLevel;
				createdData.AreaName = areaName;
				ZoneHandler.ServerInstance.SetRegionData(p, biome, createdData);
				
				//Load the region chunk immediately, so that the data is always persisted.
				world.getChunkFromChunkCoords(roundedCoords.chunkXPos, roundedCoords.chunkZPos);
			}
		}
	}

	public void onChunkSave(ChunkDataEvent.Save evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			return;
		}

		ChunkCoordIntPair regionCoords = ZoneHandler.GetRegionCoordinates(evt.getChunk().getChunkCoordIntPair());
		ChunkCoordIntPair roundedCoords = ZoneHandler.GetChunkCoordFromRegion(regionCoords);
		if(evt.getChunk().xPosition == roundedCoords.chunkXPos && evt.getChunk().zPosition == roundedCoords.chunkZPos)
		{
			NBTTagCompound compoundTag = new NBTTagCompound();
			evt.getData().setTag("TBCRegionData", compoundTag);
			HashMap<Integer, ZoneChunkData> data = ZoneHandler.ServerInstance.GetRegionDataForAllBiomes(evt.getChunk().getChunkCoordIntPair());
			if(data != null)
			{
				for(Integer key : data.keySet())
				{
					compoundTag.setString("TBCAreaName:" + key, data.get(key).AreaName);
					compoundTag.setInteger("TBCAreaLevel:" + key, data.get(key).AreaLevel);
				}
			}
		}
	}

	public void onChunkLoad(ChunkDataEvent.Load evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			return;
		}

		if(evt.getData().hasKey("TBCRegionData"))
		{
			HashMap<Integer, ZoneChunkData> data = new HashMap<Integer, ZoneChunkData>();
			NBTTagCompound compoundTag = evt.getData().getCompoundTag("TBCRegionData");
			Set keys = compoundTag.func_150296_c();
			for(Object lookupKey : keys)
			//for(Object tag : compoundTag.getTags())
			{
				String stringKey = (String)lookupKey;
				Object tag = compoundTag.getTag(stringKey);
				NBTBase base = (NBTBase)tag;
				String[] pieces = stringKey.split(":");
				int key = Integer.parseInt(pieces[1]);
				ZoneChunkData biomeData = data.get(key);
				if(biomeData == null)
				{
					biomeData = new ZoneChunkData();
				}

				if(pieces[0].equals("TBCAreaName"))
				{
					biomeData.AreaName = ((NBTTagString)base).func_150285_a_();
				}

				if(pieces[0].equals("TBCAreaLevel"))
				{
					biomeData.AreaLevel = ((NBTTagInt)base).func_150287_d();
				}
			}

			ZoneHandler.ServerInstance.SetRegionDataForAllBiomes(evt.getChunk().getChunkCoordIntPair(), data);
		}
	}

	private byte[] GetBiomesForChunk(World world, Chunk c)
	{
		return c.getBiomeArray();
	}
}
