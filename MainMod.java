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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Random;

import javax.swing.text.html.parser.Entity;

import org.lwjgl.input.Keyboard;
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
import TBC.ZoneGeneration.ZoneGenerationMod;
import TBC.ZoneGeneration.ZoneHandler;
import TBC.ZoneGeneration.ZoneChunkData;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleIndexedCodec;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.SidedProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.EnumDifficulty;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class MainMod
{
	private static boolean loadedProgress = false;
	private int questProgress = 0;

	public static boolean playerDataInit = false;
	public static ArrayList<Pair<EntityLiving, String>> setEnemies = null;
	public static EntityLivingBase enemy = null;
	public static boolean isPlayerAttacker;
	public static float lastAttackTime = 0;
	private ILevelScale levelScaling;
	private boolean wasKeyPressed = false;
	public DistanceBasedLevelScaling distanceScaling;
	
	public static SimpleNetworkWrapper setItemDataHandler;
	public static SimpleNetworkWrapper syncPlayerDataHandler;
	public static SimpleNetworkWrapper setHealthHandler;
	public static SimpleNetworkWrapper removeItemHandler;
	public static SimpleNetworkWrapper openGuiHandler;
	
	public static KeyBinding openTBCGui;
	
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
			try {
				InputStream resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("tbc", "files/" + fileName)).getInputStream();
				Files.copy(new InputStreamSupplier(resource), configFile);
			} catch (IOException e) {
				FMLLog.severe("Unable to copy default config file. %s", e.toString());
			}
		}
		return configFile;
	}

	public void load(FMLInitializationEvent evt)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(TBCMod.instance, new TBCGuiHandler());
		setItemDataHandler = new SimpleNetworkWrapper("TBCSetDur");
		setItemDataHandler.registerMessage(SetItemDataHandler.class, StringMessage.class, 0, Side.SERVER);
		setHealthHandler = new SimpleNetworkWrapper("TBCSetHealth");
		setHealthHandler.registerMessage(SetEntityHealthHandler.class, StringMessage.class, 0, Side.SERVER);
		syncPlayerDataHandler = new SimpleNetworkWrapper("TBCPlayerData");
		syncPlayerDataHandler.registerMessage(SyncPlayerDataHandler.class, NBTTagCompoundMessage.class, 0, Side.SERVER);
		syncPlayerDataHandler.registerMessage(SyncPlayerDataHandler.class, NBTTagCompoundMessage.class, 0, Side.CLIENT);
		removeItemHandler = new SimpleNetworkWrapper("TBCRemoveItem");
		removeItemHandler.registerMessage(RemoveItemHandler.class, StringMessage.class, 0, Side.SERVER);
		openGuiHandler = new SimpleNetworkWrapper("TBCOpenGui");
		openGuiHandler.registerMessage(OpenGuiHandler.class, StringMessage.class, 0, Side.CLIENT);
		openTBCGui = new KeyBinding("Open Character Screen", Keyboard.KEY_TAB, "TBC Keys");
		ClientRegistry.registerKeyBinding(openTBCGui);
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
				syncPlayerDataHandler.sendToServer(new StringMessage());
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
		ZoneChunkData data = ZoneHandler.Instance.GetRegionData(c.getChunkCoordIntPair(), biome);
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
				lastAttemptedSync = systemTime;
				StringMessage zoneDataRequest = new StringMessage();
				zoneDataRequest.Data = c.xPosition + "," + c.zPosition;
				ZoneGenerationMod.zoneDataHandler.sendToServer(zoneDataRequest);
			}
		}

		if(!playerDataInit || mc.theWorld.difficultySetting != EnumDifficulty.HARD)
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
		NBTTagCompoundMessage message = new NBTTagCompoundMessage();
		message.tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		syncPlayerDataHandler.sendToServer(message);
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
				if(Item.getIdFromItem(drops.get(i).getEntityItem().getItem()) == Item.getIdFromItem(toReplace))
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
        List list = evt.entity.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox((double)evt.x - d0, (double)evt.y - d1, (double)evt.z - d0, (double)evt.x + d0, (double)evt.y + d1, (double)evt.z + d0));
        if (!list.isEmpty())
        {
            return;
        }

		CombatEntity entity = CombatEntityLookup.Instance.GetCombatEntityForPlayer(evt.entityPlayer);
		evt.entityPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setInteger("TBCPlayerMP", entity.GetMaxMp());

		if(evt.entityPlayer instanceof EntityPlayerMP)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)evt.entityPlayer;
			NBTTagCompoundMessage playerDataMessage = new NBTTagCompoundMessage();
			playerDataMessage.tag = mpPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			syncPlayerDataHandler.sendTo(playerDataMessage, mpPlayer);
			
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
					
					StringMessage itemDurMessage = new StringMessage();
					itemDurMessage.Data = i + ",0," + hench.GetMaxMp();
					setItemDataHandler.sendTo(itemDurMessage, mpPlayer);
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
		   !(sourceEntity instanceof EntityLivingBase) ||
		   sourceEntity == e.entityLiving ||
		   e.source.damageType == "bypass")
		{
			return;
		}

		EntityLivingBase sourceLivingEntity = (EntityLivingBase)sourceEntity;
		if(!sourceLivingEntity.isEntityAlive() || !e.entityLiving.isEntityAlive())
		{
			return;
		}

		EntityLivingBase enemy = null;
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

	public void onWorldUnload(WorldEvent.Unload evt)
	{
		CombatEntityLookup.Instance.ClearCombatEntitiesForPlayers();
		//ZoneHandler.Instance.ClearData();
		this.questProgress = 0;
		this.loadedProgress = false;
		this.lastAttackTime = 0;
		this.enemy = null;
		this.setEnemies = null;
		this.playerDataInit = false;
	}

	public void SetupStaticItems()
	{
		Item smallHealthPotion = createItemCopy(Items.potionitem, "smallPotion", "Restores 20 HP");
		ItemStack smallHealthPotionStack = new ItemStack(smallHealthPotion);
		GameRegistry.addShapelessRecipe(smallHealthPotionStack, Items.apple);
		LanguageRegistry.addName(smallHealthPotion, "S.Pot");

		Item medHealthPotion = createItemCopy(Items.potionitem, "medPotion", "Restores 50 HP");
		ItemStack medHealthPotionStack = new ItemStack(medHealthPotion);
		GameRegistry.addShapelessRecipe(medHealthPotionStack, smallHealthPotion, smallHealthPotion, Items.bread);
		LanguageRegistry.addName(medHealthPotion, "M.Pot");

		Item highHealthPotion = createItemCopy(Items.potionitem, "highPotion", "Restores all HP");
		ItemStack highHealthPotionStack = new ItemStack(highHealthPotion);
		GameRegistry.addShapelessRecipe(highHealthPotionStack, medHealthPotion, medHealthPotion, Items.redstone);
		LanguageRegistry.addName(highHealthPotion, "H.Pot");

		Item smallManaPotion = createItemCopy(Items.potionitem, "smallManaPotion", "Restores 10 MP");
		ItemStack smallManaPotionStack = new ItemStack(smallManaPotion);
		GameRegistry.addShapelessRecipe(smallManaPotionStack, Items.fish);
		GameRegistry.addShapelessRecipe(smallManaPotionStack, Items.cooked_fished);
		LanguageRegistry.addName(smallManaPotion, "S.Mana Pot");

		Item highManaPotion = createItemCopy(Items.potionitem, "highManaPotion", "Restores 100 MP");
		ItemStack highManaPotionStack = new ItemStack(highManaPotion);
		GameRegistry.addShapelessRecipe(highManaPotionStack, smallManaPotion, smallManaPotion, Items.redstone);
		LanguageRegistry.addName(highManaPotion, "H.Mana Pot");

		Item elixir = createItemCopy(Items.potionitem, "elixir", "Restores all HP and MP");
		ItemStack elixirStack = new ItemStack(elixir);
		GameRegistry.addShapelessRecipe(elixirStack, highManaPotion, highHealthPotion, Items.emerald);
		LanguageRegistry.addName(elixir, "Elixir");

		Item megalixir = createItemCopy(Items.potionitem, "megalixir", "Restores entire party HP and MP");
		ItemStack megalixirStack = new ItemStack(megalixir);
		GameRegistry.addShapelessRecipe(megalixirStack, elixir, elixir, Items.dye);
		LanguageRegistry.addName(megalixir, "Megalixir");

		Item antidote = createItemCopy(Items.potionitem, "antidote", "Cures Poison");
		ItemStack antidoteStack = new ItemStack(antidote);
		GameRegistry.addShapelessRecipe(antidoteStack, Blocks.yellow_flower, Blocks.red_mushroom);
		LanguageRegistry.addName(antidote, "Antidote");

		Item echoScreen = createItemCopy(Items.potionitem, "echoScreen", "Cures Silence");
		ItemStack echoScreenStack = new ItemStack(echoScreen);
		GameRegistry.addShapelessRecipe(echoScreenStack, Items.feather, Blocks.red_flower);
		LanguageRegistry.addName(echoScreen, "Echo Screen");

		Item parlyzHeal = createItemCopy(Items.potionitem, "parlyzHeal", "Cures Paralysis");
		ItemStack parlyzHealStack = new ItemStack(parlyzHeal);
		GameRegistry.addShapelessRecipe(parlyzHealStack, Items.egg, Blocks.brown_mushroom);
		LanguageRegistry.addName(parlyzHeal, "Parlyz Heal");

		Item pinwheel = createItemCopy(Items.potionitem, "pinwheel", "Cures Confusion");
		ItemStack pinwheelStack = new ItemStack(pinwheel);
		GameRegistry.addShapelessRecipe(pinwheelStack, Items.feather, Items.paper);
		LanguageRegistry.addName(pinwheel, "Pinwheel");

		Item eyeDrops = createItemCopy(Items.potionitem, "eyeDrops", "Cures Blindness");
		ItemStack eyeDropsStack = new ItemStack(eyeDrops);
		GameRegistry.addShapelessRecipe(eyeDropsStack, Blocks.yellow_flower, Blocks.red_flower, Items.glass_bottle);
		LanguageRegistry.addName(eyeDrops, "Eye Drops");

		Item panacea = createItemCopy(Items.potionitem, "panacea", "Recovers status");
		ItemStack panaceaStack = new ItemStack(panacea);
		GameRegistry.addShapelessRecipe(panaceaStack, Blocks.yellow_flower, Blocks.red_flower, Blocks.brown_mushroom, Blocks.red_mushroom, Items.dye);
		LanguageRegistry.addName(panacea, "Panacea");
		
		Item pheonixDown = createItemCopy(Items.potionitem, "pheonixDown", "Revives ally");
		ItemStack pheonixDownStack = new ItemStack(pheonixDown);
		GameRegistry.addShapelessRecipe(pheonixDownStack, Items.feather, Items.lava_bucket);
		LanguageRegistry.addName(pheonixDown, "Pheonix Down");

		Item fireBomb = createItemCopy(Items.potionitem, "fireBomb", "Deals Fire damage to one enemy");
		ItemStack fireBombStack = new ItemStack(fireBomb);
		GameRegistry.addShapelessRecipe(fireBombStack, Items.paper, Items.flint, Items.gunpowder);
		LanguageRegistry.addName(fireBomb, "Fire Bomb");

		Item earthGem = createItemCopy(Items.potionitem, "earthGem", "Deals Earth damage to one enemy");
		ItemStack earthGemStack = new ItemStack(earthGem);
		GameRegistry.addShapelessRecipe(earthGemStack, Blocks.dirt, Items.flint, Blocks.clay);
		LanguageRegistry.addName(earthGem, "Earth Gem");

		Item iceCrystal = createItemCopy(Items.potionitem, "iceCrystal", "Deals Ice damage to one enemy");
		ItemStack iceCrystalStack = new ItemStack(iceCrystal);
		GameRegistry.addShapelessRecipe(iceCrystalStack, Blocks.ice, Items.glass_bottle, Items.coal);
		LanguageRegistry.addName(iceCrystal, "Ice Crystal");

		Item lightningRod = createItemCopy(Items.potionitem, "lightningRod", "Deals Lightning damage to one enemy");
		ItemStack lightningRodStack = new ItemStack(lightningRod);
		GameRegistry.addShapelessRecipe(lightningRodStack, Items.stick, Items.iron_ingot, Items.coal);
		LanguageRegistry.addName(lightningRod, "Lightning Rod");
	}

	public void keyDown(InputEvent.KeyInputEvent evt)
	{
		if(wasKeyPressed && openTBCGui.getIsKeyPressed())
		{
			return;
		}
		
		if(!openTBCGui.getIsKeyPressed())
		{
			wasKeyPressed = false;
			return;
		}

		wasKeyPressed = true;
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.thePlayer != null)
		{
			if(mc.currentScreen instanceof StatsGui)
			{
				mc.thePlayer.closeScreen();
				mc.displayGuiScreen((GuiScreen)null);
				return;
			}

			EntityPlayer player = mc.thePlayer;
			player.openGui(TBCMod.instance, 1, player.worldObj, player.serverPosX, player.serverPosY, player.serverPosZ);
		}
	}

	public void onItemTooltip(ItemTooltipEvent evt) 
	{
		int attackIndex = -1;
		for(int i = 0; i < evt.toolTip.size(); i++)
		{
			if(evt.toolTip.get(i).contains("Attack Damage"))
			{
				attackIndex = i;
				break;
			}
		}
				
		String name = evt.itemStack.getItem().getUnlocalizedName().replace("item.", "");
        if(EquippedItemManager.Instance.lookup.containsKey(name))
        {
        	String displayString = EquippedItemManager.Instance.lookup.get(name).GetDisplayString();
        	if(displayString != null && displayString.length() > 0)
        	{
        		if(attackIndex != -1)
        		{
        			evt.toolTip.set(attackIndex, displayString);
        			evt.toolTip.remove(attackIndex - 1);
        		}
        		else
        		{
        			evt.toolTip.add(displayString);
        		}
        	}
        }
        else if(attackIndex != -1)
        {
        	evt.toolTip.remove(attackIndex);
        	evt.toolTip.remove(attackIndex - 1);
        }
	}
	
	private Item createItemCopy(Item itemToCopy, String name, String description)
	{
		Item newItem = new CloneItem(itemToCopy, description).setUnlocalizedName(name);
		GameRegistry.registerItem(newItem, name, "tbc");
		return newItem;
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