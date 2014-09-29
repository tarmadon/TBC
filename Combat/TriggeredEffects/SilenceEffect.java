package TBC.Combat.TriggeredEffects;

import java.util.ArrayList;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.IEffectFactory;
import TBC.Combat.Effects.INonStackingEffect;

public class SilenceEffect extends BaseTriggeredEffect implements INonStackingEffect
{
	public String GetEffectName()
	{
		return "Silence";
	}

	public ArrayList<ICombatAbility> OnGetAbilities(CombatEngine combatEngine, CombatEntity entity, ArrayList<ICombatAbility> allowed)
	{
		ArrayList<ICombatAbility> nonSilenced = new ArrayList<ICombatAbility>();
		for(ICombatAbility ability : allowed)
		{
			if(!ability.IsSpell())
			{
				nonSilenced.add(ability);
			}
		}

		return nonSilenced;
	}
}
