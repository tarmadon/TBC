package TBC.EnemyLabels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.logging.Level;

import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.Combat.CombatEntitySpawnLookup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

public class EnemyLabelMod 
{
	public void Init(FMLInitializationEvent evt)
	{
		NetworkRegistry.instance().registerChannel(new EntityDataRequestHandler(), "TBCReqEData");
		NetworkRegistry.instance().registerChannel(new EntityDataResponseHandler(), "TBCResEData");
	}
	
	public void afterLoad(FMLPostInitializationEvent postEvent)
	{
		if (postEvent.getSide() == Side.CLIENT) 
		{
			ArrayList<SimpleEntry<Class, Render>> overrides = new ArrayList<SimpleEntry<Class, Render>>();
			Map renderMap = RenderManager.instance.entityRenderMap;
			for (Object o : renderMap.entrySet()) 
			{
				Entry<Class, Render> entry = (Entry<Class, Render>)o;
				if (entry.getValue() instanceof RenderWithName || entry.getValue() instanceof RenderPlayer) 
				{
					continue;
				}

				RenderWithName render = new RenderWithName(entry.getValue());
				overrides.add(new SimpleEntry<Class, Render>(entry.getKey(), render));
			}
			
			for(SimpleEntry<Class,Render> entry : overrides)
			{
				RenderingRegistry.registerEntityRenderingHandler(entry.getKey(), entry.getValue());
			}
		}
	}

	public void onEntityAddedToChunk(EntityEvent.EnteringChunk buildingEntity)
	{
		if(buildingEntity.oldChunkX != 0 || buildingEntity.oldChunkZ != 0)
		{
			return;
		}

		if(!(buildingEntity.entity instanceof EntityPlayer) && buildingEntity.entity instanceof EntityLiving)
		{
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			{
				if(!buildingEntity.entity.getEntityData().hasKey("TBCEntityName"))
				{
					CombatEntity entity = CombatEntitySpawnLookup.Instance.GetCombatEntity((EntityLiving)buildingEntity.entity);
					if(entity != null)
					{
						buildingEntity.entity.getEntityData().setString("TBCEntityName", entity.name);
					}
				}
			}
			else
			{
				EntityLiving entityLiving = (EntityLiving)buildingEntity.entity;
				if(!entityLiving.getEntityData().hasKey("TBCEntityName"))
				{
					Minecraft m = Minecraft.getMinecraft();
					if(m.thePlayer != null && m.thePlayer.sendQueue != null)
					{
						m.thePlayer.sendQueue.addToSendQueue(new Packet250CustomPayload("TBCReqEData", (entityLiving.entityId + "," + "TBCEntityName").getBytes()));
					}
				}
			}
		}
	}
	
	public void onClientMobSpawn(EntityEvent.EntityConstructing evt)
	{
		if(!(evt.entity instanceof EntityPlayer) && evt.entity instanceof EntityLiving)
		{
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			{
				EntityLiving entityLiving = (EntityLiving)evt.entity;
				Minecraft m = Minecraft.getMinecraft();
				if(m.thePlayer != null && m.thePlayer.sendQueue != null)
				{
					m.thePlayer.sendQueue.addToSendQueue(new Packet250CustomPayload("TBCReqEData", (entityLiving.entityId + "," + "TBCEntityName").getBytes()));
				}
			}
		}
	}
}
