package TBC.Combat.Abilities;

import java.util.ArrayList;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IOneTimeEffect;

public class SacrificeAbility extends StandardAbility
{
	private IOneTimeEffect[] effectsOnUser;

	public SacrificeAbility(IOneTimeEffect[] effectsOnUser, IOneTimeEffect[] effectsOnTarget, String name, int targetType, int cost, Boolean usableOutOfCombat, Boolean isSpell, ArrayList<String> description)
	{
		super(effectsOnTarget, name, targetType, cost, usableOutOfCombat, isSpell, description);
		this.effectsOnUser = effectsOnUser;
	}

	public IOneTimeEffect[] GetEffects(CombatEngine engine, CombatEntity user, ArrayList<CombatEntity> targets, ArrayList<String> messages)
	{
		for(IOneTimeEffect e : this.effectsOnUser)
		{
			e.ApplyToEntity(engine, user, user);
		}

		return super.GetEffects(engine, user, targets, messages);
	}
}
