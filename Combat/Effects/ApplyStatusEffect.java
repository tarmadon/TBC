package TBC.Combat.Effects;

import java.util.ArrayList;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatRandom;
import TBC.Combat.IStatusChange;
import TBC.Combat.TriggeredEffects.ITriggeredEffect;

public class ApplyStatusEffect implements IOneTimeEffect
{
	private IEffectFactory factory;
	private float chanceToApply;

	public ApplyStatusEffect(IEffectFactory factory)
	{
		this(factory, 1F);
	}

	public ApplyStatusEffect(IEffectFactory factory, float chanceToApply)
	{
		this.factory = factory;
		this.chanceToApply = chanceToApply;
	}

	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target)
	{
		if(CombatRandom.GetRandom().nextFloat() > this.chanceToApply)
		{
			return;
		}

		Object toApply = factory.CreateEffect(user, target);
		Object finalToApply = toApply;
		for(Object attackerEffect : user.ongoingEffects)
		{
			if(attackerEffect instanceof ITriggeredEffect)
			{
				finalToApply = ((ITriggeredEffect)attackerEffect).OnStatusChange(engine, user, target, finalToApply, true);
			}
		}

		for(Object defenderEffect : target.ongoingEffects)
		{
			if(toApply instanceof INonStackingEffect && defenderEffect instanceof INonStackingEffect)
			{
				if(((INonStackingEffect)toApply).GetEffectName() ==((INonStackingEffect)defenderEffect).GetEffectName())
				{
					return;
				}
			}

			if(defenderEffect instanceof ITriggeredEffect)
			{
				finalToApply = ((ITriggeredEffect)defenderEffect).OnStatusChange(engine, user, target, finalToApply, false);
			}
		}

		if(finalToApply == null)
		{
			return;
		}

		if(target.ongoingEffects != null)
		{
			target.ongoingEffects.add(finalToApply);
		}
		else
		{
			target.ongoingEffects = new ArrayList<IStatusChange>();
			target.ongoingEffects.add(finalToApply);
		}
	}
}
