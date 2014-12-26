package TBC;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.input.Keyboard;

import TBC.Combat.AreaBasedLevelScaling;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.Combat.CombatEntitySpawnLookup;
import TBC.Combat.CompositeLevelScaling;
import TBC.Combat.DepthBasedLevelScaling;
import TBC.Combat.DistanceBasedLevelScaling;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.ILevelScale;
import TBC.Combat.ItemReplacementLookup;
import TBC.Combat.JobLookup;
import TBC.Combat.LevelingEngine;
import TBC.Combat.TimeBasedLevelScaling;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.CombatScreen.Battle;
import TBC.CombatScreen.BattleScreenClient;
import TBC.CombatScreen.EndCombatHandler;
import TBC.CombatScreen.PlayerCommandHandler;
import TBC.CombatScreen.PlayerControlHandler;
import TBC.CombatScreen.StartCombatHandler;
import TBC.CombatScreen.SyncCombatDataHandler;
import TBC.Menu.StatsGui;
import TBC.Messages.CombatCommandMessage;
import TBC.Messages.CombatEndedMessage;
import TBC.Messages.CombatPlayerControlMessage;
import TBC.Messages.CombatStartedMessage;
import TBC.Messages.CombatSyncDataMessage;
import TBC.Messages.ItemDataMessage;
import TBC.Messages.NBTTagCompoundMessage;
import TBC.Messages.StringMessage;
import TBC.ZoneGeneration.ZoneChunkData;
import TBC.ZoneGeneration.ZoneGenerationMod;
import TBC.ZoneGeneration.ZoneHandler;

