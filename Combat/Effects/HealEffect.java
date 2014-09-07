package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.DamageType;
import net.minecraft.entity.player.EntityPlayer;

public class HealEffect implements IOneTimeEffect, IDamageEffect
{
	private int flatMagicStrength;
	private float magicMultiplier;
	
	public HealEffect(int flatMagicStrength, float magicMultiplier)
	{
		this.flatMagicStrength = flatMagicStrength;
		this.magicMultiplier = magicMultiplier;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target) 
	{
		int strength = this.flatMagicStrength + Math.round(user.GetMagic() * magicMultiplier);
		engine.DoDamage(user, target, this, -strength);
	}

	public int GetDamageType() 
	{
		return DamageType.Light;
	}
}
