package TBC.CombatScreen;

class AttackCommandFunction implements IGenericAction
{
	private BattleScreenClient sc;

	public AttackCommandFunction(BattleScreenClient sc)
	{
		this.sc = sc;
	}

	public void Invoke()
	{
		sc.DefaultAttackCommand();
	}
}