package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.IStatusChange;
import TBC.Combat.Effects.IDamageEffect;

public class PoisonImmunityEffect extends BaseTriggeredEffect
{
	public Object OnStatusChange(
			CombatEngine engine, 
			CombatEntity user,	
			CombatEntity target, 
			Object statusChange, 
			boolean effectFromAttacker) 
	{
		if(statusChange instanceof PoisonStatusEffect && !effectFromAttacker)
		{
			return null;
		}
		
		return super.OnStatusChange(engine, user, target, statusChange,	effectFromAttacker);
	}
}
