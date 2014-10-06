package TBC.CombatScreen;

public class AttemptEscapeFunction implements IGenericAction
{
	private BattleScreenClient sc;

	public AttemptEscapeFunction(BattleScreenClient sc)
	{
		this.sc = sc;
	}

	public void Invoke()
	{
		sc.AttemptEscape();
	}
}