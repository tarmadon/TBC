package TBC.Combat.TriggeredEffects;

import java.util.ArrayList;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.IStatusChange;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.IEffectFactory;

public class BaseTriggeredEffect implements ITriggeredEffect, IEffectFactory
{
	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker)
	{
		return damage;
	}

	public boolean EndOfTurn(CombatEngine engine, CombatEntity entity)
	{
		return false;
	}

	public Object OnStatusChange(CombatEngine engine, CombatEntity user, CombatEntity target, Object statusChange, boolean effectFromAttacker)
	{
		return statusChange;
	}

	public ArrayList<ICombatAbility> OnGetAbilities(CombatEngine combatEngine, CombatEntity entity, ArrayList<ICombatAbility> allowed)
	{
		return allowed;
	}

	public Object CreateEffect(CombatEntity user, CombatEntity target)
	{
		return this;
	}

	public ArrayList<ArrayList<CombatEntity>> OnChooseTarget(CombatEngine combatEngine, CombatEntity attacker, int targetType, ArrayList<ArrayList<CombatEntity>> targets, boolean effectFromAttacker)
	{
		return targets;
	}

	public Pair<ICombatAbility, ArrayList<CombatEntity>> OnTurnStart(CombatEngine combatEngine, CombatEntity user, ArrayList<CombatEntity> allies, ArrayList<CombatEntity> enemies)
	{
		return null;
	}
}
