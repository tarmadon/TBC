package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatRandom;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;

public class DeathEffect implements IOneTimeEffect, IDamageEffect
{
	private float chanceToApply;
	
	public DeathEffect(float chanceToApply)
	{
		this.chanceToApply = chanceToApply;
	}
	
	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target) 
	{
		if(CombatRandom.GetRandom().nextFloat() > chanceToApply)
		{
			return;
		}
		
		int damage = target.currentHp;
		engine.DoDamage(user, target, this, damage);
	}

	public int GetDamageType() 
	{
		return 0;
	}
}
