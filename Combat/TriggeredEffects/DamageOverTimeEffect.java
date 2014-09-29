package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IDamageEffect;
import TBC.Combat.Effects.IExpiringEffect;

public class DamageOverTimeEffect extends DurationTriggeredEffect implements IDamageEffect
{
	private int damage;
	private int damageType;
	private CombatEntity user;

	public DamageOverTimeEffect(int damage, int damageType, int durationInTurns, int durationInTicks)
	{
		this(damage, damageType, durationInTurns, durationInTicks, null);
	}

	public DamageOverTimeEffect(int damage, int damageType, int durationInTurns, int durationInTicks, CombatEntity user)
	{
		super(durationInTurns, durationInTicks);
		this.damage = damage;
		this.damageType = damageType;
		this.user = user;
	}

	public boolean EndOfTurn(CombatEngine engine, CombatEntity entity)
	{
		int damage = this.damage;
		engine.DoDamage(this.user, entity, this, damage);
		if(damage != 0)
		{
			return true;
		}

		return false;
	}

	public int GetDamageType()
	{
		return this.damageType;
	}

	public Object CreateEffect(CombatEntity user, CombatEntity target)
	{
		return new DamageOverTimeEffect(this.damage, this.damageType, this.durationInTurns, this.durationInTicks, user);
	}
}
