package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.DamageType;
import TBC.Combat.Effects.IDamageEffect;

public class DamageReturnEffect extends BaseTriggeredEffect implements IDamageEffect
{
	private int damageType;
	private int flatStrength;
	private float reflectedDamageRatio;

	public DamageReturnEffect(int damageType, int flatStrength, float reflectedDamageRatio)
	{
		this.damageType = damageType;
		this.flatStrength = flatStrength;
		this.reflectedDamageRatio = reflectedDamageRatio;
	}

	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker)
	{
		int reflected = (int)(flatStrength + (damage * reflectedDamageRatio));
		if(reflected == 0 || effectFromAttacker || ((source.GetDamageType() & this.damageType) != this.damageType))
		{
			return damage;
		}

		engine.DoDamage(defender, attacker, this, reflected);
		return damage;
	}

	public int GetDamageType()
	{
		return DamageType.Uncounterable;
	}
}
