package TBC.CombatScreen;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import TBC.BattleEntity;
import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.MainMod;
import TBC.Pair;
import TBC.PlayerSaveData;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatEntityLookup;
import TBC.Combat.LevelingEngine;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Messages.CombatCommandMessage;
import TBC.Messages.CombatEndedMessage;
import TBC.Messages.CombatPlayerControlMessage;
import TBC.Messages.CombatStartedMessage;
import TBC.Messages.CombatSyncDataMessage;
import TBC.Messages.NBTTagCompoundMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;

public class Battle
{
	public long id;
	private BattleEntity entityInWorld;
	private ArrayList<GenericGuiButton> buttonsToSwap;

	private WorldServer world;
	private ArrayList<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();
	private HashMap<CombatEntity, EntityPlayerMP> owners = new HashMap<CombatEntity, EntityPlayerMP>();
	
	private HashMap<CombatEntity, ItemStack> henchmanLookup = new HashMap<CombatEntity, ItemStack>();
	private ArrayList<CombatEntity> enemies = new ArrayList<CombatEntity>();
	private ArrayList<CombatEntity> allies = new ArrayList<CombatEntity>();
	private CombatEngine combatEngine;
	private LevelingEngine levelingEngine;
	private HashMap<CombatEntity, Pair<Double, Entity>> removedEntities = new HashMap<CombatEntity, Pair<Double, Entity>>();
	
	private DamageSource damageSource;
	private CombatEntity entityForCurrentTurn;
	private boolean mainLoopRunning = false;
	
