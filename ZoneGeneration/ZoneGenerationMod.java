package TBC.ZoneGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

public class ZoneGenerationMod 
{
	public void Init(FMLInitializationEvent evt)
	{
		NetworkRegistry.instance().registerChannel(new ZoneDataRequestHandler(), "TBCReqZData");
		NetworkRegistry.instance().registerChannel(new ZoneDataResponseHandler(), "TBCResZData");
	}
	
	public void onChunkLoad(ChunkEvent.Load evt)
	{
		World world = evt.world;
		BiomeGenBase[] allBiomes = GetBiomesForChunk(world, evt.getChunk());
		HashSet<BiomeGenBase> biomes = new HashSet<BiomeGenBase>();
		for(BiomeGenBase oneofAll : allBiomes)
		{
			biomes.add(oneofAll);
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
				Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new Packet250CustomPayload("TBCReqZData", (p.chunkXPos + "," + p.chunkZPos).getBytes()));
			}
			
			return;
		}
		
		//Load the region chunk immediately, so that the data is always persisted.
		world.getChunkFromChunkCoords(roundedCoords.chunkXPos, roundedCoords.chunkZPos);
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
			evt.getData().setCompoundTag("TBCRegionData", compoundTag);
			HashMap<Integer, ZoneChunkData> data = ZoneHandler.ServerInstance.GetRegionDataForAllBiomes(evt.getChunk().getChunkCoordIntPair());
			for(Integer key : data.keySet())
			{
				compoundTag.setString("TBCAreaName:" + key, data.get(key).AreaName);
				compoundTag.setInteger("TBCAreaLevel:" + key, data.get(key).AreaLevel);
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
			for(Object tag : compoundTag.getTags())
			{
				NBTBase base = (NBTBase)tag;
				String[] pieces = base.getName().split(":");
				int key = Integer.parseInt(pieces[1]);
				ZoneChunkData biomeData = data.get(key);
				if(biomeData == null)
				{
					biomeData = new ZoneChunkData();
				}
				
				if(pieces[0].equals("TBCAreaName"))
				{
					biomeData.AreaName = ((NBTTagString)base).data;
				}
				
				if(pieces[0].equals("TBCAreaLevel"))
				{
					biomeData.AreaLevel = ((NBTTagInt)base).data;
				}
			}

			ZoneHandler.ServerInstance.SetRegionDataForAllBiomes(evt.getChunk().getChunkCoordIntPair(), data);
		}
	}
	
	private BiomeGenBase[] GetBiomesForChunk(World world, Chunk c)
	{
		BiomeGenBase[] array = new BiomeGenBase[10];
		return world.getWorldChunkManager().getBiomeGenAt(array, c.getChunkCoordIntPair().getCenterXPos() - 8, c.getChunkCoordIntPair().getCenterZPosition() - 8, 16, 16, true);
	}
}
