package TBC.CombatScreen;


public class CancelAttackCommandFunction implements IGenericAction 
{
	private BattleScreen sc;
	
	public CancelAttackCommandFunction(BattleScreen sc)
	{
		this.sc = sc;
	}
	
	public void Invoke() 
	{
		sc.CancelAttackCommand();
	}
}