	public Battle(long id, EntityPlayerMP player, EntityLivingBase enemyEntity, boolean isAttacker)
	{
		this.id = id;
		this.world = (WorldServer)player.worldObj;
		this.damageSource = DamageSource.causePlayerDamage(player);
		this.damageSource.damageType = "bypass";

		Pair<Integer, CombatEntity> playerEntity = CombatEntity.GetCombatEntity(player);
		CombatEntity enemyCombatEntity = CombatEntity.GetCombatEntity(enemyEntity.getEntityId(), enemyEntity, 1);

		entityInWorld = new BattleEntity(world, id);
		entityInWorld.setLocationAndAngles(player.posX, player.posY + 1.25, player.posZ, 0, 0);
		this.world.spawnEntityInWorld(entityInWorld);
		
		players.add(player);
		enemies.add(enemyCombatEntity);
		this.allies = new ArrayList<CombatEntity>();
		for(int i = 0; i < 10; i++)
		{
			this.allies.add(null);
		}
		
		int currentEntityId = -1;
		if(playerEntity.item1 > 0)
		{
			allies.set(playerEntity.item1, playerEntity.item2);
			owners.put(playerEntity.item2, player);
		}

		int foundHenchmen = 0;
		for(int i = 0; i< player.inventory.mainInventory.length; i++)
		{
			if(player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof HenchmanItem)
			{
				ItemStack henchmanStack = player.inventory.mainInventory[i];
				Pair<Integer, CombatEntity> henchmanEntity = CombatEntity.GetCombatEntity(currentEntityId--, henchmanStack);
				if(henchmanEntity == null || henchmanEntity.item1 < 1)
				{
					continue;
				}
				
				allies.set(henchmanEntity.item1, henchmanEntity.item2);
				foundHenchmen++;
				henchmanLookup.put(henchmanEntity.item2, henchmanStack);
				owners.put(henchmanEntity.item2, player);
			}
		}

		ArrayList<CombatEntity> fixedAllies = new ArrayList<CombatEntity>();
		for(int i = 0; i < allies.size(); i++)
		{
			if(allies.get(i) != null)
			{
				fixedAllies.add(allies.get(i));
				if(fixedAllies.size() >= 4)
				{
					break;
				}
			}
		}
		
		if(fixedAllies.size() == 0)
		{
			fixedAllies.add(playerEntity.item2);
			owners.put(playerEntity.item2, player);
		}
		
		this.allies = fixedAllies;
		Vec3 position = enemyEntity.getPosition(1.0F);
		double enemyX = position.xCoord;
		double enemyY = position.yCoord;
		double enemyZ = position.zCoord;

		int encounterRadius = 10;
		AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(enemyX - encounterRadius, enemyY - encounterRadius, enemyZ - encounterRadius, enemyX + encounterRadius, enemyY + encounterRadius, enemyZ + encounterRadius);
		List additionalEnemies = enemyEntity.worldObj.getEntitiesWithinAABBExcludingEntity(enemyEntity, boundingBox);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put(EntityList.getEntityString(enemyEntity), 1);
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

						CombatEntity additionalEnemyCombatEntity = CombatEntity.GetCombatEntity(additionalLivingEnemy.getEntityId(), additionalLivingEnemy, numOfThisType); 
						enemies.add(additionalEnemyCombatEntity);
						map.put(EntityList.getEntityString(additionalLivingEnemy), numOfThisType);
						RemoveEntityFromGame(additionalEnemyCombatEntity, additionalLivingEnemy);
						
						if(enemies.size() >= 5)
						{
							break;
						}
					}
				}
			}
		}

		RemoveEntityFromGame(playerEntity.item2, player);
		RemoveEntityFromGame(enemyCombatEntity, enemyEntity);
		this.combatEngine = new CombatEngine(this.allies, this.enemies, isAttacker, currentEntityId);
		this.levelingEngine = new LevelingEngine();
	}

	public Battle(long id, EntityPlayerMP player, ArrayList<Pair<String, String>> setEnemies, boolean isAttacker)
	{
//		player.worldObj.removeEntity(player);
//		
//		CombatEntity playerEntity = CombatEntity.GetCombatEntity(player);
//		players.put(playerEntity, player);
//		
//		this.id = id;
//		int currentEntityId = -1;
//		allies.add(playerEntity);
//		
////		this.id = id;
////		this.player = player;
////		int currentEntityId = 0;
////		allies.add(CombatEntity.GetCombatEntity(player));
//
//		int foundHenchmen = 0;
//		for(int i = 0; i< player.inventory.getHotbarSize(); i++)
//		{
//			if(player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof HenchmanItem)
//			{
//				ItemStack henchmanStack = player.inventory.mainInventory[i];
//				HenchmanItem h = (HenchmanItem)henchmanStack.getItem();
//				CombatEntity henchmanEntity = CombatEntityLookup.Instance.GetCombatEntity(currentEntityId++, h.henchmanType, h.henchmanName);
//				henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getDamage(henchmanStack)/(float)h.getMaxDamage())));
//				if(henchmanEntity.currentHp < 1)
//				{
//					continue;
//				}
//
//				NBTTagCompound itemData = henchmanStack.getTagCompound();
//				if(itemData != null && itemData.hasKey("HenchMP"))
//				{
//					henchmanEntity.currentMp = itemData.getInteger("HenchMP");
//				}
//
//				allies.add(henchmanEntity);
//				foundHenchmen++;
//				henchmanLookup.put(henchmanEntity, henchmanStack);
//				if(foundHenchmen >= 3)
//				{
//					break;
//				}
//			}
//		}
//
//		HashMap<String, Integer> map = new HashMap<String, Integer>();
//		for(int i = 0; i< setEnemies.size(); i++)
//		{
//			Integer existingOfThisType = map.get(setEnemies.get(i).item2);
//			int numOfThisType = 1;
//			if(existingOfThisType != null)
//			{
//				numOfThisType += existingOfThisType;
//			}
//
//			enemies.add(CombatEntity.GetCombatEntity(currentEntityId++, setEnemies.get(i).item1, setEnemies.get(i).item2, 1));
//			map.put(setEnemies.get(i).item2, numOfThisType);
//		}
//
//		this.combatEngine = new CombatEngine(this.allies, this.enemies, isAttacker, currentEntityId);
//		this.levelingEngine = new LevelingEngine();
//		MainMod.combatStartedHandler.sendTo(this.GetBattleStartMessage(), player);
	}
	
	public ArrayList<EntityPlayerMP> GetInvolvedPlayers()
	{
		return this.players;
	}
	
	public boolean AddPlayerToCombat(EntityPlayerMP player)
	{
		Pair<Integer, CombatEntity> playerEntity = CombatEntity.GetCombatEntity(player);
		if(!this.combatEngine.AddEntityToCombat(this.allies.get(0), playerEntity.item2))
		{
			return false;
		}
		
		players.add(player);
		owners.put(playerEntity.item2, player);
		RemoveEntityFromGame(playerEntity.item2, player);
		MainMod.combatStartedHandler.sendTo(GetBattleStartMessage(), player);
		
		return true;
	}
	
	private void AddEntityBackToGame(Pair<Double, Entity> toReenable) 
	{
		Entity entityToReenable = toReenable.item2;
		if(entityToReenable instanceof EntityPlayer)
		{
			// Remember to set the players to living before adding them back.
			entityToReenable.isDead = false;
		}
		
		this.world.loadedEntityList.add(entityToReenable);
	}
	
	private void RemoveEntityFromGame(CombatEntity entity, EntityLivingBase additionalLivingEnemy) 
	{
		this.world.loadedEntityList.remove(additionalLivingEnemy);
		removedEntities.put(entity, new Pair<Double, Entity>(additionalLivingEnemy.posY, additionalLivingEnemy));
		if(additionalLivingEnemy instanceof EntityPlayer)
		{
			// Players are set to dead after being removed so that they drop aggro
			additionalLivingEnemy.isDead = true;
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
		for(int i = 0; i<this.players.size(); i++)
		{
			MainMod.syncCombatDataHandler.sendTo(m, this.players.get(i));
		}
		
		if(activeEnemies == 0 || activeAllies == 0)
		{
			EndCombat();
			return true;
		}
		
		return false;
	}

	public void AttemptEscape(CombatEntity user)
	{
		ArrayList<CombatEntity> allEscapees = new ArrayList<CombatEntity>();
		EntityPlayerMP owner = this.owners.get(user);
		Set<CombatEntity> allEntities = this.owners.keySet();
		for(CombatEntity e : allEntities)
		{
			if(this.owners.get(e) == owner)
			{
				allEscapees.add(e);
			}
		}
		
		ArrayList<String> messages = new ArrayList<String>();
		if(this.combatEngine.CanEscape(allEscapees))
		{
			messages.add("Successfully escaped!");
			for(CombatEntity escaped : allEscapees)
			{
				SyncCombatEntityToMinecraftWorld(escaped, DamageSource.causePlayerDamage(owner), true);
			}
			
			Object[] removedEntities = this.removedEntities.keySet().toArray();
			for(Object obj : removedEntities)
			{
				CombatEntity e = (CombatEntity)obj;
				if(allEscapees.contains(e))
				{
					Pair<Double, Entity> toReenable = this.removedEntities.get(e);
					AddEntityBackToGame(toReenable);
					this.removedEntities.remove(e);
				}
			}
			
			this.allies.removeAll(allEscapees);
			this.enemies.removeAll(allEscapees);
			for(CombatEntity e : allEscapees)
			{
				this.players.remove(e);
			}
			
			this.combatEngine.RemoveEntities(allEscapees);
			
			CombatEndedMessage m = new CombatEndedMessage();
			m.APGained = null;
			m.XPGained = null;
			m.GainedLevel = false;
			m.GainedSkill = false;
			m.Won = true;
			MainMod.combatEndedHandler.sendTo(m, owner);
		}
		else
		{
			messages.add("Failed to escape!");
		}
		
		if(!this.EndTurn(null, new ArrayList<CombatEntity>(), messages))
		{
			this.DoNextTurn();
		}
	}

	private void EndCombat()
	{
		entityInWorld.isDead = true;
		
		List toLoad = new ArrayList();
		Object[] allRemoved = this.removedEntities.values().toArray();
		for(int i = 0; i<allRemoved.length; i++)
		{
			Pair<Double, Entity> toReenable = (Pair<Double, Entity>)allRemoved[i];
			AddEntityBackToGame(toReenable);
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
			HashMap<EntityPlayerMP, ArrayList<Pair<Boolean, Boolean>>> levelUps = new HashMap<EntityPlayerMP, ArrayList<Pair<Boolean,Boolean>>>();
			for(CombatEntity e : this.allies)
			{
				EntityPlayerMP owner = this.owners.get(e);
				Pair<Boolean, Boolean> gainedLevelOrSkill = LevelingEngine.Instance.GainXP(this.world, e, rewards.getKey(), rewards.getValue(), messageQueue);
				if(!levelUps.containsKey(owner))
				{
					levelUps.put(owner, new ArrayList<Pair<Boolean,Boolean>>());
				}
			
				levelUps.get(owner).add(gainedLevelOrSkill);
			}
			
			for(EntityPlayerMP toNotify : this.players)
			{
				ArrayList<Pair<Boolean, Boolean>> levelUpsForPlayer = levelUps.get(toNotify);
				CombatEndedMessage m = new CombatEndedMessage();
				m.XPGained = rewards.getKey();
				m.APGained = rewards.getValue();
				m.GainedLevel = levelUpsForPlayer.get(0).item1;
				m.GainedSkill = levelUpsForPlayer.get(0).item2;
				m.Won = true;
				m.PlayerData = LevelingEngine.Instance.GetXpDataForPlayer(toNotify);
				MainMod.combatEndedHandler.sendTo(m, toNotify);
			}
		}
		else
		{
			CombatEndedMessage m = new CombatEndedMessage();
			m.Won = false;
			for(int i = 0; i<this.players.size(); i++)
			{
				MainMod.combatEndedHandler.sendTo(m, this.players.get(i));
			}
		}
		
		for(int i = 0; i<this.allies.size(); i++)
		{
			SyncCombatEntityToMinecraftWorld(this.allies.get(i), this.damageSource, wonBattle);
		}

		for(int i = 0; i<this.enemies.size(); i++)
		{
			SyncCombatEntityToMinecraftWorld(this.enemies.get(i), this.damageSource, !wonBattle);
		}

		for(EntityPlayer p : this.players)
		{
			MainMod.lastAttackTimes.put(p, Minecraft.getSystemTime());
		}
		
		MainMod.ServerBattles.remove(this.id);
	}

	private void SyncCombatEntityToMinecraftWorld(CombatEntity entity, DamageSource damageSource, boolean wonBattle)
	{
		if(entity.entityType == null)
		{
			EntityPlayerMP player = this.owners.get(entity);
			if(entity.currentHp < 1)
			{
				if(!wonBattle)
				{
					player.setHealth(0);
				}
				else
				{
					player.setHealth(1);
					NBTTagCompound tag = PlayerSaveData.GetPlayerTag(player);
					tag.setInteger("TBCPlayerMP", entity.currentMp);
					PlayerSaveData.SetPlayerTag(player, tag);
				}
			}
			else
			{
				float maxHealth = player.getMaxHealth();
				float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
				player.setHealth(currentHpPercentage * maxHealth);

				NBTTagCompound tag = PlayerSaveData.GetPlayerTag(player);
				tag.setInteger("TBCPlayerMP", entity.currentMp);
				PlayerSaveData.SetPlayerTag(player, tag);
			}
			
			NBTTagCompoundMessage playerData = new NBTTagCompoundMessage();
			playerData.tag = PlayerSaveData.GetPlayerTag(player);
			MainMod.syncPlayerDataHandler.sendTo(playerData, player);
		}
		else if(henchmanLookup.containsKey(entity))
		{
			float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
			int healthToSet;
			if(currentHpPercentage > 0)
			{
				healthToSet = Math.round((currentHpPercentage * 100) + .499999F);
			}
			else
			{
				healthToSet = -1;
			}
			
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
		else
		{
			if(entity.id > 0)
			{
				EntityLivingBase deadEntity = (EntityLivingBase) world.getEntityByID(entity.id);
				if(deadEntity != null)
				{
					if(entity.currentHp < 1)
					{
						deadEntity.setLastAttacker(damageSource.getSourceOfDamage());
						deadEntity.attackEntityFrom(damageSource, 1000);
					}
					else
					{
						float currentHpPercentage = (float)entity.currentHp / entity.GetMaxHp();
						deadEntity.setHealth(currentHpPercentage * deadEntity.getMaxHealth());
					}
				}
			}
		}
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

		if(abilityToUse == null)
		{
			AttemptEscape(user);
			return;
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
	
	private void ClearAttack()
	{
		this.entityForCurrentTurn = null;
	}

}
