package TBC.Combat.TriggeredEffects;

import java.util.ArrayList;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.CombatRandom;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.INonStackingEffect;

public class ConfusionEffect extends DurationTriggeredEffect implements INonStackingEffect
{
	public ConfusionEffect(int durationInTurns, int durationInTicks)
	{
		super(durationInTurns, durationInTicks);
	}

	public Pair<ICombatAbility, ArrayList<CombatEntity>> OnTurnStart(CombatEngine combatEngine, CombatEntity user, ArrayList<CombatEntity> allies, ArrayList<CombatEntity> enemies)
	{
		ArrayList<CombatEntity> allLive = new ArrayList<CombatEntity>();
		for(CombatEntity ally : allies)
		{
			if(ally.currentHp > 0)
			{
				allLive.add(ally);
			}
		}

		for(CombatEntity enemy : enemies)
		{
			if(enemy.currentHp > 0)
			{
				allLive.add(enemy);
			}
		}

		int randomTarget = CombatRandom.GetRandom().nextInt(allLive.size());
		ArrayList<CombatEntity> chosen = new ArrayList<CombatEntity>();
		chosen.add(allLive.get(randomTarget));
		return new Pair<ICombatAbility, ArrayList<CombatEntity>>(user.GetCombatAbilities()[0].item2, chosen);
	}

	public String GetEffectName()
	{
		return "Confusion";
	}

	public Object CreateEffect(CombatEntity user, CombatEntity target)
	{
		return new ConfusionEffect(this.durationInTurns, this.durationInTicks);
	}
}
