package TBC.Combat.Effects;

import java.util.Random;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.DamageAlgorithm;
import TBC.Combat.DamageType;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;

import net.minecraft.entity.player.EntityPlayer;

public class PhysicalDamageEffect implements IOneTimeEffect, IDamageEffect
{
	private int flatStrength;
	private float physMultiplier;
	private int flatDefenseReduction;
	private float defenseMultiplier;
	private int additionalDamageTypes;
	private boolean rangedAttack;
	
	public PhysicalDamageEffect(int flatStrength)
	{
		this.flatStrength = flatStrength;
		this.physMultiplier = 1.0F;
		this.flatDefenseReduction = 0;
		this.defenseMultiplier = 1.0F;
		this.additionalDamageTypes = 0;
		this.rangedAttack = false;
	}
	
	public PhysicalDamageEffect(PhysicalDamageEffect other)
	{
		this.flatStrength = other.flatStrength;
		this.physMultiplier = other.physMultiplier;
		this.flatDefenseReduction = other.flatDefenseReduction;
		this.defenseMultiplier = other.defenseMultiplier;
		this.additionalDamageTypes = other.additionalDamageTypes;
		this.rangedAttack = other.rangedAttack;
	}
	
	public PhysicalDamageEffect SetAttackMultiplier(float attackMultiplier)
	{
		this.physMultiplier = attackMultiplier;
		return this;
	}
	
	public PhysicalDamageEffect SetFlatDefenseReduction(int defenseReduction)
	{
		this.flatDefenseReduction = defenseReduction;
		return this;
	}
	
	public PhysicalDamageEffect SetDefenseMultiplier(float defenseMultiplier)
	{
		this.defenseMultiplier = defenseMultiplier;
		return this;
	}
	
	public PhysicalDamageEffect SetAdditionalDamageTypes(int additionalDamageTypes)
	{
		this.additionalDamageTypes = additionalDamageTypes;
		return this;
	} 
	
	public PhysicalDamageEffect SetIsRanged(boolean isRanged)
	{
		this.rangedAttack = isRanged;
		return this;
	}
	
	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target)
	{
		int strength = this.flatStrength + Math.round(user.GetAttack() * physMultiplier);
		int defense = Math.round(target.GetDefense() * this.defenseMultiplier) - this.flatDefenseReduction;
		int damage = DamageAlgorithm.GetPhysicalDamage(strength, defense);
		if(!rangedAttack && (!user.isFrontLine || !target.isFrontLine))
		{
			boolean reductionApplied = false;
			float effectiveDamage = damage;
			if(!user.isFrontLine && EntityTeamHasFrontLine(engine, user))
			{
				effectiveDamage = effectiveDamage / 2;
				reductionApplied = true;
			}
			
			if(!target.isFrontLine && EntityTeamHasFrontLine(engine, target))
			{
				effectiveDamage = effectiveDamage / 2;
				reductionApplied = true;
			}
			
			if(reductionApplied)
			{
				damage = Math.round(effectiveDamage);
			}
		}
		
		engine.DoDamage(user, target, this, damage);
	}

	public int GetDamageType()
	{
		return DamageType.Physical | this.additionalDamageTypes;
	}
	
	private boolean EntityTeamHasFrontLine(CombatEngine engine, CombatEntity toCheck)
	{
		boolean rightTeam = false;
		boolean hasFrontLine = false;
		for(int i = 0; i < engine.allies.size(); i++)
		{
			CombatEntity ally = engine.allies.get(i);
			if(toCheck == ally)
			{
				rightTeam = true;
			}
			else if(ally.isFrontLine)
			{
				hasFrontLine = true;
			}
		}
		
		if(rightTeam)
		{
			return hasFrontLine;
		}
		
		hasFrontLine = false;
		for(int i = 0; i < engine.enemies.size(); i++)
		{
			CombatEntity enemy = engine.enemies.get(i);
			if(enemy.isFrontLine)
			{
				hasFrontLine = true;
				break;
			}
		}
		
		return hasFrontLine;
	}
}