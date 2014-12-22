package TBC.Combat.TriggeredEffects;

import java.io.Serializable;
import java.util.ArrayList;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.IStatusChange;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.IEffect;
import TBC.Combat.Effects.IOneTimeEffect;

public interface ITriggeredEffect extends Serializable, IEffect
{
	IOneTimeEffect[] OnAttack(CombatEngine engine, CombatEntity attacker, ArrayList<CombatEntity> targets, IOneTimeEffect[] attackEffects, ArrayList<String> messages);
	int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker);
	boolean EndOfTurn(CombatEngine engine, CombatEntity entity);
	Object OnStatusChange(CombatEngine engine, CombatEntity user, CombatEntity target, Object statusChange, boolean effectFromAttacker);
	ArrayList<ICombatAbility> OnGetAbilities(CombatEngine combatEngine,	CombatEntity entity, ArrayList<ICombatAbility> allowed);
	ArrayList<ArrayList<CombatEntity>> OnChooseTarget(CombatEngine combatEngine, CombatEntity attacker,	int targetType, ArrayList<ArrayList<CombatEntity>> targets, boolean effectFromAttacker);
	Pair<ICombatAbility, ArrayList<CombatEntity>> OnTurnStart(CombatEngine combatEngine, CombatEntity user, ArrayList<CombatEntity> allies, ArrayList<CombatEntity> enemies);
}
