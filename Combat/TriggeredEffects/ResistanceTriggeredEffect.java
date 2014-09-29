package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IDamageEffect;

public class ResistanceTriggeredEffect extends BaseTriggeredEffect
{
	private int damageType;
	private float percentage;
	private int flat;

	public ResistanceTriggeredEffect(int damageType, float percentage, int flat)
	{
		this.damageType = damageType;
		this.percentage = percentage;
		this.flat = flat;
	}

	public int OnDamage(CombatEngine engine, CombatEntity attacker, CombatEntity defender, IDamageEffect source, int damage, Boolean effectFromAttacker)
	{
		if(effectFromAttacker || ((source.GetDamageType() & this.damageType) != this.damageType))
		{
			return damage;
		}

		return (int)((damage - this.flat) * this.percentage);
	}
}
