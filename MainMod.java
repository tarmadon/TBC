package TBC;

import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Random;

import javax.swing.text.html.parser.Entity;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import TBC.Combat.AreaBasedLevelScaling;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.Combat.CombatEntitySpawnLookup;
import TBC.Combat.CombatEntityTemplate;
import TBC.Combat.CompositeLevelScaling;
import TBC.Combat.DepthBasedLevelScaling;
import TBC.Combat.DistanceBasedLevelScaling;
import TBC.Combat.EquippedItem;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.FlatBonusEquippedItem;
import TBC.Combat.ILevelScale;
import TBC.Combat.ItemReplacementLookup;
import TBC.Combat.LevelingEngine;
import TBC.Combat.TimeBasedLevelScaling;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.ItemReplacementLookup.ItemReplacementData;
import TBC.ZoneGeneration.ZoneHandler;
import TBC.ZoneGeneration.ZoneChunkData;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.modloader.ModLoaderHelper;
import cpw.mods.fml.common.network.FMLPacket;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.SidedProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EnumStatus;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ChunkLoader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEffectSourceEvent;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.GameSettings;

public class MainMod 
{
	private static boolean loadedProgress = false;
	private int questProgress = 0;
	
	public static boolean playerDataInit = false;
	public static ArrayList<Pair<EntityLiving, String>> setEnemies = null;
	public static EntityLiving enemy;
	public static boolean isPlayerAttacker;
	public static float lastAttackTime = 0;
	private ILevelScale levelScaling;
	public DistanceBasedLevelScaling distanceScaling;
	
	public void preInit(FMLPreInitializationEvent evt) 
	{
		File enemyConfigFile = this.loadFileFromJar("TBCTemplates.csv");
		File itemConfigFile = this.loadFileFromJar("TBCItemTemplates.csv"); 
		File spawnConfigFile = this.loadFileFromJar("TBCWorldMobData.csv");
		
		Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		int distanceScaleMod = config.get(Configuration.CATEGORY_GENERAL, "DistanceScaleModifier", 150).getInt();
		int depthStart = config.get(Configuration.CATEGORY_GENERAL, "DepthScaleStart", 64).getInt();
		int depthScaleMod = config.get(Configuration.CATEGORY_GENERAL, "DepthScaleModifier", 10).getInt();
		int timeScaleMod = config.get(Configuration.CATEGORY_GENERAL, "TimeScaleModifier", 24000).getInt();
		config.save();
		
		this.distanceScaling = new DistanceBasedLevelScaling(distanceScaleMod);
		SetupStaticItems();
		// Ability lookup initialization has to be before items since usable items will reference it.
		AbilityLookup.Instance.Initialize();
		EquippedItemManager.Instance.Initialize(itemConfigFile);
		this.levelScaling = new CompositeLevelScaling(new ILevelScale[]
			{
				new AreaBasedLevelScaling(),
				new DepthBasedLevelScaling(depthStart, depthScaleMod),
				new TimeBasedLevelScaling(timeScaleMod)
			}, false);
		CombatEntityLookup.Instance.Initialize(enemyConfigFile, this.levelScaling);
		CombatEntitySpawnLookup.Instance.Initialize(spawnConfigFile, this.levelScaling);
		ItemReplacementLookup.Instance.SetupItems();
	}

	private File loadFileFromJar(String fileName) 
	{
		File configFile = new File(Loader.instance().getConfigDir(), fileName);
		if(!configFile.exists())
		{
			FMLLog.info("Config file was not found at: %s, attempting to copy.", configFile.getPath());
			InputStream resource = getClass().getResourceAsStream(fileName);
			try {
				Files.copy(new InputStreamSupplier(resource), configFile);
			} catch (IOException e) {
				FMLLog.severe("Unable to copy default config file. %s", e.toString());
			}			
		}
		return configFile;
	}

