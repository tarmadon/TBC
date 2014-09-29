package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IDamageEffect;

public class DeathImmunityEffect extends BaseTriggeredEffect
{
	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker)
	{
		if(source.GetDamageType() == 0 && !effectFromAttacker)
		{
			return 0;
		}

		return damage;
	}
}