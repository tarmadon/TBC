package TBC.CombatScreen;


public class AttemptEscapeFunction implements IGenericAction 
{
	private BattleScreen sc;
	
	public AttemptEscapeFunction(BattleScreen sc)
	{
		this.sc = sc;
	}
	
	public void Invoke() 
	{
		sc.AttemptEscape();
	}
}