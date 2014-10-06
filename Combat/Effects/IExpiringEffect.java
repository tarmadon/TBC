package TBC.Combat.Effects;

import java.io.Serializable;

public interface IExpiringEffect extends Serializable
{
	Boolean IsExpiredOnNextTick();
	Boolean IsExpiredOnNextTurn();
}
