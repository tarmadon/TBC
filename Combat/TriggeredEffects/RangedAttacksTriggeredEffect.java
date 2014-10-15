package TBC.Combat.TriggeredEffects;

import java.util.ArrayList;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.IOneTimeEffect;
import TBC.Combat.Effects.PhysicalDamageEffect;

public class RangedAttacksTriggeredEffect extends BaseTriggeredEffect
{
	@Override
	public IOneTimeEffect[] OnAttack(CombatEngine engine, CombatEntity attacker, ArrayList<CombatEntity> targets, IOneTimeEffect[] attackEffects, ArrayList<String> messages) 
	{
		for(int i = 0; i < attackEffects.length; i++)
		{
			IOneTimeEffect e = attackEffects[i];
			if(e instanceof PhysicalDamageEffect)
			{
				attackEffects[i] = new PhysicalDamageEffect((PhysicalDamageEffect)e).SetIsRanged(true); 
			}
		}

		return attackEffects;
	}
}
