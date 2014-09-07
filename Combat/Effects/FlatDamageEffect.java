package TBC.Combat.Effects;

import java.util.Random;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;

import net.minecraft.entity.player.EntityPlayer;

public class FlatDamageEffect implements IOneTimeEffect, IDamageEffect
{
	private int flatStrength;
	
	public FlatDamageEffect(int flatStrength)
	{
		this.flatStrength = flatStrength;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target) 
	{
		engine.DoDamage(user, target, this, this.flatStrength);
	}

	public int GetDamageType() 
	{
		return 0;
	}
}