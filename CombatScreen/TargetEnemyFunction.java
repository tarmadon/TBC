package TBC.CombatScreen;

import TBC.Combat.CombatEntity;

public class TargetEnemyFunction implements IGenericAction 
{
	private BattleScreen sc;
	private CombatEntity target;
	
	public TargetEnemyFunction(BattleScreen sc, CombatEntity target)
	{
		this.sc = sc;
		this.target = target;
	}
	
	public void Invoke() 
	{
		sc.TargetCombatEntity(target);
	}
}