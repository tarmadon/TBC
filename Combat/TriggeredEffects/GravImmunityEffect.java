package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.SetHPEffect;

public class GravImmunityEffect extends BaseTriggeredEffect 
{
	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker) 
	{
		if(source instanceof SetHPEffect && !effectFromAttacker && damage > 0)
		{
			return 0;
		}
		
		return damage;
	}
}
