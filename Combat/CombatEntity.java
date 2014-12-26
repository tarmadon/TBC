package TBC.Combat;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import TBC.HenchmanItem;
import TBC.Pair;
import TBC.CombatEntitySaveData;
import TBC.PlayerSaveData;
import TBC.Combat.Abilities.AbilityLookup;
import TBC.Combat.Abilities.ConstantAbility;
import TBC.Combat.Abilities.IAbility;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.StatChangeStatus;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CombatEntity implements Serializable
{
	public int id;
	public String entityType;
	public int currentHp;
	public int currentMp;
	public Integer lastDamageTaken = null;
	public String name;
	public boolean isFrontLine;
	public transient NBTTagCompound tag;
	public byte[] tagAsData;
	
	public List ongoingEffects;
	private CombatEntityTemplate baseStats;

	public CombatEntity(int id, String entityType, CombatEntityTemplate baseStats, NBTTagCompound tag)
	{
		this.id = id;
		this.entityType = entityType;
		this.baseStats = baseStats;
		this.tag = tag;
		this.ongoingEffects = GetStartingConstantEffects();
		PopulateWithBaseStats();
	}

	public static CombatEntity GetCombatEntity(int entityId, String entityType, String enemyName, int enemyNumber)
	{
		CombatEntity lookup;
		lookup = CombatEntityLookup.Instance.GetCombatEntity(entityId, entityType, enemyName, new NBTTagCompound());
		lookup.name = lookup.baseStats.name;
		lookup.isFrontLine = true;
		if(enemyNumber == 1)
		{
			lookup.name += " A";
		}
		else if(enemyNumber == 2)
		{
			lookup.name += " B";
		}
		else if(enemyNumber == 3)
		{
			lookup.name += " C";
		}
		else if(enemyNumber == 4)
		{
			lookup.name += " D";
		}
		else if(enemyNumber == 5)
		{
			lookup.name += " E";
		}

		lookup.currentHp = lookup.baseStats.maxHp;
		return lookup;
	}

	public static CombatEntity GetCombatEntity(int entityId, EntityLivingBase entity, int enemyNumber)
	{
		CombatEntity lookup;
		lookup = CombatEntitySpawnLookup.Instance.GetCombatEntity(entityId, entity);
		lookup.name = lookup.baseStats.name;
		lookup.isFrontLine = true;
		if(enemyNumber == 1)
		{
			lookup.name += " A";
		}
		else if(enemyNumber == 2)
		{
			lookup.name += " B";
		}
		else if(enemyNumber == 3)
		{
			lookup.name += " C";
		}
		else if(enemyNumber == 4)
		{
			lookup.name += " D";
		}
		else if(enemyNumber == 5)
		{
			lookup.name += " E";
		}

		float currentHpPercentage = (float)entity.getHealth() / entity.getMaxHealth();
		lookup.currentHp = Math.round(currentHpPercentage * lookup.baseStats.maxHp);
		return lookup;
	}

	public static Pair<Integer, CombatEntity> GetCombatEntity(EntityPlayer entity)
	{
		CombatEntitySaveData s = new CombatEntitySaveData();
		s.loadNBTData(PlayerSaveData.GetPlayerTag(entity));
		String name = entity.getDisplayName();
		CombatEntityTemplate t = CombatEntityTemplate.GetCombatEntityTemplateFromSaveData(name, s);
		CombatEntity e = new CombatEntity(entity.getEntityId(), null, t, PlayerSaveData.GetPlayerTag(entity));
		e.name = t.name;
		e.isFrontLine = s.IsFrontRow > 0 ? true : false;
		
		float currentHpPercentage = (float)entity.getHealth() / entity.getMaxHealth();
		e.currentHp = Math.round(currentHpPercentage * e.GetMaxHp());
		if(e.currentHp < 1)
		{
			e.currentHp = 1;
		}
		
		if(e.currentHp > e.GetMaxHp())
		{
			e.currentHp = e.GetMaxHp();
		}
		
		NBTTagCompound tag = PlayerSaveData.GetPlayerTag(entity);
		if(tag.hasKey("TBCPlayerMP"))
		{
			e.currentMp = tag.getInteger("TBCPlayerMP");
			if(e.currentMp > e.GetMaxMp())
			{
				e.currentMp = e.GetMaxMp();
			}
		}

		return new Pair<Integer, CombatEntity>(s.IsInParty, e);
	}

	public static Pair<Integer, CombatEntity> GetCombatEntity(int entityId, ItemStack henchmanStack)
	{
		HenchmanItem h = (HenchmanItem)henchmanStack.getItem();
		CombatEntitySaveData s = HenchmanItem.GetCombatEntitySaveData(henchmanStack);
		String name = h.henchmanName;
		CombatEntityTemplate t = CombatEntityTemplate.GetCombatEntityTemplateFromSaveData(name, s);
		CombatEntity henchmanEntity = new CombatEntity(entityId, null, t, HenchmanItem.GetTag(henchmanStack));
		henchmanEntity.name = name;
		henchmanEntity.entityType = h.henchmanType;
		henchmanEntity.isFrontLine = s.IsFrontRow > 0 ? true : false;
		henchmanEntity.currentHp = (int)(henchmanEntity.currentHp * (1.0F - (h.getDamage(henchmanStack)/(float)h.getMaxDamage())));

		NBTTagCompound itemData = henchmanStack.getTagCompound();
		if(itemData != null && itemData.hasKey("HenchMP"))
		{
			henchmanEntity.currentMp = itemData.getInteger("HenchMP");
		}
		
		return new Pair<Integer, CombatEntity>(s.IsInParty, henchmanEntity);
	}
	
	public boolean IsFrontLine()
	{
		return this.isFrontLine;
	}
	
	public int GetMaxHp()
	{
		return GetEffectiveStat(this.baseStats.maxHp, StatChangeStatus.HpChange);
	}

	public int GetMaxMp()
	{
		return GetEffectiveStat(this.baseStats.maxMp, StatChangeStatus.MpChange);
	}

	public int GetAttack()
	{
		return GetEffectiveStat(this.baseStats.attack, StatChangeStatus.AttackChange);
	}

	public int GetDefense()
	{
		return GetEffectiveStat(this.baseStats.defense, StatChangeStatus.DefenseChange);
	}

	public int GetMagic()
	{
		return GetEffectiveStat(this.baseStats.mAttack, StatChangeStatus.MagicChange);
	}

	public int GetMagicDefense()
	{
		return GetEffectiveStat(this.baseStats.mDefense, StatChangeStatus.MagicDefenseChange);
	}

	public int GetSpeed()
	{
		return GetEffectiveStat(this.baseStats.speed, StatChangeStatus.SpeedChange);
	}

	public String GetName()
	{
		return this.name;
	}

	public int GetEffectiveStat(int currentStat, int statType)
	{
		int effectiveStat = EquippedItemManager.Instance.GetEffectiveStat(this, statType, currentStat);
		if(this.ongoingEffects != null)
		{
			ArrayList<String> appliedEffects = new ArrayList<String>();
			for(int i = 0; i<this.ongoingEffects.size(); i++)
			{
				Object effect = this.ongoingEffects.get(i);
				if(effect instanceof StatChangeStatus)
				{
					StatChangeStatus statChange = (StatChangeStatus)effect;
					if(statChange.changeType == statType)
					{
						if(!appliedEffects.contains(statChange.GetEffectName()))
						{
							effectiveStat = statChange.GetEffectiveStat(this, effectiveStat);
							if(!statChange.GetEffectName().isEmpty())
							{
								appliedEffects.add(statChange.GetEffectName());
							}
						}
					}
				}
			}
		}

		return effectiveStat;
	}

	public Pair<Integer, ICombatAbility>[] GetCombatAbilities()
	{
		ArrayList<Pair<Integer, ICombatAbility>> combatAbilitiesList = new ArrayList<Pair<Integer,ICombatAbility>>(); 
		for(Pair<Integer, IAbility> ability : this.baseStats.abilities)
		{
			if(ability.item2 instanceof ICombatAbility)
			{
				Pair<Integer, ICombatAbility> combatAbility = new Pair<Integer, ICombatAbility>(ability.item1, (ICombatAbility)ability.item2);
				combatAbilitiesList.add(combatAbility);
			}
		}
		
		Pair<Integer, ICombatAbility>[] combatAbilities = new Pair[combatAbilitiesList.size()];
		combatAbilitiesList.toArray(combatAbilities);
		return combatAbilities;
	}

	public ArrayList<ConstantAbility> GetConstantAbilities()
	{
		ArrayList<ConstantAbility> contantAbilities = new ArrayList<ConstantAbility>(); 
		for(Pair<Integer, IAbility> ability : this.baseStats.abilities)
		{
			if(ability.item2 instanceof ConstantAbility)
			{
				contantAbilities.add((ConstantAbility)ability.item2);
			}
		}
		
		return contantAbilities;
	}
	
	public int GetXpValue()
	{
		return this.baseStats.xpValue;
	}

	public int GetApValue()
	{
		return this.baseStats.apValue;
	}

	public CombatEntityTemplate GetBaseStats()
	{
		return this.baseStats;
	}

	public void AddDamageTaken(int damageAmount)
	{
		if(this.lastDamageTaken != null)
		{
			this.lastDamageTaken += damageAmount;
		}
		else
		{
			this.lastDamageTaken = damageAmount;
		}
	}

	public void ApplyDamage()
	{
		if(this.lastDamageTaken != null)
		{
			this.TakeDamage(this.lastDamageTaken);
			lastDamageTaken = null;
		}
	}

	private void TakeDamage(int damageAmount)
	{
		int maxHp = this.GetMaxHp();
		if(damageAmount < 0 && this.currentHp > maxHp)
		{
			return;
		}

		this.currentHp = this.currentHp - damageAmount;
		if(this.currentHp > maxHp)
		{
			this.currentHp = maxHp;
		}

		if(this.currentHp < 0)
		{
			this.currentHp = 0;
		}
	}

	private void PopulateWithBaseStats()
	{
		this.currentHp = this.GetMaxHp();
		this.currentMp = this.GetMaxMp();
		this.name = this.baseStats.name;
	}
	
	private List GetStartingConstantEffects()
	{
		List constantEffects = new ArrayList();
		for(ConstantAbility ability : this.GetConstantAbilities())
		{
			constantEffects.addAll(ability.GetConstantEffects());
		}

		if(this.tag != null)
		{
			ArrayList<ConstantAbility> equipmentAbilities = EquippedItemManager.Instance.GetConstantAbilitiesFromEquippedItems(this.tag);
			for(ConstantAbility equipmentAbility : equipmentAbilities)
			{
				constantEffects.addAll(((ConstantAbility)equipmentAbility).GetConstantEffects());
			}
		}
		
		return constantEffects;
	}
}