import com.google.common.io.Files;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MainMod
{
	public static final Object syncObj = new Object();
	public static HashMap<Long, Battle> ServerBattles = new HashMap<Long, Battle>(); 
	public static BattleScreenClient ClientBattle = null;
	
	private static boolean loadedProgress = false;

	public static boolean playerDataInit = false;
	public static ArrayList<Pair<String, String>> setEnemies = null;
	public static EntityLivingBase enemy = null;
	public static boolean isPlayerAttacker;
	public static HashMap<EntityPlayer, Long> lastAttackTimes = new HashMap<EntityPlayer, Long>();
	
	private ILevelScale levelScaling;
	public DistanceBasedLevelScaling distanceScaling;
	private long lastAttemptedSync = 0;
	private long startupTime = 0;
	private int questProgress = 0;
	private long lastBattleId = 0;
	
	public static SimpleNetworkWrapper setItemDataHandler;
	public static SimpleNetworkWrapper syncPlayerDataHandler;
	public static SimpleNetworkWrapper setHealthHandler;
	public static SimpleNetworkWrapper removeItemHandler;
	public static SimpleNetworkWrapper addItemHandler;
	public static SimpleNetworkWrapper openGuiHandler;
	
	public static SimpleNetworkWrapper combatStartedHandler;
	public static SimpleNetworkWrapper playerCommandHandler;
	public static SimpleNetworkWrapper playerControlHandler;
	public static SimpleNetworkWrapper syncCombatDataHandler;
	public static SimpleNetworkWrapper combatEndedHandler;
	
	public static KeyBinding openTBCGui;
	
	public void preInit(FMLPreInitializationEvent evt)
	{
		File enemyConfigFile = this.loadFileFromJar("TBCTemplates.csv");
		File itemConfigFile = this.loadFileFromJar("TBCItemTemplates.csv");
		File spawnConfigFile = this.loadFileFromJar("TBCWorldMobData.csv");
		File enchantedItemFile = this.loadFileFromJar("TBCEnchantedItems.csv");
		
		File jobPrereqsFile = this.loadFileFromJar("TBCJobPrereqs.csv");
		File jobSkillsFile = this.loadFileFromJar("TBCJobSkills.csv");
		File jobStatsFile = this.loadFileFromJar("TBCJobStats.csv");

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
		EnchantedItemManager.Instance.Initialize(enchantedItemFile);

		JobLookup.Instance.InitializeStats(jobStatsFile);
		JobLookup.Instance.InitializeSkills(jobSkillsFile);
		JobLookup.Instance.InitializePrereqs(jobPrereqsFile);
		
		CombatEntitySpawnLookup.Instance.LogUnknownEntities();
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
		setItemDataHandler.registerMessage(SetItemDataHandler.class, ItemDataMessage.class, 0, Side.SERVER);
		setHealthHandler = new SimpleNetworkWrapper("TBCSetHealth");
		setHealthHandler.registerMessage(SetEntityHealthHandler.class, StringMessage.class, 0, Side.SERVER);
		syncPlayerDataHandler = new SimpleNetworkWrapper("TBCPlayerData");
		syncPlayerDataHandler.registerMessage(SyncPlayerDataHandler.class, NBTTagCompoundMessage.class, 0, Side.SERVER);
		removeItemHandler = new SimpleNetworkWrapper("TBCRemoveItem");
		removeItemHandler.registerMessage(RemoveItemHandler.class, StringMessage.class, 0, Side.SERVER);
		addItemHandler = new SimpleNetworkWrapper("TBCAddItem");
		addItemHandler.registerMessage(AddItemHandler.class, NBTTagCompoundMessage.class, 0, Side.SERVER);
		openGuiHandler = new SimpleNetworkWrapper("TBCOpenGui");
		
		combatStartedHandler = new SimpleNetworkWrapper("TBCCombatStart");
		playerControlHandler = new SimpleNetworkWrapper("TBCPlayerTurn");
		playerCommandHandler = new SimpleNetworkWrapper("TBCCombatCommand");
		playerCommandHandler.registerMessage(PlayerCommandHandler.class, CombatCommandMessage.class, 0, Side.SERVER);
		syncCombatDataHandler = new SimpleNetworkWrapper("TBCCombatSync");
		combatEndedHandler = new SimpleNetworkWrapper("TBCCombatEnd");
				
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			loadClient(evt);
		}
	}

	public void loadClient(FMLInitializationEvent evt)
	{
		syncPlayerDataHandler.registerMessage(SyncPlayerDataHandlerClient.class, NBTTagCompoundMessage.class, 0, Side.CLIENT);
		openGuiHandler.registerMessage(OpenGuiHandler.class, StringMessage.class, 0, Side.CLIENT);
		combatStartedHandler.registerMessage(StartCombatHandler.class, CombatStartedMessage.class, 0, Side.CLIENT);
		playerControlHandler.registerMessage(PlayerControlHandler.class, CombatPlayerControlMessage.class, 0, Side.CLIENT);
		syncCombatDataHandler.registerMessage(SyncCombatDataHandler.class, CombatSyncDataMessage.class, 0, Side.CLIENT);
		combatEndedHandler.registerMessage(EndCombatHandler.class, CombatEndedMessage.class, 0, Side.CLIENT);
		
		openTBCGui = new KeyBinding("Open Character Screen", Keyboard.KEY_TAB, "TBC Keys");
		ClientRegistry.registerKeyBinding(openTBCGui);
	}
	
	@SideOnly(Side.CLIENT)
	public void syncPlayerData(EntityEvent.EnteringChunk buildingEntity)
	{
		if(buildingEntity.oldChunkX != 0 || buildingEntity.oldChunkZ != 0)
		{
			return;
		}

		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && buildingEntity.entity instanceof EntityPlayer)
		{
			EntityPlayer playerEntity = (EntityPlayer)buildingEntity.entity;
			if(!PlayerSaveData.GetPlayerTag(playerEntity).hasKey("playerLevel"))
			{
				syncPlayerDataHandler.sendToServer(new StringMessage());
			}
		}
	}

	@SideOnly(Side.CLIENT)
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
			questProgress = LevelingEngine.Instance.GetPlayerSaveData(mc.thePlayer).QuestProgress;
			loadedProgress = true;
		}

		long currentTime = Minecraft.getSystemTime();
		if(startupTime == 0)
		{
			startupTime = currentTime;
		}

