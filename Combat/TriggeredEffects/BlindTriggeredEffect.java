package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatRandom;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.INonStackingEffect;

public class BlindTriggeredEffect extends BaseTriggeredEffect implements INonStackingEffect
{
	private int damageType;

	public BlindTriggeredEffect(int damageType)
	{
		this.damageType = damageType;
	}

	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker)
	{
		if(effectFromAttacker && ((source.GetDamageType() & this.damageType) == this.damageType) && CombatRandom.GetRandom().nextFloat() <= .8F)
		{
			return 0;
		}

		return damage;
	}

	public String GetEffectName()
	{
		return "Blind";
	}
}
