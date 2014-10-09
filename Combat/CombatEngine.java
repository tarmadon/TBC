package TBC.Combat;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import TBC.Pair;
import TBC.Combat.Abilities.AbilityTargetType;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.Combat.Effects.IExpiringEffect;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CombatEngine
{
	private static final int maxEntities = 5;
	public ArrayList<CombatEntity> allies;
	public ArrayList<CombatEntity> enemies;
	private Random rng;
	private int turnCounter;
	private int currentEntityId;
	
	private Hashtable<CombatEntity, Float> turnTimings = new Hashtable<CombatEntity, Float>();

	public CombatEngine(ArrayList<CombatEntity> allies, ArrayList<CombatEntity> enemies, Boolean isAttacker, int currentEntityId)
	{
		this.currentEntityId = currentEntityId;
		this.allies = allies;
		this.enemies = enemies;
		this.rng = CombatRandom.GetRandom();

		float allyStartTime = 50F;
		float enemyStartTime = 50F;
		if(isAttacker && rng.nextInt(101) > 90)
		{
			allyStartTime = 100F;
		}
		else if(!isAttacker)
		{
			enemyStartTime = 100F;
		}

		for(int i = 0; i<this.allies.size(); i++)
		{
			List constantEffects = new ArrayList();
			CombatEntity ally = this.allies.get(i);
			for(Pair<Integer, ICombatAbility> ability : ally.GetAbilities())
			{
				if(ability.item2 instanceof ConstantAbility)
				{
					constantEffects.addAll(((ConstantAbility)ability.item2).GetConstantEffects());
				}
			}

			if(ally.entityType == null)
			{
				ArrayList<ICombatAbility> equipmentAbilities = EquippedItemManager.Instance.GetAbilitiesFromEquippedItems(Minecraft.getMinecraft(), (EntityPlayer)Minecraft.getMinecraft().theWorld.getEntityByID(ally.id));
				for(ICombatAbility equipmentAbility : equipmentAbilities)
				{
					if(equipmentAbility instanceof ConstantAbility)
					{
						constantEffects.addAll(((ConstantAbility)equipmentAbility).GetConstantEffects());
					}
				}
			}

			ally.ongoingEffects = constantEffects;
			turnTimings.put(this.allies.get(i), allyStartTime);
		}

		for(int i = 0; i<this.enemies.size(); i++)
		{
			List constantEffects = new ArrayList();
			CombatEntity enemy = this.enemies.get(i);
			for(Pair<Integer, ICombatAbility> ability : enemy.GetAbilities())
			{
				if(ability.item2 instanceof ConstantAbility)
				{
					constantEffects.addAll(((ConstantAbility)ability.item2).GetConstantEffects());
				}
			}

			enemy.ongoingEffects = constantEffects;
			turnTimings.put(this.enemies.get(i), enemyStartTime);
		}
	}

	public Boolean CanEscape(ArrayList<CombatEntity> toEscape)
	{
		int allySpeed = this.GetAverageSpeed(toEscape);
		int enemySpeed = this.GetAverageSpeed(this.enemies);

		int chanceToRun = 75 + 2 * (allySpeed - enemySpeed);

		if(rng.nextInt(101) < chanceToRun)
		{
			return true;
		}

		return false;
	}

	public void RemoveEntities(ArrayList<CombatEntity> toRemove)
	{
		this.allies.removeAll(toRemove);
		this.enemies.removeAll(toRemove);
		for(CombatEntity e : toRemove)
		{
			this.turnTimings.remove(e);
		}
	}
	
	public Pair<ICombatAbility, ArrayList<CombatEntity>> Attack(CombatEntity attacker, ArrayList<String> messages)
	{
		ICombatAbility abilityToUse = this.ChooseAbilityForEnemy(attacker);
		ArrayList<CombatEntity> targets = this.ChooseTargetForEnemy(attacker, abilityToUse);
		this.Attack(attacker, targets, abilityToUse, messages);
		return new Pair<ICombatAbility, ArrayList<CombatEntity>>(abilityToUse, targets);
	}

	public boolean Attack(CombatEntity attacker, ArrayList<CombatEntity> targets, ICombatAbility ability, ArrayList<String> messages)
	{
		attacker.currentMp = attacker.currentMp - ability.GetMpCost();
		IOneTimeEffect[] attackEffects = ability.GetEffects(this, attacker, targets, messages);
		for(int i = 0; i<targets.size(); i++)
		{
			for(int j = 0; j<attackEffects.length; j++)
			{
				attackEffects[j].ApplyToEntity(this, attacker, targets.get(i));
			}
		}

		return true;
	}

	public Boolean EndTurn(CombatEntity entity)
	{
		Boolean needsDisplay = false;
		if(entity != null && entity.ongoingEffects != null)
		{
			List effectsToRemove = new ArrayList();
			for(int i = 0; i<entity.ongoingEffects.size(); i++)
			{
				Object effect = entity.ongoingEffects.get(i);
				if(effect instanceof ITriggeredEffect)
				{
					needsDisplay |= ((ITriggeredEffect)effect).EndOfTurn(this, entity);
				}

				if(effect instanceof IExpiringEffect && ((IExpiringEffect)effect).IsExpiredOnNextTurn())
				{
					effectsToRemove.add(effect);
				}
			}

			for(Object effectToRemove : effectsToRemove)
			{
				entity.ongoingEffects.remove(effectToRemove);
			}
		}

		return needsDisplay;
	}

	public CombatEntity GetNextTurn()
	{
		Set<CombatEntity> keys = this.turnTimings.keySet();
		Iterator<CombatEntity> iterator = keys.iterator();
		float fastest = 0;
		CombatEntity fastestEntity = null;
		while(iterator.hasNext())
		{
			CombatEntity entity = iterator.next();
			float currentTiming = this.turnTimings.get(entity);
			if(currentTiming > 100 && currentTiming > fastest && entity.currentHp > 0)
			{
				fastest = currentTiming;
				fastestEntity = entity;
			}
		}

		while(fastestEntity == null)
		{
			Iterator<CombatEntity> allEntities = keys.iterator();
			while(allEntities.hasNext())
			{
				CombatEntity entity = allEntities.next();
				List toRemove = new ArrayList();
				if(entity.ongoingEffects != null && !entity.ongoingEffects.isEmpty())
				{
					for(Object effect : entity.ongoingEffects)
					{
						if(effect instanceof IExpiringEffect && ((IExpiringEffect)effect).IsExpiredOnNextTick())
						{
							toRemove.add(effect);
						}
					}
				}

				for(Object removeItem : toRemove)
				{
					entity.ongoingEffects.remove(removeItem);
				}

				if(entity.currentHp > 0 && entity.GetSpeed() > -100)
				{
					float nextValue = (entity.GetSpeed() + 100)/100F;
					float oldValue = this.turnTimings.get(entity);
					float newValue = nextValue + oldValue;
					this.turnTimings.put(entity, newValue);
					if(newValue > 100 && newValue > fastest)
					{
						fastestEntity = entity;
						fastest = newValue;
					}
				}
			}
		}

		this.turnTimings.put(fastestEntity, fastest - 100);
		return fastestEntity;
	}

	public SimpleEntry<Integer, Integer> GetXpAndApReward(ArrayList<CombatEntity> enemies)
	{
		int xp = 0;
		int ap = 0;
		for(int i = 0; i<enemies.size(); i++)
		{
			if(enemies.get(i).currentHp < 1)
			{
				xp += enemies.get(i).GetXpValue();
				ap += enemies.get(i).GetApValue();
			}
		}

		return new SimpleEntry<Integer, Integer>(xp, ap);
	}

	public ArrayList<ICombatAbility> GetChoosableAbilitiesForEntity(CombatEntity entity)
	{
		Pair<Integer, ICombatAbility>[] abilities = entity.GetAbilities();
		ArrayList<ICombatAbility> allowed = new ArrayList<ICombatAbility>();
		for(Pair<Integer, ICombatAbility> ability : abilities)
		{
			if(!ability.item2.GetAbilityName().isEmpty())
			{
				allowed.add(ability.item2);
			}
		}

		for(Object ongoing : entity.ongoingEffects)
		{
			if(ongoing instanceof ITriggeredEffect)
			{
				allowed = ((ITriggeredEffect)ongoing).OnGetAbilities(this, entity, allowed);
			}
		}

		return allowed;
	}

	public void DoDamage(CombatEntity user, CombatEntity target, IDamageEffect source, int damage)
	{
		if(user.ongoingEffects != null)
		{
			for(int i = 0; i< user.ongoingEffects.size(); i++)
			{
				Object attackerEffect = user.ongoingEffects.get(i);
				if(attackerEffect instanceof ITriggeredEffect)
				{
					damage = ((ITriggeredEffect)attackerEffect).OnDamage(this, user, target, source, damage, true);
				}
			}
		}

		if(target.ongoingEffects != null)
		{
			for(int i = 0; i<target.ongoingEffects.size(); i++)
			{
				Object defenderEffect = target.ongoingEffects.get(i);
				if(defenderEffect instanceof ITriggeredEffect)
				{
					damage = ((ITriggeredEffect)defenderEffect).OnDamage(this, user, target, source, damage, false);
				}
			}
		}

		target.AddDamageTaken(damage);
	}

	private int GetAverageSpeed(ArrayList<CombatEntity> entities)
	{
		int totalSpeed = 0;
		int partyMemberCount = entities.size();
		for(int i = 0; i<partyMemberCount; i++)
		{
			totalSpeed += entities.get(i).GetSpeed();
		}

		return totalSpeed / partyMemberCount;
	}

	private ICombatAbility ChooseAbilityForEnemy(CombatEntity enemy)
	{
		int totalWeight = 0;
		ArrayList<Pair<Integer, ICombatAbility>> usableAbilities = new ArrayList<Pair<Integer, ICombatAbility>>();
		for(int i = 0; i<enemy.GetAbilities().length; i++)
		{
			if(enemy.GetAbilities()[i].item2.GetMpCost() <= enemy.currentMp)
			{
				usableAbilities.add(enemy.GetAbilities()[i]);
			}
		}

		for(int i = 0; i<usableAbilities.size(); i++)
		{
			totalWeight += usableAbilities.get(i).item1;
		}

		ICombatAbility ability = usableAbilities.get(0).item2;
		int rand = CombatRandom.GetRandom().nextInt(totalWeight);
		int weightTilNow = 0;
		for(int i = 0; i<usableAbilities.size(); i++)
		{
			weightTilNow += usableAbilities.get(i).item1;
			if(rand < weightTilNow)
			{
				ability = usableAbilities.get(i).item2;
				break;
			}
		}

		return ability;
	}

	public void AddEntityToCombat(CombatEntity user, CombatEntity toAdd)
	{
		boolean isAlly = false;
		for(CombatEntity ally : allies)
		{
			if(user == ally)
			{
				isAlly = true;
			}
		}

		if(isAlly && this.allies.size() < maxEntities)
		{
			this.allies.add(toAdd);
		}
		else if(this.enemies.size() < maxEntities)
		{
			this.enemies.add(toAdd);
		}

		List constantEffects = new ArrayList();
		for(Pair<Integer, ICombatAbility> ability : toAdd.GetAbilities())
		{
			if(ability.item2 instanceof ConstantAbility)
			{
				constantEffects.addAll(((ConstantAbility)ability.item2).GetConstantEffects());
			}
		}

		toAdd.ongoingEffects = constantEffects;
		this.turnTimings.put(toAdd, 0F);
	}

	private ArrayList<CombatEntity> ChooseTargetForEnemy(CombatEntity enemy, ICombatAbility ability)
	{
		ArrayList<ArrayList<CombatEntity>> targets = GetValidTargets(enemy, ability.GetAbilityTarget());
		int entitySize = targets.size();
		int picked = this.rng.nextInt(entitySize);
		return targets.get(picked);
	}

	public ArrayList<ArrayList<CombatEntity>> GetValidTargets(CombatEntity entityForCurrentTurn, int abilityTargetType)
	{
		boolean isAlly = false;
		ArrayList<ArrayList<CombatEntity>> targets = new ArrayList<ArrayList<CombatEntity>>();
		for(CombatEntity ally : this.allies)
		{
			if(ally == entityForCurrentTurn)
			{
				isAlly = true;
			}
		}

		if(abilityTargetType == AbilityTargetType.AllAllies || abilityTargetType == AbilityTargetType.AllEnemies)
		{
			if((isAlly && abilityTargetType == AbilityTargetType.AllAllies) || (!isAlly && abilityTargetType == AbilityTargetType.AllEnemies ))
			{
				ArrayList<CombatEntity> liveAllies = new ArrayList<CombatEntity>();
				for(CombatEntity ally : this.allies)
				{
					if(ally.currentHp > 0)
					{
						liveAllies.add(ally);
					}
				}

				targets.add(liveAllies);
			}
			else
			{
				ArrayList<CombatEntity> liveEnemies = new ArrayList<CombatEntity>();
				for(CombatEntity enemy : this.enemies)
				{
					if(enemy.currentHp > 0)
					{
						liveEnemies.add(enemy);
					}
				}

				targets.add(liveEnemies);
			}
		}
		else if(abilityTargetType == AbilityTargetType.OneAlly || abilityTargetType == AbilityTargetType.OneEnemy)
		{
			if((isAlly && abilityTargetType == AbilityTargetType.OneAlly) || (!isAlly && abilityTargetType == AbilityTargetType.OneEnemy ))
			{
				for(CombatEntity ally : this.allies)
				{
					if(ally.currentHp > 0)
					{
						ArrayList<CombatEntity> selection = new ArrayList<CombatEntity>();
						selection.add(ally);
						targets.add(selection);
					}
				}
			}
			else
			{
				for(CombatEntity enemy : this.enemies)
				{
					if(enemy.currentHp > 0)
					{
						ArrayList<CombatEntity> selection = new ArrayList<CombatEntity>();
						selection.add(enemy);
						targets.add(selection);
					}
				}
			}
		}
		else if(abilityTargetType == AbilityTargetType.Self)
		{
			ArrayList<CombatEntity> selection = new ArrayList<CombatEntity>();
			selection.add(entityForCurrentTurn);
			targets.add(selection);
		}
		else if(abilityTargetType == AbilityTargetType.OneDeadAlly)
		{
			if(isAlly)
			{
				for(CombatEntity ally : this.allies)
				{
					if(ally.currentHp <= 0)
					{
						ArrayList<CombatEntity> selection = new ArrayList<CombatEntity>();
						selection.add(ally);
						targets.add(selection);
					}
				}
			}
			else
			{
				for(CombatEntity enemy : this.enemies)
				{
					if(enemy.currentHp <= 0)
					{
						ArrayList<CombatEntity> selection = new ArrayList<CombatEntity>();
						selection.add(enemy);
						targets.add(selection);
					}
				}
			}
		}

		for(Object attackerEffect : entityForCurrentTurn.ongoingEffects)
		{
			if(attackerEffect instanceof ITriggeredEffect)
			{
				targets = ((ITriggeredEffect)attackerEffect).OnChooseTarget(this, entityForCurrentTurn, abilityTargetType, targets, true);
			}
		}

		for(ArrayList<CombatEntity> targetList : targets)
		{
			for(CombatEntity target : targetList)
			{
				for(Object potentialTargetEffect : target.ongoingEffects)
				{
					if(potentialTargetEffect instanceof ITriggeredEffect)
					{
						targets = ((ITriggeredEffect)potentialTargetEffect).OnChooseTarget(this, entityForCurrentTurn, abilityTargetType, targets, false);
					}
				}
			}
		}

		return targets;
	}

	public void DelayTurnForEntity(CombatEntity toDelay, int delayAmount)
	{
		if(this.turnTimings.containsKey(toDelay))
		{
			float currentTime = this.turnTimings.get(toDelay);
			this.turnTimings.put(toDelay, currentTime - delayAmount);
		}
	}

	public int GetNewEntityId()
	{
		return currentEntityId++;
	}
	
	public Pair<ICombatAbility, ArrayList<CombatEntity>> GetQueuedAbility(CombatEntity next)
	{
		for(Object ongoing : next.ongoingEffects)
		{
			if(ongoing instanceof ITriggeredEffect)
			{
				ITriggeredEffect triggeredEffect = (ITriggeredEffect)ongoing;
				Pair<ICombatAbility, ArrayList<CombatEntity>> targeting = triggeredEffect.OnTurnStart(this, next, this.allies, this.enemies);
				if(targeting != null)
				{
					return targeting;
				}
			}
		}

		return null;
	}
}
