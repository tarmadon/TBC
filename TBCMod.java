package TBC;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import TBC.EnemyLabels.EnemyLabelMod;
import TBC.ZoneGeneration.ZoneGenerationMod;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "TBC", name = "TBC", version = "1.0.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class TBCMod 
{
	public static MainMod MainModInstance = new MainMod();
	public static EnemyLabelMod EnemyLabelModInstance = new EnemyLabelMod();
	public static ZoneGenerationMod ZoneGenerationModInstance = new ZoneGenerationMod();
	
	@Instance ("TBC")
	public static TBCMod instance;
	
	@PreInit
	public void onPreInit(FMLPreInitializationEvent evt)
	{
		MainModInstance.preInit(evt);
	}
	
	@Init
	public void init(FMLInitializationEvent evt)
	{
		MinecraftForge.EVENT_BUS.register(this);
		MainModInstance.load(evt);
		EnemyLabelModInstance.Init(evt);
		ZoneGenerationModInstance.Init(evt);
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent evt)
	{
		EnemyLabelModInstance.afterLoad(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	public void onEnteringChunk(EntityEvent.EnteringChunk buildingEntity)
	{
		MainModInstance.syncPlayerData(buildingEntity);
		EnemyLabelModInstance.onEntityAddedToChunk(buildingEntity);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	@SideOnly(Side.CLIENT)
	public void renderGameOverlayText(RenderGameOverlayEvent.Text evt)
	{
		MainModInstance.renderHUD(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = true)
	public void onRest(PlayerSleepInBedEvent evt)
	{
		MainModInstance.onRest(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = true)
	@SideOnly(Side.CLIENT)
	public void onLivingAttacked(LivingAttackEvent evt)
	{
		MainModInstance.onLivingAttacked(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	public void onEntitySpawn(EntityEvent.EntityConstructing evt)
	{
		EnemyLabelModInstance.onClientMobSpawn(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	public void onChunkLoad(ChunkEvent.Load evt)
	{
		ZoneGenerationModInstance.onChunkLoad(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	public void onChunkDataSave(ChunkDataEvent.Save evt)
	{
		ZoneGenerationModInstance.onChunkSave(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	public void onChunkLoad(ChunkDataEvent.Load evt)
	{
		ZoneGenerationModInstance.onChunkLoad(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	public void onMobDrops(LivingDropsEvent evt)
	{
		MainModInstance.onMobDrops(evt);
	}
	
	@ForgeSubscribe(receiveCanceled = false)
	public void onEntityJoinWorld(EntityJoinWorldEvent evt)
	{
		MainModInstance.onPlayerJoin(evt);
	}
}
