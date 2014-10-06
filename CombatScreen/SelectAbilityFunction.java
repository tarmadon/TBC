package TBC.CombatScreen;

class SelectAbilityFunction implements IGenericAction
{
	private BattleScreenClient sc;

	public SelectAbilityFunction(BattleScreenClient sc)
	{
		this.sc = sc;
	}

	public void Invoke()
	{
		sc.ChooseAbilityCommand();
	}
}