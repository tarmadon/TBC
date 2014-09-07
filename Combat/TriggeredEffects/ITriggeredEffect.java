package TBC.Combat.TriggeredEffects;

import java.util.ArrayList;

import TBC.Pair;
import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.IStatusChange;
import TBC.Combat.Abilities.ICombatAbility;
import TBC.Combat.Effects.IDamageEffect;

public interface ITriggeredEffect 
{
	int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker);
	boolean EndOfTurn(CombatEngine engine, CombatEntity entity);
	Object OnStatusChange(CombatEngine engine, CombatEntity user, CombatEntity target, Object statusChange, boolean effectFromAttacker);
	ArrayList<ICombatAbility> OnGetAbilities(CombatEngine combatEngine,	CombatEntity entity, ArrayList<ICombatAbility> allowed);
	ArrayList<ArrayList<CombatEntity>> OnChooseTarget(CombatEngine combatEngine, CombatEntity attacker,	int targetType, ArrayList<ArrayList<CombatEntity>> targets, boolean effectFromAttacker);
	Pair<ICombatAbility, ArrayList<CombatEntity>> OnTurnStart(CombatEngine combatEngine, CombatEntity user, ArrayList<CombatEntity> allies, ArrayList<CombatEntity> enemies);
}