	public void load(FMLInitializationEvent evt) 
	{
		NetworkRegistry.instance().registerGuiHandler(TBCMod.instance, new TBCGuiHandler());
		NetworkRegistry.instance().registerChannel(new SetEntityHealthHandler(), "TBCSetHealth");
		NetworkRegistry.instance().registerChannel(new SetItemDataHandler(), "TBCSetDur");
		NetworkRegistry.instance().registerChannel(new SyncPlayerDataHandler(), "TBCPlayerData");
		NetworkRegistry.instance().registerChannel(new RemoveItemHandler(), "TBCRemoveItem");
		KeyBindingRegistry.registerKeyBinding(new KeyHandlerForStats(TBCMod.instance));
	}

	public void syncPlayerData(EntityEvent.EnteringChunk buildingEntity)
	{
		if(buildingEntity.oldChunkX != 0 || buildingEntity.oldChunkZ != 0)
		{
			return;
		}
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && buildingEntity.entity instanceof EntityPlayer)
		{
			EntityPlayer playerEntity = (EntityPlayer)buildingEntity.entity;
			if(!playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).hasKey("playerLevel"))
			{
				Minecraft.getMinecraft().getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCPlayerData", new byte[0]));
				CombatEntitySpawnLookup.Instance.LogUnknownEntities(playerEntity.worldObj);
			}
		}
	}
	
	private long lastAttemptedSync = 0;
	private long startupTime = 0;
	public void renderHUD(RenderGameOverlayEvent.Text evt)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Chunk c = mc.theWorld.getChunkFromBlockCoords((int)mc.thePlayer.posX, (int)mc.thePlayer.posZ);
		BiomeGenBase biome = c.getBiomeGenForWorldCoords((int)mc.thePlayer.posX & 15, (int)mc.thePlayer.posZ & 15, mc.theWorld.getWorldChunkManager());
		ZoneChunkData data = ZoneHandler.ClientInstance.GetRegionData(c.getChunkCoordIntPair(), biome);
		if(data != null)
		{
			String areaName = data.AreaName;
			String displayString = "Area Level: " + this.levelScaling.GetCurrentLevel(mc.thePlayer);
			int areaNameWidth = mc.fontRenderer.getStringWidth(areaName);
			int stringWidth = mc.fontRenderer.getStringWidth(displayString);
			mc.fontRenderer.drawString(areaName, evt.resolution.getScaledWidth()/2 - (areaNameWidth/2), 3, 14737632);
			mc.fontRenderer.drawString(displayString, evt.resolution.getScaledWidth()/2 - (stringWidth/2), 11, 14737632);
		}
		else
		{
			long systemTime = Minecraft.getSystemTime();
			if(lastAttemptedSync < systemTime + 10000)
			{
				lastAttackTime = systemTime;
				Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new Packet250CustomPayload("TBCReqZData", (c.xPosition + "," + c.zPosition).getBytes()));				
			}
		}

		
		if(!playerDataInit || mc.theWorld.difficultySetting != 3)
		{
			return;
		}
		
		if(!loadedProgress)
		{
			questProgress = LevelingEngine.Instance.GetXpDataForPlayer(mc.thePlayer).QuestProgress;
			loadedProgress = true;
		}
		
		long currentTime = Minecraft.getSystemTime();
		if(startupTime == 0)
		{
			startupTime = currentTime;
		}
		
		if(this.enemy == null && this.setEnemies == null && mc.theWorld.getWorldTime() > questProgress * 24000)
		{
			if(this.startupTime + 1000 > currentTime || this.lastAttackTime + 1000 > currentTime)
			{
				return;
			}
			
			this.lastAttackTime = currentTime;
			setEnemies = new ArrayList<Pair<EntityLiving,String>>();
			questProgress = questProgress + 1;
			PlayerXPWorldSavedData questData = LevelingEngine.Instance.GetXpDataForPlayer(mc.thePlayer);
			questData.QuestProgress = questProgress;
			LevelingEngine.Instance.SaveXpDataForPlayer(mc.thePlayer, questData);
			SyncTagToServer(mc.thePlayer);
			
			SetQuestBattle(mc);
			mc.thePlayer.openGui(TBCMod.instance, 0, mc.thePlayer.worldObj, mc.thePlayer.serverPosX, mc.thePlayer.serverPosY, mc.thePlayer.serverPosZ);
		}
	}

	private void SyncTagToServer(EntityPlayer playerEntity)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			NBTTagCompound.writeNamedTag(playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG), new DataOutputStream(outputStream));
		} catch (IOException e) {}
			
		Minecraft.getMinecraft().getNetHandler().addToSendQueue(new Packet250CustomPayload("TBCPlayerData", outputStream.toByteArray()));
	}
	
	private void SetQuestBattle(Minecraft mc) 
	{
		if(questProgress == 2)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Skeleton", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Decrepit Skeleton");
			setEnemies.add(new Pair(enemy, "Decrepit Skeleton"));
		}
		else if(questProgress == 3)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Creeper");
			setEnemies.add(new Pair(enemy, "Creeper"));
		}
		else if(questProgress == 4)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Zombie", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Ghoul");
			setEnemies.add(new Pair(enemy, "Ghoul"));
		}
		else if(questProgress == 5)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Slime", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Brown Ooze");
			setEnemies.add(new Pair(enemy, "Brown Ooze"));
		}
		else if(questProgress == 6)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Skeleton", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Skeleton Apprentice");
			EntityLiving enemy2 = (EntityLiving) EntityList.createEntityByName("Skeleton", mc.theWorld);
			enemy2.getEntityData().setString("TBCEntityName", "Skeleton Apprentice");
			setEnemies.add(new Pair(enemy, "Skeleton Apprentice"));
			setEnemies.add(new Pair(enemy2, "Skeleton Apprentice"));
		}
		else if(questProgress == 7)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Overcharged Creeper");
			EntityLiving enemy2 = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy2.getEntityData().setString("TBCEntityName", "Overcharged Creeper");
			setEnemies.add(new Pair(enemy, "Overcharged Creeper"));
			setEnemies.add(new Pair(enemy2, "Overcharged Creeper"));
		}
		else if(questProgress == 8)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Zombie", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Vampire");
			EntityLiving enemy2 = (EntityLiving) EntityList.createEntityByName("Zombie", mc.theWorld);
			enemy2.getEntityData().setString("TBCEntityName", "Vampire");
			setEnemies.add(new Pair(enemy, "Vampire"));
			setEnemies.add(new Pair(enemy2, "Vampire"));
		}
		else if(questProgress == 9)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Spider", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Wraith Spider");
			EntityLiving enemy2 = (EntityLiving) EntityList.createEntityByName("Spider", mc.theWorld);
			enemy2.getEntityData().setString("TBCEntityName", "Wraith Spider");
			EntityLiving enemy3 = (EntityLiving) EntityList.createEntityByName("Spider", mc.theWorld);
			enemy3.getEntityData().setString("TBCEntityName", "Wraith Spider");
			EntityLiving enemy4 = (EntityLiving) EntityList.createEntityByName("Spider", mc.theWorld);
			enemy4.getEntityData().setString("TBCEntityName", "Wraith Spider");
			setEnemies.add(new Pair(enemy, "Wraith Spider"));
			setEnemies.add(new Pair(enemy2, "Wraith Spider"));
			setEnemies.add(new Pair(enemy3, "Wraith Spider"));
			setEnemies.add(new Pair(enemy4, "Wraith Spider"));
		}
		else if(questProgress == 10)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Slime", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Black Pudding");
			setEnemies.add(new Pair(enemy, "Black Pudding"));
		}
		else if(questProgress == 11)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Skeleton", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Skeleton King");
			setEnemies.add(new Pair(enemy, "Skeleton King"));
		}
		else if(questProgress == 12)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Creeper Ancient");
			EntityLiving enemy2 = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy2.getEntityData().setString("TBCEntityName", "Creeper Ancient");
			EntityLiving enemy3 = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy3.getEntityData().setString("TBCEntityName", "Creeper Ancient");
			EntityLiving enemy4 = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy4.getEntityData().setString("TBCEntityName", "Creeper Ancient");
			setEnemies.add(new Pair(enemy, "Creeper Ancient"));
			setEnemies.add(new Pair(enemy2, "Creeper Ancient"));
			setEnemies.add(new Pair(enemy3, "Creeper Ancient"));
			setEnemies.add(new Pair(enemy4, "Creeper Ancient"));
		}
		else if(questProgress == 13)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Enderman", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Enderguard");
			setEnemies.add(new Pair(enemy, "Enderguard"));
		}
		else if(questProgress == 14)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Zombie", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Revenant");
			EntityLiving enemy2 = (EntityLiving) EntityList.createEntityByName("Skeleton", mc.theWorld);
			enemy2.getEntityData().setString("TBCEntityName", "Skeleton King");
			EntityLiving enemy3 = (EntityLiving) EntityList.createEntityByName("Spider", mc.theWorld);
			enemy3.getEntityData().setString("TBCEntityName", "Spider Queen");
			EntityLiving enemy4 = (EntityLiving) EntityList.createEntityByName("Creeper", mc.theWorld);
			enemy4.getEntityData().setString("TBCEntityName", "Creeper Ancient");
			setEnemies.add(new Pair(enemy, "Revenant"));
			setEnemies.add(new Pair(enemy2, "Skeleton King"));
			setEnemies.add(new Pair(enemy3, "Spider Queen"));
			setEnemies.add(new Pair(enemy4, "Creeper Ancient"));
		}
		else if(questProgress == 15)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("Enderman", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Enderlord");
			setEnemies.add(new Pair(enemy, "Enderlord"));
		}
		else if(questProgress == 16)
		{
			EntityLiving enemy = (EntityLiving) EntityList.createEntityByName("EnderDragon", mc.theWorld);
			enemy.getEntityData().setString("TBCEntityName", "Ender Dragon");
			setEnemies.add(new Pair(enemy, "Ender Dragon"));
		}
	}

	public void onMobDrops(LivingDropsEvent evt)
	{
		if(!evt.entityLiving.getEntityData().hasKey("TBCEntityName"))
		{
			return;
		}
		
		String name = evt.entityLiving.getEntityData().getString("TBCEntityName");
		ArrayList<Pair<Item, Item>> replacements = ItemReplacementLookup.Instance.GetItemReplacementForEntity(name);
		if(replacements != null)
		{
			for(Pair<Item, Item> replacement : replacements)
			{
				replaceDrops(evt, evt.drops, replacement.item1, replacement.item2);
			}
		}
	}
	
	private void replaceDrops(LivingDropsEvent evt, ArrayList<EntityItem> drops, Item toReplace, Item replaceWith)
	{
		if(toReplace == null)
		{
			EntityItem itemToAdd = new EntityItem(evt.entity.worldObj, evt.entity.posX, evt.entity.posY, evt.entity.posZ);
			ItemStack newStack = new ItemStack(replaceWith, 1);
			itemToAdd.setEntityItemStack(newStack);
			drops.add(itemToAdd);
		}
		else
		{
			for(int i = 0; i<drops.size(); i++)
			{
				if(drops.get(i).getEntityItem().itemID == toReplace.itemID)
				{
					ItemStack newStack = new ItemStack(replaceWith, drops.get(i).getEntityItem().stackSize);
					drops.get(i).setEntityItemStack(newStack);
				}
			}
		}
	}
	
	public void onRest(PlayerSleepInBedEvent evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER)
		{
			return;
		}

        double d0 = 8.0D;
        double d1 = 5.0D;
        List list = evt.entity.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getAABBPool().getAABB((double)evt.x - d0, (double)evt.y - d1, (double)evt.z - d0, (double)evt.x + d0, (double)evt.y + d1, (double)evt.z + d0));
        if (!list.isEmpty())
        {
            return;
        }
		
		CombatEntity entity = CombatEntityLookup.Instance.GetCombatEntityForPlayer(evt.entityPlayer);
		evt.entityPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setInteger("TBCPlayerMP", entity.GetMaxMp());
		
		if(evt.entityPlayer instanceof EntityPlayerMP)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)evt.entityPlayer;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				NBTTagCompound.writeNamedTag(mpPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG), new DataOutputStream(outputStream));
			} catch (IOException e) {}
			mpPlayer.playerNetServerHandler.sendPacketToPlayer(new Packet250CustomPayload("TBCPlayerData", outputStream.toByteArray()));

			ItemStack[] p = mpPlayer.inventory.mainInventory;
			for(int i = 0; i<mpPlayer.inventory.mainInventory.length; i++)
			{
				ItemStack s  = mpPlayer.inventory.mainInventory[i];
				if(s != null && s.getItem() instanceof HenchmanItem)
				{
					HenchmanItem item = (HenchmanItem)s.getItem();
					CombatEntity hench = CombatEntityLookup.Instance.GetCombatEntity(null, item.henchmanName);
					s.setItemDamage(0);
					NBTTagCompound tag = s.getTagCompound();
					if(tag == null)
					{
						tag = new NBTTagCompound();
					}

					tag.setInteger("HenchMP", hench.GetMaxMp());
					s.setTagCompound(tag);
					mpPlayer.playerNetServerHandler.sendPacketToPlayer(new Packet250CustomPayload("TBCSetDur", (i + ",0," + hench.GetMaxMp()).getBytes()));
				}
			}
		}
		
		evt.result = EnumStatus.OTHER_PROBLEM;
		long currentTime = evt.entity.worldObj.getWorldTime();
		evt.entity.worldObj.setWorldTime(currentTime + 2000);
	}
	
	public void onLivingAttacked(LivingAttackEvent e)
	{
		// If the damage source isn't a living entity, this isn't a battle.
		net.minecraft.entity.Entity sourceEntity = e.source.getEntity();
		if(sourceEntity == null || 
		   !(sourceEntity instanceof EntityLiving) || 
		   sourceEntity == e.entityLiving ||
		   e.source.damageType == "bypass")
		{
			return;
		}
		
		EntityLiving sourceLivingEntity = (EntityLiving)sourceEntity;
		if(!sourceLivingEntity.isEntityAlive() || !e.entityLiving.isEntityAlive())
		{
			return;
		}
		
		EntityLiving enemy = null;
		Boolean isPlayerAttacker = false;
		EntityPlayer player = null;
		
		if(e.entityLiving instanceof EntityPlayerMP)
		{
			enemy = (EntityLiving)sourceEntity;
			isPlayerAttacker = false;
			player = (EntityPlayer)e.entityLiving;
		}
		else if(sourceEntity instanceof EntityPlayerMP)
		{
			enemy = e.entityLiving;
			isPlayerAttacker = true;
			player = (EntityPlayer)sourceEntity;
		}
		
		if(player != null)
		{
			e.setCanceled(true);
			
			float currentTime = Minecraft.getSystemTime();
			if(this.lastAttackTime + 1000 > currentTime)
			{
				return;
			}
			
			this.lastAttackTime = Minecraft.getSystemTime();
			this.enemy = enemy;
			this.isPlayerAttacker = isPlayerAttacker;
			player.openGui(TBCMod.instance, 0, player.worldObj, player.serverPosX, player.serverPosY, player.serverPosZ);
		}
	}
	
	public void onPlayerJoin(EntityJoinWorldEvent evt)
	{
		if(evt.entity instanceof EntityPlayer)
		{
			CombatEntityLookup.Instance.ClearCombatEntityForPlayer((EntityPlayer)evt.entity);
			this.questProgress = 0;
			this.loadedProgress = false;
			this.lastAttackTime = 0;
			this.enemy = null;
			this.setEnemies = null;
			this.playerDataInit = false;
		}
	}
	
	public void SetupStaticItems()
	{
		Item smallHealthPotion = new CloneItem(5000, Item.potion, "Restores 20 HP").setUnlocalizedName("smallPotion");
		ItemStack smallHealthPotionStack = new ItemStack(smallHealthPotion);
		GameRegistry.addShapelessRecipe(smallHealthPotionStack, Item.appleRed);
		LanguageRegistry.addName(smallHealthPotion, "S.Pot");
		
		Item medHealthPotion = new CloneItem(5001, Item.potion, "Restores 50 HP").setUnlocalizedName("medPotion");
		ItemStack medHealthPotionStack = new ItemStack(medHealthPotion);
		GameRegistry.addShapelessRecipe(medHealthPotionStack, smallHealthPotion, smallHealthPotion, Item.bread);
		LanguageRegistry.addName(medHealthPotion, "M.Pot");
		
		Item highHealthPotion = new CloneItem(5002, Item.potion, "Restores all HP").setUnlocalizedName("highPotion");
		ItemStack highHealthPotionStack = new ItemStack(highHealthPotion);
		GameRegistry.addShapelessRecipe(highHealthPotionStack, medHealthPotion, medHealthPotion, Item.redstone);
		LanguageRegistry.addName(highHealthPotion, "H.Pot");
		
		Item smallManaPotion = new CloneItem(5003, Item.potion, "Restores 10 MP").setUnlocalizedName("smallManaPotion");
		ItemStack smallManaPotionStack = new ItemStack(smallManaPotion);
		GameRegistry.addShapelessRecipe(smallManaPotionStack, Item.fishRaw);
		GameRegistry.addShapelessRecipe(smallManaPotionStack, Item.fishCooked);
		LanguageRegistry.addName(smallManaPotion, "S.Mana Pot");
		
		Item highManaPotion = new CloneItem(5004, Item.potion, "Restores 100 MP").setUnlocalizedName("highManaPotion");
		ItemStack highManaPotionStack = new ItemStack(highManaPotion);
		GameRegistry.addShapelessRecipe(highManaPotionStack, smallManaPotion, smallManaPotion, Item.redstone);
		LanguageRegistry.addName(highManaPotion, "H.Mana Pot");
		
		Item elixir = new CloneItem(5005, Item.potion, "Restores all HP and MP").setUnlocalizedName("elixir");
		ItemStack elixirStack = new ItemStack(elixir);
		GameRegistry.addShapelessRecipe(elixirStack, highManaPotion, highHealthPotion, Item.emerald);
		LanguageRegistry.addName(elixir, "Elixir");
		
		Item megalixir = new CloneItem(5006, Item.potion, "Restores entire party HP and MP").setUnlocalizedName("megalixir");
		ItemStack megalixirStack = new ItemStack(megalixir);
		GameRegistry.addShapelessRecipe(megalixirStack, elixir, elixir, Item.dyePowder);
		LanguageRegistry.addName(megalixir, "Megalixir");
		
		Item antidote = new CloneItem(5007, Item.potion, "Cures Poison").setUnlocalizedName("antidote");
		ItemStack antidoteStack = new ItemStack(antidote);
		GameRegistry.addShapelessRecipe(antidoteStack, Block.plantYellow, Block.mushroomRed);
		LanguageRegistry.addName(antidote, "Antidote");
		
		Item echoScreen = new CloneItem(5008, Item.potion, "Cures Silence").setUnlocalizedName("echoScreen");
		ItemStack echoScreenStack = new ItemStack(echoScreen);
		GameRegistry.addShapelessRecipe(echoScreenStack, Item.feather, Block.plantRed);
		LanguageRegistry.addName(echoScreen, "Echo Screen");
		
		Item parlyzHeal = new CloneItem(5009, Item.potion, "Cures Paralysis").setUnlocalizedName("parlyzHeal");
		ItemStack parlyzHealStack = new ItemStack(parlyzHeal);
		GameRegistry.addShapelessRecipe(parlyzHealStack, Item.egg, Block.mushroomBrown);
		LanguageRegistry.addName(parlyzHeal, "Parlyz Heal");
		
		Item pinwheel = new CloneItem(5010, Item.potion, "Cures Confusion").setUnlocalizedName("pinwheel");
		ItemStack pinwheelStack = new ItemStack(pinwheel);
		GameRegistry.addShapelessRecipe(pinwheelStack, Item.feather, Item.paper);
		LanguageRegistry.addName(pinwheel, "Pinwheel");
		
		Item eyeDrops = new CloneItem(5011, Item.potion, "Cures Blindness").setUnlocalizedName("eyeDrops");
		ItemStack eyeDropsStack = new ItemStack(eyeDrops);
		GameRegistry.addShapelessRecipe(eyeDropsStack, Block.plantYellow, Block.plantRed, Item.glassBottle);
		LanguageRegistry.addName(eyeDrops, "Eye Drops");
		
		Item panacea = new CloneItem(5012, Item.potion, "Recovers status").setUnlocalizedName("panacea");
		ItemStack panaceaStack = new ItemStack(panacea);
		GameRegistry.addShapelessRecipe(panaceaStack, Block.plantYellow, Block.plantRed, Block.mushroomBrown, Block.mushroomRed, Item.dyePowder);
		LanguageRegistry.addName(panacea, "Panacea");
		
		Item pheonixDown = new CloneItem(5013, Item.potion, "Revives ally").setUnlocalizedName("pheonixDown");
		ItemStack pheonixDownStack = new ItemStack(pheonixDown);
		GameRegistry.addShapelessRecipe(pheonixDownStack, Item.feather, Item.bucketLava);
		LanguageRegistry.addName(pheonixDown, "Pheonix Down");
		
		Item fireBomb = new CloneItem(5014, Item.potion, "Deals Fire damage to one enemy").setUnlocalizedName("fireBomb");
		ItemStack fireBombStack = new ItemStack(fireBomb);
		GameRegistry.addShapelessRecipe(fireBombStack, Item.paper, Item.flint, Item.gunpowder);
		LanguageRegistry.addName(fireBomb, "Fire Bomb");
		
		Item earthGem = new CloneItem(5015, Item.potion, "Deals Earth damage to one enemy").setUnlocalizedName("earthGem");
		ItemStack earthGemStack = new ItemStack(earthGem);
		GameRegistry.addShapelessRecipe(earthGemStack, Block.dirt, Item.flint, Block.blockClay);
		LanguageRegistry.addName(earthGem, "Earth Gem");
		
		Item iceCrystal = new CloneItem(5016, Item.potion, "Deals Ice damage to one enemy").setUnlocalizedName("iceCrystal");
		ItemStack iceCrystalStack = new ItemStack(iceCrystal);
		GameRegistry.addShapelessRecipe(iceCrystalStack, Block.ice, Item.glassBottle, Item.coal);
		LanguageRegistry.addName(iceCrystal, "Ice Crystal");
		
		Item lightningRod = new CloneItem(5017, Item.potion, "Deals Lightning damage to one enemy").setUnlocalizedName("lightningRod");
		ItemStack lightningRodStack = new ItemStack(lightningRod);
		GameRegistry.addShapelessRecipe(lightningRodStack, Item.stick, Item.ingotIron, Item.coal);
		LanguageRegistry.addName(lightningRod, "Lightning Rod");
	}
	
//	@ForgeSubscribe
//	@SideOnly(Side.CLIENT)
//	public void onSoundFX(PlaySoundEffectSourceEvent sfx)
//	{
//		float masterVolume = 1F;
//		try 
//		{
//			Field options = sfx.manager.getClass().getDeclaredField("options");
//			options.setAccessible(true);
//			masterVolume = ((GameSettings)options.get(sfx.manager)).soundVolume;
//		} 
//		catch (Exception e) {}
//		
//		
//		float volume = sfx.manager.sndSystem.getVolume(sfx.name);
//		if(volume != 0 && volume < (masterVolume * .25F))
//		{
//			sfx.manager.sndSystem.setVolume(sfx.name, volume * 4.0F);
//		}
//	}
}