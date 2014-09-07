package TBC.Combat.Effects;

public interface IExpiringEffect 
{
	Boolean IsExpiredOnNextTick();
	Boolean IsExpiredOnNextTurn();
}
