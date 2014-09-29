package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;

public class SetHPEffect implements IOneTimeEffect, IDamageEffect
{
	private Integer newValue = null;
	private Float multiplier = null;

	public SetHPEffect(int newValue)
	{
		this.newValue = newValue;
	}

	public SetHPEffect(float multiplier)
	{
		this.multiplier = multiplier;
	}

	public int GetDamageType()
	{
		return 0;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target)
	{
		int calculatedNewValue = 0;
		if(this.newValue != null)
		{
			calculatedNewValue = this.newValue;
		}
		else
		{
			calculatedNewValue = (int)Math.ceil(target.currentHp * this.multiplier);
		}

		int damage = target.currentHp - calculatedNewValue;
		engine.DoDamage(user, target, this, damage);
	}
}
