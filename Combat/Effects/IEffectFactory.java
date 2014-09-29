package TBC.Combat.Effects;

import TBC.Combat.CombatEntity;

public interface IEffectFactory
{
	Object CreateEffect(CombatEntity user, CombatEntity target);
}
