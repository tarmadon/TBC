package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.FlatDamageEffect;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.IEffectFactory;
import TBC.Combat.Effects.INonStackingEffect;

public class PoisonStatusEffect extends BaseTriggeredEffect implements INonStackingEffect
{
	public boolean EndOfTurn(CombatEngine engine, CombatEntity entity)
	{
		int maxHp = entity.GetMaxHp();
		FlatDamageEffect poisonDamage = new FlatDamageEffect(Math.max(1, maxHp / 10));
		poisonDamage.ApplyToEntity(engine, entity, entity);
		return true;
	}

	public String GetEffectName()
	{
		return "Poison";
	}
}
