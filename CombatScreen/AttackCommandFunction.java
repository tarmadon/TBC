package TBC.CombatScreen;


class AttackCommandFunction implements IGenericAction 
{
	private BattleScreen sc;
	
	public AttackCommandFunction(BattleScreen sc)
	{
		this.sc = sc;
	}
	
	public void Invoke() 
	{
		sc.DefaultAttackCommand();
	}
}