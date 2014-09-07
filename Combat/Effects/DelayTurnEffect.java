package TBC.Combat.Effects;

import TBC.Combat.CombatEngine;
import TBC.Combat.CombatEntity;

public class DelayTurnEffect implements IOneTimeEffect
{
	private int delayAmount;
	
	public DelayTurnEffect(int delayAmount)
	{
		this.delayAmount = delayAmount;
	}
	
	public void ApplyToEntity(CombatEngine engine, CombatEntity user, CombatEntity target) 
	{
		engine.DelayTurnForEntity(target, this.delayAmount);
	}
}
