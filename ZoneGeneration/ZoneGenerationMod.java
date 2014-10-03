package TBC.ZoneGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Level;

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
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class ZoneGenerationMod
{
	public static SimpleNetworkWrapper zoneDataHandler;
	private boolean regenZones = false;
	
	public void preInit(FMLPreInitializationEvent evt)
	{
		Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		regenZones = config.get(Configuration.CATEGORY_GENERAL, "RegenZoneData", false).getBoolean();
		config.save();
	}
	
	public void Init(FMLInitializationEvent evt)
	{
		zoneDataHandler = new SimpleNetworkWrapper("TBCZoneData");
		zoneDataHandler.registerMessage(ZoneDataRequestHandler.class, StringMessage.class, 0, Side.SERVER);
		zoneDataHandler.registerMessage(ZoneDataResponseHandler.class, ZoneDataMessage.class, 1, Side.CLIENT);
	}

	public void onChunkPopulate(PopulateChunkEvent.Post evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			return;
		}
		
		Chunk c = evt.chunkProvider.provideChunk(evt.chunkX, evt.chunkZ);
		World world = evt.world;
		ChunkCoordIntPair p = new ChunkCoordIntPair(evt.chunkX, evt.chunkZ);
		RecalculateChunkLevel(c, p, world);
	}
	
	public void onChunkLoad(ChunkEvent.Load evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && Minecraft.getMinecraft().thePlayer != null)
		{
			World world = evt.world;
			byte[] allBiomes = GetBiomesFromChunk(world, evt.getChunk());
			HashSet<BiomeGenBase> biomes = new HashSet<BiomeGenBase>();
			for(byte oneofAll : allBiomes)
			{
				if(oneofAll >= 0)
				{
					biomes.add(BiomeGenBase.getBiome(oneofAll));
				}
			}

			ChunkCoordIntPair p = evt.getChunk().getChunkCoordIntPair();
			boolean hasUpToDateZone = true;
			for(BiomeGenBase b : biomes)
			{
				if(ZoneHandler.Instance.GetRegionData(p, b) == null)
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
	}

	public void onChunkUnload(ChunkEvent.Unload evt)
	{
		//ZoneHandler.Instance.ClearDataForRegion(evt.getChunk().getChunkCoordIntPair());
	}
	
	public void onChunkSave(ChunkDataEvent.Save evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			return;
		}

		HashMap<Integer, ZoneChunkData> data = ZoneHandler.Instance.GetRegionDataForAllBiomes(evt.getChunk().getChunkCoordIntPair());
		if(data != null)
		{
			NBTTagCompound compoundTag = new NBTTagCompound();
			evt.getData().setTag("TBCRegionData", compoundTag);
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

		if(evt.getData().hasKey("TBCRegionData") && !evt.getData().getCompoundTag("TBCRegionData").hasNoTags() && !regenZones)
		{
			HashMap<Integer, ZoneChunkData> data = new HashMap<Integer, ZoneChunkData>();
			NBTTagCompound compoundTag = evt.getData().getCompoundTag("TBCRegionData");
			Set keys = compoundTag.func_150296_c();
			for(Object lookupKey : keys)
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
				
				data.put(key, biomeData);
			}

			ZoneHandler.Instance.SetRegionDataForAllBiomes(evt.getChunk().getChunkCoordIntPair(), data);
		}
		else
		{
			ChunkCoordIntPair p = evt.getChunk().getChunkCoordIntPair();
			HashMap<Integer, ZoneChunkData> allData = ZoneHandler.Instance.GetRegionDataForAllBiomes(p);
			if(regenZones || allData == null || allData.size() == 0)
			{
				Chunk c = evt.getChunk();
				World world = evt.world;
				RecalculateChunkLevel(c, p, world);
			}
		}
	}

	private void RecalculateChunkLevel(Chunk c, ChunkCoordIntPair p, World world)
	{
		BiomeGenBase[] allBiomes = GetBiomesForChunk(world, c);
		HashSet<BiomeGenBase> biomes = new HashSet<BiomeGenBase>();
		for(BiomeGenBase oneofAll : allBiomes)
		{
			biomes.add(oneofAll);
		}

		if(biomes.size() == 0)
		{
			FMLLog.log(Level.ERROR, "Chunk has no biomes.");
		}
		
		HashMap<Integer, ZoneChunkData> biomeData = new HashMap<Integer, ZoneChunkData>();
		for(BiomeGenBase biome : biomes)
		{
			Integer areaLevel = null;
			String areaName = null;
			ArrayList<Chunk> surroundingRegionChunks = new ArrayList<Chunk>();
			int maxSearch = 7;
			for(int i = 0; i < maxSearch; i++)
			{
				for(int j = 0; j < maxSearch; j++)
				{
					if(i == 0 && j == 0)
					{
						continue;
					}

					int posX = p.chunkXPos + (i - maxSearch/2);
					int posZ = p.chunkZPos + (j - maxSearch/2);
					ZoneChunkData regionValue = ZoneHandler.Instance.GetRegionDataByRegionCoordinates(new ChunkCoordIntPair(posX,  posZ), biome);
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
			biomeData.put(biome.biomeID, createdData);
		}
		
		ZoneHandler.Instance.SetRegionDataForAllBiomes(p, biomeData);
	}
	
	private BiomeGenBase[] GetBiomesForChunk(World world, Chunk c)
	{
		BiomeGenBase[] array = new BiomeGenBase[10];
		return world.getWorldChunkManager().getBiomeGenAt(array, c.getChunkCoordIntPair().getCenterXPos() - 8, c.getChunkCoordIntPair().getCenterZPosition() - 8, 16, 16, true);
	}
	
	private byte[] GetBiomesFromChunk(World world, Chunk c)
	{
		return c.getBiomeArray();
	}
}
