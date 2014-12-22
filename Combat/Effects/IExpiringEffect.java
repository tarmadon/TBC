package TBC.Combat.Effects;

import java.io.Serializable;

public interface IExpiringEffect extends Serializable, IEffect
{
	Boolean IsExpiredOnNextTick();
	Boolean IsExpiredOnNextTurn();
}
