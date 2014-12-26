package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatRandom;
import TBC.Combat.DamageType;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.INonStackingEffect;

public class MissChanceTriggeredEffect extends BaseTriggeredEffect
{
	private float missPercent;

	public MissChanceTriggeredEffect(float missPercent)
	{
		this.missPercent = missPercent;
	}

	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker)
	{
		if(effectFromAttacker && ((source.GetDamageType() & DamageType.Physical) == DamageType.Physical) && CombatRandom.GetRandom().nextFloat() <= missPercent)
		{
			return 0;
		}

		return damage;
	}
}
