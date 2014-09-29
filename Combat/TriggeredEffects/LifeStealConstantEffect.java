package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.DamageType;
import TBC.Combat.Effects.IDamageEffect;

public class LifeStealConstantEffect extends BaseTriggeredEffect implements IDamageEffect
{
	private int damageType;
	private float stealRatio;

	public LifeStealConstantEffect(int damageType, float stealRatio)
	{
		this.damageType = damageType;
		this.stealRatio = stealRatio;
	}

	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker)
	{
		if(effectFromAttacker && ((source.GetDamageType() & this.damageType) == this.damageType) && damage > 0)
		{
			int lifeSteal = (int)(damage * stealRatio);
			engine.DoDamage(attacker, attacker, this, -lifeSteal);
		}

		return damage;
	}

	public int GetDamageType()
	{
		return DamageType.Uncounterable;
	}
}
