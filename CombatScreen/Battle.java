package TBC.CombatScreen;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import sun.font.LayoutPathImpl.EndType;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.Pair;
import TBC.Triplet;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.Combat.CombatEntityTemplate;
import TBC.Combat.CombatRandom;
import TBC.Combat.EquippedItemManager;
import TBC.Combat.LevelingEngine;
import TBC.Combat.Abilities.AbilityTargetType;
import TBC.Combat.Abilities.DefaultAttackAbility;
import TBC.Combat.Abilities.DelayedAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;
import TBC.Messages.CombatCommandMessage;
import TBC.Messages.CombatEndedMessage;
import TBC.Messages.CombatPlayerControlMessage;
import TBC.Messages.CombatStartedMessage;
import TBC.Messages.CombatSyncDataMessage;
import TBC.Messages.NBTTagCompoundMessage;
import TBC.Messages.StringMessage;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class Battle
{
	public long id;
	private ArrayList<GenericGuiButton> buttonsToSwap;

	private WorldServer world;
	private HashMap<CombatEntity, EntityPlayerMP> players = new HashMap<CombatEntity, EntityPlayerMP>();
	private HashMap<CombatEntity, EntityPlayerMP> owners = new HashMap<CombatEntity, EntityPlayerMP>();
	
	private HashMap<CombatEntity, ItemStack> henchmanLookup = new HashMap<CombatEntity, ItemStack>();
	private ArrayList<CombatEntity> enemies = new ArrayList<CombatEntity>();
	private ArrayList<CombatEntity> allies = new ArrayList<CombatEntity>();
	private CombatEngine combatEngine;
	private LevelingEngine levelingEngine;
	private ArrayList<Entity> removedEntities = new ArrayList<Entity>();
	
	private CombatEntity entityForCurrentTurn;
	private boolean mainLoopRunning = false;
	
	public Battle(long id, EntityPlayerMP player, EntityLivingBase enemyEntity, boolean isAttacker)
	{
		this.world = (WorldServer)player.worldObj;
		this.world.loadedEntityList.remove(player);
		player.isDead = true;
		
		this.world.loadedEntityList.remove(enemyEntity);
		removedEntities.add(enemyEntity);
		enemyEntity.isDead = true;
		
		CombatEntity playerEntity = CombatEntity.GetCombatEntity(player);
		players.put(playerEntity, player);
		owners.put(playerEntity, player);
		
		this.id = id;
		int currentEntityId = -1;
		allies.add(playerEntity);

		int foundHenchmen = 0;
		for(int i = 0; i< player.inventory.getHotbarSize(); i++)
		{
			if(player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof HenchmanItem)
			{
				ItemStack henchmanStack = player.inventory.mainInventory[i];
				HenchmanItem h = (HenchmanItem)henchmanStack.getItem();
				CombatEntity henchmanEntity = CombatEntityLookup.Instance.GetCombatEntity(currentEntityId--, h.henchmanType, h.henchmanName);
				henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getDamage(henchmanStack)/(float)h.getMaxDamage())));
				if(henchmanEntity.currentHp < 1)
				{
					continue;
				}

				NBTTagCompound itemData = henchmanStack.getTagCompound();
				if(itemData != null && itemData.hasKey("HenchMP"))
				{
					henchmanEntity.currentMp = itemData.getInteger("HenchMP");
				}

				allies.add(henchmanEntity);
				foundHenchmen++;
				henchmanLookup.put(henchmanEntity, henchmanStack);
				owners.put(henchmanEntity, player);
				if(foundHenchmen >= 3)
				{
					break;
				}
			}
		}

		Vec3 position = enemyEntity.getPosition(1.0F);
		double enemyX = position.xCoord;
		double enemyY = position.yCoord;
		double enemyZ = position.zCoord;

		int encounterRadius = 10;
		AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(enemyX - encounterRadius, enemyY - encounterRadius, enemyZ - encounterRadius, enemyX + encounterRadius, enemyY + encounterRadius, enemyZ + encounterRadius);
		List additionalEnemies = enemyEntity.worldObj.getEntitiesWithinAABBExcludingEntity(enemyEntity, boundingBox);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put(EntityList.getEntityString(enemyEntity), 1);
		enemies.add(CombatEntity.GetCombatEntity(enemyEntity.getEntityId(), enemyEntity, 1));

		if(additionalEnemies != null && additionalEnemies.size() > 0)
		{
			for(int i = 0; i<additionalEnemies.size(); i++)
			{
				Object additionalEnemy = additionalEnemies.get(i);
				if(additionalEnemy instanceof EntityPlayer)
				{
					continue;
				}

				if(additionalEnemy instanceof EntityLiving)
				{
					EntityLiving additionalLivingEnemy = (EntityLiving)additionalEnemy;
					if(additionalLivingEnemy.canEntityBeSeen(player))
					{
						Integer existingOfThisType = map.get(EntityList.getEntityString(additionalLivingEnemy));
						int numOfThisType = 1;
						if(existingOfThisType != null)
						{
							numOfThisType += existingOfThisType;
						}

						enemies.add(CombatEntity.GetCombatEntity(additionalLivingEnemy.getEntityId(), additionalLivingEnemy, numOfThisType));
						map.put(EntityList.getEntityString(additionalLivingEnemy), numOfThisType);
						if(enemies.size() >= 5)
						{
							break;
						}
					}
				}
			}
		}

		this.combatEngine = new CombatEngine(this.allies, this.enemies, isAttacker, currentEntityId);
		this.levelingEngine = new LevelingEngine();
	}

	public Battle(long id, EntityPlayerMP player, ArrayList<Pair<String, String>> setEnemies, boolean isAttacker)
	{
		player.worldObj.removeEntity(player);
		
		CombatEntity playerEntity = CombatEntity.GetCombatEntity(player);
		players.put(playerEntity, player);
		
		this.id = id;
		int currentEntityId = -1;
		allies.add(playerEntity);
		
//		this.id = id;
//		this.player = player;
//		int currentEntityId = 0;
//		allies.add(CombatEntity.GetCombatEntity(player));

		int foundHenchmen = 0;
		for(int i = 0; i< player.inventory.getHotbarSize(); i++)
		{
			if(player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof HenchmanItem)
			{
				ItemStack henchmanStack = player.inventory.mainInventory[i];
				HenchmanItem h = (HenchmanItem)henchmanStack.getItem();
				CombatEntity henchmanEntity = CombatEntityLookup.Instance.GetCombatEntity(currentEntityId++, h.henchmanType, h.henchmanName);
				henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getDamage(henchmanStack)/(float)h.getMaxDamage())));
				if(henchmanEntity.currentHp < 1)
				{
					continue;
				}

				NBTTagCompound itemData = henchmanStack.getTagCompound();
				if(itemData != null && itemData.hasKey("HenchMP"))
				{
					henchmanEntity.currentMp = itemData.getInteger("HenchMP");
				}

				allies.add(henchmanEntity);
				foundHenchmen++;
				henchmanLookup.put(henchmanEntity, henchmanStack);
				if(foundHenchmen >= 3)
				{
					break;
				}
			}
		}

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0; i< setEnemies.size(); i++)
		{
			Integer existingOfThisType = map.get(setEnemies.get(i).item2);
			int numOfThisType = 1;
			if(existingOfThisType != null)
			{
				numOfThisType += existingOfThisType;
			}

			enemies.add(CombatEntity.GetCombatEntity(currentEntityId++, setEnemies.get(i).item1, setEnemies.get(i).item2, 1));
			map.put(setEnemies.get(i).item2, numOfThisType);
		}

		this.combatEngine = new CombatEngine(this.allies, this.enemies, isAttacker, currentEntityId);
		this.levelingEngine = new LevelingEngine();
		MainMod.combatStartedHandler.sendTo(this.GetBattleStartMessage(), player);
	}

	public void HandlePlayerCommand(CombatCommandMessage message)
	{
		if(mainLoopRunning)
		{
			return;
		}
		else
		{
			HandlePlayerCommand(message.User, message.AbilityToUse, message.Targets);
		}
	}
	
	public void HandlePlayerCommand(Integer userId, ICombatAbility abilityToUse, ArrayList<Integer> targetIds)
	{
		CombatEntity user = null;
		for(int i = 0; i < allies.size(); i++)
		{
			if(allies.get(i).id == userId)
			{
				user = allies.get(i);
				break;
			}
		}
		
		ArrayList<CombatEntity> targets = new ArrayList<CombatEntity>();
		for(int i = 0; i < targetIds.size(); i++)
		{
			boolean found = false;
			for(int j = 0; j < enemies.size(); j++)
			{
				if(enemies.get(j).id == targetIds.get(i))
				{
					targets.add(enemies.get(j));
					found = true;
					break;
				}
			}
			
			if(found)
			{
				break;
			}
			
			for(int j = 0; j < allies.size(); j++)
			{
				if(allies.get(j).id == targetIds.get(i))
				{
					targets.add(allies.get(j));
					break;
				}
			}
		}
		
		ArrayList<String> messages = new ArrayList<String>();
		this.combatEngine.Attack(user, targets, abilityToUse, messages);
		if(!this.EndTurn(abilityToUse, targets, messages))
		{
			this.DoNextTurn();
		}
	}
	
	public CombatStartedMessage GetBattleStartMessage()
	{
		CombatStartedMessage m = new CombatStartedMessage();
		m.Allies = this.allies;
		m.Enemies = this.enemies;
		m.CombatId = this.id;
		return m;
	}

	public void AttemptEscape()
	{
		if(this.combatEngine.CanEscape())
		{
			ArrayList<String> messages = new ArrayList<String>();
			messages.add("Successfully escaped!");
//			TurnState next = new TurnState(Minecraft.getMinecraft());
//			next.SetState(TurnState.EndOfCombat, null, null, (CombatEntity)null);
//			this.turnState.SetDisplayMessageState(messages, next);
		}
		else
		{
			ArrayList<String> messages = new ArrayList<String>();
			messages.add("Failed to escape!");
//			TurnState next = new TurnState(Minecraft.getMinecraft());
//			next.SetState(TurnState.DisplayingEndOfTurn, null, null, (CombatEntity)null);
//			this.turnState.SetDisplayMessageState(messages, next);
		}
	}

	public void DoNextTurn()
	{
		while(true)
		{
			mainLoopRunning = true;
			CombatEntity next = this.combatEngine.GetNextTurn();
			this.entityForCurrentTurn = next;
			if(this.allies.contains(next)) 	// If next turn is for player
			{
				Pair<ICombatAbility, ArrayList<CombatEntity>> queued = this.combatEngine.GetQueuedAbility(next);
				if(queued != null)
				{
					ArrayList<String> messageQueue = new ArrayList<String>();
					this.combatEngine.Attack(next, queued.item2, queued.item1, messageQueue);
					if(EndTurn(queued.item1, queued.item2, messageQueue))
					{
						break;
					}
				}
				else
				{
					CombatPlayerControlMessage msg = new CombatPlayerControlMessage();
					msg.Active = this.entityForCurrentTurn.id;
					MainMod.playerControlHandler.sendTo(msg, owners.get(next));
					break;
				}
			}
			else  // If next turn is for enemies
			{
				ArrayList<String> messageQueue = new ArrayList<String>();
				Pair<ICombatAbility, ArrayList<CombatEntity>> attack = this.combatEngine.Attack(next, messageQueue);
				if(EndTurn(attack.item1, attack.item2, messageQueue))
				{
					break;
				}
			}
		}
		
		mainLoopRunning = false;
	}

	private boolean EndTurn(ICombatAbility abilityUsed, ArrayList<CombatEntity> targets, ArrayList<String> messageQueue)
	{
		CombatSyncDataMessage m = new CombatSyncDataMessage();
		m.User = new Pair<Integer, Integer>(this.entityForCurrentTurn.id, this.entityForCurrentTurn.lastDamageTaken);
		m.AbilityUsed = abilityUsed;
		m.Messages = messageQueue;
		
		ArrayList<Pair<Integer, Integer>> targetDamage = new ArrayList<Pair<Integer,Integer>>();
		for(int i = 0; i < targets.size(); i++)
		{
			CombatEntity targetWithDamage = targets.get(i);
			targetDamage.add(new Pair<Integer, Integer>(targetWithDamage.id, targetWithDamage.lastDamageTaken));
		}
		
		m.Targets = targetDamage;
		this.combatEngine.EndTurn(this.entityForCurrentTurn);
		int activeEnemies = 0;
		for(CombatEntity enemy : this.enemies)
		{
			enemy.ApplyDamage();
			if(enemy.currentHp > 0)
			{
				activeEnemies++;
			}
		}

		int activeAllies = 0;
		for(CombatEntity ally : this.allies)
		{
			ally.ApplyDamage();
			if(ally.currentHp > 0)
			{
				activeAllies++;
			}
		}
		
		m.Allies = this.allies;
		m.Enemies = this.enemies;
		Object[] playerValues = this.players.values().toArray();
		for(int i = 0; i<playerValues.length; i++)
		{
			MainMod.syncCombatDataHandler.sendTo(m, (EntityPlayerMP)playerValues[i]);
		}
		
		if(activeEnemies == 0 || activeAllies == 0)
		{
			EndCombat();
			return true;
		}
		
		return false;
	}
	
	private void ClearAttack()
	{
		this.entityForCurrentTurn = null;
	}

	private void EndCombat()
	{
		Object[] playerValues = this.players.values().toArray();
		List toLoad = new ArrayList();
		for(int i = 0; i<playerValues.length; i++)
		{
			EntityPlayerMP toReenable = (EntityPlayerMP)playerValues[i];
			toReenable.isDead = false;
			this.world.loadedEntityList.add(toReenable);
		}
		
		for(int i = 0; i<this.removedEntities.size(); i++)
		{
			Entity toReenable = this.removedEntities.get(i);
			toReenable.isDead = false;
			this.world.loadedEntityList.add(toReenable);
		}
		
		boolean wonBattle = false;
		for(CombatEntity entity : this.allies)
		{
			if(entity.currentHp > 0)
			{
				wonBattle = true;
			}
		}

		ArrayList<String> messageQueue = new ArrayList<String>();
		if(wonBattle)
		{
			SimpleEntry<Integer, Integer> rewards = this.combatEngine.GetXpAndApReward(this.enemies);
			for(CombatEntity e : this.allies)
			{
				Pair<Boolean, Boolean> gainedLevelOrSkill = LevelingEngine.Instance.GainXP(e, rewards.getKey(), rewards.getValue(), messageQueue);
				if(this.players.containsKey(e))
				{
					EntityPlayerMP player = this.players.get(e);
					CombatEndedMessage m = new CombatEndedMessage();
					m.XPGained = rewards.getKey();
					m.APGained = rewards.getValue();
					m.GainedLevel = gainedLevelOrSkill.item1;
					m.GainedSkill = gainedLevelOrSkill.item2;
					m.Won = true;
					m.PlayerData = LevelingEngine.Instance.GetXpDataForPlayer(player);
					MainMod.combatEndedHandler.sendTo(m, player);
				}
			}
		}
		else
		{
			CombatEndedMessage m = new CombatEndedMessage();
			m.Won = false;
			for(int i = 0; i<playerValues.length; i++)
			{
				MainMod.combatEndedHandler.sendTo(m, (EntityPlayerMP)playerValues[i]);
			}
		}
		
		EntityPlayerMP firstPlayer = (EntityPlayerMP)playerValues[0];
		DamageSource source = DamageSource.causePlayerDamage(firstPlayer);
		source.damageType = "bypass";
		for(int i = 0; i<this.allies.size(); i++)
		{
			CombatEntity entity = this.allies.get(i);
			if(entity.currentHp < 1)
			{
				if(this.players.containsKey(entity))
				{
					EntityPlayerMP otherPlayer = this.players.get(entity);
					if(!wonBattle)
					{
						otherPlayer.setHealth(0);
					}
					else
					{
						otherPlayer.setHealth(1);
						MainMod.setHealthHandler.sendToServer(new StringMessage("1"));
						NBTTagCompound tag = otherPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
						tag.setInteger("TBCPlayerMP", entity.currentMp);
						otherPlayer.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
						// Sync tag to client?
					}
				}
				else if(henchmanLookup.containsKey(entity))
				{
					ItemStack h = henchmanLookup.get(entity);
					int currentMp = entity.currentMp;
					h.setItemDamage(101);
					NBTTagCompound existingTag = h.getTagCompound();
					if(existingTag == null)
					{
						existingTag = new NBTTagCompound();
					}

					existingTag.setInteger("HenchMP", currentMp);
					h.setTagCompound(existingTag);
					// Sync tag to client?
				}
			}
			if(this.players.containsKey(entity))
			{
				EntityPlayerMP otherPlayer = this.players.get(entity);
				float maxHealth = otherPlayer.getMaxHealth();
				float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
				int healthToSet = Math.round((currentHpPercentage * maxHealth) + .499999F);
				otherPlayer.setHealth(healthToSet);

				NBTTagCompound tag = otherPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
				tag.setInteger("TBCPlayerMP", entity.currentMp);
				otherPlayer.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
				// Sync tag to client?
			}
			else if(henchmanLookup.containsKey(entity))
			{
				float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
				int healthToSet = Math.round((currentHpPercentage * 100) + .499999F);
				ItemStack h = this.henchmanLookup.get(entity);
				
				h.setItemDamage(100 - healthToSet);
				NBTTagCompound existingTag = h.getTagCompound();
				if(existingTag == null)
				{
					existingTag = new NBTTagCompound();
				}

				existingTag.setInteger("HenchMP", entity.currentMp);
				h.setTagCompound(existingTag);
				// Sync tag to client?
			}
		}

		for(int i = 0; i<this.enemies.size(); i++)
		{
			CombatEntity entity = this.enemies.get(i);
			if(entity.currentHp < 1)
			{
				if(entity.id > 0)
				{
					EntityLivingBase deadEntity = (EntityLivingBase) world.getEntityByID(entity.id);
					
					// In the case of creepers, the entity is already dead.
					if(deadEntity != null)
					{
						deadEntity.setLastAttacker(firstPlayer);
						deadEntity.attackEntityFrom(source, 1000);
					}
				}
			}
		}

		MainMod.ServerBattles.remove(this.id);
	}
}