//		if(this.enemy == null && this.setEnemies == null && mc.theWorld.getWorldTime() > questProgress * 24000)
//		{
//			if(this.startupTime + 1000 > currentTime || this.lastAttackTime + 1000 > currentTime)
//			{
//				return;
//			}
//
//			this.lastAttackTime = currentTime;
//			setEnemies = new ArrayList<Pair<String,String>>();
//			questProgress = questProgress + 1;
//			CombatEntitySaveData questData = LevelingEngine.Instance.GetXpDataForPlayer(mc.thePlayer);
//			questData.QuestProgress = questProgress;
//			LevelingEngine.Instance.SaveXpDataForPlayer(mc.thePlayer, questData);
//			SyncTagToServer(mc.thePlayer);
//
//			SetQuestBattle(mc);
//			mc.thePlayer.openGui(TBCMod.instance, 0, mc.thePlayer.worldObj, mc.thePlayer.serverPosX, mc.thePlayer.serverPosY, mc.thePlayer.serverPosZ);
//		}
	}

	private void SetQuestBattle(Minecraft mc)
	{
		if(questProgress == 2)
		{
			setEnemies.add(new Pair("Skeleton", "Decrepit Skeleton"));
		}
		else if(questProgress == 3)
		{
			setEnemies.add(new Pair("Creeper", "Creeper"));
		}
		else if(questProgress == 4)
		{
			setEnemies.add(new Pair("Zombie", "Ghoul"));
		}
		else if(questProgress == 5)
		{
			setEnemies.add(new Pair("Slime", "Brown Ooze"));
		}
		else if(questProgress == 6)
		{
			setEnemies.add(new Pair("Skeleton", "Skeleton Apprentice"));
			setEnemies.add(new Pair("Skeleton", "Skeleton Apprentice"));
		}
		else if(questProgress == 7)
		{
			setEnemies.add(new Pair("Creeper", "Overcharged Creeper"));
			setEnemies.add(new Pair("Creeper", "Overcharged Creeper"));
		}
		else if(questProgress == 8)
		{
			setEnemies.add(new Pair("Zombie", "Vampire"));
			setEnemies.add(new Pair("Zombie", "Vampire"));
		}
		else if(questProgress == 9)
		{
			setEnemies.add(new Pair("Spider", "Wraith Spider"));
			setEnemies.add(new Pair("Spider", "Wraith Spider"));
			setEnemies.add(new Pair("Spider", "Wraith Spider"));
			setEnemies.add(new Pair("Spider", "Wraith Spider"));
		}
		else if(questProgress == 10)
		{
			setEnemies.add(new Pair("Slime", "Black Pudding"));
		}
		else if(questProgress == 11)
		{
			setEnemies.add(new Pair("Skeleton", "Skeleton King"));
		}
		else if(questProgress == 12)
		{
			setEnemies.add(new Pair("Creeper", "Creeper Ancient"));
			setEnemies.add(new Pair("Creeper", "Creeper Ancient"));
			setEnemies.add(new Pair("Creeper", "Creeper Ancient"));
			setEnemies.add(new Pair("Creeper", "Creeper Ancient"));
		}
		else if(questProgress == 13)
		{
			setEnemies.add(new Pair("Enderman", "Enderguard"));
		}
		else if(questProgress == 14)
		{
			setEnemies.add(new Pair("Zombie", "Revenant"));
			setEnemies.add(new Pair("Skeleton", "Skeleton King"));
			setEnemies.add(new Pair("Spider", "Spider Queen"));
			setEnemies.add(new Pair("Creeper", "Creeper Ancient"));
		}
		else if(questProgress == 15)
		{
			setEnemies.add(new Pair("Enderman", "Enderlord"));
		}
		else if(questProgress == 16)
		{
			setEnemies.add(new Pair("EnderDragon", "Ender Dragon"));
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
		PlayerSaveData.GetPlayerTag(evt.entityPlayer).setInteger("TBCPlayerMP", entity.GetMaxMp());

		if(evt.entityPlayer instanceof EntityPlayerMP)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)evt.entityPlayer;
			NBTTagCompoundMessage playerDataMessage = new NBTTagCompoundMessage();
			playerDataMessage.tag = PlayerSaveData.GetPlayerTag(mpPlayer);
			syncPlayerDataHandler.sendTo(playerDataMessage, mpPlayer);
			
			ItemStack[] p = mpPlayer.inventory.mainInventory;
			for(int i = 0; i<mpPlayer.inventory.mainInventory.length; i++)
			{
				ItemStack s  = mpPlayer.inventory.mainInventory[i];
				if(s != null && s.getItem() instanceof HenchmanItem)
				{
					HenchmanItem item = (HenchmanItem)s.getItem();
					CombatEntity hench = CombatEntityLookup.Instance.GetCombatEntity(0, item.henchmanType, item.henchmanName, HenchmanItem.GetTag(s));
					s.setItemDamage(0);
					NBTTagCompound tag = s.getTagCompound();
					if(tag == null)
					{
						tag = new NBTTagCompound();
					}

					tag.setInteger("HenchMP", hench.GetMaxMp());
					s.setTagCompound(tag);
					
					ItemDataMessage itemDurMessage = new ItemDataMessage();
					CombatEntitySaveData d = HenchmanItem.GetCombatEntitySaveData(s);
					d.CurrentMp = hench.GetMaxMp();
					HenchmanItem.SetCombatEntitySaveData(d, s);
					itemDurMessage.Slot = i;
					itemDurMessage.ItemDurability = 0;
					itemDurMessage.tag = s.getTagCompound();
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
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT || e.isCanceled())
		{
			return;
		}
		
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
		EntityPlayerMP player = null;

		if(e.entityLiving instanceof EntityPlayerMP)
		{
			enemy = (EntityLivingBase)sourceEntity;
			if(sourceEntity instanceof EntityPlayerMP)
			{
				// PVP not currently supported
				e.setCanceled(true);
				return;
			}
			
			isPlayerAttacker = false;
			player = (EntityPlayerMP)e.entityLiving;
		}
		else if(sourceEntity instanceof EntityPlayerMP)
		{
			enemy = e.entityLiving;
			isPlayerAttacker = true;
			player = (EntityPlayerMP)sourceEntity;
		}

		if(player != null)
		{
			e.setCanceled(true);

			synchronized (syncObj) 
			{
				if(e.entityLiving.worldObj.getEntityByID(e.entityLiving.getEntityId()) == null)
				{
					return;
				}

				float currentTime = MinecraftServer.getServer().getSystemTimeMillis();
				if(lastAttackTimes.containsKey(player) && lastAttackTimes.get(player) + 2000 > currentTime)
				{
					return;
				}
				
				for(Battle b : this.ServerBattles.values())
				{
					if(b.GetInvolvedPlayers().contains(player))
					{
						return;
					}
				}
				
				Battle b = new Battle(lastBattleId++, player, enemy, isPlayerAttacker);
				this.ServerBattles.put(b.id, b);
				b.SpawnInWorld(player);
				this.combatStartedHandler.sendTo(b.GetBattleStartMessage(), player);
				b.DoNextTurn();
			}
		}
	}

	public void onWorldUnload(WorldEvent.Unload evt)
	{
		CombatEntityLookup.Instance.ClearCombatEntitiesForPlayers();
		//ZoneHandler.Instance.ClearData();
		this.questProgress = 0;
		this.loadedProgress = false;
		lastAttackTimes = new HashMap<EntityPlayer, Long>();
		this.enemy = null;
		this.setEnemies = null;
		this.playerDataInit = false;
	}

	public void SetupStaticItems()
	{
		EntityRegistry.registerModEntity(BattleEntity.class, "Battle", 1, TBCMod.instance, 40, 1, false);
		
		Item weaponCore = createItemCopy(Items.ender_eye, "weaponCore");
		ItemStack weaponCoreStack = new ItemStack(weaponCore);
		GameRegistry.addShapedRecipe(weaponCoreStack, " y "," y "," x ", 'x',Items.stick, 'y', Blocks.gravel);
		LanguageRegistry.addName(weaponCoreStack, "Weapon Core");
		
		Item armorCore = createItemCopy(Items.ender_pearl, "armorCore");
		ItemStack armorCoreStack = new ItemStack(armorCore);
		GameRegistry.addShapedRecipe(weaponCoreStack, "x x","xxx","xxx", 'x',Blocks.gravel);
		LanguageRegistry.addName(armorCoreStack, "Armor Core");
		
		//Greatswords
		Item stoneGSword = createItemCopy(Items.stone_sword, "stoneGSword");
		ItemStack stoneGSwordStack = new ItemStack(stoneGSword);
		GameRegistry.addShapedRecipe(stoneGSwordStack, " y "," y "," x ", 'x',weaponCore, 'y', Blocks.cobblestone);
		LanguageRegistry.addName(stoneGSwordStack, "Stone Greatsword");
		
		Item ironGSword = createItemCopy(Items.iron_sword, "ironGSword");
		ItemStack ironGSwordStack = new ItemStack(ironGSword);
		GameRegistry.addShapedRecipe(ironGSwordStack, " y "," y "," x ", 'x',weaponCore, 'y', Items.iron_ingot);
		LanguageRegistry.addName(ironGSwordStack, "Iron Greatsword");
		
		Item goldGSword = createItemCopy(Items.golden_sword, "goldGSword");
		ItemStack goldGSwordStack = new ItemStack(goldGSword);
		GameRegistry.addShapedRecipe(goldGSwordStack, " y "," y "," x ", 'x',weaponCore, 'y', Items.gold_ingot);
		LanguageRegistry.addName(goldGSwordStack, "Gold Greatsword");
		
		Item diamondGSword = createItemCopy(Items.diamond_sword, "diamondGSword");
		ItemStack diamondGSwordStack = new ItemStack(diamondGSword);
		GameRegistry.addShapedRecipe(diamondGSwordStack, " y "," y "," x ", 'x',weaponCore, 'y', Items.diamond);
		LanguageRegistry.addName(diamondGSwordStack, "Diamond Greatsword");
		
		//Maces
		Item stoneMace = createItemCopy(Items.stone_sword, "stoneMace");
		ItemStack stoneMaceStack = new ItemStack(stoneMace);
		GameRegistry.addShapedRecipe(stoneMaceStack, "yyy"," y "," x ", 'x',weaponCore, 'y', Blocks.cobblestone);
		LanguageRegistry.addName(stoneMaceStack, "Stone Mace");
		
		Item ironMace = createItemCopy(Items.iron_sword, "ironMace");
		ItemStack ironMaceStack = new ItemStack(ironMace);
		GameRegistry.addShapedRecipe(ironMaceStack, "yyy"," y "," x ", 'x',weaponCore, 'y', Items.iron_ingot);
		LanguageRegistry.addName(ironMaceStack, "Iron Mace");
		
		Item goldMace = createItemCopy(Items.golden_sword, "goldMace");
		ItemStack goldMaceStack = new ItemStack(goldMace);
		GameRegistry.addShapedRecipe(goldMaceStack, "yyy"," y "," x ", 'x',weaponCore, 'y', Items.gold_ingot);
		LanguageRegistry.addName(goldMaceStack, "Gold Mace");
		
		Item diamondMace = createItemCopy(Items.diamond_sword, "diamondMace");
		ItemStack diamondMaceStack = new ItemStack(diamondMace);
		GameRegistry.addShapedRecipe(diamondMaceStack, "yyy"," y "," x ", 'x',weaponCore, 'y', Items.diamond);
		LanguageRegistry.addName(diamondMaceStack, "Diamond Mace");

		//Bows
		Item ironBow = createItemCopy(Items.bow, "ironBow");
		ItemStack ironBowStack = new ItemStack(ironBow);
		GameRegistry.addShapedRecipe(ironBowStack, " z "," y "," x ", 'x',weaponCore, 'y', Items.bow, 'z', Items.iron_ingot);
		LanguageRegistry.addName(ironBowStack, "Iron Bow");
		
		Item goldBow = createItemCopy(Items.bow, "goldBow");
		ItemStack goldBowStack = new ItemStack(goldBow);
		GameRegistry.addShapedRecipe(goldBowStack, " z "," y "," x ", 'x',weaponCore, 'y', Items.bow, 'z', Items.gold_ingot);
		LanguageRegistry.addName(goldBowStack, "Gold Bow");
		
		Item diamondBow = createItemCopy(Items.bow, "diamondBow");
		ItemStack diamondBowStack = new ItemStack(diamondBow);
		GameRegistry.addShapedRecipe(diamondBowStack, " z "," y "," x ", 'x',weaponCore, 'y', Items.bow, 'z', Items.diamond);
		LanguageRegistry.addName(diamondBowStack, "Diamond Bow");
		
		
		//Daggers
		Item stoneDagger = createItemCopy(Items.stone_sword, "stoneDagger");
		ItemStack stoneDaggerStack = new ItemStack(stoneDagger);
		GameRegistry.addShapedRecipe(stoneDaggerStack, "   "," y "," x ", 'x',weaponCore, 'y', Blocks.cobblestone);
		LanguageRegistry.addName(stoneDaggerStack, "Stone Dagger");
		
		Item ironDagger = createItemCopy(Items.iron_sword, "ironDagger");
		ItemStack ironDaggerStack = new ItemStack(ironDagger);
		GameRegistry.addShapedRecipe(ironDaggerStack, "   "," y "," x ", 'x',weaponCore, 'y', Items.iron_ingot);
		LanguageRegistry.addName(ironDaggerStack, "Iron Dagger");
		
		Item goldDagger = createItemCopy(Items.golden_sword, "goldDagger");
		ItemStack goldDaggerStack = new ItemStack(goldDagger);
		GameRegistry.addShapedRecipe(goldDaggerStack, "   "," y "," x ", 'x',weaponCore, 'y', Items.gold_ingot);
		LanguageRegistry.addName(goldDaggerStack, "Gold Dagger");
		
		Item diamondDagger = createItemCopy(Items.diamond_sword, "diamondDagger");
		ItemStack diamondDaggerStack = new ItemStack(diamondDagger);
		GameRegistry.addShapedRecipe(diamondDaggerStack, "   "," y "," x ", 'x',weaponCore, 'y', Items.diamond);
		LanguageRegistry.addName(diamondDaggerStack, "Diamond Dagger");
		
		
		//Staffs
		Item stoneStaff = createItemCopy(Items.stone_sword, "stoneStaff");
		ItemStack stoneStaffStack = new ItemStack(stoneStaff);
		GameRegistry.addShapedRecipe(stoneStaffStack, "x  "," y ","  y", 'x',weaponCore, 'y', Blocks.cobblestone);
		LanguageRegistry.addName(stoneStaffStack, "Stone Staff");
		
		Item ironStaff = createItemCopy(Items.iron_sword, "ironStaff");
		ItemStack ironStaffStack = new ItemStack(ironStaff);
		GameRegistry.addShapedRecipe(ironStaffStack, "x  "," y ","  y", 'x',weaponCore, 'y', Items.iron_ingot);
		LanguageRegistry.addName(ironStaffStack, "Iron Staff");
		
		Item goldStaff = createItemCopy(Items.golden_sword, "goldStaff");
		ItemStack goldStaffStack = new ItemStack(goldStaff);
		GameRegistry.addShapedRecipe(goldStaffStack, "x  "," y ","  y", 'x',weaponCore, 'y', Items.gold_ingot);
		LanguageRegistry.addName(goldStaffStack, "Gold Staff");
		
		Item diamondStaff = createItemCopy(Items.diamond_sword, "diamondStaff");
		ItemStack diamondStaffStack = new ItemStack(diamondStaff);
		GameRegistry.addShapedRecipe(diamondStaffStack, "x  "," y ","  y", 'x',weaponCore, 'y', Items.diamond);
		LanguageRegistry.addName(diamondStaffStack, "Diamond Staff");
		
		
		//Wands
		Item stoneWand = createItemCopy(Items.stone_sword, "stoneWand");
		ItemStack stoneWandStack = new ItemStack(stoneWand);
		GameRegistry.addShapedRecipe(stoneWandStack, "   ","y  "," x ", 'x',weaponCore, 'y', Blocks.cobblestone);
		LanguageRegistry.addName(stoneWandStack, "Stone Wand");
		
		Item ironWand = createItemCopy(Items.iron_sword, "ironWand");
		ItemStack ironWandStack = new ItemStack(ironWand);
		GameRegistry.addShapedRecipe(ironWandStack, "   ","y  "," x ", 'x',weaponCore, 'y', Items.iron_ingot);
		LanguageRegistry.addName(ironWandStack, "Iron Wand");
		
		Item goldWand = createItemCopy(Items.golden_sword, "goldWand");
		ItemStack goldWandStack = new ItemStack(goldWand);
		GameRegistry.addShapedRecipe(goldWandStack, "   ","y  "," x ", 'x',weaponCore, 'y', Items.gold_ingot);
		LanguageRegistry.addName(goldWandStack, "Gold Wand");
		
		Item diamondWand = createItemCopy(Items.diamond_sword, "diamondWand");
		ItemStack diamondWandStack = new ItemStack(diamondWand);
		GameRegistry.addShapedRecipe(diamondWandStack, "   ","y  "," x ", 'x',weaponCore, 'y', Items.diamond);
		LanguageRegistry.addName(diamondWandStack, "Diamond Wand");
		
		
		//Armor
		Item clothArmor = createItemCopy(Items.leather_chestplate, "clothArmor");
		ItemStack clothArmorStack = new ItemStack(clothArmor);
		GameRegistry.addShapelessRecipe(clothArmorStack, Blocks.wool, Blocks.wool, Items.leather_chestplate, Blocks.wool);
		LanguageRegistry.addName(clothArmorStack, "Cloth Armor");
		
		Item leatherArmor = createItemCopy(Items.leather_chestplate, "leatherArmor");
		ItemStack leatherArmorStack = new ItemStack(leatherArmor);
		GameRegistry.addShapelessRecipe(leatherArmorStack, Items.leather_boots, Items.leather_leggings, Items.leather_chestplate, Items.leather_helmet);
		LanguageRegistry.addName(leatherArmorStack, "Leather Armor");
		
		Item ironArmor = createItemCopy(Items.iron_chestplate, "ironArmor");
		ItemStack ironArmorStack = new ItemStack(ironArmor);
		GameRegistry.addShapelessRecipe(ironArmorStack, Items.iron_boots, Items.iron_leggings, Items.iron_chestplate, Items.iron_helmet);
		LanguageRegistry.addName(ironArmorStack, "Iron Armor");
		
		Item goldArmor = createItemCopy(Items.golden_chestplate, "goldArmor");
		ItemStack goldArmorStack = new ItemStack(goldArmor);
		GameRegistry.addShapelessRecipe(goldArmorStack, Items.golden_boots, Items.golden_leggings, Items.golden_chestplate, Items.golden_helmet);
		LanguageRegistry.addName(goldArmorStack, "Gold Armor");
		
		Item diamondArmor = createItemCopy(Items.iron_chestplate, "diamondArmor");
		ItemStack diamondArmorStack = new ItemStack(diamondArmor);
		GameRegistry.addShapelessRecipe(diamondArmorStack, Items.diamond_boots, Items.diamond_leggings, Items.diamond_chestplate, Items.diamond_helmet);
		LanguageRegistry.addName(diamondArmorStack, "Diamond Armor");
		
		
		Item smallHealthPotion = createItemCopy(Items.potionitem, "smallPotion");
		ItemStack smallHealthPotionStack = new ItemStack(smallHealthPotion);
		GameRegistry.addShapelessRecipe(smallHealthPotionStack, Items.apple);
		LanguageRegistry.addName(smallHealthPotion, "S.Pot");

		Item medHealthPotion = createItemCopy(Items.potionitem, "medPotion");
		ItemStack medHealthPotionStack = new ItemStack(medHealthPotion);
		GameRegistry.addShapelessRecipe(medHealthPotionStack, smallHealthPotion, smallHealthPotion, Items.bread);
		LanguageRegistry.addName(medHealthPotion, "M.Pot");

		Item highHealthPotion = createItemCopy(Items.potionitem, "highPotion");
		ItemStack highHealthPotionStack = new ItemStack(highHealthPotion);
		GameRegistry.addShapelessRecipe(highHealthPotionStack, medHealthPotion, medHealthPotion, Items.redstone);
		LanguageRegistry.addName(highHealthPotion, "H.Pot");

		Item smallManaPotion = createItemCopy(Items.potionitem, "smallManaPotion");
		ItemStack smallManaPotionStack = new ItemStack(smallManaPotion);
		GameRegistry.addShapelessRecipe(smallManaPotionStack, Items.fish);
		GameRegistry.addShapelessRecipe(smallManaPotionStack, Items.cooked_fished);
		LanguageRegistry.addName(smallManaPotion, "S.Mana Pot");

		Item highManaPotion = createItemCopy(Items.potionitem, "highManaPotion");
		ItemStack highManaPotionStack = new ItemStack(highManaPotion);
		GameRegistry.addShapelessRecipe(highManaPotionStack, smallManaPotion, smallManaPotion, Items.redstone);
		LanguageRegistry.addName(highManaPotion, "H.Mana Pot");

		Item elixir = createItemCopy(Items.potionitem, "elixir");
		ItemStack elixirStack = new ItemStack(elixir);
		GameRegistry.addShapelessRecipe(elixirStack, highManaPotion, highHealthPotion, Items.emerald);
		LanguageRegistry.addName(elixir, "Elixir");

		Item megalixir = createItemCopy(Items.potionitem, "megalixir");
		ItemStack megalixirStack = new ItemStack(megalixir);
		GameRegistry.addShapelessRecipe(megalixirStack, elixir, elixir, Items.dye);
		LanguageRegistry.addName(megalixir, "Megalixir");

		Item antidote = createItemCopy(Items.potionitem, "antidote");
		ItemStack antidoteStack = new ItemStack(antidote);
		GameRegistry.addShapelessRecipe(antidoteStack, Blocks.yellow_flower, Blocks.red_mushroom);
		LanguageRegistry.addName(antidote, "Antidote");

		Item echoScreen = createItemCopy(Items.potionitem, "echoScreen");
		ItemStack echoScreenStack = new ItemStack(echoScreen);
		GameRegistry.addShapelessRecipe(echoScreenStack, Items.feather, Blocks.red_flower);
		LanguageRegistry.addName(echoScreen, "Echo Screen");

		Item parlyzHeal = createItemCopy(Items.potionitem, "parlyzHeal");
		ItemStack parlyzHealStack = new ItemStack(parlyzHeal);
		GameRegistry.addShapelessRecipe(parlyzHealStack, Items.egg, Blocks.brown_mushroom);
		LanguageRegistry.addName(parlyzHeal, "Parlyz Heal");

		Item pinwheel = createItemCopy(Items.potionitem, "pinwheel");
		ItemStack pinwheelStack = new ItemStack(pinwheel);
		GameRegistry.addShapelessRecipe(pinwheelStack, Items.feather, Items.paper);
		LanguageRegistry.addName(pinwheel, "Pinwheel");

		Item eyeDrops = createItemCopy(Items.potionitem, "eyeDrops");
		ItemStack eyeDropsStack = new ItemStack(eyeDrops);
		GameRegistry.addShapelessRecipe(eyeDropsStack, Blocks.yellow_flower, Blocks.red_flower, Items.glass_bottle);
		LanguageRegistry.addName(eyeDrops, "Eye Drops");

		Item panacea = createItemCopy(Items.potionitem, "panacea");
		ItemStack panaceaStack = new ItemStack(panacea);
		GameRegistry.addShapelessRecipe(panaceaStack, Blocks.yellow_flower, Blocks.red_flower, Blocks.brown_mushroom, Blocks.red_mushroom, Items.dye);
		LanguageRegistry.addName(panacea, "Panacea");
		
		Item pheonixDown = createItemCopy(Items.potionitem, "phoenixDown");
		ItemStack pheonixDownStack = new ItemStack(pheonixDown);
		GameRegistry.addShapelessRecipe(pheonixDownStack, Items.feather, Items.lava_bucket);
		LanguageRegistry.addName(pheonixDown, "Phoenix Down");

		Item fireBomb = createItemCopy(Items.potionitem, "fireBomb");
		ItemStack fireBombStack = new ItemStack(fireBomb);
		GameRegistry.addShapelessRecipe(fireBombStack, Items.paper, Items.flint, Items.gunpowder);
		LanguageRegistry.addName(fireBomb, "Fire Bomb");

		Item earthGem = createItemCopy(Items.potionitem, "earthGem");
		ItemStack earthGemStack = new ItemStack(earthGem);
		GameRegistry.addShapelessRecipe(earthGemStack, Blocks.dirt, Items.flint, Blocks.clay);
		LanguageRegistry.addName(earthGem, "Earth Gem");

		Item iceCrystal = createItemCopy(Items.potionitem, "iceCrystal");
		ItemStack iceCrystalStack = new ItemStack(iceCrystal);
		GameRegistry.addShapelessRecipe(iceCrystalStack, Blocks.ice, Items.glass_bottle, Items.coal);
		LanguageRegistry.addName(iceCrystal, "Ice Crystal");

		Item lightningRod = createItemCopy(Items.potionitem, "lightningRod");
		ItemStack lightningRodStack = new ItemStack(lightningRod);
		GameRegistry.addShapelessRecipe(lightningRodStack, Items.stick, Items.iron_ingot, Items.coal);
		LanguageRegistry.addName(lightningRod, "Lightning Rod");
	}

	@SideOnly(Side.CLIENT)
	public void keyDown(InputEvent.KeyInputEvent evt)
	{
		if(!openTBCGui.getIsKeyPressed())
		{
			return;
		}

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

	@SideOnly(Side.CLIENT)
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
			
		if(attackIndex != -1)
        {
        	evt.toolTip.remove(attackIndex);
        	evt.toolTip.remove(attackIndex - 1);
        }
		
		String name = evt.itemStack.getItem().getUnlocalizedName().replace("item.", "");
        if(EquippedItemManager.Instance.lookup.containsKey(name))
        {
        	ArrayList<String> descriptions = EquippedItemManager.Instance.lookup.get(name).DescriptionStrings();
        	for(String displayString : descriptions)
        	{
        		if(displayString != null && displayString.length() > 0)
	        	{
        			evt.toolTip.add(displayString);
	        	}
        	}
        }
        else if(EquippedItemManager.Instance.usableLookup.containsKey(name))
        {
        	ArrayList<String> descriptions = EquippedItemManager.Instance.usableLookup.get(name).DescriptionStrings();
        	for(String displayString : descriptions)
        	{
        		if(displayString != null && displayString.length() > 0)
	        	{
        			evt.toolTip.add(displayString);
	        	}
        	}
        }
	}
	
	private Item createItemCopy(Item itemToCopy, String name)
	{
		Item newItem = new CloneItem(itemToCopy).setUnlocalizedName(name);
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