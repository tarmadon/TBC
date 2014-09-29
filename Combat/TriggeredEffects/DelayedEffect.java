package TBC.Combat.TriggeredEffects;

import java.util.ArrayList;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Abilities.ICombatAbility;

public class DelayedEffect extends DurationTriggeredEffect
{
	private ArrayList<CombatEntity> targets;
	private ICombatAbility ability;

	public DelayedEffect(int durationInTurns, int durationInTicks, ICombatAbility ability, ArrayList<CombatEntity> targets)
	{
		super(durationInTurns, durationInTicks);
		this.ability = ability;
		this.targets = targets;
	}

	public Pair<ICombatAbility, ArrayList<CombatEntity>> OnTurnStart(CombatEngine combatEngine, CombatEntity user, ArrayList<CombatEntity> allies, ArrayList<CombatEntity> enemies)
	{
		return new Pair<ICombatAbility, ArrayList<CombatEntity>>(ability, targets);
	}

	public Object CreateEffect(CombatEntity user, CombatEntity target)
	{
		return new DelayedEffect(durationInTurns, durationInTicks, ability, targets);
	}
}
