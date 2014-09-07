package TBC.CombatScreen;


class SelectAbilityFunction implements IGenericAction 
{
	private BattleScreen sc;
	
	public SelectAbilityFunction(BattleScreen sc)
	{
		this.sc = sc;
	}
	
	public void Invoke() 
	{
		sc.ChooseAbilityCommand();
	}
}