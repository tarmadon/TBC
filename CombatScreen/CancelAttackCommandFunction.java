package TBC.CombatScreen;

public class CancelAttackCommandFunction implements IGenericAction
{
	private BattleScreenClient sc;

	public CancelAttackCommandFunction(BattleScreenClient sc)
	{
		this.sc = sc;
	}

	public void Invoke()
	{
		sc.CancelAttackCommand();
	}
}