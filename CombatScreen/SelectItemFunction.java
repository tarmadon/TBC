package TBC.CombatScreen;

class SelectItemFunction implements IGenericAction
{
	private BattleScreenClient sc;

	public SelectItemFunction(BattleScreenClient sc)
	{
		this.sc = sc;
	}

	public void Invoke()
	{
		sc.ChooseItemCommand();
	}
}