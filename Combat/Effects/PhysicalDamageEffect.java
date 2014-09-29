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

	public PhysicalDamageEffect(int flatStrength)
	{
		this(flatStrength, 1.0F, 0, 1.0F);
	}

	public PhysicalDamageEffect(int flatStrength, float physMultiplier, int flatDefenseReduction, float defenseMultiplier)
	{
		this(flatStrength, physMultiplier, flatDefenseReduction, defenseMultiplier, 0);
	}

	public PhysicalDamageEffect(int flatStrength, float physMultiplier, int flatDefenseReduction, float defenseMultiplier, int additionalDamageTypes)
	{
		this.flatStrength = flatStrength;
		this.physMultiplier = physMultiplier;
		this.flatDefenseReduction = flatDefenseReduction;
		this.defenseMultiplier = defenseMultiplier;
		this.additionalDamageTypes = additionalDamageTypes;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target)
	{
		int strength = this.flatStrength + Math.round(user.GetAttack() * physMultiplier);
		int defense = Math.round(target.GetDefense() * this.defenseMultiplier) - this.flatDefenseReduction;
		int damage = DamageAlgorithm.GetPhysicalDamage(strength, defense);
		engine.DoDamage(user, target, this, damage);
	}

	public int GetDamageType()
	{
		return DamageType.Physical | this.additionalDamageTypes;
	}
}