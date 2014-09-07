package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.DamageAlgorithm;
import TBC.Combat.DamageType;
import net.minecraft.entity.player.EntityPlayer;

public class LifeStealEffect implements IOneTimeEffect, IDamageEffect
{
	private int flatPhysicalStrength;
	private int flatMagicalStrength;
	private float physMultiplier;
	private float magMultiplier;
	private float conversionRate;
	
	public LifeStealEffect(int flatPhysicalStrength, int flatMagicalStrength, float physMultiplier, float magMultiplier, float conversionRate)
	{
		this.flatPhysicalStrength = flatPhysicalStrength;
		this.flatMagicalStrength = flatMagicalStrength;
		this.physMultiplier = physMultiplier;
		this.magMultiplier = magMultiplier;
		this.conversionRate = conversionRate;
	}
	
	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target) 
	{
		int physicalStrength = this.flatPhysicalStrength + Math.round(user.GetAttack() * this.physMultiplier);
		int magicalStrength = this.flatMagicalStrength + Math.round(user.GetMagic() * this.magMultiplier);
		int mDefense = target.GetMagicDefense();
		int defense = target.GetDefense();
		int damage = DamageAlgorithm.GetPhysicalDamage(physicalStrength, defense);
		damage += DamageAlgorithm.GetMagicalDamage(magicalStrength, mDefense);
		engine.DoDamage(user, target, this, damage);
		
		int healing = (int)(conversionRate * damage);
		engine.DoDamage(user, user, this, -healing);
	}

	public int GetDamageType() 
	{
		int damageType = 0;
		if(flatPhysicalStrength != 0 || physMultiplier > 0)
		{
			damageType |= DamageType.Physical;
		}
		
		if(flatMagicalStrength != 0 || magMultiplier > 0)
		{
			damageType |= DamageType.Magical;
		}
		
		return damageType;
	}
}