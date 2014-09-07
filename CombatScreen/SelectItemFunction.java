package TBC.CombatScreen;


class SelectItemFunction implements IGenericAction 
{
	private BattleScreen sc;
	
	public SelectItemFunction(BattleScreen sc)
	{
		this.sc = sc;
	}
	
	public void Invoke() 
	{
		sc.ChooseItemCommand();
	}
}