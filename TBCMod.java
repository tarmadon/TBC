package TBC;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import TBC.EnemyLabels.EnemyLabelMod;
import TBC.ZoneGeneration.ZoneGenerationMod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "tbc", name = "tbc", version = "1.0.0")
public class TBCMod
{
	public static MainMod MainModInstance = new MainMod();
	public static EnemyLabelMod EnemyLabelModInstance = new EnemyLabelMod();
	public static ZoneGenerationMod ZoneGenerationModInstance = new ZoneGenerationMod();

	public static ResourceLocation statsGuiBackground = new ResourceLocation("tbc", "textures/gui/charScreen.png");
	public static ResourceLocation battleScreenCharWindow = new ResourceLocation("tbc", "textures/gui/charWindow.png");
	public static ResourceLocation battleScreenDivider = new ResourceLocation("tbc", "textures/gui/divider.png");
	public static ResourceLocation statusEffects = new ResourceLocation("tbc", "textures/gui/statusEffects.png");
	public static ResourceLocation combatDecals = new ResourceLocation("tbc", "textures/gui/combatDecals.png");
	
	public static ResourceLocation vanillaGui = new ResourceLocation("textures/gui/widgets.png");
	
	@Instance ("tbc")
	public static TBCMod instance;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent evt)
	{
		MainModInstance.preInit(evt);
		ZoneGenerationModInstance.preInit(evt);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		MainModInstance.load(evt);
		EnemyLabelModInstance.Init(evt);
		ZoneGenerationModInstance.Init(evt);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		EnemyLabelModInstance.afterLoad(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onEnteringChunk(EntityEvent.EnteringChunk buildingEntity)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			MainModInstance.syncPlayerData(buildingEntity);
		}
		
		EnemyLabelModInstance.onEntityAddedToChunk(buildingEntity);
	}

	@SubscribeEvent(receiveCanceled = false)
	@SideOnly(Side.CLIENT)
	public void renderGameOverlayText(RenderGameOverlayEvent.Text evt)
	{
		MainModInstance.renderHUD(evt);
	}
	
	@SubscribeEvent(receiveCanceled = true)
	public void onRest(PlayerSleepInBedEvent evt)
	{
		MainModInstance.onRest(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onLivingAttacked(LivingAttackEvent evt)
	{
		MainModInstance.onLivingAttacked(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onEntitySpawn(EntityEvent.EntityConstructing evt)
	{
		EnemyLabelModInstance.onClientMobSpawn(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onChunkLoad(ChunkEvent.Load evt)
	{
		ZoneGenerationModInstance.onChunkLoad(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onChunkUnload(ChunkEvent.Unload evt)
	{
		ZoneGenerationModInstance.onChunkUnload(evt);
	}
	
	@SubscribeEvent(receiveCanceled = false)
	public void onChunkDataSave(ChunkDataEvent.Save evt)
	{
		ZoneGenerationModInstance.onChunkSave(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onChunkLoad(ChunkDataEvent.Load evt)
	{
		ZoneGenerationModInstance.onChunkLoad(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void afterPopulate(PopulateChunkEvent.Post evt)
	{
		ZoneGenerationModInstance.onChunkPopulate(evt);
	}
	
	@SubscribeEvent(receiveCanceled = false)
	public void onMobDrops(LivingDropsEvent evt)
	{
		MainModInstance.onMobDrops(evt);
	}

	@SubscribeEvent(receiveCanceled = false)
	public void onWorldUnLoad(WorldEvent.Unload evt)
	{
		MainModInstance.onWorldUnload(evt);
	}
	
	@SubscribeEvent(receiveCanceled = false)
	public void openStatGui(InputEvent.KeyInputEvent evt)
	{
		MainModInstance.keyDown(evt);
	}
	
	@SubscribeEvent(receiveCanceled = false)
	public void itemTooltip(ItemTooltipEvent evt)
	{
		MainModInstance.onItemTooltip(evt);
	}
}
