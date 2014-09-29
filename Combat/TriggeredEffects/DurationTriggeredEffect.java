package TBC.Combat.TriggeredEffects;

import TBC.Combat.CombatEntity;
import TBC.Combat.Effects.IExpiringEffect;

public abstract class DurationTriggeredEffect extends BaseTriggeredEffect implements IExpiringEffect
{
	protected int durationInTurns;
	protected int durationInTicks;

	public DurationTriggeredEffect(int durationInTurns, int durationInTicks)
	{
		this.durationInTurns = durationInTurns;
		this.durationInTicks = durationInTicks;
	}

	public Boolean IsExpiredOnNextTurn()
	{
		if(this.durationInTurns == -1)
		{
			return false;
		}

		this.durationInTurns--;
		if(this.durationInTurns < 1)
		{
			return true;
		}

		return false;
	}

	public Boolean IsExpiredOnNextTick()
	{
		if(this.durationInTicks == -1)
		{
			return false;
		}

		this.durationInTicks--;
		if(this.durationInTicks < 1)
		{
			return true;
		}

		return false;
	}

	public abstract Object CreateEffect(CombatEntity user, CombatEntity target);
}
